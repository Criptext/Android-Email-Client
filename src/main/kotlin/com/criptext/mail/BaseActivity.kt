package com.criptext.mail

import android.app.NotificationManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.push.data.IntentExtrasData
import com.criptext.mail.push.services.LinkDeviceActionService
import com.criptext.mail.push.services.NewMailActionService
import com.criptext.mail.push.services.SyncDeviceActionService
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.scenes.WebViewActivity
import com.criptext.mail.scenes.composer.ComposerModel
import com.criptext.mail.scenes.composer.data.ComposerAttachment
import com.criptext.mail.scenes.composer.data.ComposerType
import com.criptext.mail.scenes.emaildetail.EmailDetailSceneModel
import com.criptext.mail.scenes.linking.LinkingModel
import com.criptext.mail.scenes.mailbox.MailboxActivity
import com.criptext.mail.scenes.mailbox.MailboxSceneModel
import com.criptext.mail.scenes.mailbox.emailsource.EmailSourceModel
import com.criptext.mail.scenes.params.*
import com.criptext.mail.scenes.restorebackup.RestoreBackupModel
import com.criptext.mail.scenes.search.SearchSceneModel
import com.criptext.mail.scenes.settings.SettingsActivity
import com.criptext.mail.scenes.settings.SettingsModel
import com.criptext.mail.scenes.settings.changepassword.ChangePasswordActivity
import com.criptext.mail.scenes.settings.changepassword.ChangePasswordModel
import com.criptext.mail.scenes.settings.cloudbackup.CloudBackupModel
import com.criptext.mail.scenes.settings.devices.DevicesModel
import com.criptext.mail.scenes.settings.labels.LabelsModel
import com.criptext.mail.scenes.settings.pinlock.PinLockModel
import com.criptext.mail.scenes.settings.pinlock.pinscreen.LockScreenActivity
import com.criptext.mail.scenes.settings.privacy.PrivacyModel
import com.criptext.mail.scenes.settings.profile.ProfileModel
import com.criptext.mail.scenes.settings.profile.data.ProfileUserData
import com.criptext.mail.scenes.settings.recovery_email.RecoveryEmailModel
import com.criptext.mail.scenes.settings.replyto.ReplyToModel
import com.criptext.mail.scenes.settings.signature.SignatureModel
import com.criptext.mail.scenes.settings.syncing.SyncingModel
import com.criptext.mail.scenes.signin.SignInActivity
import com.criptext.mail.scenes.signin.SignInSceneModel
import com.criptext.mail.scenes.signup.SignUpActivity
import com.criptext.mail.scenes.signup.SignUpSceneModel
import com.criptext.mail.services.MessagingInstance
import com.criptext.mail.splash.SplashActivity
import com.criptext.mail.utils.*
import com.criptext.mail.utils.compat.PermissionUtilsCompat
import com.criptext.mail.utils.dialog.SingletonProgressDialog
import com.criptext.mail.utils.file.FileUtils
import com.criptext.mail.utils.file.IntentUtils
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.mailtemplates.CriptextMailTemplate
import com.criptext.mail.utils.mailtemplates.FWMailTemplate
import com.criptext.mail.utils.mailtemplates.REMailTemplate
import com.criptext.mail.utils.mailtemplates.SupportMailTemplate
import com.criptext.mail.utils.ui.ActivityMenu
import com.criptext.mail.utils.ui.StartGuideTapped
import com.criptext.mail.validation.AccountDataValidator
import com.criptext.mail.validation.FormData
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.validation.TextInput
import com.github.omadahealth.lollipin.lib.PinCompatActivity
import com.github.omadahealth.lollipin.lib.managers.AppLock
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.firebase.analytics.FirebaseAnalytics
import droidninja.filepicker.FilePickerConst
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.io.File
import java.lang.Exception
import java.util.*


/**
 * Base class for all of our activities. If you extend this class you don't need to implement
 * `onCreate`, `onStart` or `onStop`. This class will create your controller with `initController`
 * and then forward `onStart` and `onStop` events to it.
 * Created by gabriel on 2/14/18.
 */

abstract class BaseActivity: PinCompatActivity(), IHostActivity {

    /**
     * Resource Id of the layout to be used by this activity. This value will be used on `onCreate`
     * to inflate the activity's views. Your layout must contain a toolbar somewhere.
     */
    abstract val layoutId: Int

    private val progressDialog: SingletonProgressDialog by lazy { SingletonProgressDialog(this) }
    private val storage: KeyValueStorage by lazy { KeyValueStorage.SharedPrefs(this) }

    /**
     * Resource Id of your activity's toolbar. After the layout is inflated, BaseActivity will call
     * `findViewById` with this value to get the toolbar and set it as action bar. If no toolbar
     * is found your activity will crash.
     */
    abstract val toolbarId: Int?

    lateinit var controller: SceneController
    lateinit var model: Any
    var mFirebaseAnalytics: FirebaseAnalytics? = null

    private val handler = Handler()

    /**
     * Called during `onCreate` to create a controller for this activity given the current active
     * model which is passed as parameter. `BaseActivity` will call this once and keep a private
     * reference to the controller in order to forward events like `onStart` and `onStop`. There
     * is no need for your activity to keep another reference to the controller.
     * @param receivedModel The model that your controller should use. You should coerce this value
     * into the type that your controller expects.
     */
    abstract fun initController(receivedModel: Any): SceneController
    protected val photoUtil = PhotoUtil.Default()

    private fun getCachedModel(): Any? {
        return cachedModels[javaClass]
    }

    private fun getSavedInstanceModel(savedInstanceState: Bundle?): SceneModel? {
        if(savedInstanceState == null) return null
        val activeAccount = ActiveAccount.loadFromStorage(this) ?: return null
        return if(savedInstanceState.getString("type") != null){
            when(savedInstanceState.getString("type")){
                EMAIL_DETAIL_MODEL -> {
                    EmailDetailSceneModel(
                            threadId = savedInstanceState.getString("threadId")!!,
                            currentLabel = Label.fromJSON(savedInstanceState.getString("currentLabel")!!, activeAccount.id),
                            threadPreview = EmailPreview.emailPreviewFromJSON(savedInstanceState.getString("threadPreview")!!),
                            doReply = savedInstanceState.getBoolean("doReply")
                    )
                }
                COMPOSER_MODEL -> {
                    val composerModel = ComposerModel(
                            type = ComposerType.fromJSON(savedInstanceState.getString("composerType")!!, this),
                            currentLabel = Label.fromJSON(savedInstanceState.getString("currentLabel")!!, activeAccount.id)
                    )
                    composerModel.subject = savedInstanceState.getString("subject")!!
                    composerModel.body = savedInstanceState.getString("body")!!
                    composerModel.to = LinkedList(Contact.fromJSONArray(savedInstanceState.getString("to")!!))
                    composerModel.cc = LinkedList(Contact.fromJSONArray(savedInstanceState.getString("cc")!!))
                    composerModel.bcc = LinkedList(Contact.fromJSONArray(savedInstanceState.getString("bcc")!!))
                    composerModel.attachments = ArrayList(ComposerAttachment.fromJSONArray(savedInstanceState.getString("attachments")!!))
                    composerModel.fileKey = savedInstanceState.getString("fileKey")
                    composerModel.initialized = true
                    composerModel

                }
                PROFILE_MODEL -> {
                    ProfileModel(savedInstanceState.getBoolean("comesFromMailbox"))
                }
                RECOVERY_EMAIL_MODEL -> {
                    val userData = ProfileUserData(
                            name = savedInstanceState.getString("name")!!,
                            email = savedInstanceState.getString("email")!!,
                            isEmailConfirmed = savedInstanceState.getBoolean("isEmailConfirmed"),
                            replyToEmail = savedInstanceState.getString("replyToEmail"),
                            isLastDeviceWith2FA = savedInstanceState.getBoolean("isLastDeviceWith2FA"),
                            recoveryEmail = savedInstanceState.getString("recoveryEmail")!!
                    )
                    RecoveryEmailModel(userData)
                }
                REPLY_TO_MODEL -> {
                    val userData = ProfileUserData(
                            name = savedInstanceState.getString("name")!!,
                            email = savedInstanceState.getString("email")!!,
                            isEmailConfirmed = savedInstanceState.getBoolean("isEmailConfirmed"),
                            replyToEmail = savedInstanceState.getString("replyToEmail"),
                            isLastDeviceWith2FA = savedInstanceState.getBoolean("isLastDeviceWith2FA"),
                            recoveryEmail = savedInstanceState.getString("recoveryEmail")!!
                    )
                    ReplyToModel(userData)
                }
                SIGNATURE_MODEL -> {
                    SignatureModel(
                            recipientId = savedInstanceState.getString("recipientId")!!,
                            domain = savedInstanceState.getString("domain")!!
                    )
                }
                SIGN_UP_MODEL -> {
                    val signUpModel = SignUpSceneModel(
                        isMultiple = savedInstanceState.getBoolean("isMultiple")
                    )
                    val username = savedInstanceState.getString("username")!!
                    signUpModel.username.copy(value = username,
                            state = FormInputState.Unknown())
                    val fullName = savedInstanceState.getString("fullName")!!
                    signUpModel.fullName.copy(value = fullName,
                            state = FormInputState.Unknown())
                    signUpModel.password = savedInstanceState.getString("password")!!
                    signUpModel.confirmPassword = savedInstanceState.getString("confirmPassword")!!
                    signUpModel.passwordState = FormInputState.Unknown()
                    val recoveryEmail = savedInstanceState.getString("recoveryEmail")!!
                    signUpModel.recoveryEmail.copy(value = recoveryEmail,
                            state = FormInputState.Unknown())
                    signUpModel.checkTermsAndConditions = savedInstanceState.getBoolean("checkTermsAndConditions")
                    signUpModel
                }
                else -> null
            }
        }else
            null
    }

    private fun dismissAllNotifications() {
        val notificationManager = this.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(CriptextNotification.INBOX_ID)
        notificationManager.cancel(CriptextNotification.OPEN_ID)
        notificationManager.cancel(CriptextNotification.ERROR_ID)
        notificationManager.cancel(CriptextNotification.LINK_DEVICE_ID)
        storage.getInt(KeyValueStorage.StringKey.NewMailNotificationCount, 0)
        storage.getInt(KeyValueStorage.StringKey.SyncNotificationCount, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (storage.getBool(KeyValueStorage.StringKey.HasDarkTheme, false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            setTheme(R.style.DarkAppTheme)
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
        setContentView(layoutId)

        val toolbarId = this.toolbarId
        if(toolbarId != null) {
            val toolbar = findViewById<Toolbar>(toolbarId)
            setSupportActionBar(toolbar)
        }

        val savedInstanceModel = getSavedInstanceModel(savedInstanceState)
        val cacheModel = if(savedInstanceState == null || savedInstanceModel == null) getCachedModel()
        else savedInstanceModel

        if(cacheModel == null){
            restartApplication()
            return
        }else{
            model = cacheModel
        }
        try {
            controller = initController(model)
        } catch (ex: Exception) {
            restartApplication()
        }
    }

    override fun onStart() {
        super.onStart()
        dismissAllNotifications()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        if (controller.onStart(activityMessage))
            activityMessage = null
    }

    override fun onResume() {
        super.onResume()
        if (controller.onResume(activityMessage))
            activityMessage = null
    }

    override fun onPause() {
        super.onPause()
        controller.onPause()
    }

    override fun onStop() {
        handler.removeCallbacksAndMessages(null)
        mFirebaseAnalytics = null
        super.onStop()
        controller.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if(item.isCheckable){
            item.isChecked = true
        }
        controller.onOptionsItemSelected(itemId)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val currentModel = model
        when(currentModel) {
            is EmailDetailSceneModel -> {
                outState.putString("type", EMAIL_DETAIL_MODEL)
                outState.putString("threadId", currentModel.threadId)
                outState.putString("currentLabel", Label.toJSON(currentModel.currentLabel).toString())
                outState.putString("threadPreview", EmailPreview.emailPreviewToJSON(currentModel.threadPreview))
                outState.putBoolean("doReply", currentModel.doReply)
            }
            is ComposerModel -> {
                outState.putString("type", COMPOSER_MODEL)
                outState.putString("composerType", ComposerType.toJSON(currentModel.type))
                outState.putString("currentLabel", Label.toJSON(currentModel.currentLabel).toString())
                outState.putString("subject", currentModel.subject)
                outState.putString("body", currentModel.body)
                outState.putString("to", Contact.toJSON(currentModel.to).toString())
                outState.putString("cc", Contact.toJSON(currentModel.cc).toString())
                outState.putString("bcc", Contact.toJSON(currentModel.bcc).toString())
                outState.putString("attachments", ComposerAttachment.toJSON(currentModel.attachments).toString())
                outState.putString("fileKey", currentModel.fileKey)
            }
            is PrivacyModel -> {
                outState.putString("type", PRIVACY_MODEL)
                outState.putBoolean("readReceipts", currentModel.readReceipts)
                outState.putBoolean("twoFA", currentModel.twoFA)
                outState.putBoolean("isEmailConfirmed", currentModel.isEmailConfirmed)
            }
            is ProfileModel -> {
                outState.putString("type", PROFILE_MODEL)
                outState.putBoolean("comesFromMailbox", currentModel.comesFromMailbox)
            }
            is RecoveryEmailModel -> {
                outState.putString("type", RECOVERY_EMAIL_MODEL)
                outState.putString("name", currentModel.userData.name)
                outState.putString("email", currentModel.userData.email)
                outState.putBoolean("isEmailConfirmed", currentModel.userData.isEmailConfirmed)
                if(currentModel.userData.replyToEmail != null)
                    outState.putString("replyToEmail", currentModel.userData.replyToEmail)
                outState.putBoolean("isLastDeviceWith2FA", currentModel.userData.isLastDeviceWith2FA)
                outState.putString("recoveryEmail", currentModel.userData.recoveryEmail)
            }
            is ReplyToModel -> {
                outState.putString("type", REPLY_TO_MODEL)
                outState.putString("name", currentModel.userData.name)
                outState.putString("email", currentModel.userData.email)
                outState.putBoolean("isEmailConfirmed", currentModel.userData.isEmailConfirmed)
                if(currentModel.userData.replyToEmail != null)
                    outState.putString("replyToEmail", currentModel.userData.replyToEmail)
                outState.putBoolean("isLastDeviceWith2FA", currentModel.userData.isLastDeviceWith2FA)
                outState.putString("recoveryEmail", currentModel.userData.recoveryEmail)
            }
            is SignatureModel -> {
                outState.putString("type", SIGNATURE_MODEL)
                outState.putString("recipientId", currentModel.recipientId)
                outState.putString("domain", currentModel.domain)
            }
            is SignUpSceneModel -> {
                outState.putString("type", SIGN_UP_MODEL)
                outState.putBoolean("isMultiple", currentModel.isMultiple)
                outState.putString("username", currentModel.username.value)
                outState.putString("fullName", currentModel.fullName.value)
                outState.putString("password", currentModel.password)
                outState.putString("confirmPassword", currentModel.confirmPassword)
                outState.putString("recoveryEmail", currentModel.recoveryEmail.value)
                outState.putBoolean("checkTermsAndConditions", currentModel.checkTermsAndConditions)
            }
        }
    }

    override fun onBackPressed() {
        val shouldCallSuper = controller.onBackPressed()
        if (shouldCallSuper) super.onBackPressed()
    }

    private fun startActivity(activityClass: Class<*>, isExitCompletely: Boolean  = false) {
        val intent = Intent(this, activityClass)
        if(isExitCompletely)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if(!this::controller.isInitialized) return false
        val activeSceneMenu = controller.menuResourceId
        if(activeSceneMenu != null) menuInflater.inflate(activeSceneMenu, menu)
        controller.onMenuChanged(ActivityMenu(menu))
        return true
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    private fun createNewSceneFromParams(params: SceneParams): Any {
        return when(params) {
            is SearchParams -> SearchSceneModel()
            is SignUpParams -> SignUpSceneModel(params.isMultiple)
            is SignInParams -> SignInSceneModel(params.isMultiple)
            is MailboxParams -> MailboxSceneModel(params.showWelcome, params.askForRestoreBackup)
            is  EmailDetailParams -> EmailDetailSceneModel(params.threadId,
                    params.currentLabel, params.threadPreview, params.doReply)
            is ComposerParams -> ComposerModel(params.type, params.currentLabel)
            is SettingsParams -> SettingsModel(params.hasChangedTheme)
            is SignatureParams -> SignatureModel(params.recipientId, params.domain)
            is RecoveryEmailParams -> RecoveryEmailModel(params.userData)
            is ChangePasswordParams -> ChangePasswordModel()
            is LinkingParams -> LinkingModel(params.activeAccount,
                    params.deviceId, params.randomId, params.deviceType)
            is PinLockParams -> PinLockModel()
            is DevicesParams -> DevicesModel()
            is LabelsParams -> LabelsModel()
            is PrivacyParams -> PrivacyModel()
            is SyncingParams -> SyncingModel(params.email, params.deviceId, params.randomId,
                    params.deviceType, params.authorizerName)
            is EmailSourceParams -> EmailSourceModel(params.emailSource)
            is ReplyToParams -> ReplyToModel(params.userData)
            is ProfileParams -> ProfileModel(params.comesFromMailbox)
            is CloudBackupParams -> CloudBackupModel()
            is RestoreBackupParams -> RestoreBackupModel(params.isLocal, params.localFile)
            else -> throw IllegalArgumentException("Don't know how to create a model from ${params.javaClass}")
        }
    }

    override fun refreshToolbarItems() {

        this.invalidateOptionsMenu()
    }

    override fun getLocalizedString(message: UIMessage): String {
        return getLocalizedUIMessage(message)
    }

    override fun goToScene(params: SceneParams, keep: Boolean, deletePastIntents: Boolean,
                           activityMessage: ActivityMessage?) {
        BaseActivity.activityMessage = activityMessage
        val newSceneModel = createNewSceneFromParams(params)
        cachedModels[params.activityClass] = newSceneModel
        startActivity(params.activityClass, deletePastIntents)

        if (! keep) finish()
    }

    override fun showStartGuideView(view: View, title: Int, dimension: Int) {
        val showStartGuideEmail = StartGuideTapped(this)
        showStartGuideEmail.showViewTapped(
                view,
                this,
                title,
                dimension)
    }

    override fun postDelay(runnable: Runnable, delayMilliseconds: Long) {
        handler.postDelayed(runnable, delayMilliseconds)
    }

    override fun exitToScene(params: SceneParams, activityMessage: ActivityMessage?,
                             forceAnimation: Boolean, deletePastIntents: Boolean) {
        finish()
        if(forceAnimation) {
            overridePendingTransition(0, R.anim.slide_out_right)
        }
        goToScene(params, false, deletePastIntents, activityMessage)
    }

    override fun getIntentExtras(): IntentExtrasData? {
        val extras = intent.extras
        if(extras != null && !extras.isEmpty) {
            val action = intent.action ?: return null
            when(action){
                Intent.ACTION_MAIN ->    {
                    val threadId = extras.getString(MessagingInstance.THREAD_ID) ?: return null
                    val account = extras.getString("account") ?: return null
                    val domain = extras.getString("domain") ?: return null
                    for (key in extras.keySet()){
                        intent.removeExtra(key)
                    }
                    return IntentExtrasData.IntentExtrasDataMail(action, threadId, account, domain)
                }
                LinkDeviceActionService.APPROVE ->    {
                    val uuid = extras.getString("randomId") ?: return null
                    val deviceType = DeviceUtils.getDeviceType(extras.getInt("deviceType"))
                    val version = extras.getInt("version")
                    val account = extras.getString("account") ?: return null
                    val domain = extras.getString("domain") ?: return null
                    for (key in extras.keySet()){
                        intent.removeExtra(key)
                    }
                    return IntentExtrasData.IntentExtrasDataDevice(action, uuid, deviceType, version, account, domain)
                }
                SyncDeviceActionService.APPROVE ->    {
                    val uuid = extras.getString("randomId") ?: return null
                    val deviceType = DeviceUtils.getDeviceType(extras.getInt("deviceType"))
                    val version = extras.getInt("version")
                    val deviceId = extras.getInt("deviceId")
                    val deviceName = extras.getString("deviceName") ?: return null
                    val account = extras.getString("account") ?: return null
                    val domain = extras.getString("domain") ?: return null
                    for (key in extras.keySet()){
                        intent.removeExtra(key)
                    }
                    return IntentExtrasData.IntentExtrasSyncDevice(action, uuid, deviceId, deviceName, deviceType, version, account, domain)
                }
                NewMailActionService.REPLY -> {
                    val threadId = extras.getString(MessagingInstance.THREAD_ID) ?: return null
                    val metadataKey = extras.getLong("metadataKey")
                    val account = extras.getString("account") ?: return null
                    val domain = extras.getString("domain") ?: return null
                    for (key in extras.keySet()){
                        intent.removeExtra(key)
                    }
                    return IntentExtrasData.IntentExtrasReply(action, threadId, metadataKey, account, domain)
                }
                Intent.ACTION_VIEW -> {
                    val mailTo = intent.data ?: return null
                    val activeAccount = ActiveAccount.loadFromStorage(this)!!
                    val account = extras.getString("account")?: activeAccount.recipientId
                    val domain = extras.getString("domain") ?: activeAccount.domain
                    if(mailTo.toString().contains("mailto:"))
                        return IntentExtrasData.IntentExtrasMailTo(action, mailTo.toString().removePrefix("mailto:"),
                                account, domain)
                }
                Intent.ACTION_SEND -> {
                    val data = intent
                    if (data != null) {
                        val activeAccount = ActiveAccount.loadFromStorage(this)!!
                        val account = extras.getString("account") ?: activeAccount.recipientId
                        val domain = extras.getString("domain") ?: activeAccount.domain
                        val finalData = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
                        if(finalData != null) {
                            finalData.also { uri ->
                                val attachment = FileUtils.getPathAndSizeFromUri(uri, contentResolver, this, data)
                                if (attachment != null)
                                    return IntentExtrasData.IntentExtrasSend(action, listOf(attachment), listOf(), account, domain)
                            }
                        } else if(data.type == "text/plain") {
                            val text = data.getStringExtra(Intent.EXTRA_TEXT) ?: return null
                            return if(text.contains("http")){
                                val url = "<a href=\"$text\">$text</a>"
                                IntentExtrasData.IntentExtrasSend(action, listOf(), listOf(url), account, domain)
                            } else {
                                IntentExtrasData.IntentExtrasSend(action, listOf(), listOf(text), account, domain)
                            }
                        }
                    }
                }
                Intent.ACTION_SEND_MULTIPLE -> {
                    val data = intent
                    val activeAccount = ActiveAccount.loadFromStorage(this)!!
                    val account = extras.getString("account")?: activeAccount.recipientId
                    val domain = extras.getString("domain") ?: activeAccount.domain
                    val clipData = data.clipData ?: return null
                    val attachmentList = mutableListOf<Pair<String, Long>>()
                    val urlList = mutableListOf<String>()
                    for (i in 0 until clipData.itemCount) {
                        clipData.getItemAt(i).also { item ->
                            if (item.uri != null) {
                                val attachment = FileUtils.getPathAndSizeFromUri(item.uri,
                                        contentResolver, this, data)
                                if (attachment != null)
                                    attachmentList.add(attachment)
                            }
                            if(item.text != null){
                                urlList.add("<a href=\"${item.text}\">${item.text}</a>")
                            }
                        }
                    }
                    if (attachmentList.isNotEmpty() || urlList.isNotEmpty())
                        return IntentExtrasData.IntentExtrasSend(action, attachmentList, urlList, account, domain)
                }
            }


        } else {
            val action = intent.action ?: return null
            when(action){
                Intent.ACTION_SENDTO -> {
                    val mailTo = intent.data ?: return null
                    val activeAccount = ActiveAccount.loadFromStorage(this)!!
                    if(mailTo.toString().contains("mailto:"))
                        return IntentExtrasData.IntentExtrasMailTo(action, mailTo.toString().removePrefix("mailto:"),
                                activeAccount.recipientId, activeAccount.domain)
                }
            }
        }
        return null
    }

    override fun finishScene() {
        finish()
    }

    private fun restartApplication() {
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }

    override fun showDialog(message: UIMessage) {
        progressDialog.show(getLocalizedString(message))
    }

    override fun dismissDialog() {
        progressDialog.dismiss()
    }

    override fun launchExternalActivityForResult(params: ExternalActivityParams) {
        when(params){
            is ExternalActivityParams.ProfileImagePicker -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                }
                startActivityForResult(intent, FilePickerConst.REQUEST_CODE_PHOTO)
            }
            is ExternalActivityParams.FilePicker -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    type = "*/*"
                }
                startActivityForResult(intent, FilePickerConst.REQUEST_CODE_DOC)
            }
            is ExternalActivityParams.ImagePicker -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    val mimeTypes = arrayOf("image/*", "video/*")
                    type = "*/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                }
                startActivityForResult(intent, FilePickerConst.REQUEST_CODE_PHOTO)
            }
            is ExternalActivityParams.Camera -> {
                val file = photoUtil.createImageFile()
                if(file != null) {
                    val photoIntent = IntentUtils.createIntentToOpenCamera(this, file)
                    if (photoIntent.resolveActivity(this.packageManager) != null)
                        startActivityForResult(photoIntent, PhotoUtil.REQUEST_CODE_CAMERA)
                }
            }
            is ExternalActivityParams.FilePresent -> {
                val file = File(params.filepath)
                val newIntent = IntentUtils.createIntentToOpenFileInExternalApp(this, file)
                startActivity(newIntent)
            }
            is ExternalActivityParams.PinScreen -> {
                if(params.isFirstTime) {
                    val intent = Intent(this, LockScreenActivity::class.java)
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK)
                    startActivityForResult(intent, ExternalActivityParams.PIN_REQUEST_CODE)
                }else{
                    val intent = Intent(this, LockScreenActivity::class.java)
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.CHANGE_PIN)
                    startActivityForResult(intent, ExternalActivityParams.PIN_REQUEST_CODE)
                }
            }
            is ExternalActivityParams.GoToCriptextUrl -> {
                val intent = Intent(this, WebViewActivity::class.java)
                intent.putExtra("url", "https://criptext.com/${Locale.getDefault().language}/${params.dir}")
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
            }
            is ExternalActivityParams.InviteFriend -> {
                val share = Intent(Intent.ACTION_SEND)
                share.type = "text/plain"
                share.putExtra(Intent.EXTRA_SUBJECT, "Invite a Friend")
                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.invite_text))
                startActivity(Intent.createChooser(share, getString(R.string.invite_title)))

                val bundle = Bundle()
                bundle.putString("app_source", "Unknown")
                mFirebaseAnalytics?.logEvent("invite_friend", bundle)
            }
            is ExternalActivityParams.ExportBackupFile -> {
                val file = File(params.filePath)
                if(file.exists()){
                    val fileUri: Uri? = try {
                        FileProvider.getUriForFile(
                                this,
                                "com.criptext.mail.fileProvider",
                                file)
                    } catch (e: IllegalArgumentException) {
                        Log.e("File Selector",
                                "The selected file can't be shared: ${file.name}")
                        null
                    }
                    val extension = if(params.isEncrypted) ".${UserDataWriter.FILE_ENCRYPTED_EXTENSION}"
                    else ".${UserDataWriter.FILE_UNENCRYPTED_EXTENSION}"
                    val saveIntent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    saveIntent.putExtra(Intent.EXTRA_TITLE, "Backup-${DateAndTimeUtils
                            .printDateWithServerFormat(Date(System.currentTimeMillis())).plus(extension)}")
                    saveIntent.setDataAndType(fileUri, "application/octet-stream")
                    startActivityForResult(saveIntent, ExternalActivityParams.WRITE_REQUEST_CODE)
                }
            }
            is ExternalActivityParams.SignInGoogleDrive -> {
               val signInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                        .build()
                val client = GoogleSignIn.getClient(this, signInOptions)

                // The result of the sign-in Intent is handled in onActivityResult.
                startActivityForResult(client.signInIntent, ExternalActivityParams.REQUEST_CODE_SIGN_IN)
            }
            is ExternalActivityParams.SignOutGoogleDrive -> {
                val signInOptions =
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                                .build()
                val client = GoogleSignIn.getClient(this, signInOptions)

                client.signOut()
            }
            is ExternalActivityParams.ChangeAccountGoogleDrive -> {
                val signInOptions =
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                                .build()
                val client = GoogleSignIn.getClient(this, signInOptions)

                client.signOut()

                // The result of the sign-in Intent is handled in onActivityResult.
                startActivityForResult(client.signInIntent, ExternalActivityParams.REQUEST_CODE_SIGN_IN)
            }
            is ExternalActivityParams.OpenGooglePlay -> {
                // you can also use BuildConfig.APPLICATION_ID
                val appId = getPackageName()
                val rateIntent = Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$appId"))
                var marketFound = false

                // find all applications able to handle our rateIntent
                val otherApps = getPackageManager()
                        .queryIntentActivities(rateIntent, 0)
                for (otherApp in otherApps) {
                    // look for Google Play application
                    if (otherApp.activityInfo.applicationInfo.packageName == "com.android.vending") {

                        val otherAppActivity = otherApp.activityInfo
                        val componentName = ComponentName(
                                otherAppActivity.applicationInfo.packageName,
                                otherAppActivity.name
                        )
                        // make sure it does NOT open in the stack of your activity
                        rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        // task reparenting if needed
                        rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                        // if the Google Play was already open in a search result
                        //  this make sure it still go to the app page you requested
                        rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        // this make sure only the Google Play app is allowed to
                        // intercept the intent
                        rateIntent.component = componentName
                        startActivity(rateIntent)
                        marketFound = true
                        break

                    }
                }

                // if GP not present on device, open web browser
                if (!marketFound) {
                    val webIntent = Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$appId"))
                    startActivity(webIntent)
                }
            }
        }
    }

    override fun getContentResolver(): ContentResolver? {
        return this.applicationContext.contentResolver
    }

    override fun getHandler(): Handler? {
        return handler
    }

    override fun setAppTheme(themeResource: Int) {
        setTheme(themeResource)
    }

    override fun contextMenuRegister(view: View) {
        registerForContextMenu(view)
    }

    override fun checkPermissions(requestCode: Int, permission: String): Boolean =
        if (PermissionUtilsCompat.checkPermission(applicationContext, permission)) {
            true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            false
        }

    override fun getMailTemplate(type: CriptextMailTemplate.TemplateType): CriptextMailTemplate {
        return when (type) {
            CriptextMailTemplate.TemplateType.SUPPORT -> SupportMailTemplate(this)
            CriptextMailTemplate.TemplateType.FW -> FWMailTemplate(this)
            CriptextMailTemplate.TemplateType.RE -> REMailTemplate(this)
        }
    }

    protected fun setActivityMessage(message: ActivityMessage?) {
        activityMessage = message
    }

    companion object {
        private val cachedModels = HashMap<Class<*>, Any>()
        private var activityMessage: ActivityMessage? = null

        init {
            // set initial state
            cachedModels[MailboxActivity::class.java] = MailboxSceneModel()
            cachedModels[SignInActivity::class.java] = SignInSceneModel()
            cachedModels[SettingsActivity::class.java] = SettingsModel()
            cachedModels[ChangePasswordActivity::class.java] = ChangePasswordModel()
        }

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun setCachedModel(clazz: Class<*>, model: Any) {
            cachedModels[clazz] = model
        }

        private const val EMAIL_DETAIL_MODEL = "EmailDetailModel"
        private const val COMPOSER_MODEL = "ComposerModel"
        private const val PRIVACY_MODEL = "PrivacyModel"
        private const val PROFILE_MODEL = "ProfileModel"
        private const val RECOVERY_EMAIL_MODEL = "RecoveryEmailModel"
        private const val REPLY_TO_MODEL = "ReplyToModel"
        private const val SIGNATURE_MODEL = "SignatureModel"
        private const val SIGN_UP_MODEL = "SignUpModel"
    }

    enum class RequestCode {
        writeAccess, readAccess
    }
}
