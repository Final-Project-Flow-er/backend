import java.sql.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class TestSettlementLogic {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mariadb://beyond21.iptime.org:7001/chaing";
        String user = "root";
        String pass = "root";

        Class.forName("org.mariadb.jdbc.Driver");

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String dateStr = "2026-03-06";
            System.out.println("=== Testing Settlement Logic for " + dateStr + " ===");

            // 1. Sales Aggregation
            String salesSql = "SELECT si.unit_price, si.quantity " +
                    "FROM sales_item si " +
                    "JOIN sales s ON si.sales_id = s.sales_id " +
                    "WHERE s.franchise_id = 1 AND s.deleted_at IS NULL " +
                    "AND s.is_canceled = false " +
                    "AND CAST(s.created_at AS DATE) = ?";

            BigDecimal totalSale = BigDecimal.ZERO;
            try (PreparedStatement stmt = conn.prepareStatement(salesSql)) {
                stmt.setString(1, dateStr);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    totalSale = totalSale.add(rs.getBigDecimal("unit_price").multiply(rs.getBigDecimal("quantity")));
                }
            }
            System.out.println("Total Sale: " + totalSale);

            // 2. Commission Fee Calculation
            BigDecimal commissionFee = totalSale.multiply(new BigDecimal("0.033"))
                    .setScale(0, RoundingMode.HALF_UP);
            System.out.println("Calculated Commission: " + commissionFee);
        }
    }
}
