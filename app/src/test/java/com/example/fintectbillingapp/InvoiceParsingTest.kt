package com.example.fintectbillingapp

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InvoiceParsingTest {

    private val gson = Gson()

    @Test
    fun testParseMerchant() {
        val jsonString = """
            {
              "merchant_id": "MCH-99201",
              "merchant_name": "Mega Elektrindo Ritel",
              "city_location": "Bandung",
              "is_verified": true
            }
        """.trimIndent()

        val merchant = gson.fromJson(jsonString, Merchant::class.java)

        assertEquals("MCH-99201", merchant.merchantId)
        assertEquals("Mega Elektrindo Ritel", merchant.merchantName)
        assertEquals("Bandung", merchant.cityLocation)
        assertTrue(merchant.isVerified)
    }

    @Test
    fun testParseInvoiceWithoutVoucher() {
        val jsonString = """
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
                "discount_amount": 50000.0,
                "tax_ppn_11": 60500.0,
                "service_fee": 2500.0,
                "total_paid": 613000.0,
                "voucher_code": null,
                "voucher_discount_percent": null
              }
            }
        """.trimIndent()

        val invoice = gson.fromJson(jsonString, InvoiceResponse::class.java)

        assertEquals("INV-2026-FT9012", invoice.invoiceNumber)
        assertEquals("PAID_SETTLED", invoice.paymentStatus)
        assertEquals(3, invoice.items.size)
        assertNull(invoice.summary.voucherCode)
        assertNull(invoice.summary.voucherDiscountPercent)
        assertEquals(613000.0, invoice.summary.totalPaid, 0.001)
    }

    @Test
    fun testParseInvoiceWithVoucher() {
        val jsonString = """
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
              "items": [],
              "summary": {
                "subtotal_amount": 600000.0,
                "discount_amount": 60000.0,
                "tax_ppn_11": 59400.0,
                "service_fee": 2500.0,
                "total_paid": 601900.0,
                "voucher_code": "HEMAT10",
                "voucher_discount_percent": 10
              }
            }
        """.trimIndent()

        val invoice = gson.fromJson(jsonString, InvoiceResponse::class.java)

        assertEquals("HEMAT10", invoice.summary.voucherCode)
        assertEquals(10, invoice.summary.voucherDiscountPercent)
        assertEquals(601900.0, invoice.summary.totalPaid, 0.001)
    }

    @Test
    fun testFriendlyDateExtension() {
        val validDate = "2026-09-23 10:15:00"
        val formatted = validDate.toFriendlyDate()
        assertEquals("Rabu, 23 September 2026 - Pukul 10:15 WIB", formatted)

        val invalidDate = "invalid-date-format"
        val formattedInvalid = invalidDate.toFriendlyDate()
        assertEquals("invalid-date-format", formattedInvalid)
    }

    @Test
    fun testToRupiahExtension() {
        assertEquals("Rp613.000", 613000.0.toRupiah())
        assertEquals("Rp60.000", 60000.0.toRupiah())
        assertEquals("Rp2.500", 2500.0.toRupiah())
    }
}
