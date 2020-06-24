package com.criptext.mail.scenes

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.params.LinkingParams
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.services.jobs.CloudBackupJobService
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.ui.data.TransitionAnimationData

/**
 * Base class for all the main controllers.
 * Created by sebas on 1/30/18.
 */
abstract class SceneController(private val host: IHostActivity,
                               private var activeAccount: ActiveAccount?,
                               private var storage: KeyValueStorage) {
    /**
     * Host activity will check this value every time it has to redraw the toolbar's menu. You
     * should return the resource id of the menu you wish to display.
     */
    abstract val menuResourceId: Int?

    /**
     * Called during the host activity's `onStart()`. This where your controller's "setup" code
     * should go.
     * @param activityMessage A message sent from another controller. The implementation should
     * try to handle it.
     * @return true if the message was handled correctly, otherwise false.
     */
    abstract fun onStart(activityMessage: ActivityMessage?): Boolean

    /**
     * Called during the host activity's `onStop()`. This where your controller's "teardown" code
     * should go.
     */
    abstract fun onResume(activityMessage: ActivityMessage?): Boolean

    /**
     * Called during the host activity's `onStop()`. This where your controller's "teardown" code
     * should go.
     */
    abstract fun onPause()

    /**
     * Called during the host activity's `onStop()`. This where your controller's "teardown" code
     * should go.
     */
    abstract fun onStop()

    /**
     * Called during the host activity's `onBackPressed`. If this function returns true, host
     * activity with call `super.onBackPressed()`, potentially closing the activity.
     *
     * If you don't want your host activity to be closed after the account presses back, make this
     * function return false.
     */
    abstract fun onBackPressed(): Boolean

    /**
     * Called during the host activity's onOptionsItemSelected. You only get the selected item's
     * id number, to avoid coupling the controller code with android APIs.
     */
    abstract fun onOptionsItemSelected(itemId: Int)

    /**
     * Called during the host activity's onRequestPermissionResult.
     */
    abstract fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)

    /**
     * Called during the host activity's onCreateOptionsMenu.
     */
    abstract fun onMenuChanged(menu: IHostActivity.IActivityMenu)

    /**
     * Called to tell the controller to send an specific event.
     */
    abstract fun onNeedToSendEvent(event: Int)

    protected fun onLogout(result: GeneralResult.Logout){
        val account = activeAccount
        if(account != null) {
            when (result) {
                is GeneralResult.Logout.Success -> {
                    CloudBackupJobService.cancelJob(storage, result.oldAccountId)
                    if (result.activeAccount == null)
                        host.goToScene(
                                params = SignInParams(),
                                activityMessage = ActivityMessage.ShowUIMessage(UIMessage(R.string.expired_session)),
                                animationData = TransitionAnimationData(
                                        forceAnimation = true,
                                        enterAnim = android.R.anim.fade_in,
                                        exitAnim = android.R.anim.fade_out
                                ),
                                deletePastIntents = true,
                                keep = false)
                    else {
                        activeAccount = result.activeAccount
                        host.goToScene(
                                params = MailboxParams(),
                                activityMessage = ActivityMessage.ShowUIMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(account.userEmail))),
                                deletePastIntents = true,
                                keep = false)
                    }

                }
            }
        }
    }

    protected fun onSyncAccept(resultData: GeneralResult.SyncAccept){
        when (resultData) {
            is GeneralResult.SyncAccept.Success -> {
                host.goToScene(
                        params = LinkingParams(resultData.syncAccount, resultData.deviceId,
                                resultData.uuid, resultData.deviceType),
                        activityMessage = ActivityMessage.SyncMailbox(),
                        keep = false, deletePastIntents = true)
            }
            is GeneralResult.SyncAccept.Failure -> {
                host.showToastMessage(resultData.message)
            }
        }
    }


    protected fun onDeviceRemovedRemotely(result: GeneralResult.DeviceRemoved){
        val account = activeAccount
        if(account != null) {
            when (result) {
                is GeneralResult.DeviceRemoved.Success -> {
                    if (result.activeAccount == null)
                        host.goToScene(params = SignInParams(), activityMessage = ActivityMessage.ShowUIMessage(UIMessage(R.string.device_removed_remotely_exception)),
                                animationData = TransitionAnimationData(
                                        forceAnimation = true,
                                        enterAnim = android.R.anim.fade_in,
                                        exitAnim = android.R.anim.fade_out
                                ), deletePastIntents = true, keep = false)
                    else {
                        activeAccount = result.activeAccount
                        host.goToScene(params = MailboxParams(),
                                activityMessage = ActivityMessage.ShowUIMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(account.userEmail))),
                                keep = false, deletePastIntents = true)
                    }
                }
            }
        }
    }

    protected fun onPasswordChangedRemotely(result: GeneralResult.ConfirmPassword){
        host.toggleLoad(false)
        when (result) {
            is GeneralResult.ConfirmPassword.Success -> {
                host.dismissConfirmPasswordDialog()
                host.showToastMessage(UIMessage(R.string.update_password_success))
            }
            is GeneralResult.ConfirmPassword.Failure -> {
                host.setConfirmPasswordError(UIMessage(R.string.password_enter_error))
            }
        }
    }

    protected fun onLinkAccept(resultData: GeneralResult.LinkAccept){
        when (resultData) {
            is GeneralResult.LinkAccept.Success -> {
                host.goToScene(
                        params = LinkingParams(resultData.linkAccount, resultData.deviceId,
                                resultData.uuid, resultData.deviceType),
                        activityMessage = null,
                        keep = false, deletePastIntents = true
                )
            }
            is GeneralResult.LinkAccept.Failure -> {
                host.showToastMessage(resultData.message)
            }
        }
    }
}
