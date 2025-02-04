import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class Diary {
    private static final String DB_URL = "jdbc:sqlite:diary.db";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        createTables();

        while (true) {
            System.out.println("\nPersonal Diary and Savings Tracker");
            System.out.println("1. Add Diary Entry");
            System.out.println("2. View Diary Entries");
            System.out.println("3. Add Daily Savings");
            System.out.println("4. View Total Savings");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();
                continue;
            }
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    addDiaryEntry();
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
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void private static void createTables() {
        System.out.println("Database file location: " + DB_URL);

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String createDiaryTable = "CREATE TABLE IF NOT EXISTS diary (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "date TEXT NOT NULL, " +
                    "entry TEXT NOT NULL)";

            String createSavingsTable = "CREATE TABLE IF NOT EXISTS savings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "date TEXT NOT NULL, " +
                    "amount REAL NOT NULL)";

            stmt.execute(createDiaryTable);
            stmt.execute(createSavingsTable);

            System.out.println("Database tables initialized.");
        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
        }
    }

    private static void addDiaryEntry() {
        System.out.print("Enter today's diary entry: ");
        String entry = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO diary (date, entry) VALUES (?, ?)")) {
            pstmt.setString(1, LocalDate.now().toString());
            pstmt.setString(2, entry);
            pstmt.executeUpdate();
            System.out.println("Entry saved!");
        } catch (SQLException e) {
            System.out.println("Error saving entry: " + e.getMessage());
        }
    }

    private static void viewDiaryEntries() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT date, entry FROM diary")) {

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
            System.out.println("Invalid input. Please enter a valid number.");
            scanner.next();
            return;
        }
        double amount = scanner.nextDouble();
        scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO savings (date, amount) VALUES (?, ?)")) {
            pstmt.setString(1, LocalDate.now().toString());
            pstmt.setDouble(2, amount);
            pstmt.executeUpdate();
            System.out.println("Savings recorded!");
        } catch (SQLException e) {
            System.out.println("Error saving amount: " + e.getMessage());
        }
    }

    private static void viewTotalSavings() {
        double total = 0;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(amount) AS total FROM savings")) {

            if (rs.next() && rs.getString("total") != null) {
                total = rs.getDouble("total");
            }
            System.out.println("Total Savings: $" + total);
        } catch (SQLException e) {
            System.out.println("Error retrieving savings: " + e.getMessage());
        }
    }
}
