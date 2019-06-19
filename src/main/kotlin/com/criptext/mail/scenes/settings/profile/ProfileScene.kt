package com.criptext.mail.scenes.settings.profile

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.scenes.settings.SettingsLogoutDialog
import com.criptext.mail.scenes.settings.profile.ui.BottomDialog
import com.criptext.mail.scenes.settings.profile.ui.ProfileNameDialog
import com.criptext.mail.utils.*
import com.criptext.mail.utils.ui.*
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.uiobserver.UIObserver
import de.hdodenhof.circleimageview.CircleImageView


interface ProfileScene{

    fun attachView(uiObserver: ProfileUIObserver, recipientId: String, domain: String, model: ProfileModel)
    fun showMessage(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun dismissConfirmPasswordDialog()
    fun setConfirmPasswordError(message: UIMessage)
    fun showForgotPasswordDialog(email: String)
    fun showBottomDialog(observer: ProfileUIObserver?)
    fun showProfileNameDialog(fullName: String)
    fun updateProfilePicture(image: Bitmap)
    fun updateProfileName(name: String)
    fun resetProfilePicture(name: String)
    fun showProfilePictureProgress()
    fun hideProfilePictureProgress()
    fun showPreparingFileDialog()
    fun dismissPreparingFileDialog()
    fun showLogoutDialog(isLastDeviceWith2FA: Boolean)
    fun showGeneralDialogWithInputPassword(dialogData: DialogData.DialogMessageData)
    fun setGeneralDialogWithInputError(message: UIMessage)
    fun toggleGeneralDialogLoad(isLoading: Boolean)
    fun showMessageAndProgressDialog(message: UIMessage)
    fun dismissMessageAndProgressDialog()
    fun showAccountSuspendedDialog(observer: UIObserver, email: String, showButton: Boolean)
    fun dismissAccountSuspendedDialog()

    class Default(val view: View): ProfileScene{
        private lateinit var profileUIObserver: ProfileUIObserver

        private val context = view.context
        private val preparingFileDialog = MessageAndProgressDialog(context, UIMessage(R.string.preparing_file))


        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val profileNameButton: View by lazy {
            view.findViewById<View>(R.id.profile_name_button)
        }

        private val profileSignatureButton: View by lazy {
            view.findViewById<View>(R.id.profile_signature_button)
        }

        private val profilePasswordButton: View by lazy {
            view.findViewById<View>(R.id.profile_password_button)
        }

        private val profileRecoveryEmailButton: View by lazy {
            view.findViewById<View>(R.id.profile_recovery_button)
        }
        private val textViewConfirmText: TextView by lazy {
            view.findViewById<TextView>(R.id.not_confirmed_text)
        }

        private val profileReplyToEmailButton: View by lazy {
            view.findViewById<View>(R.id.profile_change_reply_to)
        }

        private val profileLogoutlButton: View by lazy {
            view.findViewById<View>(R.id.profile_logout_button)
        }

        private val profileDeleteAccountButton: View by lazy {
            view.findViewById<View>(R.id.profile_delete_account_button)
        }

        private val profilePicture: CircleImageView by lazy {
            view.findViewById<CircleImageView>(R.id.profile_picture)
        }

        private val profilePictureLoading: ProgressBar by lazy {
            view.findViewById<ProgressBar>(R.id.profile_picture_progress)
        }

        private val changePictureButton: View by lazy {
            view.findViewById<View>(R.id.edit_img)
        }

        private val nameText: TextView by lazy {
            view.findViewById<TextView>(R.id.profile_name)
        }

        private val emailText: TextView by lazy {
            view.findViewById<TextView>(R.id.profile_email)
        }

        private val confirmPasswordDialog = ConfirmPasswordDialog(context)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)
        private val syncAuthDialog = SyncDeviceAlertDialog(context)
        private val attachmentBottomDialog = BottomDialog(context)
        private val settingsProfileNameDialog = ProfileNameDialog(context)
        private val settingLogoutDialog = SettingsLogoutDialog(context)
        private var generalDialogWithInputPassword: GeneralDialogWithInputPassword? = null
        private var messageAndProgressDialog: MessageAndProgressDialog? = null
        private val accountSuspended = AccountSuspendedDialog(context)


        override fun attachView(uiObserver: ProfileUIObserver, recipientId: String, domain: String,
                                model: ProfileModel) {

            profileUIObserver = uiObserver

            backButton.setOnClickListener {
                profileUIObserver.onBackButtonPressed()
            }

            profileNameButton.setOnClickListener {
                profileUIObserver.onEditProfileNamePressed()
            }

            profileSignatureButton.setOnClickListener {
                profileUIObserver.onSignatureOptionClicked()
            }

            profilePasswordButton.setOnClickListener {
                profileUIObserver.onChangePasswordOptionClicked()
            }

            profileRecoveryEmailButton.setOnClickListener {
                profileUIObserver.onRecoveryEmailOptionClicked()
            }

            profileReplyToEmailButton.setOnClickListener {
                profileUIObserver.onReplyToChangeClicked()
            }

            profileLogoutlButton.setOnClickListener {
                profileUIObserver.onLogoutClicked()
            }

            profileDeleteAccountButton.setOnClickListener {
                profileUIObserver.onDeleteAccountClicked()
            }

            changePictureButton.setOnClickListener {
                profileUIObserver.onEditPicturePressed()
            }

            nameText.text = model.userData.name
            emailText.text = model.userData.email
            updateCurrentEmailStatus(model.userData.isEmailConfirmed)

            showProfilePictureProgress()
            UIUtils.setProfilePicture(profilePicture, context.resources, domain, recipientId,
                    model.userData.name,
                    Runnable { hideProfilePictureProgress() })
        }

        private fun updateCurrentEmailStatus(isEmailConfirmed: Boolean){
            if(isEmailConfirmed) {
                textViewConfirmText.setTextColor(ContextCompat.getColor(
                        view.context, R.color.green))
                textViewConfirmText.setText(R.string.status_confirmed)
            }else{
                textViewConfirmText.setTextColor(ContextCompat.getColor(
                        view.context, R.color.red))
                textViewConfirmText.setText(R.string.status_not_confirmed)
            }
        }

        override fun showConfirmPasswordDialog(observer: UIObserver) {
            confirmPasswordDialog.showDialog(observer)
        }

        override fun dismissConfirmPasswordDialog() {
            confirmPasswordDialog.dismissDialog()
        }

        override fun setConfirmPasswordError(message: UIMessage) {
            confirmPasswordDialog.setPasswordError(message)
        }

        override fun showForgotPasswordDialog(email: String) {
            ForgotPasswordDialog(context, email).showForgotPasswordDialog()
        }

        override fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            if(linkAuthDialog.isShowing() != null && linkAuthDialog.isShowing() == false)
                linkAuthDialog.showLinkDeviceAuthDialog(profileUIObserver, untrustedDeviceInfo)
            else if(linkAuthDialog.isShowing() == null)
                linkAuthDialog.showLinkDeviceAuthDialog(profileUIObserver, untrustedDeviceInfo)
        }

        override fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            if(syncAuthDialog.isShowing() != null && syncAuthDialog.isShowing() == false)
                syncAuthDialog.showLinkDeviceAuthDialog(profileUIObserver, trustedDeviceInfo)
            else if(syncAuthDialog.isShowing() == null)
                syncAuthDialog.showLinkDeviceAuthDialog(profileUIObserver, trustedDeviceInfo)
        }

        override fun showBottomDialog(observer: ProfileUIObserver?) {
            attachmentBottomDialog.showDialog(observer)
        }

        override fun showProfileNameDialog(fullName: String) {
            settingsProfileNameDialog.showProfileNameDialog(fullName, profileUIObserver)
        }

        override fun updateProfilePicture(image: Bitmap) {
            profilePicture.setImageBitmap(image)
        }

        override fun updateProfileName(name: String) {
            nameText.text = name
        }

        override fun resetProfilePicture(name: String) {
            profilePicture.setImageBitmap(Utility.getBitmapFromText(
                    name,250, 250))
        }

        override fun showProfilePictureProgress() {
            profilePictureLoading.visibility = View.VISIBLE
        }

        override fun hideProfilePictureProgress() {
            profilePictureLoading.visibility = View.GONE
        }

        override fun showPreparingFileDialog() {
            preparingFileDialog.showDialog()
        }

        override fun dismissPreparingFileDialog() {
            preparingFileDialog.dismiss()
        }

        override fun showLogoutDialog(isLastDeviceWith2FA: Boolean) {
            settingLogoutDialog.showLogoutDialog(profileUIObserver, isLastDeviceWith2FA)
        }

        override fun showGeneralDialogWithInputPassword(dialogData: DialogData.DialogMessageData) {
            generalDialogWithInputPassword = GeneralDialogWithInputPassword(context, dialogData)
            generalDialogWithInputPassword?.showDialog(profileUIObserver)
        }

        override fun setGeneralDialogWithInputError(message: UIMessage) {
            generalDialogWithInputPassword?.setPasswordError(message)
        }

        override fun toggleGeneralDialogLoad(isLoading: Boolean) {
            generalDialogWithInputPassword?.toggleLoad(isLoading)
        }

        override fun showMessageAndProgressDialog(message: UIMessage) {
            messageAndProgressDialog = MessageAndProgressDialog(context, message)
            messageAndProgressDialog?.showDialog()
        }

        override fun dismissMessageAndProgressDialog() {
            messageAndProgressDialog?.dismiss()
        }

        override fun dismissAccountSuspendedDialog() {
            accountSuspended.dismissDialog()
        }

        override fun showAccountSuspendedDialog(observer: UIObserver, email: String, showButton: Boolean) {
            accountSuspended.showDialog(observer, email, showButton)
        }

        override fun showMessage(message: UIMessage) {
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    context,
                    context.getLocalizedUIMessage(message),
                    duration)
            toast.show()
        }
    }

}