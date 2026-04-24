import java.sql.SQLException;

public interface BankService {

    // =========================
    // USER MANAGEMENT
    // =========================

    // registration now requires PIN
    boolean register(String username, String password, String pin);

    User login(String username, String password);

    boolean isAdmin(String username) throws SQLException;

    // =========================
    // BALANCE
    // =========================

    double getBalance(int userId) throws SQLException;

    // =========================
    // DEPOSIT / WITHDRAW
    // =========================

    boolean deposit(int userId, double amount) throws SQLException;

    // withdraw now requires PIN
    boolean withdraw(int userId, double amount, String pin) throws SQLException;

    // =========================
    // TRANSACTIONS
    // =========================

    void printTransactions(int userId) throws SQLException;

    // =========================
    // TRANSFER
    // =========================

    // transfer now requires PIN
    void transfer(int senderId, String receiverUsername, double amount, String pin) throws SQLException;

    // =========================
    // ADMIN / SUPER ADMIN
    // =========================

    void listAllUsers() throws SQLException;

    void promoteToAdmin(String username) throws SQLException;

    double totalBankMoney() throws SQLException;

    void softDeleteUser(String username) throws SQLException;

    void blockUser(String username) throws SQLException;

    void unblockUser(String username) throws SQLException;
}