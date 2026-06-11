package moji.deliverytracker

import java.math.BigDecimal
import java.math.RoundingMode

object MoneyCalculator {
    /**
     * Calculate commission amount with validation and precision
     */
    fun commissionAmount(total: Long, commissionPercent: Float): Long {
        // Validate inputs
        if (total <= 0 || commissionPercent <= 0f) return 0
        if (commissionPercent > 100f) return 0
        
        return try {
            val totalBD = BigDecimal(total)
            val percentBD = BigDecimal(commissionPercent.toDouble())
            val result = totalBD.multiply(percentBD)
                .divide(BigDecimal(100), 0, RoundingMode.HALF_UP)
                .toLong()
            
            // Ensure commission doesn't exceed total
            if (result > total) total else result
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Calculate net income after commission
     */
    fun netIncome(total: Long, commissionPercent: Float): Long {
        return try {
            val commission = commissionAmount(total, commissionPercent)
            total - commission
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Calculate remaining balance
     */
    fun balance(netIncome: Long, totalPaid: Long): Long {
        return try {
            netIncome - totalPaid
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Calculate total revenue from multiple orders
     */
    fun totalRevenue(orders: List<Long>): Long {
        return try {
            orders.fold(0L) { acc, amount ->
                if (amount <= 0) acc else acc + amount
            }
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Calculate average order value
     */
    fun averageOrderValue(totalRevenue: Long, orderCount: Int): Long {
        return if (orderCount > 0) totalRevenue / orderCount else 0L
    }

    /**
     * Validate if payment is valid
     */
    fun isValidPayment(paymentAmount: Long, balance: Long): Boolean {
        return paymentAmount > 0 && paymentAmount <= balance
    }
}
