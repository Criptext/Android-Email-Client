package com.criptext.mail.scenes.restorebackup

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import com.criptext.mail.R
import com.criptext.mail.androidui.progressdialog.IntervalTimer
import com.criptext.mail.scenes.restorebackup.holders.*
import com.criptext.mail.scenes.settings.DevicesListItemListener
import com.criptext.mail.scenes.signin.SignInSceneController
import com.criptext.mail.scenes.signin.SignInSceneModel
import com.criptext.mail.scenes.signin.holders.*
import com.criptext.mail.utils.*
import com.criptext.mail.utils.ui.MessageAndProgressDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.textfield.TextInputLayout
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.util.*


interface RestoreBackupScene{

    fun attachView(model: RestoreBackupModel, uiObserver: RestoreBackupUIObserver,
                   message: UIMessage? = null)
    fun showMessage(message : UIMessage)
    fun setProgress(progress: Int, onFinish: (() -> Unit)? = null)
    fun getGoogleDriveService(): Drive?
    fun updateFileData(fileSize: Long, lastModified: Long, isLocal: Boolean)
    fun enableRestoreButton(isEnabled: Boolean)
    fun showPreparingFileDialog()
    fun dismissPreparingFileDialog()
    fun localPercentageAnimation()


    var uiObserver: RestoreBackupUIObserver?

    class Default(private val view: View): RestoreBackupScene {

        private val context = view.context
        private val viewGroup = view.parent as ViewGroup
        private var holder: BaseRestoreBackupHolder? = null

        override var uiObserver: RestoreBackupUIObserver? = null

        private val preparingFileDialog = MessageAndProgressDialog(context, UIMessage(R.string.preparing_file))
        private val restoreButton: Button = view.findViewById(R.id.restore_button)

        override fun attachView(model: RestoreBackupModel, uiObserver: RestoreBackupUIObserver,
                                message: UIMessage?) {
            removeAllViews()
            this.uiObserver = uiObserver
            val state = model.state
            holder = when (state) {
                is RestoreBackupLayoutState.Searching -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_restore_backup_searching, viewGroup)
                    SearchingHolder(newLayout)
                }
                is RestoreBackupLayoutState.Found -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_restore_backup_found, viewGroup)
                    FoundHolder(newLayout, model)
                }
                is RestoreBackupLayoutState.NotFound -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_restore_backup_not_found, viewGroup)
                    SearchingHolder(newLayout)
                }
                is RestoreBackupLayoutState.Restoring -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_restore_backup_restoring, viewGroup)
                    RestoringHolder(newLayout, model)
                }
                is RestoreBackupLayoutState.Error -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_restore_backup_error, viewGroup)
                    ErrorHolder(newLayout, model.isLocal, message)
                }
            }
            holder?.uiObserver = uiObserver

        }

        private fun removeAllViews() {
            viewGroup.removeAllViews()
            holder?.uiObserver = null
        }

        override fun enableRestoreButton(isEnabled: Boolean) {
            restoreButton.isEnabled = isEnabled
        }

        override fun setProgress(progress: Int, onFinish: (() -> Unit)?) {
            val currentHolder = holder as RestoringHolder
            currentHolder.setProgress(progress, onFinish)
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

        override fun updateFileData(fileSize: Long, lastModified: Long, isLocal: Boolean) {
            val currentHolder = holder as FoundHolder
            currentHolder.updateFileData(fileSize, lastModified, isLocal)
        }

        override fun showPreparingFileDialog() {
            preparingFileDialog.showDialog()
        }

        override fun dismissPreparingFileDialog() {
            preparingFileDialog.dismiss()
        }

        override fun localPercentageAnimation() {
            val currentHolder = holder as RestoringHolder
            currentHolder.localPercentageAnimation()
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