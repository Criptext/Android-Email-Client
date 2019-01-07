package com.criptext.mail.utils

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.content.Context.PRINT_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.print.PrintManager
import android.webkit.WebView
import com.criptext.mail.R


object PrintUtils{
    fun createWebPrintJob(webView: WebView, context: Context, documentName: String) {

        val printManager = context
                .getSystemService(Context.PRINT_SERVICE) as PrintManager

        val printAdapter = webView.createPrintDocumentAdapter(documentName)

        val jobName = context.getString(R.string.app_name) + " Print Test"

        printManager.print(jobName, printAdapter,
                PrintAttributes.Builder().build())
    }
}