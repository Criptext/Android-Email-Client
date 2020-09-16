package com.criptext.mail.scenes.mailbox.ui

import android.view.View
import com.criptext.mail.BaseActivity
import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver

/**
 * Created by gabriel on 2/28/18.
 */

abstract class MailboxUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity) : UIObserver(generalDataSource, host) {
    abstract fun onOpenComposerButtonClicked()
    abstract fun onRefreshMails()
    abstract fun onBackButtonPressed()
    abstract fun onFeedDrawerClosed()
    abstract fun onEmptyTrashPressed()
    abstract fun onUpdateBannerXPressed()
    abstract fun onWelcomeTourHasFinished()
    abstract fun onSyncPhoneBookYes()
    abstract fun onStartGuideEmail()
    abstract fun showStartGuideEmail(view: View)
    abstract fun showStartGuideMultiple(view: View)
    abstract fun showSecureIconGuide(view: View)
    abstract fun restoreFromBackupPressed()
    abstract fun turnOnAutoBackup()
    abstract fun notNowAutoBackup()
    abstract fun restoreFromLocalBackupPressed()
    abstract fun openAdminWebsite()
}