package com.example.fintectbillingapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fintectbillingapp.databinding.ActivityMainBinding
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupActionListeners()
    }

    private fun setupActionListeners() {
        binding.btnParseBasic.setOnClickListener {
            parseMerchantJson()
        }

        binding.btnOpenInvoiceNoVoucher.setOnClickListener {
            openInvoiceActivity(withVoucher = false)
        }

        binding.btnOpenInvoiceVoucher.setOnClickListener {
            openInvoiceActivity(withVoucher = true)
        }
    }

    private fun parseMerchantJson() {
        val jsonString = """
            {
              "merchant_id": "MCH-99201",
              "merchant_name": "Mega Elektrindo Ritel",
              "city_location": "Bandung",
              "is_verified": true
            }
        """.trimIndent()

        val gson = Gson()
        val merchant = gson.fromJson(jsonString, Merchant::class.java)

        val verificationStatus = if (merchant.isVerified) "RESMI (Ya)" else "Belum"

        val resultText = """
            ID Mitra: ${merchant.merchantId}
            Nama Toko: ${merchant.merchantName}
            Kota: ${merchant.cityLocation}
            Status Verifikasi: $verificationStatus
        """.trimIndent()

        binding.tvOutputBasic.text = resultText
    }

    private fun openInvoiceActivity(withVoucher: Boolean) {
        val intent = Intent(this, InvoiceActivity::class.java).apply {
            putExtra(InvoiceActivity.EXTRA_WITH_VOUCHER, withVoucher)
        }
        startActivity(intent)
    }
}
