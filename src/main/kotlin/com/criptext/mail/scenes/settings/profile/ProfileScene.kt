package com.criptext.mail.scenes.settings.profile

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.scenes.settings.privacyandsecurity.pinscreen.MessageAndProgressDialog
import com.criptext.mail.scenes.settings.profile.ui.BottomDialog
import com.criptext.mail.scenes.settings.profile.ui.ProfileNameDialog
import com.criptext.mail.utils.*
import com.criptext.mail.utils.ui.ConfirmPasswordDialog
import com.criptext.mail.utils.ui.ForgotPasswordDialog
import com.criptext.mail.utils.ui.LinkNewDeviceAlertDialog
import com.criptext.mail.utils.uiobserver.UIObserver
import de.hdodenhof.circleimageview.CircleImageView


interface ProfileScene{

    fun attachView(uiObserver: ProfileUIObserver, recipientId: String, model: ProfileModel)
    fun showMessage(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
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

    class Default(val view: View): ProfileScene{
        private lateinit var profileUIObserver: ProfileUIObserver

        private val context = view.context
        private val preparingFileDialog = MessageAndProgressDialog(context, UIMessage(R.string.preparing_file))


        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val nameText: TextView by lazy {
            view.findViewById<TextView>(R.id.profile_name)
        }

        private val changeNameButton: View by lazy {
            view.findViewById<View>(R.id.edit_name_button)
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

        private val confirmPasswordDialog = ConfirmPasswordDialog(context)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)
        private val attachmentBottomDialog = BottomDialog(context)
        private val settingsProfileNameDialog = ProfileNameDialog(context)


        override fun attachView(uiObserver: ProfileUIObserver, recipientId: String,
                                model: ProfileModel) {

            profileUIObserver = uiObserver

            backButton.setOnClickListener {
                profileUIObserver.onBackButtonPressed()
            }

            changeNameButton.setOnClickListener {
                profileUIObserver.onEditProfileNamePressed()
            }

            changePictureButton.setOnClickListener {
                profileUIObserver.onEditPicturePressed()
            }

            nameText.text = model.name
            showProfilePictureProgress()
            UIUtils.setProfilePicture(profilePicture, context.resources, recipientId, model.name, Runnable { hideProfilePictureProgress() })
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