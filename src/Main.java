import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {

        final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

        try (Connection conn = DatabaseConnection.getConnection()) {

            if (conn == null) {
                IO.println("Database connection failed.");
                return;
            }

            Scanner scanner = new Scanner(System.in);
            BankService bank = new Bank(conn);

            User user = null;

            while (true) {

                // =========================
                // LOGIN / REGISTER MENU
                // =========================
                if (user == null) {

                    IO.println("\n=== BANK SYSTEM ===");
                    IO.println("1. Register");
                    IO.println("2. Login");
                    IO.println("3. Quit");

                    int choice = InputUtils.readInt(scanner, "Type: ");

                    switch (choice) {

                        case 1 -> {
                            IO.println("=== REGISTRATION ===");

                            IO.print("Username: ");
                            String u = scanner.nextLine();

                            IO.print("Password: ");
                            String p = scanner.nextLine();

                            String pin;
                            while (true) {
                                IO.print("4-digit PIN: ");
                                pin = scanner.nextLine();
                                if (pin.matches("\\d{4}")) break;
                                IO.println("Invalid PIN!");
                            }

                            if (bank.register(u, p, pin)) {
                                IO.println("Registered!");
                            }
                        }

                        case 2 -> {
                            IO.println("=== LOGIN ===");

                            IO.print("Username: ");
                            String u = scanner.nextLine();

                            IO.print("Password: ");
                            String p = scanner.nextLine();

                            user = bank.login(u, p);

                            if (user != null) {
                                IO.println("Welcome " + user.getUsername());
                            }
                        }

                        case 3 -> {
                            IO.println("Bye!");
                            return;
                        }

                        default -> IO.println("Invalid choice.");
                    }

                } else {

                    Map<Integer, MenuItem> menu = buildMenu(user, bank, scanner, LOGGER);

                    boolean logout = runMenu(menu, scanner);

                    if (logout) {
                        user = null;
                        IO.println("Logged out.");
                    }
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(Main.class.getName())
                    .log(Level.SEVERE, "DB error", e);
        }
    }

    // =========================
    // MENU BUILDER
    // =========================
    private static Map<Integer, MenuItem> buildMenu(
            User user,
            BankService bank,
            Scanner scanner,
            Logger logger
    ) {

        Map<Integer, MenuItem> menu = new LinkedHashMap<>();
        int i = 1;

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isSuper = user.getRole() == Role.SUPER_ADMIN;
        boolean privileged = isAdmin || isSuper;

        // =====================
        // USER ACTIONS
        // =====================

        menu.put(i++, new MenuItem("Show balance", () -> {
            double balance = bank.getBalance(user.getId());
            System.out.printf("Balance: %.2f%n", balance);
        }));

        menu.put(i++, new MenuItem("Deposit", () -> {
            double amount = InputUtils.readPositiveDouble(scanner, "Amount: ");
            bank.deposit(user.getId(), amount);
            IO.println("Done.");
        }));

        menu.put(i++, new MenuItem("Withdraw", () -> {
            double amount = InputUtils.readPositiveDouble(scanner, "Amount: ");

            IO.print("PIN: ");
            String pin = scanner.nextLine();

            bank.withdraw(user.getId(), amount, pin);
            IO.println("Done.");
        }));

        menu.put(i++, new MenuItem("Transactions", () -> {
            bank.printTransactions(user.getId());
        }));

        menu.put(i++, new MenuItem("Transfer", () -> {
            IO.print("Receiver: ");
            String r = scanner.nextLine();

            double amount = InputUtils.readPositiveDouble(scanner, "Amount: ");

            IO.print("PIN: ");
            String pin = scanner.nextLine();

            bank.transfer(user.getId(), r, amount, pin);
        }));

        // =====================
        // ADMIN MENU
        // =====================

        if (privileged) {

            menu.put(i++, new MenuItem("List users", () -> {
                bank.listAllUsers();
            }));

            menu.put(i++, new MenuItem("Total bank money", () -> {
                IO.println(bank.totalBankMoney());
            }));

            menu.put(i++, new MenuItem("Soft delete user", () -> {
                IO.print("User: ");
                String u = scanner.nextLine();
                bank.softDeleteUser(u);
            }));

            menu.put(i++, new MenuItem("Block user", () -> {
                IO.print("User: ");
                String u = scanner.nextLine();
                bank.blockUser(u);
            }));

            menu.put(i++, new MenuItem("Unblock user", () -> {
                IO.print("User: ");
                String u = scanner.nextLine();
                bank.unblockUser(u);
            }));
        }

        // =====================
        // SUPER ADMIN ONLY
        // =====================

        if (isSuper) {

            menu.put(i++, new MenuItem("Promote user", () -> {
                IO.print("User: ");
                String u = scanner.nextLine();
                bank.promoteToAdmin(u);
            }));
        }

        // =====================
        // LOGOUT
        // =====================

        menu.put(i, new MenuItem("Logout", () -> {}));

        return menu;
    }

    // =========================
    // MENU RUNNER
    // =========================

    private static boolean runMenu(
            Map<Integer, MenuItem> menu,
            Scanner scanner
    ) {

        while (true) {

            IO.println("\n=== MENU ===");

            menu.forEach((k, v) ->
                    IO.println(k + ". " + v.label())
            );

            int choice = InputUtils.readInt(scanner, "Type: ");

            MenuItem item = menu.get(choice);

            if (item == null) {
                IO.println("Invalid choice.");
                continue;
            }

            try {
                item.action().execute();
            } catch (SQLException e) {
                IO.println("Database error.");
                e.printStackTrace();
            }

            if (item.label().equalsIgnoreCase("Logout")) {
                return true;
            }
        }
    }
}