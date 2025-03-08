package encryption-tool

import com.vpnforms.utils.StringEncryption

fun main() {
    val encryptedUrl = "nZOkuz1G2k9YpbsjNmANZ8DK4vbfc+x1jUFYmYjBSDf4AOmWxFa1iA1vwNq77VA2h1yFR007BWQqIj78A87/JPWI0m5CQy/ZFcDvKNMk/YM="
    val decryptedUrl = StringEncryption.decrypt(encryptedUrl)
    println("Decrypted URL: $decryptedUrl")
}
