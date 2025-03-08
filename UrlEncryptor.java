import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.Console;
import java.util.Scanner;

public class UrlEncryptor {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    // These values should match those in CryptoUtil.kt
    private static final byte[] SALT = "YourCustomSalt123".getBytes();
    private static final byte[] IV = new byte[] {
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
        0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
    }; // 16 bytes IV
    private static final String SECRET_KEY = "YourSecretKey123";

    public static void main(String[] args) {
        System.out.println("=== URL Encryption Tool ===");
        System.out.println("Enter your GitHub raw URL (e.g., https://raw.githubusercontent.com/username/repo/main/forms.json):");
        
        Scanner scanner = new Scanner(System.in);
        String url = scanner.nextLine();
        
        try {
            String encrypted = encrypt(url);
            System.out.println("\nEncryption successful!");
            System.out.println("\nEncrypted URL:");
            System.out.println(encrypted);
            System.out.println("\nTo use this encrypted URL:");
            System.out.println("1. Copy the encrypted string above");
            System.out.println("2. Open Constants.kt");
            System.out.println("3. Replace ENCRYPTED_GITHUB_RAW_URL value with your encrypted string");
            
            // Verify decryption
            String decrypted = decrypt(encrypted);
            System.out.println("\nVerification - Decrypted URL:");
            System.out.println(decrypted);
            System.out.println("(should match your original URL)");
            
        } catch (Exception e) {
            System.out.println("\nError: " + e.getMessage());
            e.printStackTrace();
        }
        
        scanner.close();
    }

    private static String encrypt(String text) throws Exception {
        SecretKeySpec secretKey = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV));
        byte[] encrypted = cipher.doFinal(text.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private static String decrypt(String encryptedText) throws Exception {
        SecretKeySpec secretKey = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV));
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decrypted);
    }

    private static SecretKeySpec generateKey() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        PBEKeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT, ITERATIONS, KEY_LENGTH);
        byte[] key = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }
}
