package moji.deliverytracker

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PaymentsActivity : BaseAuthActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: PaymentAdapter
    private lateinit var shimmer: ShimmerFrameLayout
    private var firstLoad = true

    override fun onAuthenticationSuccess() {
        setContentView(R.layout.activity_payments)

        supportActionBar?.title = getString(R.string.payments_history_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.recyclerPayments)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        tvEmpty = findViewById(R.id.tvEmptyPayments)
        shimmer = findViewById(R.id.shimmerPayments)

        adapter = PaymentAdapter(mutableListOf())
        recyclerView.adapter = adapter

        observePayments()
    }

    private fun observePayments() {
        shimmer.startShimmer()
        lifecycleScope.launch {
            db.paymentDao().getAllWithDriverFlow().collectLatest { payments ->
                if (firstLoad) {
                    firstLoad = false
                    shimmer.stopShimmer()
                    shimmer.visibility = View.GONE
                }
                if (payments.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
                adapter.updateList(payments)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
