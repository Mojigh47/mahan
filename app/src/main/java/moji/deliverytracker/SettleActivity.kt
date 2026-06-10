package moji.deliverytracker

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.*
import androidx.core.content.ContextCompat

class SettleActivity : BaseAuthActivity() {
    private lateinit var actDriver: AutoCompleteTextView
    private lateinit var btnLoad: MaterialButton
    private lateinit var rvOrders: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var tvCommission: TextView
    private lateinit var tvNet: TextView
    private lateinit var tvPaid: TextView
    private lateinit var tvBalance: TextView
    private lateinit var btnPayment: MaterialButton
    private lateinit var adapter: OrderAdapter
    private lateinit var shimmer: ShimmerFrameLayout
    private var currentDriverId: Int = -1
    private var driverJob: Job? = null
    private var hasPromptedSettle = false
    private var driverFirstLoad = true
    private var currentBalance: Long = 0L
    private var lastSettlementTime: String = "0000-01-01 00:00:00"
    private var isLoadingDriver = false

    override fun onAuthenticationSuccess() {
        setContentView(R.layout.activity_settle)

        supportActionBar?.title = getString(R.string.settle_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initializeViews()
    }

    private fun initializeViews() {
        db = AppDatabase.getInstance(this)
        actDriver = findViewById(R.id.actDriver)
        btnLoad = findViewById(R.id.btnLoad)
        rvOrders = findViewById(R.id.rvOrders)
        tvTotal = findViewById(R.id.tvTotal)
        tvCommission = findViewById(R.id.tvCommission)
        tvNet = findViewById(R.id.tvNet)
        tvPaid = findViewById(R.id.tvPaid)
        tvBalance = findViewById(R.id.tvBalance)
        btnPayment = findViewById(R.id.btnPayment)
        shimmer = findViewById(R.id.shimmerSettle)

        adapter = OrderAdapter(mutableListOf(), this) {}
        rvOrders.layoutManager = LinearLayoutManager(this)
        rvOrders.adapter = adapter

        lifecycleScope.launch {
            db.driverDao().getNamesFlow().collectLatest { drivers ->
                actDriver.setAdapter(ArrayAdapter(this@SettleActivity, android.R.layout.simple_dropdown_item_1line, drivers))
            }
        }

        btnLoad.setOnClickListener {
            if (isLoadingDriver) return@setOnClickListener
            val driverName = actDriver.text.toString().trim()
            if (driverName.isEmpty()) {
                Toast.makeText(this, getString(R.string.select_driver), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            isLoadingDriver = true
            btnLoad.isEnabled = false
            lifecycleScope.launch {
                val driverId = db.driverDao().getIdByName(driverName)
                if (driverId == null) {
                    Toast.makeText(this@SettleActivity, getString(R.string.select_driver), Toast.LENGTH_SHORT).show()
                    isLoadingDriver = false
                    btnLoad.isEnabled = true
                    return@launch
                }
                currentDriverId = driverId
                hasPromptedSettle = false
                driverFirstLoad = true
                shimmer.visibility = View.VISIBLE
                shimmer.startShimmer()
                rvOrders.visibility = View.GONE
                observeDriver(driverId, driverName)
            }
        }

        btnPayment.setOnClickListener {
            if (currentDriverId == -1) {
                Toast.makeText(this, getString(R.string.load_driver_first), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showPaymentDialog()
        }
    }

    private fun observeDriver(driverId: Int, driverName: String) {
        driverJob?.cancel()
        driverJob = lifecycleScope.launch {
            val ordersFlow = db.orderDao().getUnsettledByDriverWithNamesFlow(driverId)
            val lastSettlementFlow = db.orderDao().getLastSettlementTimeFlow(driverId)
            val paidFlow = lastSettlementFlow.flatMapLatest { since ->
                lastSettlementTime = since ?: "0000-01-01 00:00:00"
                db.paymentDao().getTotalPaidSinceFlow(driverId, lastSettlementTime)
            }
            val commissionFlow = db.driverDao().getCommissionFlow(driverId)

            combine(ordersFlow, paidFlow, commissionFlow) { orders, totalPaid, commissionValue ->
                Triple(orders, totalPaid, commissionValue ?: 0f)
            }.collectLatest { (orders, totalPaid, commission) ->
                if (driverFirstLoad) {
                    driverFirstLoad = false
                    isLoadingDriver = false
                    btnLoad.isEnabled = true
                    shimmer.stopShimmer()
                    shimmer.visibility = View.GONE
                    rvOrders.visibility = View.VISIBLE
                }

                val total = orders.sumOf { it.amount }
                val commissionAmount = MoneyCalculator.commissionAmount(total, commission)
                val netIncome = MoneyCalculator.netIncome(total, commission)
                val balance = MoneyCalculator.balance(netIncome, totalPaid)
                currentBalance = balance

                tvTotal.text = getString(R.string.settle_total_format, CurrencyFormatter.formatNumber(total))
                tvCommission.text = getString(R.string.settle_commission_format, commission, CurrencyFormatter.formatNumber(commissionAmount))
                tvNet.text = getString(R.string.settle_net_format, CurrencyFormatter.formatNumber(netIncome))
                tvPaid.text = getString(R.string.settle_paid_format, CurrencyFormatter.formatNumber(totalPaid))
                tvBalance.text = getString(R.string.settle_balance_format, CurrencyFormatter.formatNumber(balance))
                tvBalance.setTextColor(
                    ContextCompat.getColor(
                        this@SettleActivity,
                        if (balance <= 0) R.color.color_success else R.color.md_theme_error
                    )
                )

                adapter.updateList(orders.toMutableList())

                if (!hasPromptedSettle && orders.isNotEmpty() && balance <= 0) {
                    hasPromptedSettle = true
                    showSettleDialog(driverName, netIncome)
                }
            }
        }
    }

    private fun showPaymentDialog() {
        if (currentBalance <= 0) {
            Toast.makeText(this, getString(R.string.settle_balance_cleared), Toast.LENGTH_SHORT).show()
            return
        }
        val dialogView = layoutInflater.inflate(R.layout.dialog_payment, null)
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.etPaymentAmount)
        val rgMethod = dialogView.findViewById<RadioGroup>(R.id.rgPaymentMethod)
        etAmount.setText(currentBalance.toString())

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.payment_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.action_save)) { _, _ ->
                val amount = etAmount.text.toString().toLongOrNull() ?: 0L
                if (amount <= 0) {
                    Toast.makeText(this, getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (amount > currentBalance) {
                    Toast.makeText(this, getString(R.string.payment_over_balance), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val method = when (rgMethod.checkedRadioButtonId) {
                    R.id.rbCash -> getString(R.string.payment_cash)
                    R.id.rbCard -> getString(R.string.payment_card)
                    else -> getString(R.string.payment_cash)
                }
                lifecycleScope.launch {
                    val result = TransactionalSettlementHelper.recordPaymentWithOptionalSettle(
                        db,
                        currentDriverId,
                        amount,
                        method,
                        DateTimeUtils.nowDb(),
                        autoSettle = false
                    )

                    when (result) {
                        is TransactionalSettlementHelper.Result.Success -> {
                            Toast.makeText(this@SettleActivity, getString(R.string.payment_success), Toast.LENGTH_SHORT).show()
                            if (amount == currentBalance) {
                                showSettleDialog(actDriver.text.toString(), 0L)
                            }
                        }
                        is TransactionalSettlementHelper.Result.Failure -> {
                            Toast.makeText(this@SettleActivity, "Error: ${result.error}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton(getString(R.string.action_cancel), null)
            .show()
    }

    private fun showSettleDialog(driver: String, amount: Long) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.settle_dialog_title))
            .setMessage(getString(R.string.settle_dialog_message, driver, CurrencyFormatter.formatNumber(amount)))
            .setPositiveButton(getString(R.string.action_yes)) { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.settle_final_title))
                    .setMessage(getString(R.string.settle_final_message))
                    .setPositiveButton(getString(R.string.action_yes)) { _, _ ->
                        lifecycleScope.launch {
                            val result = TransactionalSettlementHelper.settleAllOrdersForDriver(
                                db,
                                currentDriverId,
                                DateTimeUtils.nowDb()
                            )

                            when (result) {
                                is TransactionalSettlementHelper.Result.Success -> {
                                    Toast.makeText(this@SettleActivity, getString(R.string.settle_success), Toast.LENGTH_SHORT).show()
                                }
                                is TransactionalSettlementHelper.Result.Failure -> {
                                    Toast.makeText(this@SettleActivity, "Error: ${result.error}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    .setNegativeButton(getString(R.string.action_no), null)
                    .show()
            }
            .setNegativeButton(getString(R.string.action_no), null)
            .show()
    }



    override fun onDestroy() {
        super.onDestroy()
        driverJob?.cancel()
        driverJob = null
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
