import java.sql.*;
import java.math.BigDecimal;

public class AuditReturnDetail {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mariadb://beyond21.iptime.org:7001/chaing";
        String user = "root";
        String pass = "root";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Return Detail Audit");

            String sql = "SELECT r.return_code, r.return_type, r.total_return_amount, ri.product_id, ri.quantity, ri.total_price "
                    +
                    "FROM returns r " +
                    "JOIN return_item ri ON r.return_id = ri.return_id " +
                    "WHERE r.return_code IN ('SE0120260307001', 'SE0120260307002')";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    System.out.printf("Code: %s, Type: %s, TotalAmt: %s, Prod: %d, Qty: %d, ItemPrice: %s%n",
                            rs.getString("return_code"),
                            rs.getString("return_type"),
                            rs.getBigDecimal("total_return_amount"),
                            rs.getLong("product_id"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("total_price"));
                }
            }
        }
    }
}
