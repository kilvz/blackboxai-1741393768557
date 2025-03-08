import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object ManualDecrypt {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_ALGORITHM = "PBKDF2WithHmacSHA1"
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256

    private val SALT = byteArrayOf(
        0x43.toByte(), 0x76.toByte(), 0x95.toByte(), 0xc7.toByte(),
        0x5b.toByte(), 0xd7.toByte(), 0x45.toByte(), 0x17.toByte(),
        0x35.toByte(), 0x98.toByte(), 0x9d.toByte(), 0x31.toByte(),
        0x04.toByte(), 0x2e.toByte(), 0x8a.toByte(), 0x0f.toByte()
    )

    private val IV = byteArrayOf(
        0x00.toByte(), 0x01.toByte(), 0x02.toByte(), 0x03.toByte(),
        0x04.toByte(), 0x05.toByte(), 0x06.toByte(), 0x07.toByte(),
        0x08.toByte(), 0x09.toByte(), 0x0a.toByte(), 0x0b.toByte(),
        0x0c.toByte(), 0x0d.toByte(), 0x0e.toByte(), 0x0f.toByte()
    )

    private const val SECRET_KEY = "H@rdt0Gu3ss1234!@#$"

    @JvmStatic
    fun main(args: Array<String>) {
        val encryptedUrl = "nZOkuz1G2k9YpbsjNmANZ8DK4vbfc+x1jUFYmYjBSDf4AOmWxFa1iA1vwNq77VA2h1yFR007BWQqIj78A87/JPWI0m5CQy/ZFcDvKNMk/YM="
        val decryptedUrl = decrypt(encryptedUrl)
        println("Decrypted URL: $decryptedUrl")
    }

    private fun decrypt(encryptedData: String): String {
        val secretKey = generateKey()
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(IV))
        val decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData))
        return String(decrypted)
    }

    private fun generateKey(): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance(KEY_ALGORITHM)
        val spec = PBEKeySpec(SECRET_KEY.toCharArray(), SALT, ITERATIONS, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }
}
