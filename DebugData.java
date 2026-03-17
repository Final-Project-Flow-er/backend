import java.sql.*;

public class DebugData {
    public static void main(String[] args) {
        String url = "jdbc:mariadb://beyond21.iptime.org:7001/chaing";
        String user = "chaing";
        String pass = "chaing1234!";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("--- Current User Info (Franchise 1) ---");
            String fSql = "SELECT franchise_id, name, owner_name FROM franchise WHERE franchise_id = 1";
            try (PreparedStatement pstmt = conn.prepareStatement(fSql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    System.out.printf("Franchise 1: %s (Owner: %s)%n", rs.getString("name"),
                            rs.getString("owner_name"));
                } else {
                    System.out.println("Franchise 1 NOT FOUND!");
                }
            }

            System.out.println("\n--- All Sales Records ---");
            String sSql = "SELECT franchise_id, sales_code, created_at, is_canceled FROM sales LIMIT 20";
            try (PreparedStatement pstmt = conn.prepareStatement(sSql)) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    System.out.printf("FranchiseIdx: %d, Code: %s, Created: %s, Canceled: %b%n",
                            rs.getLong("franchise_id"),
                            rs.getString("sales_code"),
                            rs.getTimestamp("created_at"),
                            rs.getBoolean("is_canceled"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
