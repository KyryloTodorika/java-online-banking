import org.jetbrains.annotations.NotNull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordUtils provides a secure way to hash passwords using SHA-256.
 * This ensures that plain-text passwords are never stored in the database.
 */
public class PasswordUtils {

    /**
     * Hashes a password using SHA-256.
     *
     * @param password Plain-text password
     * @return Hexadecimal SHA-256 hash of the password
     * @throws RuntimeException if SHA-256 algorithm is not available
     */
    public static @NotNull String hashPassword(@NotNull String password) {
        try {
            // Create a MessageDigest instance for SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Compute the hash as a byte array
            byte[] hashedBytes = md.digest(password.getBytes());

            // Convert byte array to hexadecimal string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b)); // 2 hex digits per byte
            }

            return sb.toString(); // Return the hash string

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 should always exist in Java; throw RuntimeException if not
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}