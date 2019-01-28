package com.criptext.mail.mocks

import android.content.ContentResolver
import android.os.Handler
import android.view.View
import com.criptext.mail.ExternalActivityParams
import com.criptext.mail.IHostActivity
import com.criptext.mail.push.data.IntentExtrasData
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.params.SceneParams
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.mailtemplates.CriptextMailTemplate

/**
 * Created by gabriel on 3/1/18.
 */

class MockedIHostActivity: IHostActivity{
    override fun contextMenuRegister(view: View) {
        
    }

    override fun getMailTemplate(type: CriptextMailTemplate.TemplateType): CriptextMailTemplate? {
        return null
    }

    override fun setAppTheme(themeResource: Int) {
        
    }

    override fun getHandler(): Handler? {
        return null
    }

    override fun getContentResolver(): ContentResolver? {
        return null
    }

    override fun postDelay(runnable: Runnable, delayMilliseconds: Long) {

    }

    override fun runOnUiThread(runnable: Runnable) {
        
    }

    override fun launchExternalActivityForResult(params: ExternalActivityParams) {
        activityLaunched = true
    }

    var isFinished: Boolean = false
    var activityLaunched: Boolean = false

    override fun exitToScene(params: SceneParams, activityMessage: ActivityMessage?, forceAnimation: Boolean,
                             deletePastIntents: Boolean) {
        isFinished = true
    }

    override fun showDialog(message: UIMessage) {
    }

    override fun dismissDialog() {
    }

    override fun refreshToolbarItems() {
    }

    override fun goToScene(params: SceneParams, keep: Boolean, deletePastIntents: Boolean) {
    }

    override fun finishScene() {
        isFinished = true
    }

    override fun getLocalizedString(message: UIMessage): String {
        return "test"
    }

    override fun checkPermissions(requestCode: Int, permission: String): Boolean {
        return true
    }

    override fun getIntentExtras(): IntentExtrasData? {
        return null
    }

    override fun showStartGuideView(view: View, title: Int, dimension: Int) {
    }
}