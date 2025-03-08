import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

fun main() {
    println("=== URL Encryption Tool ===")
    println("Enter your GitHub raw URL (e.g., https://raw.githubusercontent.com/username/repo/main/forms.json):")
    
    val url = readLine() ?: return
    
    try {
        val encrypted = encrypt(url)
        println("\nEncryption successful!")
        println("\nEncrypted URL:")
        println(encrypted)
        println("\nTo use this encrypted URL:")
        println("1. Copy the encrypted string above")
        println("2. Open Constants.kt")
        println("3. Replace ENCRYPTED_GITHUB_RAW_URL value with your encrypted string")
        
        // Verify decryption
        val decrypted = decrypt(encrypted)
        println("\nVerification - Decrypted URL:")
        println(decrypted)
        println("(should match your original URL)")
        
    } catch (e: Exception) {
        println("\nError: ${e.message}")
    }
}

private const val ALGORITHM = "AES/CBC/PKCS5Padding"
private const val KEY_ALGORITHM = "PBKDF2WithHmacSHA1"
private const val ITERATIONS = 10000
private const val KEY_LENGTH = 256

// These values should match those in CryptoUtil.kt
private val SALT = "YourCustomSalt123".toByteArray()
private val IV = "YourCustomIV12345".toByteArray()
private const val SECRET_KEY = "YourSecretKey123"

fun encrypt(text: String): String {
    val secretKey = generateKey()
    val cipher = Cipher.getInstance(ALGORITHM)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(IV))
    val encrypted = cipher.doFinal(text.toByteArray())
    return Base64.getEncoder().encodeToString(encrypted)
}

fun decrypt(encryptedText: String): String {
    val secretKey = generateKey()
    val cipher = Cipher.getInstance(ALGORITHM)
    cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(IV))
    val decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText))
    return String(decrypted)
}

private fun generateKey(): SecretKeySpec {
    val factory = SecretKeyFactory.getInstance(KEY_ALGORITHM)
    val spec = PBEKeySpec(SECRET_KEY.toCharArray(), SALT, ITERATIONS, KEY_LENGTH)
    val tmp = factory.generateSecret(spec)
    return SecretKeySpec(tmp.encoded, "AES")
}
