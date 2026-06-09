package moji.deliverytracker

import android.content.Context
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog

/**
 * Lightweight Shamsi (Jalali) date picker built from three NumberPickers.
 * Avoids any third-party dependency and matches the app's Persian UX.
 */
object PersianDatePickerDialog {

    fun show(
        context: Context,
        initial: PersianDate.Jalali = PersianDate.today(),
        title: String,
        onSelected: (PersianDate.Jalali) -> Unit
    ) {
        val today = PersianDate.today()

        val yearPicker = NumberPicker(context).apply {
            minValue = today.year - 5
            maxValue = today.year + 1
            value = initial.year.coerceIn(minValue, maxValue)
            wrapSelectorWheel = false
        }
        val monthPicker = NumberPicker(context).apply {
            minValue = 1
            maxValue = 12
            displayedValues = PersianDate.months
            value = initial.month.coerceIn(1, 12)
            wrapSelectorWheel = false
        }
        val dayPicker = NumberPicker(context).apply {
            minValue = 1
            maxValue = daysInJalaliMonth(initial.year, initial.month)
            value = initial.day.coerceIn(1, maxValue)
            wrapSelectorWheel = false
        }

        val refreshDays = {
            val max = daysInJalaliMonth(yearPicker.value, monthPicker.value)
            dayPicker.maxValue = max
            if (dayPicker.value > max) dayPicker.value = max
        }
        monthPicker.setOnValueChangedListener { _, _, _ -> refreshDays() }
        yearPicker.setOnValueChangedListener { _, _, _ -> refreshDays() }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            val pad = (16 * context.resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            addView(dayPicker, lp)
            addView(monthPicker, lp)
            addView(yearPicker, lp)
        }

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                onSelected(PersianDate.Jalali(yearPicker.value, monthPicker.value, dayPicker.value))
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun daysInJalaliMonth(jy: Int, jm: Int): Int = when {
        jm <= 6 -> 31
        jm <= 11 -> 30
        isLeapJalali(jy) -> 30
        else -> 29
    }

    private fun isLeapJalali(jy: Int): Boolean {
        // 33-year cycle remainder check (matches jdf algorithm leap years).
        val r = jy % 33
        return r == 1 || r == 5 || r == 9 || r == 13 || r == 17 || r == 22 || r == 26 || r == 30
    }
}
