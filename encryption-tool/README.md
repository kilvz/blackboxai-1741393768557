# VPN Forms Encryption Tool

This tool helps you encrypt GitHub URLs and constants for the VPN Forms app.

## Files Structure

```
encryption-tool/
├── README.md
├── ConstantsEncryptor.java
├── StringEncryption.kt
└── run.sh
```

## How to Use

1. Place your GitHub URL in `urls.txt`:
```
https://raw.githubusercontent.com/username/repo/main/forms.json
```

2. Run the encryption tool:
```bash
./run.sh
```

3. Copy the encrypted values to your app:
- Copy URL encryption to `Constants.kt`: `ENCRYPTED_GITHUB_RAW_URL`
- Copy other encrypted values to their respective places

## Encryption Details

The tool uses:
- AES/CBC/PKCS5Padding encryption
- 256-bit key length
- Custom IV and SALT values
- Secure key derivation

## Security Notes

1. Change these values in both the encryption tool and app:
```kotlin
private val SALT = byteArrayOf(
    0x43.toByte(), 0x76.toByte(), /* ... */
)

private val IV = byteArrayOf(
    0x00.toByte(), 0x01.toByte(), /* ... */
)

private const val SECRET_KEY = "YourSecretKey123"
```

2. Keep encryption parameters secure
3. Use different values for each build
4. Don't commit encryption keys
5. Protect configuration files

## Example Usage

1. Encrypt GitHub URL:
```bash
$ echo "https://raw.githubusercontent.com/user/repo/main/forms.json" > urls.txt
$ ./run.sh
Encrypted URL: "TSPUf7UhG+TJPeKQSORunviAXvJ+..."
```

2. Update Constants.kt:
```kotlin
object Constants {
    private const val ENCRYPTED_GITHUB_RAW_URL = "TSPUf7UhG+TJPeKQSORunviAXvJ+..."
    
    val GITHUB_RAW_URL: String
        get() = StringEncryption.decrypt(ENCRYPTED_GITHUB_RAW_URL)
}
```

## Encryption Process

1. String Encryption:
```kotlin
// StringEncryption.kt
object StringEncryption {
    fun encrypt(text: String): String {
        val secretKey = generateKey()
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(IV))
        val encrypted = cipher.doFinal(text.getBytes())
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }
}
```

2. Constants Encryption:
```java
// ConstantsEncryptor.java
public class ConstantsEncryptor {
    public static void main(String[] args) {
        // Read values from urls.txt
        String url = readFile("urls.txt");
        
        // Encrypt values
        String encrypted = encrypt(url);
        
        // Output encrypted values
        System.out.println("Encrypted URL: \"" + encrypted + "\"");
    }
}
```

## Verification

Always verify decryption works:
```kotlin
val decrypted = StringEncryption.decrypt(encrypted)
println("Decrypted: $decrypted")
// Should match original value
```

## Security Best Practices

1. Change encryption parameters:
   - Generate new SALT
   - Generate new IV
   - Use strong SECRET_KEY

2. Protect encryption files:
   - Don't commit encryption tool
   - Keep parameters secure
   - Use different values per build

3. Verify encryption:
   - Test decryption
   - Validate values
   - Check app functionality
