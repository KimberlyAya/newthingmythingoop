import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

//depend
interface DatabaseManager {
    void executeUpdate(String query, Object... params) throws SQLException;
    ResultSet executeQuery(String query, Object... params) throws SQLException;
}

class SQLiteManager implements DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:diary.db";

    public SQLiteManager() {
        createTables();
    }

    private void createTables() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS diary (id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT NOT NULL, entry TEXT NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS savings (id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT NOT NULL, amount REAL NOT NULL)");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    @Override
    public void executeUpdate(String query, Object... params) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
        }
    }

    @Override
    public ResultSet executeQuery(String query, Object... params) throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        PreparedStatement pstmt = conn.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        return pstmt.executeQuery();
    }
}

//single responsobility
abstract class Record {
    protected final DatabaseManager dbManager;
    protected final LocalDate date;

    public Record(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.date = LocalDate.now();
    }

    public abstract void save();
}

// inheritance
class DiaryEntry extends Record {
    private final String entry;

    public DiaryEntry(DatabaseManager dbManager, String entry) {
        super(dbManager);
        this.entry = entry;
    }

    @Override
    public void save() {
        try {
            dbManager.executeUpdate("INSERT INTO diary (date, entry) VALUES (?, ?)", date.toString(), entry);
            System.out.println("Diary entry saved!");
        } catch (SQLException e) {
            System.out.println("Error saving entry: " + e.getMessage());
        }
    }
}

//polymorphism
class Savings extends Record {
    private final double amount;

    public Savings(DatabaseManager dbManager, double amount) {
        super(dbManager);
        this.amount = amount;
    }

    @Override
    public void save() {
        try {
            dbManager.executeUpdate("INSERT INTO savings (date, amount) VALUES (?, ?)", date.toString(), amount);
            System.out.println("Savings recorded!");
        } catch (SQLException e) {
            System.out.println("Error saving amount: " + e.getMessage());
        }
    }
}

public class Diary {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DatabaseManager dbManager = new SQLiteManager();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\nDiary and Savings");
            System.out.println("1. Add Diary Entry");
            System.out.println("2. View Diary Entries");
            System.out.println("3. Add Daily Savings");
            System.out.println("4. View Total Savings");
            System.out.println("5. Exit");
            System.out.print("Choose: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid.");
                scanner.next();
                continue;
            }
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter today's diary entry: ");
                    new DiaryEntry(dbManager, scanner.nextLine()).save();
                    break;
                case 2:
                    viewDiaryEntries();
                    break;
                case 3:
                    addDailySavings();
                    break;
                case 4:
                    viewTotalSavings();
                    break;
                case 5:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid.");
            }
        }
    }

    private static void viewDiaryEntries() {
        try (ResultSet rs = dbManager.executeQuery("SELECT date, entry FROM diary")) {
            boolean found = false;
            while (rs.next()) {
                System.out.println(rs.getString("date") + " - " + rs.getString("entry"));
                found = true;
            }
            if (!found) {
                System.out.println("No diary entries found.");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving entries: " + e.getMessage());
        }
    }

    private static void addDailySavings() {
        System.out.print("Enter amount saved today: ");
        if (!scanner.hasNextDouble()) {
            System.out.println("Invalid.");
            scanner.next();
            return;
        }
        new Savings(dbManager, scanner.nextDouble()).save();
        scanner.nextLine();
    }

    private static void viewTotalSavings() {
        try (ResultSet rs = dbManager.executeQuery("SELECT SUM(amount) AS total FROM savings")) {
            if (rs.next() && rs.getString("total") != null) {
                System.out.println("Total Savings: $" + rs.getDouble("total"));
            } else {
                System.out.println("Total Savings: $0");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving savings: " + e.getMessage());
        }
    }
}
