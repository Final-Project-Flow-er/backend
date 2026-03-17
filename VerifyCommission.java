import java.math.BigDecimal;
import java.math.RoundingMode;

public class VerifyCommission {
    public static void main(String[] args) {
        BigDecimal totalSale = new BigDecimal("44000");
        BigDecimal commissionRate = new BigDecimal("0.033");

        BigDecimal commissionFee = totalSale.multiply(commissionRate)
                .setScale(0, RoundingMode.HALF_UP);

        System.out.println("Total Sale: " + totalSale);
        System.out.println("Commission Rate: 3.3%");
        System.out.println("Calculated Commission: " + commissionFee);

        // Expected: 44000 * 0.033 = 1452
        if (commissionFee.compareTo(new BigDecimal("1452")) == 0) {
            System.out.println("SUCCESS: Commission calculation is correct.");
        } else {
            System.out.println("FAILURE: Commission calculation is incorrect.");
        }

        // Test with a non-integer result
        BigDecimal totalSale2 = new BigDecimal("10000");
        BigDecimal commissionFee2 = totalSale2.multiply(commissionRate).setScale(0, RoundingMode.HALF_UP);
        System.out.println("\nTotal Sale: " + totalSale2);
        System.out.println("Calculated Commission (rounded): " + commissionFee2);
        // 10000 * 0.033 = 330.00
    }
}
