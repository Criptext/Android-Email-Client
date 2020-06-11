package com.criptext.mail.scenes.settings.cloudbackup

import android.app.Activity
import android.view.View
import android.widget.*
import com.criptext.mail.R
import com.criptext.mail.services.jobs.CloudBackupJobService
import com.criptext.mail.utils.*
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
import com.lmntrx.android.library.livin.missme.ProgressDialog
import java.util.*


interface CloudBackupScene{

    fun attachView(model: CloudBackupModel, cloudBackupUIObserver1: CloudBackupUIObserver)
    fun updateCloudBackupData(model: CloudBackupModel)
    fun showMessage(message : UIMessage)
    fun showProgressDialog()
    fun setProgressDialog(progress: Int)
    fun hideProgressDialog()
    fun setCloudBackupSwitchState(isChecked: Boolean)
    fun showUploadProgressBar(show: Boolean)
    fun updateFileInfo(fileSize: Long, date: Date?)
    fun setUploadProgress(progress: Int)
    fun showEncryptBackupDialog(observer: CloudBackupUIObserver?)
    fun disableSaveButtonOnDialog()
    fun enableSaveButtonOnDialog()
    fun getGoogleDriveService(): Drive?
    fun scheduleCloudBackupJob(period: Int, accountId: Long, useWifiOnly: Boolean)
    fun removeFromScheduleCloudBackupJob(accountId: Long)
    fun checkCloudBackupIcon()
    fun backingUpNow(isBackingUp: Boolean)
    fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType)
    fun dismissAccountSuspendedDialog()
    fun showPreparingFileDialog()
    fun showLoadingSettingsDialog()
    fun dismissPreparingFileDialog()
    fun dismissLoadingSettingsDialog()

    var cloudBackupUIObserver: CloudBackupUIObserver?

    class Default(private val view: View): CloudBackupScene {

        private val context = view.context

        override var cloudBackupUIObserver: CloudBackupUIObserver? = null

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }
        private val accountEmail: TextView by lazy {
            view.findViewById<TextView>(R.id.account_email)
        }
        private val backupSize: TextView by lazy {
            view.findViewById<TextView>(R.id.backup_size)
        }
        private val backupLastModified: TextView by lazy {
            view.findViewById<TextView>(R.id.last_backup)
        }
        private val backupFileInfo: View by lazy {
            view.findViewById<View>(R.id.backup_information)
        }
        private val uploadProgressLayout: LinearLayout by lazy {
            view.findViewById<LinearLayout>(R.id.cloud_upload_progress_layout)
        }
        private val uploadProgressBar: ProgressBar by lazy {
            view.findViewById<ProgressBar>(R.id.cloud_upload_progress_bar)
        }
        private val uploadProgressMessage: TextView by lazy {
            view.findViewById<TextView>(R.id.upload_progress_message)
        }
        private val cloudBackupSwitch: Switch by lazy {
            view.findViewById<Switch>(R.id.cloud_backup_switch)
        }
        private val backUpNow: View by lazy {
            view.findViewById<View>(R.id.backup_now)
        }
        private val backUpNowText: TextView by lazy {
            view.findViewById<TextView>(R.id.backup_now_text)
        }
        private val autoBackupSpinner: Spinner by lazy {
            view.findViewById<Spinner>(R.id.auto_backup_spinner)
        }
        private val autoBackupText: TextView by lazy {
            view.findViewById<TextView>(R.id.auto_backup_text)
        }
        private val changeGoogleAccount: View by lazy {
            view.findViewById<View>(R.id.change_google_account)
        }
        private val changeGoogleAccountText: TextView by lazy {
            view.findViewById<TextView>(R.id.change_google_account_text)
        }
        private val backupOverText: TextView by lazy {
            view.findViewById<TextView>(R.id.backup_over_text)
        }
        private val backupOverSwitch: Switch by lazy {
            view.findViewById<Switch>(R.id.backup_over_switch)
        }
        private val cloudBackupIcon: ImageView by lazy {
            view.findViewById<ImageView>(R.id.small_cloud_icon)
        }
        private val progressBarBackupNow: ProgressBar by lazy {
            view.findViewById<ProgressBar>(R.id.back_up_now_progress)
        }
        private val exportBackupFile: View by lazy {
            view.findViewById<View>(R.id.export_backup_file)
        }
        private val restoreBackupFile: View by lazy {
            view.findViewById<View>(R.id.restore_backup)
        }

        private val progressDialog = ProgressDialog(view.context as Activity)
        private val encryptPassphraseDialog = EncryptPassphraseDialog(view.context)
        private val accountSuspended = AccountSuspendedDialog(context)
        private val preparingFileDialog = MessageAndProgressDialog(context, UIMessage(R.string.preparing_file))
        private val loadingSettings = MessageAndProgressDialog(context, UIMessage(R.string.loading_cloud_backup_settings))

        private val autoBackupSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                cloudBackupUIObserver?.onFrequencyChanged(p2)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        override fun attachView(model: CloudBackupModel, cloudBackupUIObserver1: CloudBackupUIObserver) {
            this.cloudBackupUIObserver = cloudBackupUIObserver1

            accountEmail.text = model.activeAccountEmail
            enableCloudBackup(cloudBackupSwitch.isChecked)
            setListeners()
        }

        override fun updateCloudBackupData(model: CloudBackupModel) {
            updateFileInfo(model.lastBackupSize, model.lastTimeBackup)
            setCloudBackupSwitchState(model.hasCloudBackup)
            setWifiOnlySwitchState(model.wifiOnly)
            accountEmail.text = model.activeAccountEmail
            enableCloudBackup(cloudBackupSwitch.isChecked)
            autoBackupSpinner.onItemSelectedListener = null
            setUpSpinnerItemsAndListeners(model)
        }

        override fun showEncryptBackupDialog(observer: CloudBackupUIObserver?) {
            encryptPassphraseDialog.showDialog(observer)
        }

        override fun enableSaveButtonOnDialog() {
            encryptPassphraseDialog.enableSaveButton()
        }

        override fun disableSaveButtonOnDialog() {
            encryptPassphraseDialog.disableSaveButton()
        }

        override fun showUploadProgressBar(show: Boolean) {
            uploadProgressLayout.visibility = if(show) View.VISIBLE else View.GONE
        }

        override fun setUploadProgress(progress: Int) {
            uploadProgressBar.progress = progress
            uploadProgressMessage.text =
                    context.getLocalizedUIMessage(UIMessage(R.string.cloud_progress_upload_message,
                            arrayOf(progress)))
        }

        override fun updateFileInfo(fileSize: Long, date: Date?) {
            if(fileSize > 0L && date != null) {
                backupFileInfo.visibility = View.VISIBLE
                backupSize.text = Utility.humanReadableByteCount(fileSize, true)
                backupLastModified.text = DateAndTimeUtils.getTimeForBackup(date.time)
            }
        }

        override fun showProgressDialog() {
            backUpNow.isEnabled = false
            progressDialog.setCancelable(false)
            progressDialog.setMessage(context.getLocalizedUIMessage(UIMessage(R.string.cloud_backup_create_file_progress_message, arrayOf(20))))
            progressDialog.show()
        }

        override fun setProgressDialog(progress: Int) {
            progressDialog.setMessage(context.getLocalizedUIMessage(UIMessage(R.string.cloud_backup_create_file_progress_message, arrayOf(progress))))
        }

        override fun hideProgressDialog() {
            progressDialog.dismiss()
        }

        override fun setCloudBackupSwitchState(isChecked: Boolean) {
            cloudBackupSwitch.setOnCheckedChangeListener { _,_ -> }
            cloudBackupSwitch.isChecked = isChecked
            enableCloudBackup(isChecked)
            cloudBackupSwitch.setOnCheckedChangeListener { _, checked ->
                enableCloudBackup(checked)
                cloudBackupUIObserver?.onCloudBackupActivated(checked)
            }
        }

        override fun backingUpNow(isBackingUp: Boolean) {
            backUpNow.isEnabled = !isBackingUp
            backUpNowText.isEnabled = !isBackingUp
            if(isBackingUp) {
                progressBarBackupNow.visibility = View.VISIBLE
                backUpNowText.text = context.getLocalizedUIMessage(UIMessage(R.string.cloud_backup_backing_up))
            }else{
                progressBarBackupNow.visibility = View.INVISIBLE
                backUpNowText.text = context.getLocalizedUIMessage(UIMessage(R.string.cloud_backup_now))
            }
        }

        override fun dismissAccountSuspendedDialog() {
            accountSuspended.dismissDialog()
        }

        override fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType) {
            accountSuspended.showDialog(observer, email, dialogType)
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

        override fun scheduleCloudBackupJob(period: Int, accountId: Long, useWifiOnly: Boolean) {
            val cloudBackupJobService = CloudBackupJobService()
            cloudBackupJobService.schedule(context, AccountUtils.getFrequencyPeriod(period), accountId, useWifiOnly)
        }

        override fun removeFromScheduleCloudBackupJob(accountId: Long) {
            val cloudBackupJobService = CloudBackupJobService()
            cloudBackupJobService.cancel(context, accountId)
        }

        override fun checkCloudBackupIcon() {
            cloudBackupIcon.setImageResource(R.drawable.small_check_cloud)
        }

        override fun showPreparingFileDialog() {
            preparingFileDialog.showDialog()
        }

        override fun dismissPreparingFileDialog() {
            preparingFileDialog.dismiss()
        }

        override fun showLoadingSettingsDialog() {
            loadingSettings.showDialog()
        }

        override fun dismissLoadingSettingsDialog() {
            loadingSettings.dismiss()
        }

        private fun setWifiOnlySwitchState(isChecked: Boolean) {
            backupOverSwitch.setOnCheckedChangeListener { _,_ -> }
            backupOverSwitch.isChecked = isChecked
            backupOverSwitch.setOnCheckedChangeListener { _, checked ->
                cloudBackupUIObserver?.onWifiOnlySwitched(checked)
            }
        }

        private fun setListeners(){
            backButton.setOnClickListener {
                cloudBackupUIObserver?.onBackButtonPressed()
            }
            changeGoogleAccount.setOnClickListener {
                cloudBackupUIObserver?.onChangeGoogleDriveAccount()
            }
            backUpNow.setOnClickListener {
                cloudBackupUIObserver?.backUpNowPressed()
            }
            exportBackupFile.setOnClickListener {
                cloudBackupUIObserver?.exportBackupPressed()
            }
            restoreBackupFile.setOnClickListener {
                cloudBackupUIObserver?.restoreBackupPressed()
            }
        }

        private fun setUpSpinnerItemsAndListeners(model: CloudBackupModel){
            ArrayAdapter.createFromResource(
                    context,
                    R.array.auto_backup_options,
                    android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                autoBackupSpinner.adapter = adapter
            }

            autoBackupSpinner.setSelection(model.autoBackupFrequency)
            autoBackupSpinner.onItemSelectedListener = autoBackupSelectedListener
        }

        private fun enableCloudBackup(enable: Boolean){
            autoBackupSpinner.isEnabled = enable
            backUpNow.isEnabled = enable
            changeGoogleAccount.isEnabled = enable
            backupOverSwitch.isEnabled = enable
            backupOverText.isEnabled = enable
            changeGoogleAccountText.isEnabled = enable
            backUpNowText.isEnabled = enable
            autoBackupText.isEnabled = enable
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