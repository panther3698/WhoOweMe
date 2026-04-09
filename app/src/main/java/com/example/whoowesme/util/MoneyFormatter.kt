package com.example.whoowesme.util

import java.util.Locale
import kotlin.math.abs

object MoneyFormatter {
    private const val RUPEE_SYMBOL = "\u20B9"

    fun format(amount: Double, absolute: Boolean = false): String {
        val value = if (absolute) abs(amount) else amount
        return "$RUPEE_SYMBOL\u00A0${String.format(Locale.getDefault(), "%,.2f", value)}"
    }
}
