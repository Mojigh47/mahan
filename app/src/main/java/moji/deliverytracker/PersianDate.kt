package moji.deliverytracker

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale

/**
 * Self-contained Jalali (Shamsi) calendar utilities for display.
 *
 * Storage stays Gregorian ISO ("yyyy-MM-dd HH:mm:ss") so DB sorting/filtering keep working;
 * everything the user sees is converted to Shamsi here, including the Persian weekday name
 * and Persian digits.
 */
object PersianDate {

    data class Jalali(val year: Int, val month: Int, val day: Int)

    private val persianMonths = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    // Indexed by Calendar.DAY_OF_WEEK (SUNDAY=1 .. SATURDAY=7)
    private val persianWeekDays = arrayOf(
        "", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه"
    )

    private fun dbFormat(): SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    /** Standard Gregorian -> Jalali conversion (jdf algorithm). */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Jalali {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        val gy2 = gy - 1600
        val gm2 = gm - 1
        val gd2 = gd - 1

        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400
        for (i in 0 until gm2) gDayNo += gDaysInMonth[i]
        if (gm2 > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) gDayNo++
        gDayNo += gd2

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var i = 0
        while (i < 11 && jDayNo >= jDaysInMonth[i]) {
            jDayNo -= jDaysInMonth[i]
            i++
        }
        val jm = i + 1
        val jd = jDayNo + 1
        return Jalali(jy, jm, jd)
    }

    /** Standard Jalali -> Gregorian conversion (jdf algorithm). */
    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jy2 = jy - 979
        val jm2 = jm - 1
        val jd2 = jd - 1

        var jDayNo = 365 * jy2 + (jy2 / 33) * 8 + (jy2 % 33 + 3) / 4
        for (i in 0 until jm2) jDayNo += if (i < 6) 31 else 30
        jDayNo += jd2

        var gDayNo = jDayNo + 79
        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524
            if (gDayNo >= 365) gDayNo++ else leap = false
        }
        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461
        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }

        val gDaysInMonth = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var i = 0
        while (i < 12 && gDayNo >= gDaysInMonth[i]) {
            gDayNo -= gDaysInMonth[i]
            i++
        }
        return Triple(gy, i + 1, gDayNo + 1)
    }

    fun toPersianDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            sb.append(
                when (ch) {
                    in '0'..'9' -> ('۰' + (ch - '0'))
                    else -> ch
                }
            )
        }
        return sb.toString()
    }

    private fun two(n: Int): String = if (n < 10) "0$n" else n.toString()

    /** "شنبه ۱۴۰۳/۰۳/۱۹" */
    fun formatDate(date: Date): String {
        val cal = Calendar.getInstance().apply { time = date }
        val j = gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        val weekday = persianWeekDays[cal.get(Calendar.DAY_OF_WEEK)]
        return toPersianDigits("$weekday ${j.year}/${two(j.month)}/${two(j.day)}")
    }

    /** "۱۹ خرداد ۱۴۰۳" */
    fun formatDateLong(date: Date): String {
        val cal = Calendar.getInstance().apply { time = date }
        val j = gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        return toPersianDigits("${j.day} ${persianMonths[j.month - 1]} ${j.year}")
    }

    /** "۱۴:۳۲" */
    fun formatTime(date: Date): String {
        val cal = Calendar.getInstance().apply { time = date }
        return toPersianDigits("${two(cal.get(Calendar.HOUR_OF_DAY))}:${two(cal.get(Calendar.MINUTE))}")
    }

    /** "شنبه ۱۴۰۳/۰۳/۱۹ - ۱۴:۳۲" */
    fun formatStamp(date: Date): String = "${formatDate(date)} - ${formatTime(date)}"

    private fun parseDb(dbDateTime: String): Date? =
        try {
            dbFormat().parse(dbDateTime)
        } catch (e: Exception) {
            null
        }

    /** Format a DB datetime string ("yyyy-MM-dd HH:mm:ss") to Shamsi with weekday + time. */
    fun formatStampFromDb(dbDateTime: String): String {
        val d = parseDb(dbDateTime) ?: return toPersianDigits(dbDateTime)
        return formatStamp(d)
    }

    /** Format a DB datetime string to Shamsi date only (with weekday). */
    fun formatDateFromDb(dbDateTime: String): String {
        val d = parseDb(dbDateTime) ?: return toPersianDigits(dbDateTime)
        return formatDate(d)
    }

    /** Build a DB date prefix ("yyyy-MM-dd") for a given Jalali day. */
    fun jalaliToDbDatePrefix(jy: Int, jm: Int, jd: Int): String {
        val (gy, gm, gd) = jalaliToGregorian(jy, jm, jd)
        return "${gy}-${two(gm)}-${two(gd)}"
    }

    /** Today's Jalali date. */
    fun today(): Jalali {
        val cal = Calendar.getInstance()
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /** Start-of-day Date for a Jalali day (local time). */
    fun jalaliStartOfDay(jy: Int, jm: Int, jd: Int): Date {
        val (gy, gm, gd) = jalaliToGregorian(jy, jm, jd)
        return GregorianCalendar(gy, gm - 1, gd, 0, 0, 0).time
    }

    val months: Array<String> get() = persianMonths
}
