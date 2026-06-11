package moji.deliverytracker

import java.math.BigDecimal

/**
 * Comprehensive financial validator for all monetary calculations.
 * Ensures precision, prevents overflow, and validates all financial operations.
 */
object FinancialValidator {

    private const val MAX_AMOUNT = 999_999_999L  // Maximum amount in Tomans
    private const val MIN_AMOUNT = 1L
    private const val COMMISSION_PERCENTAGE_MAX = 100.0
    private const val COMMISSION_PERCENTAGE_MIN = 0.0

    /**
     * Validate order amount
     */
    fun validateOrderAmount(amount: Long): ValidationResult {
        return when {
            amount < MIN_AMOUNT -> ValidationResult.Invalid("مبلغ باید بیشتر از صفر باشد")
            amount > MAX_AMOUNT -> ValidationResult.Invalid("مبلغ بیش از حد مجاز است")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate commission percentage
     */
    fun validateCommissionPercentage(percentage: Double): ValidationResult {
        return when {
            percentage < COMMISSION_PERCENTAGE_MIN -> ValidationResult.Invalid("درصد پورسانت نمی‌تواند منفی باشد")
            percentage > COMMISSION_PERCENTAGE_MAX -> ValidationResult.Invalid("درصد پورسانت نمی‌تواند بیش از ۱۰۰٪ باشد")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Calculate commission with precision using BigDecimal
     */
    fun calculateCommission(amount: Long, commissionPercentage: Double): Long {
        // Validate inputs first
        if (validateOrderAmount(amount) !is ValidationResult.Valid) {
            return 0L
        }
        if (validateCommissionPercentage(commissionPercentage) !is ValidationResult.Valid) {
            return 0L
        }

        return try {
            val amountBD = BigDecimal(amount)
            val percentageBD = BigDecimal(commissionPercentage)
            val hundredBD = BigDecimal(100)

            val commission = amountBD
                .multiply(percentageBD)
                .divide(hundredBD, BigDecimal.ROUND_HALF_UP)
                .toLong()

            // Ensure commission doesn't exceed amount
            if (commission > amount) amount else commission
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Calculate net amount after commission
     */
    fun calculateNetAmount(amount: Long, commissionPercentage: Double): Long {
        val commission = calculateCommission(amount, commissionPercentage)
        return amount - commission
    }

    /**
     * Validate payment amount against driver balance
     */
    fun validatePaymentAmount(paymentAmount: Long, driverBalance: Long): ValidationResult {
        return when {
            paymentAmount < MIN_AMOUNT -> ValidationResult.Invalid("مبلغ پرداخت باید بیشتر از صفر باشد")
            paymentAmount > driverBalance -> ValidationResult.Invalid("مبلغ پرداخت بیشتر از بدهی رانندگی است")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Calculate total revenue for a period
     */
    fun calculateTotalRevenue(orders: List<Long>): Long {
        return try {
            orders.fold(0L) { acc, amount ->
                if (acc + amount > MAX_AMOUNT) MAX_AMOUNT else acc + amount
            }
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Calculate average order value
     */
    fun calculateAverageOrderValue(totalRevenue: Long, orderCount: Int): Long {
        return if (orderCount > 0) totalRevenue / orderCount else 0L
    }

    /**
     * Validate settlement data
     */
    fun validateSettlementData(
        driverId: Int,
        totalAmount: Long,
        commissionPercentage: Double,
        orderCount: Int
    ): ValidationResult {
        if (driverId <= 0) return ValidationResult.Invalid("شناسه راننده نامعتبر است")
        if (validateOrderAmount(totalAmount) !is ValidationResult.Valid) return ValidationResult.Invalid("مبلغ کل نامعتبر است")
        if (validateCommissionPercentage(commissionPercentage) !is ValidationResult.Valid) return ValidationResult.Invalid("درصد پورسانت نامعتبر است")
        if (orderCount <= 0) return ValidationResult.Invalid("تعداد سفارش‌ها نامعتبر است")

        return ValidationResult.Valid
    }

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }
}
