import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Bank implements BankService {

    private final Connection conn;
    private static final Logger LOGGER = Logger.getLogger(Bank.class.getName());

    public Bank(Connection conn) {
        this.conn = conn;
    }

    // =========================
    // REGISTER
    // =========================
    public boolean register(String username, String password, String pin) {

        String passwordHash = PasswordUtils.hashPassword(password);
        String pinHash = PasswordUtils.hashPassword(pin);

        String sql = "INSERT INTO users (username, password_hash, pin_hash, role) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, pinHash);
            stmt.setString(4, Role.USER.name());

            stmt.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Username already exists!");
            return false;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Registration failed", e);
            return false;
        }
    }

    // =========================
    // LOGIN
    // =========================
    public User login(String username, String password) {

        String sql = "SELECT * FROM users WHERE username = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                System.out.println("User not found.");
                return null;
            }

            int id = rs.getInt("id");
            String storedHash = rs.getString("password_hash");
            double balance = rs.getDouble("balance");
            Role role = Role.valueOf(rs.getString("role"));
            int failedAttempts = rs.getInt("failed_login_attempts");
            boolean accountLocked = rs.getBoolean("account_locked");

            if (accountLocked) {
                System.out.println("Account is locked!");
                return null;
            }

            String inputHash = PasswordUtils.hashPassword(password);

            if (storedHash.equals(inputHash)) {

                resetFailedAttempts(id);
                return new User(id, username, balance, role);

            } else {

                failedAttempts++;

                if (failedAttempts >= 3) {
                    lockAccount(id);
                    System.out.println("Account locked after 3 failed attempts!");
                } else {
                    updateFailedAttempts(id, failedAttempts);
                    System.out.println("Wrong password! Attempt " + failedAttempts + "/3");
                }

                return null;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Login failed", e);
            return null;
        }
    }

    // =========================
    // PIN VERIFICATION
    // =========================
    private boolean isPinValid(int userId, String pin) throws SQLException {

        String sql = "SELECT pin_hash FROM users WHERE id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                String storedHash = rs.getString("pin_hash");
                String inputHash = PasswordUtils.hashPassword(pin);

                return !storedHash.equals(inputHash);
            }
        }

        return true;
    }

    // =========================
    // LOGIN HELPERS
    // =========================
    private void updateFailedAttempts(int userId, int attempts) throws SQLException {

        String sql = "UPDATE users SET failed_login_attempts=? WHERE id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attempts);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    private void resetFailedAttempts(int userId) throws SQLException {

        String sql = "UPDATE users SET failed_login_attempts=0 WHERE id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    private void lockAccount(int userId) throws SQLException {

        String sql = "UPDATE users SET account_locked=1 WHERE id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    // =========================
    // BALANCE
    // =========================
    public double getBalance(int userId) throws SQLException {

        String sql = "SELECT balance FROM users WHERE id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("balance");
            }

            throw new SQLException("User not found");
        }
    }

    // =========================
    // DEPOSIT
    // =========================
    public boolean deposit(int userId, double amount) throws SQLException {

        if (amount <= 0) return false;

        String sql = "UPDATE users SET balance = balance + ? WHERE id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, amount);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }

        insertTransaction(userId, "DEPOSIT", amount);

        return true;
    }

    // =========================
    // WITHDRAW WITH PIN
    // =========================
    public boolean withdraw(int userId, double amount, String pin) throws SQLException {

        // PIN check
        if (isPinValid(userId, pin)) {
            System.out.println("Invalid PIN!");
            return false;
        }

        double balance = getBalance(userId);

        if (balance < amount) {
            System.out.println("Insufficient balance!");
            return false;
        }

        String sql = "UPDATE users SET balance = balance - ? WHERE id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }

        insertTransaction(userId, "WITHDRAW", amount);

        return true;
    }

    // =========================
    // TRANSFER WITH PIN
    // =========================
    public void transfer(int senderId, String receiverUsername, double amount, String pin) throws SQLException {

        if (isPinValid(senderId, pin)) {
            System.out.println("Invalid PIN!");
            return;
        }

        if (amount <= 0) {
            System.out.println("Amount must be positive!");
            return;
        }

        int receiverId;

        String findUser = "SELECT id FROM users WHERE username=?";

        try (PreparedStatement stmt = conn.prepareStatement(findUser)) {

            stmt.setString(1, receiverUsername);

            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                System.out.println("Receiver not found!");
                return;
            }

            receiverId = rs.getInt("id");
        }

        double balance = getBalance(senderId);

        if (balance < amount) {
            System.out.println("Insufficient balance!");
            return;
        }

        conn.setAutoCommit(false);

        try {

            updateBalance(senderId, -amount);
            updateBalance(receiverId, amount);

            insertTransfer(senderId, receiverId, amount, "TRANSFER_OUT");
            insertTransfer(receiverId, senderId, amount, "TRANSFER_IN");

            conn.commit();

        } catch (SQLException e) {

            conn.rollback();
            throw e;

        } finally {
            conn.setAutoCommit(true);
        }
    }

    private void updateBalance(int userId, double amount) throws SQLException {

        String sql = "UPDATE users SET balance = balance + ? WHERE id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, amount);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    private void insertTransaction(int userId, String type, double amount) throws SQLException {

        String sql = """
                INSERT INTO transactions (user_id,type,amount,balance_after)
                VALUES (?, ?, ?, (SELECT balance FROM users WHERE id=?))
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, type);
            stmt.setDouble(3, amount);
            stmt.setInt(4, userId);

            stmt.executeUpdate();
        }
    }

    private void insertTransfer(int userId, int refUser, double amount, String type) throws SQLException {

        String sql = """
                INSERT INTO transactions (user_id,type,amount,balance_after,reference_user_id)
                VALUES (?, ?, ?, (SELECT balance FROM users WHERE id=?), ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, type);
            stmt.setDouble(3, amount);
            stmt.setInt(4, userId);
            stmt.setInt(5, refUser);

            stmt.executeUpdate();
        }
    }

    // =========================
    // ADMIN METHODS
    // =========================

    public boolean isAdmin(String username) throws SQLException {

        String sql = "SELECT role FROM users WHERE username=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                Role role = Role.valueOf(rs.getString("role"));

                return role == Role.ADMIN || role == Role.SUPER_ADMIN;
            }
        }

        return false;
    }

    public void listAllUsers() throws SQLException {

        String sql = "SELECT id, username, role, balance FROM users";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                System.out.printf(
                        "ID:%d | %s | %s | Balance: %.2f%n",
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getDouble("balance")
                );
            }
        }
    }

    public double totalBankMoney() throws SQLException {

        String sql = "SELECT SUM(balance) AS total FROM users WHERE is_deleted=0";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }
        }

        return 0;
    }

    public void promoteToAdmin(String username) throws SQLException {

        String sql = "UPDATE users SET role='ADMIN' WHERE username=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }

    public void softDeleteUser(String username) throws SQLException {

        String sql = "UPDATE users SET is_deleted=1 WHERE username=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }

    public void blockUser(String username) throws SQLException {

        String sql = "UPDATE users SET account_locked=1 WHERE username=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }

    public void unblockUser(String username) throws SQLException {

        String sql = "UPDATE users SET account_locked=0 WHERE username=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }

    public void printTransactions(int userId) throws SQLException {

        String sql = """
        SELECT id, type, amount, balance_after, created_at
        FROM transactions
        WHERE user_id = ?
        ORDER BY created_at DESC
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("=== TRANSACTIONS ===");

            while (rs.next()) {

                System.out.printf(
                        "ID:%d | %s | Amount: %.2f | Balance: %.2f | Date: %s%n",
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getDouble("balance_after"),
                        rs.getTimestamp("created_at")
                );
            }
        }
    }
}