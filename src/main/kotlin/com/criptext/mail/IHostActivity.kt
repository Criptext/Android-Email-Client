package com.criptext.mail

import android.content.ContentResolver
import android.os.Handler
import android.view.MenuItem
import android.view.View
import com.criptext.mail.push.data.IntentExtrasData
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.params.SceneParams
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.mailtemplates.CriptextMailTemplate
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.TransitionAnimationData
import com.criptext.mail.utils.uiobserver.UIObserver

/**
 * Created by sebas on 1/29/18.
 */
interface IHostActivity {
    fun refreshToolbarItems()
    /**
     * Launches a new activity to show the new scene.
     * @param params object used to create the new scene. If the activity for the scene is used,
     * it will be reused.
     * @param activityMessage an object to pass to the activity that will take the foreground. This
     * is useful when you want to return to an activity that already exists so params won't work.
     * @param keep if true, the current activity will not be finished after the transition, so
     * that the user can return by pressing back.
     * @param animationData a data class to determinate if you want to force the transition animation.
     */
    fun goToScene(params: SceneParams, keep: Boolean, deletePastIntents: Boolean = false,
                  activityMessage: ActivityMessage? = null, animationData: TransitionAnimationData? = null)
    /**
     * Finishes the current activity.
     */
    fun finishScene(activityMessage: ActivityMessage? = null, animationData: TransitionAnimationData? = null)
    fun getLocalizedString(message: UIMessage): String
    fun getIntentExtras(): IntentExtrasData?
    fun showDialog(message: UIMessage)
    fun dismissDialog()
    fun runOnUiThread(runnable: Runnable)
    fun postDelay(runnable: Runnable, delayMilliseconds: Long)
    fun stopMessagesAndCallbacks()
    fun getContentResolver(): ContentResolver?
    fun getHandler(): Handler?
    fun setAppTheme(themeResource: Int)

    fun contextMenuRegister(view: View)
    /**
     * Launch an activity for a result, and then pass that result as an ActivityMessage
     * @param params Object with the necessary data to launch the correct activity.
     */
    fun launchExternalActivityForResult(params: ExternalActivityParams)

    fun checkPermissions(requestCode: Int, permission: String): Boolean

    fun showStartGuideView(view: View, title: Int, dimension: Int)

    fun showCriptextPlusDialog(dialogData: DialogData.DialogCriptextPlusData, uiObserver: UIObserver)
    fun dismissCriptextPlusDialog()

    interface IActivityMenu {
        fun findItemById(id: Int): MenuItem?
    }

    fun getMailTemplate(type: CriptextMailTemplate.TemplateType): CriptextMailTemplate?

    //General Scenes Functionality
    fun dismissConfirmPasswordDialog()
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun toggleLoad(isLoading: Boolean)
    fun setConfirmPasswordError(message: UIMessage)
    fun showToastMessage(message : UIMessage)
}
