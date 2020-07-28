package com.criptext.mail.scenes.signup.customize

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.criptext.mail.R
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.signup.customize.holder.*
import com.criptext.mail.scenes.signup.customize.ui.BottomDialog
import com.criptext.mail.scenes.signup.customize.ui.CustomizeUIObserver
import com.criptext.mail.services.jobs.CloudBackupJobService
import com.criptext.mail.utils.AccountUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.GeneralDialogConfirmation
import com.criptext.mail.utils.ui.MessageAndProgressDialog
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.validation.ProgressButtonState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.util.*

interface CustomizeScene {

    fun showError(message : UIMessage)
    fun initLayout(model: CustomizeSceneModel, uiObserver: CustomizeUIObserver, activeAccount: ActiveAccount)
    fun setSubmitButtonState(state : ProgressButtonState)
    fun showBottomDialog(observer: CustomizeUIObserver?)
    fun showProfilePictureProgress(show: Boolean)
    fun changeToNextButton()
    fun updateProfilePicture(image: Bitmap)
    fun showPreparingFileDialog()
    fun dismissPreparingFileDialog()
    fun getGoogleDriveService(): Drive?
    fun scheduleCloudBackupJob(period: Int, accountId: Long, useWifiOnly: Boolean)
    fun removeFromScheduleCloudBackupJob(accountId: Long)
    fun updateContactSwitch(isChecked: Boolean)
    fun updateRecoveryEmailVerification(isVerified: Boolean)
    fun showSkipRecoveryEmailWarningDialog()
    fun setupRecoveryEmailTimer()
    fun showAwesomeText(show: Boolean)

    var uiObserver: CustomizeUIObserver?

    class CustomizeSceneView(private val view: View): CustomizeScene {

        private var holder: BaseCustomizeHolder

        private val viewGroup = view.parent as ViewGroup
        private val preparingFileDialog = MessageAndProgressDialog(viewGroup.context, UIMessage(R.string.preparing_file))
        private val recoveryEmailWarning = GeneralDialogConfirmation(
                context = viewGroup.context,
                data = DialogData.DialogConfirmationData(
                        title = UIMessage(R.string.recovery_email_warning_dialog_title),
                        confirmButtonText = R.string.btn_skip,
                        type = DialogType.Message(),
                        message = listOf(UIMessage(R.string.recovery_email_warning_dialog_message))
                )
        )
        private val attachmentBottomDialog = BottomDialog(viewGroup.context)

        override var uiObserver: CustomizeUIObserver? = null
            set(value) {
                holder.uiObserver = value
                field = value
            }

        init {
            val customizeLayout = View.inflate(
                    view.context,
                    R.layout.holder_customize_account_created, viewGroup)
            holder = CustomizeAccountCreatedHolder(customizeLayout, "", "")
        }

        override fun initLayout(model: CustomizeSceneModel, uiObserver: CustomizeUIObserver, activeAccount: ActiveAccount) {
            removeAllViews()
            val state = model.state ?: return
            holder = when (state) {
                is CustomizeLayoutState.AccountCreated -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_customize_account_created, viewGroup)
                    CustomizeAccountCreatedHolder(newLayout, state.name, state.email)
                }
                is CustomizeLayoutState.ProfilePicture -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_customize_profile, viewGroup)
                    CustomizePictureHolder(newLayout, state.name, activeAccount.recipientId)
                }
                is CustomizeLayoutState.DarkTheme -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_customize_theme, viewGroup)
                    CustomizeThemeHolder(newLayout, model.hasDarkTheme)
                }
                is CustomizeLayoutState.Contacts -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_customize_contacts, viewGroup)
                    CustomizeContactsHolder(newLayout, state.hasAllowedContacts)
                }
                is CustomizeLayoutState.VerifyRecoveryEmail -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_customize_recovery_email, viewGroup)
                    CustomizeRecoveryEmailHolder(newLayout, state.recoveryEmail)
                }
                is CustomizeLayoutState.CloudBackup -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_customize_cloud_backup, viewGroup)
                    CustomizeCloudBackupHolder(newLayout)
                }
            }
            this.uiObserver = uiObserver
        }

        override fun showProfilePictureProgress(show: Boolean) {
            val currentHolder = holder as? CustomizePictureHolder
            currentHolder?.showProfilePictureProgress(show)

        }

        override fun showBottomDialog(observer: CustomizeUIObserver?) {
            attachmentBottomDialog.showDialog(observer)
        }

        override fun setSubmitButtonState(state: ProgressButtonState) {
            holder.setSubmitButtonState(state)
        }

        override fun changeToNextButton() {
            val currentHolder = holder
            when(currentHolder){
                is CustomizePictureHolder -> {
                    currentHolder.changeNextButton()
                }
                is CustomizeRecoveryEmailHolder -> {
                    currentHolder.changeNextButton()
                }
            }
        }

        override fun updateProfilePicture(image: Bitmap) {
            val currentHolder = holder as? CustomizePictureHolder
            currentHolder?.setImageBitmap(image)
        }

        override fun showPreparingFileDialog() {
            preparingFileDialog.showDialog()
        }

        override fun dismissPreparingFileDialog() {
            preparingFileDialog.dismiss()
        }

        override fun getGoogleDriveService(): Drive? {
            val googleAccount = GoogleSignIn.getLastSignedInAccount(viewGroup.context) ?: return null
            val credential = GoogleAccountCredential.usingOAuth2(
                    viewGroup.context, Collections.singleton(DriveScopes.DRIVE_FILE))
            credential.selectedAccount = googleAccount.account
            return Drive.Builder(
                    NetHttpTransport(),
                    GsonFactory(),
                    credential)
                    .setApplicationName("Criptext Secure Email")
                    .build()
        }

        override fun scheduleCloudBackupJob(period: Int, accountId: Long, useWifiOnly: Boolean) {
            val cloudBackupJobService = CloudBackupJobService()
            cloudBackupJobService.schedule(viewGroup.context, AccountUtils.getFrequencyPeriod(period), accountId, useWifiOnly)
        }

        override fun removeFromScheduleCloudBackupJob(accountId: Long) {
            val cloudBackupJobService = CloudBackupJobService()
            cloudBackupJobService.cancel(viewGroup.context, accountId)
        }

        override fun updateContactSwitch(isChecked: Boolean) {
            val currentHolder = holder as? CustomizeContactsHolder
            currentHolder?.updateContactSwitch(isChecked)
        }

        override fun showSkipRecoveryEmailWarningDialog() {
            recoveryEmailWarning.showDialog(uiObserver)
        }

        override fun updateRecoveryEmailVerification(isVerified: Boolean) {
            val currentHolder = holder as? CustomizeRecoveryEmailHolder
            currentHolder?.updateRecoveryEmailVerification(isVerified)
        }

        override fun setupRecoveryEmailTimer() {
            val currentHolder = holder as? CustomizeRecoveryEmailHolder
            currentHolder?.setupRecoveryEmailTimer()
        }

        override fun showAwesomeText(show: Boolean) {
            val currentHolder = holder as? CustomizeContactsHolder
            currentHolder?.showAwesomeText(show)
        }

        override fun showError(message: UIMessage) {
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    view.context,
                    view.context.getLocalizedUIMessage(message),
                    duration)
            toast.show()
        }

        private fun removeAllViews() {
            holder.uiObserver = null
            viewGroup.removeAllViews()
        }
    }
}
