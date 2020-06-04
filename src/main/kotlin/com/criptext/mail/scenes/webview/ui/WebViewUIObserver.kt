package com.criptext.mail.scenes.webview.ui

import com.criptext.mail.utils.uiobserver.UIObserver

interface WebViewUIObserver: UIObserver {
    fun onBackButtonPressed()
    fun onUrlChanged(newUrl: String)
    fun onPageStartedLoading(url: String?)
    fun onPageFinishedLoading(url: String)
    fun onJSInterfaceClose()
    fun onBrowserDownload(url: String, contentDisposition: String)
}