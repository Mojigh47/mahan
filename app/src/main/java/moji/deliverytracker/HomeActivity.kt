package moji.deliverytracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class HomeActivity : BaseAuthActivity() {
    private var dateTimeJob: kotlinx.coroutines.Job? = null

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            performBackup()
        } else {
            Toast.makeText(this, getString(R.string.backup_permission_error), Toast.LENGTH_LONG).show()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> /* No action needed, just request */ }

    override fun onAuthenticationSuccess() {
        setContentView(R.layout.activity_home)
        supportActionBar?.title = getString(R.string.home_app_title)

        requestNotificationPermissionIfNeeded()
        startDateTimeClock()

        findViewById<MaterialButton>(R.id.btnQuickOrder).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardNewOrder).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardOrders).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardDrivers).setOnClickListener {
            startActivity(Intent(this, DriversListActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardCustomers).setOnClickListener {
            startActivity(Intent(this, CustomersListActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardNeighborhoods).setOnClickListener {
            startActivity(Intent(this, ManageActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardSettle).setOnClickListener {
            startActivity(Intent(this, SettleActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardPayments).setOnClickListener {
            startActivity(Intent(this, PaymentsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardReports).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardDriverReport)?.setOnClickListener {
            startActivity(Intent(this, DriverReportActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardBackup)?.setOnClickListener {
            if (EncryptedBackupHelper.hasStoragePermission(this)) {
                performBackup()
            } else {
                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        observeStats()
    }

    private fun startDateTimeClock() {
        val tvDateTime = findViewById<TextView>(R.id.tvDateTime)
        dateTimeJob?.cancel()
        dateTimeJob = lifecycleScope.launch {
            while (true) {
                val now = Date()
                tvDateTime.text = getString(
                    R.string.home_datetime_format,
                    DateTimeUtils.formatDisplayDate(now),
                    DateTimeUtils.formatDisplayTime(now)
                )
                kotlinx.coroutines.delay(30_000L)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dateTimeJob?.cancel()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun performBackup() {
        lifecycleScope.launch {
            val (success, message) = EncryptedBackupHelper.exportSecureBackup(this@HomeActivity, db)
            val text = if (success) {
                getString(R.string.backup_success, message)
            } else {
                getString(R.string.backup_error, message)
            }
            Toast.makeText(this@HomeActivity, text, Toast.LENGTH_LONG).show()
        }
    }

    private fun observeStats() {
        val todayPrefix = DateTimeUtils.todayPrefixDb() + "%"

        lifecycleScope.launch {
            db.orderDao().getSummaryFlow(todayPrefix).collectLatest { summary ->
                findViewById<TextView>(R.id.tvTodayTotal).text = getString(
                    R.string.home_today_total_format,
                    CurrencyFormatter.formatNumber(summary.total.toLong())
                )
                findViewById<TextView>(R.id.tvTodayCount).text = getString(R.string.home_today_count_format, summary.count)
            }
        }
        lifecycleScope.launch {
            db.driverDao().getCountFlow().collectLatest { count ->
                findViewById<TextView>(R.id.tvDriverCount)?.text = getString(R.string.home_drivers_format, count)
            }
        }
        lifecycleScope.launch {
            db.customerDao().getCountFlow().collectLatest { count ->
                findViewById<TextView>(R.id.tvCustomerCount)?.text = getString(R.string.home_customers_format, count)
            }
        }
        lifecycleScope.launch {
            db.neighborhoodDao().getCountFlow().collectLatest { count ->
                findViewById<TextView>(R.id.tvNeighborhoodCount)?.text = getString(R.string.home_neighborhoods_format, count)
            }
        }
    }
}
