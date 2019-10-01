package com.criptext.mail.utils.apputils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import com.criptext.mail.R
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.GeneralDialogConfirmation
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogType


object AppRater {
    private const val APP_NAME = "com.criptext.mail"// Package Name

    private const val DAYS_UNTIL_PROMPT = 3//Min number of days
    private const val LAUNCHES_UNTIL_PROMPT = 3//Min number of launches

    fun appLaunched(ctx: Context, storage: KeyValueStorage) {
        if (storage.getBool(KeyValueStorage.StringKey.RateDontShowAgain, false)) {
            return
        }

        val launchCount = storage.getLong(KeyValueStorage.StringKey.RateLaunchCount, 0) + 1
        storage.putLong(KeyValueStorage.StringKey.RateLaunchCount, launchCount)

        // Get date of first launch
        var dateFirstLaunch = storage.getLong(KeyValueStorage.StringKey.RateDateFirstLaunch, 0L)
        if (dateFirstLaunch == 0L) {
            dateFirstLaunch = System.currentTimeMillis()
            storage.putLong(KeyValueStorage.StringKey.RateDateFirstLaunch, dateFirstLaunch)
        }

        // Wait at least n days before opening
        if (launchCount >= LAUNCHES_UNTIL_PROMPT) {
            showRateDialog(ctx, storage)
            if (System.currentTimeMillis() >= dateFirstLaunch + DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000) {
                showRateDialog(ctx, storage)
            }
        }
    }

    private fun showRateDialog(mContext: Context, storage: KeyValueStorage) {
        val dialogData = DialogData.DialogConfirmationData(
                title = UIMessage(R.string.rate_us_title),
                message = listOf(UIMessage(R.string.rate_us_message)),
                type = DialogType.Message()
        )
        val generalDialogConfirmation = GeneralDialogConfirmation(mContext, dialogData)
        generalDialogConfirmation.showDialog(null)
        generalDialogConfirmation.btnOk.text = mContext.getLocalizedUIMessage(UIMessage(R.string.rate_us))
        generalDialogConfirmation.btnCancel.text = mContext.getLocalizedUIMessage(UIMessage(R.string.rate_remind_later))
        generalDialogConfirmation.btnOk.setOnClickListener {
            mContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$APP_NAME")))
            generalDialogConfirmation.dismissDialog()
        }

        generalDialogConfirmation.btnCancel.setOnClickListener {
            generalDialogConfirmation.dismissDialog()
        }
    }
}