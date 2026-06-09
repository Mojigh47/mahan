package moji.deliverytracker

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private fun dbDateTimeFormat(): SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    private fun dbDateOnlyFormat(): SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun nowDb(): String = dbDateTimeFormat().format(Date())

    fun formatDb(date: Date): String = dbDateTimeFormat().format(date)

    fun todayPrefixDb(): String = dbDateOnlyFormat().format(Date())

    // Display is Shamsi (Jalali) with Persian digits; storage stays Gregorian ISO.
    fun formatDisplayDate(date: Date): String = PersianDate.formatDate(date)

    fun formatDisplayTime(date: Date): String = PersianDate.formatTime(date)
}
