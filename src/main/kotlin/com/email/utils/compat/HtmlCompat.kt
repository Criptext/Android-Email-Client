package com.email.utils.compat

import android.text.Html
import android.text.Spanned



/**
 * Avoids deprecated calls of Html functions in newer platforms
 * Created by gabriel on 7/18/17.
 */

class HtmlCompat {
    companion object {
        fun fromHtml(html: String): Spanned =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(html)
            }
    }

    fun fromHtml(html: String) = Companion.fromHtml(html)
}