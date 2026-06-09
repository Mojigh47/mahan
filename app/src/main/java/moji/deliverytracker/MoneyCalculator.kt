package moji.deliverytracker

import java.math.BigDecimal
import java.math.RoundingMode

object MoneyCalculator {
    fun commissionAmount(total: Long, commissionPercent: Float): Long {
        if (total <= 0 || commissionPercent <= 0f) return 0
        val totalBD = BigDecimal(total)
        val percentBD = BigDecimal(commissionPercent.toString())
        return totalBD.multiply(percentBD)
            .divide(BigDecimal(100), 0, RoundingMode.HALF_UP)
            .toLong()
    }

    fun netIncome(total: Long, commissionPercent: Float): Long {
        return total - commissionAmount(total, commissionPercent)
    }

    fun balance(netIncome: Long, totalPaid: Long): Long {
        return netIncome - totalPaid
    }
}
