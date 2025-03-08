package com.vpnforms.utils

import android.util.Log
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

class PacketHandler(
    private val inputStream: FileInputStream,
    private val outputStream: FileOutputStream
) {
    companion object {
        private const val TAG = "PacketHandler"
        private const val IP_HEADER_SIZE = 20
        private const val TCP_HEADER_SIZE = 20
        private const val UDP_HEADER_SIZE = 8
        private const val DNS_PORT = 53
        private const val PROTOCOL_TCP = 6
        private const val PROTOCOL_UDP = 17
        private const val MAX_PACKET_SIZE = 32767
        private const val IP_VERSION_4 = 4
    }

    private val buffer = ByteBuffer.allocate(MAX_PACKET_SIZE)
    private val dnsCache = ConcurrentHashMap<String, String>()

    fun handlePacket(): Boolean {
        val length = inputStream.read(buffer.array())
        if (length <= 0) return false

        try {
            if (isValidPacket(length)) {
                when (getProtocol()) {
                    PROTOCOL_UDP -> handleUdpPacket(length)
                    PROTOCOL_TCP -> handleTcpPacket(length)
                    else -> dropPacket()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling packet", e)
            return false
        } finally {
            buffer.clear()
        }

        return true
    }

    private fun isValidPacket(length: Int): Boolean {
        if (length < IP_HEADER_SIZE) return false

        val firstByte = buffer.get(0)
        val version = (firstByte.toInt() shr 4) and 0xF
        return version == IP_VERSION_4
    }

    private fun getProtocol(): Int {
        return buffer.get(9).toInt() and 0xFF
    }

    private fun handleUdpPacket(length: Int) {
        val headerLength = (buffer.get(0).toInt() and 0xF) * 4
        if (length < headerLength + UDP_HEADER_SIZE) return

        val sourcePort = getPort(headerLength)
        val destPort = getPort(headerLength + 2)

        // Handle DNS queries
        if (sourcePort == DNS_PORT || destPort == DNS_PORT) {
            handleDnsPacket(length, headerLength)
        } else {
            // For non-DNS UDP packets, check destination
            if (isAllowedDestination()) {
                forwardPacket(length)
            }
        }
    }

    private fun handleTcpPacket(length: Int) {
        if (isAllowedDestination()) {
            forwardPacket(length)
        }
    }

    private fun handleDnsPacket(length: Int, headerLength: Int) {
        val dnsStart = headerLength + UDP_HEADER_SIZE
        if (length <= dnsStart) return

        val dnsPacket = DnsPacket(buffer.array().copyOfRange(dnsStart, length))
        if (!dnsPacket.isValidQuery()) return

        val domain = dnsPacket.getDomain()
        if (domain.isEmpty()) return

        if (isAllowedDomain(domain)) {
            // Forward DNS query
            forwardPacket(length)
        } else {
            // Block domain by sending error response
            val response = dnsPacket.createBlockedResponse()
            sendDnsResponse(response, headerLength)
        }
    }

    private fun isAllowedDomain(domain: String): Boolean {
        return Constants.ALLOWED_DOMAINS.any { domain.endsWith(it) } ||
               Constants.REQUIRED_DOMAINS.any { domain.endsWith(it) }
    }

    private fun isAllowedDestination(): Boolean {
        val destIp = getDestinationIp()
        return Constants.GOOGLE_IP_RANGES.any { range ->
            isIpInRange(destIp, range)
        }
    }

    private fun getDestinationIp(): String {
        return "${buffer.get(16).toInt() and 0xFF}." +
               "${buffer.get(17).toInt() and 0xFF}." +
               "${buffer.get(18).toInt() and 0xFF}." +
               "${buffer.get(19).toInt() and 0xFF}"
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
                result = result shl 8 or part.toLong()
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error converting IP to long", e)
            0
        }
    }

    private fun getPort(offset: Int): Int {
        return (buffer.get(offset).toInt() and 0xFF shl 8) or
               (buffer.get(offset + 1).toInt() and 0xFF)
    }

    private fun forwardPacket(length: Int) {
        outputStream.write(buffer.array(), 0, length)
    }

    private fun dropPacket() {
        // Do nothing, effectively dropping the packet
    }

    private fun sendDnsResponse(response: ByteArray, headerLength: Int) {
        // Swap source and destination addresses
        for (i in 12..15) {
            val temp = buffer.get(i)
            buffer.put(i, buffer.get(i + 4))
            buffer.put(i + 4, temp)
        }

        // Swap ports
        for (i in 0..1) {
            val temp = buffer.get(headerLength + i)
            buffer.put(headerLength + i, buffer.get(headerLength + 2 + i))
            buffer.put(headerLength + 2 + i, temp)
        }

        // Update UDP length
        val udpLength = response.size + UDP_HEADER_SIZE
        buffer.put(headerLength + 4, (udpLength shr 8).toByte())
        buffer.put(headerLength + 5, udpLength.toByte())

        // Copy DNS response
        System.arraycopy(response, 0, buffer.array(), headerLength + UDP_HEADER_SIZE, response.size)

        // Update IP total length
        val totalLength = headerLength + udpLength
        buffer.put(2, (totalLength shr 8).toByte())
        buffer.put(3, totalLength.toByte())

        // Forward modified packet
        outputStream.write(buffer.array(), 0, totalLength)
    }
}
