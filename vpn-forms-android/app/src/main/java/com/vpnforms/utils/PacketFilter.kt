package com.vpnforms.utils

import android.util.Log
import java.net.InetAddress
import java.nio.ByteBuffer

class PacketFilter {
    companion object {
        private const val TAG = "PacketFilter"
        private const val IP_HEADER_LENGTH = 20
        private const val TCP_HEADER_LENGTH = 20
        private const val UDP_HEADER_LENGTH = 8
        private const val PROTOCOL_TCP = 6
        private const val PROTOCOL_UDP = 17
        private const val DNS_PORT = 53
        private val ALLOWED_PORTS = listOf(80, 443) // HTTP and HTTPS ports
    }

    fun shouldAllowPacket(packet: ByteArray, length: Int): Boolean {
        if (length < IP_HEADER_LENGTH) return false

        try {
            val buffer = ByteBuffer.wrap(packet, 0, length)
            val firstByte = buffer.get(0)
            val version = (firstByte.toInt() shr 4) and 0xF

            // Only handle IPv4 packets
            if (version != 4) return false

            val protocol = buffer.get(9).toInt() and 0xFF
            val sourceIp = ByteArray(4)
            val destIp = ByteArray(4)

            // Extract source and destination IP addresses
            buffer.position(12)
            buffer.get(sourceIp)
            buffer.get(destIp)

            // Convert IPs to readable format
            val destAddress = InetAddress.getByAddress(destIp)
            val destHost = destAddress.hostAddress ?: return false

            // Check protocol and ports
            when (protocol) {
                PROTOCOL_TCP -> {
                    if (!isTcpAllowed(buffer, firstByte)) {
                        Log.d(TAG, "Blocking TCP packet: port not allowed")
                        return false
                    }
                }
                PROTOCOL_UDP -> {
                    // Only allow DNS queries
                    val headerLength = (firstByte.toInt() and 0xF) * 4
                    if (length >= headerLength + UDP_HEADER_LENGTH) {
                        val sourcePort = getPort(buffer, headerLength)
                        val destPort = getPort(buffer, headerLength + 2)
                        if (sourcePort == DNS_PORT || destPort == DNS_PORT) {
                            Log.d(TAG, "Allowing DNS query")
                            return true
                        }
                    }
                    return false
                }
                else -> {
                    Log.d(TAG, "Blocking non-TCP/UDP packet")
                    return false
                }
            }

            // Check if destination is in allowed Google IP ranges
            val isAllowedIp = isGoogleFormsIP(destHost)
            
            if (isAllowedIp) {
                Log.d(TAG, "Allowing Google Forms IP: $destHost")
            } else {
                Log.d(TAG, "Blocking non-Forms IP: $destHost")
            }
            
            return isAllowedIp

        } catch (e: Exception) {
            Log.e(TAG, "Error processing packet", e)
            return false // Block on error to be safe
        }
    }

    private fun isTcpAllowed(buffer: ByteBuffer, firstByte: Byte): Boolean {
        val headerLength = (firstByte.toInt() and 0xF) * 4
        if (buffer.capacity() >= headerLength + TCP_HEADER_LENGTH) {
            val sourcePort = getPort(buffer, headerLength)
            val destPort = getPort(buffer, headerLength + 2)
            
            return ALLOWED_PORTS.contains(destPort) ||
                   ALLOWED_PORTS.contains(sourcePort)
        }
        return false
    }

    private fun getPort(buffer: ByteBuffer, offset: Int): Int {
        return ((buffer.get(offset).toInt() and 0xFF) shl 8) or
               (buffer.get(offset + 1).toInt() and 0xFF)
    }

    private fun isGoogleFormsIP(ip: String): Boolean {
        return Constants.GOOGLE_IP_RANGES.any { ipRange ->
            isIpInRange(ip, ipRange)
        }
    }

    private fun isIpInRange(ip: String, range: String): Boolean {
        try {
            val (rangeIp, maskBits) = range.split("/")
            val mask = -1L shl (32 - maskBits.toInt())

            val ipValue = ipToLong(ip)
            val rangeValue = ipToLong(rangeIp)

            return (ipValue and mask) == (rangeValue and mask)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking IP range", e)
            return false
        }
    }

    private fun ipToLong(ip: String): Long {
        return try {
            val parts = ip.split(".")
            var result = 0L
            for (part in parts) {
                result = (result shl 8) or (part.toInt() and 0xFF).toLong()
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error converting IP to long", e)
            0
        }
    }
}
