import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

public class AuditReturn {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mariadb://beyond21.iptime.org:7001/chaing";
        String user = "root";
        String pass = "root";

        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MariaDB Driver not found!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("--- DB Diagnostic Info ---");
            System.out.println("Catalog: " + conn.getCatalog());
            System.out.println("Schema: " + conn.getSchema());

            System.out.println("\n--- Returns Table Snapshot (No Filter) ---");
            String sqlSnapshot = "SELECT return_code, created_at, total_return_amount FROM returns LIMIT 10";

            try (PreparedStatement stmtSnapshot = conn.prepareStatement(sqlSnapshot)) {
                ResultSet rsSnapshot = stmtSnapshot.executeQuery();
                if (!rsSnapshot.isBeforeFirst()) {
                    System.out.println("No data found in returns table!");
                }
                while (rsSnapshot.next()) {
                    System.out.printf("Code: %s | Created: %s | Amount: %s%n",
                            rsSnapshot.getString("return_code"),
                            rsSnapshot.getTimestamp("created_at"),
                            rsSnapshot.getBigDecimal("total_return_amount"));
                }
            }

            System.out.println("\nReturn Data Audit for 2026-03-07");

            String sql = "SELECT r.franchise_id, r.return_code, r.return_type, r.total_return_amount, " +
                    "foi.unit_price, foi.quantity, foi.total_price " +
                    "FROM returns r " +
                    "JOIN return_item ri ON r.return_id = ri.return_id " +
                    "JOIN franchise_order_item foi ON ri.franchise_order_item_id = foi.franchise_order_item_id " +
                    "WHERE r.created_at >= '2026-03-07 00:00:00' AND r.created_at < '2026-03-08 00:00:00'";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                System.out.println("Detailed Item Breakdown for Returns on 3/7:");
                while (rs.next()) {
                    System.out.printf(
                            "Franchise: %d | [%s] Type: %s, ReturnTotal: %s | Unit: %s, Qty: %d, LineTotal: %s%n",
                            rs.getLong("franchise_id"),
                            rs.getString("return_code"),
                            rs.getString("return_type"),
                            rs.getBigDecimal("total_return_amount"),
                            rs.getBigDecimal("unit_price"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("total_price"));
                }
            }
        }
    }
}
