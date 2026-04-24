import java.sql.SQLException;

@FunctionalInterface
public interface MenuAction {
    void execute() throws SQLException;
}