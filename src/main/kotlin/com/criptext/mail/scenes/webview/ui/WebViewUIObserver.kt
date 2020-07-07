package com.criptext.mail.scenes.webview.ui

import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver

abstract class WebViewUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onBackButtonPressed()
    abstract fun onUrlChanged(newUrl: String)
    abstract fun onPageStartedLoading(url: String?)
    abstract fun onPageFinishedLoading(url: String)
    abstract fun onJSInterfaceClose()
    abstract fun onCopyText(text: String)
    abstract fun onBrowserDownload(url: String, contentDisposition: String)
    abstract fun onPageReceiveError()
}