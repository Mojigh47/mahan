package moji.deliverytracker

import androidx.room.withTransaction

/**
 * Transactional settlement helper ensuring atomic financial operations.
 * All payment and settlement operations must go through this helper to guarantee consistency.
 */
object TransactionalSettlementHelper {

    /**
     * Record a payment and optionally settle orders in a single atomic transaction.
     * If any part fails, the entire transaction is rolled back.
     */
    suspend fun recordPaymentWithOptionalSettle(
        db: AppDatabase,
        driverId: Int,
        amount: Long,
        method: String,
        dateTime: String,
        autoSettle: Boolean = false
    ): Result {
        return try {
            db.withTransaction {
                // Insert payment
                val payment = Payment(
                    driverId = driverId,
                    amount = amount,
                    method = method,
                    dateTime = dateTime
                )
                val paymentId = db.paymentDao().insert(payment)
                if (paymentId == -1L) {
                    throw Exception("Failed to insert payment")
                }

                // Optionally settle orders if requested
                if (autoSettle) {
                    val settledCount = db.orderDao()
                        .updateSettledForDriver(driverId, true, dateTime)
                    if (settledCount == 0) {
                        throw Exception("No orders to settle")
                    }
                }

                Result.Success(paymentId)
            }
        } catch (e: Exception) {
            Result.Failure(e.message ?: "Unknown error during payment recording")
        }
    }

    /**
     * Settle all unsettled orders for a driver in a single atomic transaction.
     * This ensures all orders are marked settled at the same timestamp.
     */
    suspend fun settleAllOrdersForDriver(
        db: AppDatabase,
        driverId: Int,
        settledAt: String
    ): Result {
        return try {
            db.withTransaction {
                val settledCount = db.orderDao()
                    .updateSettledForDriver(driverId, true, settledAt)
                
                if (settledCount == 0) {
                    Result.Failure("No unsettled orders found for this driver")
                } else {
                    Result.Success(settledCount.toLong())
                }
            }
        } catch (e: Exception) {
            Result.Failure(e.message ?: "Unknown error during settlement")
        }
    }

    /**
     * Validate settlement data before processing.
     * Ensures amount is positive and driver exists.
     */
    suspend fun validateSettlementData(
        db: AppDatabase,
        driverId: Int,
        amount: Long
    ): ValidationResult {
        if (amount <= 0) {
            return ValidationResult.Invalid("Amount must be positive")
        }

        val driver = db.driverDao().getById(driverId)
        if (driver == null) {
            return ValidationResult.Invalid("Driver not found")
        }

        return ValidationResult.Valid
    }

    sealed class Result {
        data class Success(val id: Long) : Result()
        data class Failure(val error: String) : Result()
    }

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }
}
