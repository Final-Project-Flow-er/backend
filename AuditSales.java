import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AuditSales {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mariadb://beyond21.iptime.org:7001/chaing";
        String user = "root";
        String pass = "root";

        Class.forName("org.mariadb.jdbc.Driver");

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== Real-time Sales Aggregation Audit ===");

            String sql = "SELECT si.unit_price, si.quantity, s.sales_id, s.sales_code, s.is_canceled, s.created_at " +
                    "FROM sales_item si " +
                    "JOIN sales s ON si.sales_id = s.sales_id " +
                    "WHERE s.franchise_id = 1 AND s.deleted_at IS NULL ";

            BigDecimal totalSale = BigDecimal.ZERO;
            int count = 0;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    boolean canceled = rs.getBoolean("is_canceled");
                    BigDecimal price = rs.getBigDecimal("unit_price");
                    int qty = rs.getInt("quantity");
                    BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));

                    System.out.printf("[%s] Canceled: %b | Price: %s | Qty: %d | LineTotal: %s | Created: %s%n",
                            rs.getString("sales_code"), canceled, price, qty, lineTotal, rs.getTimestamp("created_at"));

                    if (!canceled) {
                        totalSale = totalSale.add(lineTotal);
                        count++;
                    }
                }
            }

            System.out.println("\n--- Aggregation Summary (2026-03-13) ---");
            System.out.println("Valid Sales Count: " + count);
            System.out.println("Total Sales Amount: " + totalSale);
        }
    }
}
