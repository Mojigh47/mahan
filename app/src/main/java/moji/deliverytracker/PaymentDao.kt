package moji.deliverytracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert
    suspend fun insert(payment: Payment): Long

    @Query(
        """
        SELECT p.id, p.driver_id AS driverId, p.amount, p.payment_method AS method, p.date_time AS dateTime,
               d.name AS driverName
        FROM payments p
        JOIN drivers d ON d.id = p.driver_id
        ORDER BY p.id DESC
        """
    )
    fun getAllWithDriverFlow(): Flow<List<PaymentWithDriverName>>

    @Query(
        """
        SELECT p.id, p.driver_id AS driverId, p.amount, p.payment_method AS method, p.date_time AS dateTime,
               d.name AS driverName
        FROM payments p
        JOIN drivers d ON d.id = p.driver_id
        ORDER BY p.id DESC
        """
    )
    suspend fun getAllWithDriverOnce(): List<PaymentWithDriverName>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE driver_id = :driverId")
    suspend fun getTotalPaid(driverId: Int): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE driver_id = :driverId")
    fun getTotalPaidFlow(driverId: Int): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE driver_id = :driverId AND date_time >= :since")
    fun getTotalPaidSinceFlow(driverId: Int, since: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE driver_id = :driverId AND date_time >= :since")
    suspend fun getTotalPaidSinceOnce(driverId: Int, since: String): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE driver_id = :driverId AND date_time BETWEEN :start AND :end")
    suspend fun getTotalPaidBetweenOnce(driverId: Int, start: String, end: String): Long

    @Query("SELECT COUNT(*) FROM payments WHERE driver_id = :driverId")
    suspend fun countByDriver(driverId: Int): Int
}
