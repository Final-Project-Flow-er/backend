import java.sql.*;
import java.math.BigDecimal;

public class AuditReceipts {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mariadb://beyond21.iptime.org:7001/chaing";
        String user = "root";
        String pass = "root";

        Class.forName("org.mariadb.jdbc.Driver");

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== Daily Settlement Receipt Audit ===");

            String sql = "SELECT settlement_date, total_sale_amount, commission_fee, final_amount, franchise_id " +
                    "FROM daily_settlement_receipt " +
                    "ORDER BY settlement_date DESC";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    System.out.printf("Date: %s | Sales: %s | Commission: %s | Final: %s%n",
                            rs.getDate("settlement_date"),
                            rs.getBigDecimal("total_sale_amount"),
                            rs.getBigDecimal("commission_fee"),
                            rs.getBigDecimal("final_amount"));
                }
            }
        }
    }
}
