/**
 * User represents a bank account holder.
 * It stores the user's ID, username, balance, and role.
 */
public class User {

    private final int id;
    private final String username;
    private double balance;
    private final Role role; // NEW: Use Role enum

    public User(int id, String username, double balance, Role role) {
        this.id = id;
        this.username = username;
        this.balance = balance;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    @SuppressWarnings("unused")
    public double getBalance() {
        return balance;
    }

    @SuppressWarnings("unused")
    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Role getRole() {
        return role;
    }
}