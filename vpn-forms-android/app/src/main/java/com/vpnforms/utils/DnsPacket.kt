package com.vpnforms.utils

import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DnsPacket(private val data: ByteArray) {
    companion object {
        private const val TAG = "DnsPacket"
        private const val DNS_HEADER_SIZE = 12
        private const val TYPE_A = 1
        private const val TYPE_AAAA = 28
        private const val CLASS_IN = 1
    }

    private val buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN)
    private var position = 0

    fun getDomain(): String {
        position = DNS_HEADER_SIZE
        return try {
            readDomain()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing DNS domain", e)
            ""
        }
    }

    fun getQueryType(): Int {
        return try {
            buffer.getShort(position).toInt() and 0xFFFF
        } catch (e: Exception) {
            Log.e(TAG, "Error getting query type", e)
            0
        }
    }

    private fun readDomain(): String {
        val domain = StringBuilder()
        var length = buffer.get(position).toInt() and 0xFF

        while (length > 0) {
            position++
            for (i in 0 until length) {
                domain.append(buffer.get(position + i).toInt().toChar())
            }
            position += length
            length = buffer.get(position).toInt() and 0xFF
            if (length > 0) {
                domain.append('.')
            }
        }
        return domain.toString()
    }

    fun createBlockedResponse(): ByteArray {
        val response = ByteArray(data.size)
        System.arraycopy(data, 0, response, 0, data.size)

        // Set response flags
        response[2] = (response[2].toInt() or 0x80).toByte() // Set QR bit
        response[3] = (response[3].toInt() or 0x83).toByte() // Set AA and RCODE (Name Error)

        return response
    }

    fun isValidQuery(): Boolean {
        if (data.size < DNS_HEADER_SIZE) return false

        val flags = buffer.getShort(2).toInt() and 0xFFFF
        val qr = (flags shr 15) and 1
        val opcode = (flags shr 11) and 0xF
        val qdcount = buffer.getShort(4).toInt() and 0xFFFF

        // Must be a query (QR=0), standard query (OPCODE=0), and have exactly one question
        return qr == 0 && opcode == 0 && qdcount == 1
    }

    fun isTypeAOrAAAA(): Boolean {
        if (!isValidQuery()) return false

        try {
            position = DNS_HEADER_SIZE
            readDomain() // Skip domain name
            position++ // Skip the zero byte after domain name
            val type = buffer.getShort(position).toInt() and 0xFFFF
            return type == TYPE_A || type == TYPE_AAAA
        } catch (e: Exception) {
            Log.e(TAG, "Error checking query type", e)
            return false
        }
    }

    fun createAllowedResponse(ip: String): ByteArray {
        val response = ByteArray(data.size + 16) // Extra space for the answer
        System.arraycopy(data, 0, response, 0, data.size)

        // Set response flags
        response[2] = (response[2].toInt() or 0x80).toByte() // Set QR bit
        response[3] = (response[3].toInt() or 0x80).toByte() // Set AA bit

        // Add answer section
        val answerOffset = data.size
        System.arraycopy(byteArrayOf(0xC0.toByte(), 0x0C), 0, response, answerOffset, 2) // Name pointer
        System.arraycopy(shortToBytes(TYPE_A), 0, response, answerOffset + 2, 2) // Type A
        System.arraycopy(shortToBytes(CLASS_IN), 0, response, answerOffset + 4, 2) // Class IN
        System.arraycopy(intToBytes(300), 0, response, answerOffset + 6, 4) // TTL 300 seconds
        System.arraycopy(shortToBytes(4), 0, response, answerOffset + 10, 2) // Data length
        System.arraycopy(ipToBytes(ip), 0, response, answerOffset + 12, 4) // IP address

        return response
    }

    private fun shortToBytes(s: Int): ByteArray {
        return byteArrayOf((s shr 8).toByte(), s.toByte())
    }

    private fun intToBytes(i: Int): ByteArray {
        return byteArrayOf(
            (i shr 24).toByte(),
            (i shr 16).toByte(),
            (i shr 8).toByte(),
            i.toByte()
        )
    }

    private fun ipToBytes(ip: String): ByteArray {
        return ip.split(".").map { it.toInt().toByte() }.toByteArray()
    }
}
