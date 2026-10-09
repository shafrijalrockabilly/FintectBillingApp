package com.example.fintectbillingapp

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun String.toFriendlyDate(): String {
    return try {
        val localeId = Locale.forLanguageTag("id-ID")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", localeId)
        inputFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        val date = inputFormat.parse(this) ?: return this

        val outputFormat = SimpleDateFormat("EEEE, d MMMM yyyy - 'Pukul' HH:mm 'WIB'", localeId)
        outputFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        outputFormat.format(date)
    } catch (_: Exception) {
        this
    }
}

fun Double.toRupiah(): String {
    val localeId = Locale.forLanguageTag("id-ID")
    val numberFormat = NumberFormat.getNumberInstance(localeId)
    return "Rp" + numberFormat.format(this.toLong())
}
