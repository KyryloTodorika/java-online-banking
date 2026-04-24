import java.util.Scanner;

/**
 * InputUtils provides safe input methods for the console.
 * These methods prevent the program from crashing when the user
 * types invalid input (e.g., letters instead of numbers).
 */
public class InputUtils {

    /**
     * Reads an integer from the console safely.
     * If the user enters a non-integer, it will prompt again.
     *
     * @param scanner Scanner object to read from System.in
     * @param prompt  Message displayed to the user
     * @return a valid integer entered by the user
     */
    public static int readInt(Scanner scanner, String prompt) {
        int value;
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                value = scanner.nextInt();
                scanner.nextLine(); // consume leftover newline
                break; // exit loop on valid integer
            } else {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine(); // consume invalid input
            }
        }
        return value;
    }

    /**
     * Reads a positive double (decimal number) from the console safely.
     * If the user enters a non-number or negative value, it will prompt again.
     *
     * @param scanner Scanner object to read from System.in
     * @param prompt  Message displayed to the user
     * @return a positive double entered by the user
     */
    public static double readPositiveDouble(Scanner scanner, String prompt) {
        double value;
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextDouble()) {
                value = scanner.nextDouble();
                scanner.nextLine(); // consume leftover newline
                if (value > 0) break; // only allow positive numbers
                System.out.println("Amount must be positive!");
            } else {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine(); // consume invalid input
            }
        }
        return value;
    }
}