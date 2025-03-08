import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ConstantsEncryptor {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    // Must match values in StringEncryption.kt
    private static final byte[] SALT = {
        (byte)0x43, (byte)0x76, (byte)0x95, (byte)0xc7,
        (byte)0x5b, (byte)0xd7, (byte)0x45, (byte)0x17,
        (byte)0x35, (byte)0x98, (byte)0x9d, (byte)0x31,
        (byte)0x04, (byte)0x2e, (byte)0x8a, (byte)0x0f
    };

    private static final byte[] IV = {
        (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03,
        (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07,
        (byte)0x08, (byte)0x09, (byte)0x0a, (byte)0x0b,
        (byte)0x0c, (byte)0x0d, (byte)0x0e, (byte)0x0f
    };

    private static final String SECRET_KEY = "H@rdt0Gu3ss1234!@#$";

    public static void main(String[] args) {
        try {
            // Read GitHub URL from urls.txt
            System.out.println("Reading GitHub URL from urls.txt...");
            String url = readFile("urls.txt").trim();
            
            // Encrypt URL
            System.out.println("\nEncrypting URL...");
            String encryptedUrl = encrypt(url);
            System.out.println("Encrypted URL: \"" + encryptedUrl + "\"");
            
            // Verify decryption
            String decryptedUrl = decrypt(encryptedUrl);
            System.out.println("\nVerification - Decrypted URL:");
            System.out.println(decryptedUrl);
            System.out.println("(should match your original URL)");
            
            // Instructions
            System.out.println("\nTo use this encrypted URL:");
            System.out.println("1. Copy the encrypted string above");
            System.out.println("2. Open Constants.kt");
            System.out.println("3. Replace ENCRYPTED_GITHUB_RAW_URL value with your encrypted string");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String readFile(String path) throws Exception {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
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
