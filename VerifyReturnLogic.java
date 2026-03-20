import java.sql.*;
import java.math.BigDecimal;
import java.util.*;

public class VerifyReturnLogic {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mariadb://beyond21.iptime.org:7001/chaing";
        String user = "root";
        String pass = "root";

        Class.forName("org.mariadb.jdbc.Driver");

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("=== Return Logic Simulation (Match UI Calculation) ===");

            String sql = "SELECT r.return_id, r.return_code, r.return_type, r.return_status " +
                    "FROM returns r " +
                    "WHERE r.created_at >= '2026-03-07 00:00:00' AND r.created_at < '2026-03-08 00:00:00' " +
                    "AND r.deleted_at IS NULL";

            BigDecimal totalRefund = BigDecimal.ZERO;
            BigDecimal totalLoss = BigDecimal.ZERO;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    long returnId = rs.getLong("return_id");
                    String code = rs.getString("return_code");
                    String type = rs.getString("return_type");

                    BigDecimal currentReturnTotal = BigDecimal.ZERO;

                    // Simulation of item-based calculation
                    String itemSql = "SELECT foi.franchise_order_item_id, foi.unit_price, p.name " +
                            "FROM return_item ri " +
                            "JOIN franchise_order_item foi ON ri.franchise_order_item_id = foi.franchise_order_item_id "
                            +
                            "JOIN product p ON foi.product_id = p.product_id " +
                            "WHERE ri.return_id = ?";

                    try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                        itemStmt.setLong(1, returnId);
                        ResultSet itemRs = itemStmt.executeQuery();
                        while (itemRs.next()) {
                            BigDecimal price = itemRs.getBigDecimal("unit_price");
                            currentReturnTotal = currentReturnTotal.add(price);
                            System.out.printf("  - Item ID: %d | Product: %s | Price: %s%n",
                                    itemRs.getLong("franchise_order_item_id"),
                                    itemRs.getString("name"),
                                    price);
                        }
                    }

                    System.out.printf("[%s] Type: %s | Calculated Amount: %s%n", code, type, currentReturnTotal);

                    if ("PRODUCT_DEFECT".equals(type)) {
                        totalRefund = totalRefund.add(currentReturnTotal);
                    } else if ("MISORDER".equals(type)) {
                        totalLoss = totalLoss.add(currentReturnTotal);
                    }
                }
            }

            System.out.println("\n--- Final Aggregation Result ---");
            System.out.println("Total Refund (Product Defect): " + totalRefund);
            System.out.println("Total Loss (Misorder): " + totalLoss);
        }
    }
}
