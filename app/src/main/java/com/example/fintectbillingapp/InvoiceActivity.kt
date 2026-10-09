package com.example.fintectbillingapp

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fintectbillingapp.databinding.ActivityInvoiceBinding
import com.google.gson.Gson

class InvoiceActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_WITH_VOUCHER = "extra_with_voucher"
    }

    private lateinit var binding: ActivityInvoiceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityInvoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val withVoucher = intent.getBooleanExtra(EXTRA_WITH_VOUCHER, false)
        loadAndDisplayInvoice(withVoucher)
    }

    private fun loadAndDisplayInvoice(withVoucher: Boolean) {
        val jsonPayload = getMockInvoiceJson(withVoucher)
        val gson = Gson()
        val invoice = gson.fromJson(jsonPayload, InvoiceResponse::class.java)

        bindInvoiceToUi(invoice)
    }

    private fun bindInvoiceToUi(invoice: InvoiceResponse) {
        // Header
        binding.tvInvoiceNo.text = invoice.invoiceNumber
        binding.tvStatusBadge.text = invoice.paymentStatus
        binding.tvTransactionDate.text = invoice.transactionDate.toFriendlyDate()
        binding.tvPaymentMethod.text = getString(R.string.payment_method_format, invoice.paymentMethod)

        // Merchant & Customer
        binding.tvMerchantName.text = getString(
            R.string.merchant_name_format,
            invoice.merchant.merchantName,
            invoice.merchant.cityLocation,
        )
        binding.tvCustomerName.text = getString(
            R.string.customer_name_format,
            invoice.customer.fullName,
            invoice.customer.phone,
        )

        // RecyclerView items
        binding.rvInvoiceItems.layoutManager = LinearLayoutManager(this)
        binding.rvInvoiceItems.adapter = InvoiceItemAdapter(invoice.items)

        // Summary
        val summary = invoice.summary
        binding.tvSubtotalAmt.text = summary.subtotalAmount.toRupiah()
        binding.tvTaxAmt.text = summary.taxPpn11.toRupiah()
        binding.tvFeeAmt.text = summary.serviceFee.toRupiah()
        binding.tvTotalPaid.text = summary.totalPaid.toRupiah()

        // FR-4 Defensive Nullable Handling for Voucher
        if (summary.voucherCode.isNullOrEmpty()) {
            binding.rowDiscount.visibility = View.GONE
            binding.tvPromoInfo.visibility = View.VISIBLE
            binding.tvPromoInfo.text = getString(R.string.no_promo_used)
        } else {
            binding.rowDiscount.visibility = View.VISIBLE
            binding.tvPromoInfo.visibility = View.GONE
            val discountPercent = summary.voucherDiscountPercent ?: 0
            binding.tvDiscountLabel.text = getString(
                R.string.discount_voucher_format,
                summary.voucherCode,
                discountPercent,
            )
            binding.tvDiscountAmt.text = getString(
                R.string.negative_amount_format,
                summary.discountAmount.toRupiah(),
            )
        }
    }

    private fun getMockInvoiceJson(withVoucher: Boolean): String {
        val voucherCodeValue = if (withVoucher) "\"HEMAT10\"" else "null"
        val voucherDiscountValue = if (withVoucher) "10" else "null"
        val discountAmountValue = if (withVoucher) "60000.0" else "50000.0"
        val taxAmountValue = if (withVoucher) "59400.0" else "60500.0"
        val totalPaidValue = if (withVoucher) "601900.0" else "613000.0"

        return """
            {
              "invoice_number": "INV-2026-FT9012",
              "transaction_date": "2026-09-23 10:15:00",
              "payment_status": "PAID_SETTLED",
              "payment_method": "Fintech Pay Later",
              "merchant": {
                "merchant_id": "MCH-99201",
                "merchant_name": "Mega Elektrindo Ritel",
                "city_location": "Bandung",
                "is_verified": true
              },
              "customer": {
                "customer_id": "CUST-8831",
                "full_name": "Ahmad Dahlan",
                "phone": "081234567890"
              },
              "items": [
                {
                  "item_id": "ITM-001",
                  "item_name": "Keyboard Mechanical RGB",
                  "qty": 1,
                  "unit_price": 250000.0,
                  "subtotal": 250000.0
                },
                {
                  "item_id": "ITM-002",
                  "item_name": "Mouse Wireless Ergonomis",
                  "qty": 2,
                  "unit_price": 100000.0,
                  "subtotal": 200000.0
                },
                {
                  "item_id": "ITM-003",
                  "item_name": "Kabel USB Type-C Braided",
                  "qty": 3,
                  "unit_price": 50000.0,
                  "subtotal": 150000.0
                }
              ],
              "summary": {
                "subtotal_amount": 600000.0,
                "discount_amount": $discountAmountValue,
                "tax_ppn_11": $taxAmountValue,
                "service_fee": 2500.0,
                "total_paid": $totalPaidValue,
                "voucher_code": $voucherCodeValue,
                "voucher_discount_percent": $voucherDiscountValue
              }
            }
        """.trimIndent()
    }
}
