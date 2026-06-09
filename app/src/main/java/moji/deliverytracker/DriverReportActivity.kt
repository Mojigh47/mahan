package moji.deliverytracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class DriverReportActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var actDriver: MaterialAutoCompleteTextView
    private lateinit var chipPeriod: ChipGroup
    private lateinit var rowCustomRange: View
    private lateinit var btnFrom: MaterialButton
    private lateinit var btnTo: MaterialButton
    private lateinit var tvRange: TextView
    private lateinit var tvCount: TextView
    private lateinit var tvTotalSales: TextView
    private lateinit var tvCommission: TextView
    private lateinit var tvNet: TextView
    private lateinit var tvPaid: TextView
    private lateinit var tvBalance: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var rvOrders: RecyclerView
    private lateinit var adapter: OrderAdapter

    private var fromJalali: PersianDate.Jalali = PersianDate.today()
    private var toJalali: PersianDate.Jalali = PersianDate.today()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_report)

        supportActionBar?.title = getString(R.string.driver_report_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = AppDatabase.getInstance(this)
        bindViews()
        setupDriverDropdown()
        setupPeriod()

        adapter = OrderAdapter(mutableListOf(), this) { order ->
            startActivity(Intent(this, EditOrderActivity::class.java).putExtra("orderId", order.id))
        }
        rvOrders.layoutManager = LinearLayoutManager(this)
        rvOrders.adapter = adapter
    }

    private fun bindViews() {
        actDriver = findViewById(R.id.actDriver)
        chipPeriod = findViewById(R.id.chipPeriod)
        rowCustomRange = findViewById(R.id.rowCustomRange)
        btnFrom = findViewById(R.id.btnFrom)
        btnTo = findViewById(R.id.btnTo)
        tvRange = findViewById(R.id.tvRange)
        tvCount = findViewById(R.id.tvCount)
        tvTotalSales = findViewById(R.id.tvTotalSales)
        tvCommission = findViewById(R.id.tvCommission)
        tvNet = findViewById(R.id.tvNet)
        tvPaid = findViewById(R.id.tvPaid)
        tvBalance = findViewById(R.id.tvBalance)
        tvEmpty = findViewById(R.id.tvEmpty)
        rvOrders = findViewById(R.id.rvOrders)
    }

    private fun setupDriverDropdown() {
        lifecycleScope.launch {
            db.driverDao().getNamesFlow().collectLatest { names ->
                actDriver.setAdapter(
                    ArrayAdapter(this@DriverReportActivity, android.R.layout.simple_dropdown_item_1line, names)
                )
            }
        }
        actDriver.setOnItemClickListener { _, _, _, _ -> refresh() }
    }

    private fun setupPeriod() {
        chipPeriod.setOnCheckedStateChangeListener { _, _ ->
            val custom = chipPeriod.checkedChipId == R.id.chipCustom
            rowCustomRange.visibility = if (custom) View.VISIBLE else View.GONE
            refresh()
        }
        btnFrom.setOnClickListener {
            PersianDatePickerDialog.show(this, fromJalali, getString(R.string.report_pick_from)) {
                fromJalali = it
                refresh()
            }
        }
        btnTo.setOnClickListener {
            PersianDatePickerDialog.show(this, toJalali, getString(R.string.report_pick_to)) {
                toJalali = it
                refresh()
            }
        }
    }

    private fun rangeForSelection(): Pair<String, String> {
        val now = Date()
        return when (chipPeriod.checkedChipId) {
            R.id.chipWeek -> {
                val start = Calendar.getInstance().apply { time = now; add(Calendar.DAY_OF_YEAR, -7) }
                DateTimeUtils.formatDb(start.time) to DateTimeUtils.formatDb(now)
            }
            R.id.chipMonth -> {
                val start = Calendar.getInstance().apply { time = now; add(Calendar.MONTH, -1) }
                DateTimeUtils.formatDb(start.time) to DateTimeUtils.formatDb(now)
            }
            R.id.chipCustom -> {
                val start = PersianDate.jalaliStartOfDay(fromJalali.year, fromJalali.month, fromJalali.day)
                val (gy, gm, gd) = PersianDate.jalaliToGregorian(toJalali.year, toJalali.month, toJalali.day)
                val end = Calendar.getInstance().apply { set(gy, gm - 1, gd, 23, 59, 59) }
                tvRange.text = getString(
                    R.string.report_range_format,
                    PersianDate.formatDateLong(start),
                    PersianDate.formatDateLong(end.time)
                )
                DateTimeUtils.formatDb(start) to DateTimeUtils.formatDb(end.time)
            }
            else -> {
                val start = Calendar.getInstance().apply {
                    time = now
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                }
                DateTimeUtils.formatDb(start.time) to DateTimeUtils.formatDb(now)
            }
        }
    }

    private fun refresh() {
        if (chipPeriod.checkedChipId != R.id.chipCustom) tvRange.text = ""
        val driverName = actDriver.text?.toString()?.trim().orEmpty()
        if (driverName.isEmpty()) {
            tvCount.text = getString(R.string.driver_report_hint)
            clearSummary()
            adapter.updateList(mutableListOf())
            tvEmpty.visibility = View.GONE
            return
        }

        val (start, end) = rangeForSelection()
        lifecycleScope.launch {
            val driverId = db.driverDao().getIdByName(driverName) ?: return@launch
            val driver = db.driverDao().getById(driverId)
            val commission = driver?.commission ?: 0f

            val orders = db.orderDao().getByDriverBetweenWithNames(driverId, start, end)
            val paid = db.paymentDao().getTotalPaidBetweenOnce(driverId, start, end)

            val totalSales = orders.sumOf { it.amount.toLong() }
            var totalCommission = 0L
            orders.forEach { totalCommission += MoneyCalculator.commissionAmount(it.amount, commission) }
            val net = totalSales - totalCommission
            val balance = net - paid

            tvCount.text = getString(R.string.driver_report_count_format, PersianDate.toPersianDigits(orders.size.toString()))
            tvTotalSales.text = getString(R.string.settle_total_format, CurrencyFormatter.formatNumber(totalSales))
            tvCommission.text = getString(R.string.settle_commission_format, commission, CurrencyFormatter.formatNumber(totalCommission))
            tvNet.text = getString(R.string.settle_net_format, CurrencyFormatter.formatNumber(net))
            tvPaid.text = getString(R.string.settle_paid_format, CurrencyFormatter.formatNumber(paid.toLong()))
            tvBalance.text = getString(R.string.settle_balance_format, CurrencyFormatter.formatNumber(balance))

            adapter.updateList(orders.toMutableList())
            tvEmpty.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun clearSummary() {
        tvTotalSales.text = ""
        tvCommission.text = ""
        tvNet.text = ""
        tvPaid.text = ""
        tvBalance.text = ""
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
