package com.vpnforms.utils

import android.util.Log

/**
 * Utility class to help encrypt and decrypt URLs for the app.
 * Run the main function to encrypt your URL.
 */
object UrlEncryptor {
    private const val TAG = "UrlEncryptor"

    @JvmStatic
    fun main(args: Array<String>) {
        // Replace this with your actual GitHub raw URL
        val url = "https://raw.githubusercontent.com/YOUR_USERNAME/YOUR_REPO/main/forms.json"
        
        try {
            val encrypted = CryptoUtil.encrypt(url)
            println("\n=== URL Encryption Result ===")
            println("Original URL: $url")
            println("Encrypted URL: $encrypted")
            println("\nTo use this encrypted URL:")
            println("1. Copy the encrypted string above")
            println("2. Replace ENCRYPTED_GITHUB_RAW_URL in Constants.kt with your encrypted string")
            println("3. Test the app to ensure it can decrypt and access your URL")
            
            // Verify decryption works
            val decrypted = CryptoUtil.decrypt(encrypted)
            println("\nVerification - Decrypted URL: $decrypted")
            println("(should match your original URL)")
            
        } catch (e: Exception) {
            println("\nError encrypting URL: ${e.message}")
            Log.e(TAG, "Error encrypting URL", e)
        }
    }

    /**
     * Helper method to quickly encrypt a URL
     */
    fun encryptUrl(url: String): String {
        return try {
            val encrypted = CryptoUtil.encrypt(url)
            println("Encrypted URL: $encrypted")
            encrypted
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting URL", e)
            throw e
        }
    }

    /**
     * Helper method to verify decryption works
     */
    fun testDecryption(encrypted: String) {
        try {
            val decrypted = CryptoUtil.decrypt(encrypted)
            println("Decryption test - URL: $decrypted")
        } catch (e: Exception) {
            Log.e(TAG, "Error testing decryption", e)
            throw e
        }
    }
}
