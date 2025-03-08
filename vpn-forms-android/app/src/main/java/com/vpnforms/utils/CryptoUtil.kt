package com.vpnforms.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtil {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_ALGORITHM = "PBKDF2WithHmacSHA1"
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    
    private val SALT = "YourCustomSalt123".toByteArray()
    private val IV = byteArrayOf(
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
        0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
    )
    private const val SECRET_KEY = "YourSecretKey123"

    fun encrypt(text: String): String {
        try {
            val secretKey = generateKey()
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(IV))
            val encrypted = cipher.doFinal(text.toByteArray())
            return Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw RuntimeException("Error encrypting", e)
        }
    }

    fun decrypt(encryptedText: String): String {
        try {
            val secretKey = generateKey()
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(IV))
            val decrypted = cipher.doFinal(Base64.decode(encryptedText, Base64.NO_WRAP))
            return String(decrypted)
        } catch (e: Exception) {
            throw RuntimeException("Error decrypting", e)
        }
    }

    private fun generateKey(): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance(KEY_ALGORITHM)
        val spec = PBEKeySpec(SECRET_KEY.toCharArray(), SALT, ITERATIONS, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }
}
