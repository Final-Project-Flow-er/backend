import java.sql.*;
import java.math.BigDecimal;

public class VerifyDetailedOrders {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mariadb://beyond21.iptime.org:7001/chaing";
        String user = "root";
        String pass = "root";

        Class.forName("org.mariadb.jdbc.Driver");

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String dateStr = "2026-03-13";
            System.out.println("=== Verifying Detailed Order Aggregation for " + dateStr + " ===");

            // 1. Find all order IDs for the date
            String orderSql = "SELECT franchise_order_id, order_code, order_status FROM franchise_order " +
                    "WHERE franchise_id = 1 AND CAST(created_at AS DATE) = ?";

            BigDecimal totalOrderAmount = BigDecimal.ZERO;
            int orderCount = 0;
            int totalItemCount = 0;

            try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
                stmt.setString(1, dateStr);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    long orderId = rs.getLong("franchise_order_id");
                    String code = rs.getString("order_code");
                    String status = rs.getString("order_status");
                    orderCount++;

                    System.out.println("\n[Order: " + code + "] Status: " + status);

                    // 2. Fetch items for this specific order
                    String itemSql = "SELECT product_id, unit_price, quantity, total_price FROM franchise_order_item " +
                            "WHERE franchise_order_id = ? AND deleted_at IS NULL";

                    try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                        itemStmt.setLong(1, orderId);
                        ResultSet itemRs = itemStmt.executeQuery();
                        while (itemRs.next()) {
                            BigDecimal itemTotal = itemRs.getBigDecimal("total_price");
                            long productId = itemRs.getLong("product_id");
                            int qty = itemRs.getInt("quantity");

                            System.out.printf("  - Product ID: %d (Qty: %d) | Total: %s%n", productId, qty, itemTotal);
                            totalOrderAmount = totalOrderAmount.add(itemTotal);
                            totalItemCount++;
                        }
                    }
                }
            }

            System.out.println("\n--- Aggregation Summary ---");
            System.out.println("Total Orders: " + orderCount);
            System.out.println("Total Items Collected: " + totalItemCount);
            System.out.println("Calculated Daily Order Amount: " + totalOrderAmount);
        }
    }
}
