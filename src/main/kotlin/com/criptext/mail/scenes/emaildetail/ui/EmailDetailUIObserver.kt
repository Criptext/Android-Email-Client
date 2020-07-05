package com.criptext.mail.scenes.emaildetail.ui

import android.view.View
import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver

/**
 * Created by gabriel on 2/28/18.
 */

abstract class EmailDetailUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onBackButtonPressed()
    abstract fun showStartGuideEmailIsRead(view: View)
    abstract fun showStartGuideMenu(view: View)
}