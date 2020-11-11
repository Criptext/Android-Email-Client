package com.criptext.mail.scenes.import_mailbox

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.criptext.mail.R
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.AccountSuspendedDialog
import com.criptext.mail.utils.ui.MessageAndProgressDialog
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.uiobserver.UIObserver
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.util.*


interface ImportMailboxScene{

    fun attachView(model: ImportMailboxModel, importUIObserver: ImportMailboxUIObserver)
    fun showPasswordLoginDialog()
    fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType)
    fun dismissAccountSuspendedDialog()
    fun showPreparingFileDialog()
    fun dismissPreparingFileDialog()
    fun showCheckingForDevicesDialog()
    fun dismissCheckingForDevicesDialog()
    fun getGoogleDriveService(): Drive?
    fun showMessage(message : UIMessage)


    var importUIObserver: ImportMailboxUIObserver?

    class Default(private val view: View): ImportMailboxScene {

        private val context = view.context

        override var importUIObserver: ImportMailboxUIObserver? = null

        private val syncDeviceButton: Button by lazy {
            view.findViewById<Button>(R.id.sync_device)
        }
        private val fromFileButton: Button by lazy {
            view.findViewById<Button>(R.id.from_file)
        }
        private val fromCloudButton: Button by lazy {
            view.findViewById<Button>(R.id.from_cloud)
        }
        private val skipLayout: LinearLayout by lazy {
            view.findViewById<LinearLayout>(R.id.skip_layout)
        }

        private val passwordLoginDialog = PasswordLoginDialog(view.context)
        private val accountSuspended = AccountSuspendedDialog(context)
        private val preparingFileDialog = MessageAndProgressDialog(context, UIMessage(R.string.preparing_file))
        private val checkForDevices = MessageAndProgressDialog(context, UIMessage(R.string.checking_for_devices))

        override fun attachView(model: ImportMailboxModel, importUIObserver: ImportMailboxUIObserver) {
            this.importUIObserver = importUIObserver
            setListeners(importUIObserver)
        }


        private fun setListeners(uiObserver: ImportMailboxUIObserver) {
            syncDeviceButton.setOnClickListener {
                uiObserver.onAnotherDevicePressed()
            }
            fromFileButton.setOnClickListener {
                uiObserver.onFromFilePressed()
            }
            fromCloudButton.setOnClickListener {
                uiObserver.onFromCloudPressed()
            }
            skipLayout.setOnClickListener {
                uiObserver.onSkipPressed()
            }
        }

        override fun showPasswordLoginDialog() {
            passwordLoginDialog.showPasswordLoginDialog(importUIObserver)
        }

        override fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType) {
            accountSuspended.showDialog(observer, email, dialogType)
        }

        override fun dismissAccountSuspendedDialog() {
            accountSuspended.dismissDialog()
        }

        override fun showPreparingFileDialog() {
            preparingFileDialog.showDialog()
        }

        override fun dismissPreparingFileDialog() {
            preparingFileDialog.dismiss()
        }

        override fun showCheckingForDevicesDialog() {
            checkForDevices.showDialog()
        }

        override fun dismissCheckingForDevicesDialog() {
            checkForDevices.dismiss()
        }

        override fun getGoogleDriveService(): Drive? {
            val googleAccount = GoogleSignIn.getLastSignedInAccount(context) ?: return null
            val credential = GoogleAccountCredential.usingOAuth2(
                    context, Collections.singleton(DriveScopes.DRIVE_FILE))
            credential.selectedAccount = googleAccount.account
            return Drive.Builder(
                    NetHttpTransport(),
                    GsonFactory(),
                    credential)
                    .setApplicationName("Criptext Secure Email")
                    .build()
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