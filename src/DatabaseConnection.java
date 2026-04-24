import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * DatabaseConnection is a utility class for establishing a JDBC connection
 * to the MariaDB/MySQL database. It provides a single static method to get
 * a live Connection object.
 */
public class DatabaseConnection {

    // JDBC URL to connect to MariaDB/MySQL
    private static final String URL = "jdbc:mariadb://localhost:3306/bank";

    // Database username and password
    private static final String USER = "java";
    private static final String PASSWORD = "java";

    // Logger for logging connection attempts and errors
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    /**
     * Establishes a connection to the database.
     *
     * @return a Connection object if successful, or null if connection fails
     */
    @Nullable
    public static Connection getConnection() {
        try {
            // Attempt to create a database connection
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            LOGGER.info("Connection to the database successful!"); // Log success
            return conn; // Return the live connection

        } catch (SQLException e) {
            // Log detailed error if connection fails
            LOGGER.log(Level.SEVERE, "Connection failed!", e);
            return null; // Return null to indicate failure
        }
    }
}