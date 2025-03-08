import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
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
        // VPN Configuration
        encryptAndPrint("VPN_ADDRESS", "10.0.0.2");
        encryptAndPrint("VPN_DNS", "8.8.8.8");

        // Google IP Ranges
        List<String> ipRanges = Arrays.asList(
            "142.250.0.0/15",
            "172.217.0.0/16",
            "216.58.192.0/19",
            "74.125.0.0/16",
            "64.233.160.0/19",
            "216.239.32.0/19",
            "108.177.0.0/17",
            "35.190.247.0/24",
            "35.191.0.0/16",
            "130.211.0.0/22"
        );
        System.out.println("\nGoogle IP Ranges:");
        for (String range : ipRanges) {
            encryptAndPrint("IP_RANGE", range);
        }

        // Allowed Domains
        List<String> domains = Arrays.asList(
            "docs.google.com",
            "forms.gle",
            "forms.google.com",
            "accounts.google.com",
            "ssl.gstatic.com",
            "www.gstatic.com",
            "fonts.gstatic.com",
            "fonts.googleapis.com"
        );
        System.out.println("\nAllowed Domains:");
        for (String domain : domains) {
            encryptAndPrint("DOMAIN", domain);
        }

        // Blocked Domains
        List<String> blockedDomains = Arrays.asList(
            "mail.google.com",
            "drive.google.com",
            "calendar.google.com",
            "meet.google.com",
            "chat.google.com",
            "photos.google.com",
            "play.google.com",
            "youtube.com",
            "*.youtube.com",
            "google.com"
        );
        System.out.println("\nBlocked Domains:");
        for (String domain : blockedDomains) {
            encryptAndPrint("BLOCKED_DOMAIN", domain);
        }
    }

    private static void encryptAndPrint(String name, String value) {
        try {
            String encrypted = encrypt(value);
            System.out.println(name + ": \"" + encrypted + "\"");
        } catch (Exception e) {
            System.err.println("Error encrypting " + name + ": " + e.getMessage());
        }
    }

    private static String encrypt(String text) throws Exception {
        SecretKeySpec secretKey = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV));
        byte[] encrypted = cipher.doFinal(text.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private static SecretKeySpec generateKey() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        PBEKeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT, ITERATIONS, KEY_LENGTH);
        byte[] key = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }
}
