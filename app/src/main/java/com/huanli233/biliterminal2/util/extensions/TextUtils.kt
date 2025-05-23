package com.huanli233.biliterminal2.util.extensions

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("SimpleDateFormat")
fun Long.formatToDate(
    format: String = "yyyy-MM-dd HH:mm:ss"
): String = SimpleDateFormat(format).format(Date(this))

fun Number.formatNumber(
    thousand: String,
    million: String,
): String {
    val num = this.toLong()
    return when {
        num >= 100_000_000 -> "%.1f${thousand}".format(Locale.getDefault(), num / 100_000_000.0)
        num >= 10_000 -> "%.1f${million}".format(Locale.getDefault(), num / 10_000.0)
        else -> num.toString()
    }
}