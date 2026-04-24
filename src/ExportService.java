import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExportService implements ExportServiceInterface {

    private final Connection conn;

    public ExportService(Connection conn) {
        this.conn = conn;
    }

    public void exportUsers() {

        String sql = """
                SELECT id, username, balance, role,
                       failed_login_attempts, account_locked,
                       created_at, is_deleted
                FROM users
                """;

        List<String[]> users = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                users.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("username"),
                        rs.getBigDecimal("balance").toString(),
                        rs.getString("role"),
                        String.valueOf(rs.getInt("failed_login_attempts")),
                        String.valueOf(rs.getBoolean("account_locked")),
                        rs.getTimestamp("created_at").toString(),
                        String.valueOf(rs.getBoolean("is_deleted"))
                });
            }

        } catch (SQLException e) {
            System.out.println("Failed to load users.");
            return;
        }

        exportCSV(users);
        exportJSON(users);
    }

    private void ensureExportDir() {
        File dir = new File("exports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private void exportCSV(List<String[]> users) {

        ensureExportDir();

        try (FileWriter writer = new FileWriter("exports/users_export.csv")) {

            writer.append("id,username,balance,role,failed_attempts,locked,created_at,is_deleted\n");

            for (String[] u : users) {
                writer.append(String.join(",", u));
                writer.append("\n");
            }

            System.out.println("CSV export created.");

        } catch (IOException e) {
            System.out.println("CSV export failed.");
        }
    }

    private void exportJSON(List<String[]> users) {

        ensureExportDir();

        try (FileWriter writer = new FileWriter("exports/users_export.json")) {

            writer.write("[\n");

            for (int i = 0; i < users.size(); i++) {

                String[] u = users.get(i);

                writer.write("""
                {
                  "id": %s,
                  "username": "%s",
                  "balance": %s,
                  "role": "%s",
                  "failed_attempts": %s,
                  "locked": %s,
                  "created_at": "%s",
                  "is_deleted": %s
                }
                """.formatted(
                        u[0], u[1], u[2], u[3], u[4], u[5], u[6], u[7]
                ));

                if (i < users.size() - 1) {
                    writer.write(",");
                }

                writer.write("\n");
            }

            writer.write("]");

            System.out.println("JSON export created.");

        } catch (IOException e) {
            System.out.println("JSON export failed.");
        }
    }
}