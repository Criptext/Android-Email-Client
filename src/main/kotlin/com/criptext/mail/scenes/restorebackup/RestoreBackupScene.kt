package com.criptext.mail.scenes.restorebackup

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import com.beardedhen.androidbootstrap.BootstrapProgressBar
import com.criptext.mail.R
import com.criptext.mail.utils.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.textfield.TextInputLayout
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.util.*


interface RestoreBackupScene{

    fun attachView(model: RestoreBackupModel, uiObserver: RestoreBackupUIObserver)
    fun showMessage(message : UIMessage)
    fun setProgress(progress: Int)
    fun showBackupFoundLayout(isEncrypted: Boolean)
    fun showProgressLayout()
    fun showBackupNotFoundLayout()
    fun showBackupRetryLayout()
    fun getGoogleDriveService(): Drive?
    fun updateFileData(fileSize: Long, lastModified: Long)
    fun enableRestoreButton(isEnabled: Boolean)


    var uiObserver: RestoreBackupUIObserver?

    class Default(private val view: View): RestoreBackupScene {

        private val context = view.context

        override var uiObserver: RestoreBackupUIObserver? = null



        private val backupFoundLayout: View = view.findViewById(R.id.backup_found_text)
        private val progressLayout: View = view.findViewById(R.id.progress_layout)
        private val backupNotFoundLayout: View = view.findViewById(R.id.backup_not_found)
        private val backupNeedRetryLayout: View = view.findViewById(R.id.backup_need_retry)
        private val textViewEmail: TextView = view.findViewById(R.id.restore_email)
        private val textViewSize: TextView = view.findViewById(R.id.restore_size)
        private val textViewTitle: TextView = view.findViewById(R.id.restore_title)
        private val textViewLastModified: TextView = view.findViewById(R.id.restore_last_modified)
        private val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        private val progressBarNumber: TextView = view.findViewById(R.id.percentage_advanced)
        private val restoreButton: Button = view.findViewById(R.id.restore_button)
        private val retryButton: Button = view.findViewById(R.id.retry_button)
        private val changeAccountButton: Button = view.findViewById(R.id.restore_change_account_button)
        private val cancelRestore: TextView = view.findViewById(R.id.skip_restore)
        private val cloudIcon: ImageView = view.findViewById(R.id.cloud_icon)

        private val password: AppCompatEditText = view.findViewById(R.id.password)
        private val passwordInput: TextInputLayout = view.findViewById(R.id.password_input)

        override fun attachView(model: RestoreBackupModel, uiObserver: RestoreBackupUIObserver) {
            this.uiObserver = uiObserver
            textViewEmail.text = model.accountEmail
            textViewSize.text = context.getLocalizedUIMessage(UIMessage(R.string.restore_backup_title, arrayOf(0)))
            textViewLastModified.text = context.getLocalizedUIMessage(UIMessage(R.string.restore_backup_last_modified, arrayOf(model.lastModified)))
            textViewTitle.text = context.getLocalizedUIMessage(UIMessage(R.string.restore_backup_title))

            passwordInput.isPasswordVisibilityToggleEnabled = true
            passwordInput.setPasswordVisibilityToggleTintList(
                    AppCompatResources.getColorStateList(context, R.color.non_criptext_email_send_eye))

            setListeners()
            assignPasswordTextListener()


        }

        private fun assignPasswordTextListener() {
            password.addTextChangedListener( object : TextWatcher {
                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    uiObserver?.onPasswordChangedListener(text.toString())
                }

                override fun afterTextChanged(p0: Editable?) {
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
        }

        private fun setListeners(){
            changeAccountButton.setOnClickListener {
                uiObserver?.onChangeDriveAccount()
            }
            cancelRestore.setOnClickListener {
                uiObserver?.onCancelRestore()
            }
            restoreButton.setOnClickListener {
                uiObserver?.onRestore()
            }
            retryButton.setOnClickListener {
                uiObserver?.onRetryRestore()
            }
        }

        override fun showBackupFoundLayout(isEncrypted: Boolean) {
            cloudIcon.setImageResource(R.drawable.restore_cloud)
            backupFoundLayout.visibility = View.VISIBLE
            progressLayout.visibility = View.GONE
            backupNotFoundLayout.visibility = View.GONE
            backupNeedRetryLayout.visibility = View.GONE
            changeAccountButton.visibility = View.VISIBLE
            if(isEncrypted) {
                textViewTitle.text = context.getLocalizedUIMessage(UIMessage(R.string.restore_backup_title_encrypted))
                passwordInput.visibility = View.VISIBLE
            } else {
                textViewTitle.text = context.getLocalizedUIMessage(UIMessage(R.string.restore_backup_title))
                passwordInput.visibility = View.GONE
            }
        }

        override fun showProgressLayout() {
            cloudIcon.setImageResource(R.drawable.restore_cloud)
            textViewTitle.text = context.getLocalizedUIMessage(UIMessage(R.string.restore_backup_restoring_title))
            progressLayout.visibility = View.VISIBLE
            backupFoundLayout.visibility = View.GONE
            backupNotFoundLayout.visibility = View.GONE
            backupNeedRetryLayout.visibility = View.GONE
            changeAccountButton.visibility = View.GONE
        }

        override fun showBackupNotFoundLayout() {
            cloudIcon.setImageResource(R.drawable.no_cloud)
            textViewTitle.text = context.getLocalizedUIMessage(UIMessage(R.string.restore_backup_not_found_title))
            progressLayout.visibility = View.GONE
            backupFoundLayout.visibility = View.GONE
            backupNotFoundLayout.visibility = View.VISIBLE
            backupNeedRetryLayout.visibility = View.GONE
            changeAccountButton.visibility = View.VISIBLE
        }

        override fun showBackupRetryLayout() {
            cloudIcon.setImageResource(R.drawable.oops_cloud)
            textViewTitle.text = context.getLocalizedUIMessage(UIMessage(R.string.keep_waiting_title))
            backupFoundLayout.visibility = View.GONE
            backupNotFoundLayout.visibility = View.GONE
            progressLayout.visibility = View.GONE
            backupNeedRetryLayout.visibility = View.VISIBLE
            changeAccountButton.visibility = View.VISIBLE
        }

        override fun enableRestoreButton(isEnabled: Boolean) {
            restoreButton.isEnabled = isEnabled
        }

        override fun setProgress(progress: Int) {
            val anim = UIUtils.animationForProgressBar(progressBar, progress, progressBarNumber, 1000)
            anim.start()
        }

        override fun getGoogleDriveService(): Drive? {
            val googleAccount = GoogleSignIn.getLastSignedInAccount(context) ?: return null
            val credential = GoogleAccountCredential.usingOAuth2(
                    context, Collections.singleton(DriveScopes.DRIVE_FILE))
            credential.selectedAccount = googleAccount.account
            return Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential)
                    .setApplicationName("Criptext Secure Email")
                    .build()
        }

        override fun updateFileData(fileSize: Long, lastModified: Long) {
            textViewSize.text = Utility.humanReadableByteCount(fileSize, true)
            textViewLastModified.text = context.getLocalizedUIMessage(UIMessage(R.string.restore_backup_last_modified,
                    arrayOf(DateAndTimeUtils.getTimeForBackup(lastModified))))
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