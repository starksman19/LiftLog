package com.liftlog.app.core.util

import java.text.Collator
import java.util.Locale

object PolishTextComparator : Comparator<String> {
    private val collators = ThreadLocal.withInitial {
        Collator.getInstance(Locale.forLanguageTag("pl-PL")).apply {
            strength = Collator.PRIMARY
        }
    }

    override fun compare(first: String, second: String): Int = collators.get()!!.compare(first, second)
}
