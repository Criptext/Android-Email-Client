package com.criptext.mail

import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.annotation.VisibleForTesting
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.push.data.IntentExtrasData
import com.criptext.mail.push.services.LinkDeviceActionService
import com.criptext.mail.push.services.NewMailActionService
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.composer.ComposerModel
import com.criptext.mail.scenes.emaildetail.EmailDetailSceneModel
import com.criptext.mail.scenes.linking.LinkingModel
import com.criptext.mail.scenes.mailbox.MailboxActivity
import com.criptext.mail.scenes.mailbox.MailboxSceneModel
import com.criptext.mail.scenes.params.*
import com.criptext.mail.scenes.search.SearchSceneModel
import com.criptext.mail.scenes.settings.SettingsActivity
import com.criptext.mail.scenes.settings.SettingsModel
import com.criptext.mail.scenes.settings.changepassword.ChangePasswordActivity
import com.criptext.mail.scenes.settings.changepassword.ChangePasswordModel
import com.criptext.mail.scenes.settings.privacyandsecurity.PrivacyAndSecurityModel
import com.criptext.mail.scenes.settings.privacyandsecurity.pinscreen.LockScreenActivity
import com.criptext.mail.scenes.settings.recovery_email.RecoveryEmailModel
import com.criptext.mail.scenes.settings.signature.SignatureModel
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
import com.criptext.mail.utils.ui.ActivityMenu
import com.criptext.mail.utils.ui.StartGuideTapped
import com.github.omadahealth.lollipin.lib.PinCompatActivity
import com.github.omadahealth.lollipin.lib.managers.AppLock
import com.github.omadahealth.lollipin.lib.managers.LockManager
import com.google.firebase.analytics.FirebaseAnalytics
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.io.File
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

    private fun dismissAllNotifications() {
        val notificationManager = this.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        storage.getInt(KeyValueStorage.StringKey.NewMailNotificationCount, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)

        val toolbarId = this.toolbarId
        if(toolbarId != null) {
            val toolbar = findViewById<Toolbar>(toolbarId)
            setSupportActionBar(toolbar)
        }

        val cacheModel = getCachedModel()
        if(cacheModel == null){
            restartApplication()
            return
        }else{
            model = cacheModel
        }
        controller = initController(model)
    }

    override fun onStart() {
        super.onStart()
        dismissAllNotifications()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        if (controller.onStart(activityMessage))
            activityMessage = null
    }

    override fun onStop() {
        handler.removeCallbacksAndMessages(null)
        mFirebaseAnalytics = null
        super.onStop()
        controller.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        controller.onOptionsItemSelected(itemId)
        return true
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
            is SignUpParams -> SignUpSceneModel()
            is SignInParams -> SignInSceneModel()
            is MailboxParams -> MailboxSceneModel(params.showWelcome)
            is  EmailDetailParams -> EmailDetailSceneModel(params.threadId,
                    params.currentLabel, params.threadPreview, params.doReply)
            is ComposerParams -> ComposerModel(params.type)
            is SettingsParams -> SettingsModel()
            is SignatureParams -> SignatureModel(params.recipientId)
            is RecoveryEmailParams -> RecoveryEmailModel(params.isConfirmed, params.recoveryEmail)
            is ChangePasswordParams -> ChangePasswordModel()
            is LinkingParams -> LinkingModel(params.email, params.deviceId, params.randomId, params.deviceType)
            is PrivacyAndSecurityParams -> PrivacyAndSecurityModel(params.hasReadReceipts)
            else -> throw IllegalArgumentException("Don't know how to create a model from ${params.javaClass}")
        }
    }

    override fun refreshToolbarItems() {
        this.invalidateOptionsMenu()
    }

    override fun getLocalizedString(message: UIMessage): String {
        return getString(message.resId, *message.args)
    }

    override fun goToScene(params: SceneParams, keep: Boolean, deletePastIntents: Boolean) {
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
        BaseActivity.activityMessage = activityMessage
        finish()
        if(forceAnimation) {
            overridePendingTransition(0, R.anim.slide_out_right)
        }
        goToScene(params, false, deletePastIntents)
    }

    override fun getIntentExtras(): IntentExtrasData? {
        if(intent.extras != null && !intent.extras.isEmpty) {
            PinLockUtils.disablePinLock()
            when(intent.action){
                Intent.ACTION_MAIN ->    {
                    val threadId = intent.extras.get(MessagingInstance.THREAD_ID).toString()
                    if(intent.extras != null) {
                        for (key in intent.extras.keySet()){
                            intent.removeExtra(key)
                        }
                    }
                    return IntentExtrasData.IntentExtrasDataMail(intent.action, threadId)
                }
                LinkDeviceActionService.APPROVE ->    {
                    val uuid = intent.extras.get("randomId").toString()
                    val deviceType = DeviceUtils.getDeviceType(intent.extras.getInt("deviceType"))
                    if(intent.extras != null) {
                        for (key in intent.extras.keySet()){
                            intent.removeExtra(key)
                        }
                    }
                    return IntentExtrasData.IntentExtrasDataDevice(intent.action, uuid, deviceType)
                }
                NewMailActionService.REPLY -> {
                    val threadId = intent.extras.get(MessagingInstance.THREAD_ID).toString()
                    val metadataKey = intent.extras.getLong("metadataKey")
                    if(intent.extras != null) {
                        for (key in intent.extras.keySet()){
                            intent.removeExtra(key)
                        }
                    }
                    return IntentExtrasData.IntentExtrasReply(intent.action, threadId, metadataKey)
                }
                Intent.ACTION_VIEW -> {
                    val mailTo = intent.data
                    if(mailTo.toString().contains("mailto:"))
                        return IntentExtrasData.IntentExtrasMailTo(intent.action, mailTo.toString().removePrefix("mailto:"))
                }
                Intent.ACTION_SEND,
                Intent.ACTION_SEND_MULTIPLE -> {
                    val data = intent
                    if(data != null) {
                        val clipData = data.clipData
                        if(clipData == null) {
                            data.data?.also { uri ->
                                val attachment = FileUtils.getPathAndSizeFromUri(uri, contentResolver, this)
                                if (attachment != null)
                                    return IntentExtrasData.IntentExtrasSend(intent.action, listOf(attachment))
                            }
                        }else{
                            if(clipData.itemCount < EmailUtils.ATTACHMENT_LIMIT) {
                                val attachmentList = mutableListOf<Pair<String, Long>>()
                                for (i in 0 until clipData.itemCount) {
                                    clipData.getItemAt(i).also { item ->
                                        if (item.uri != null) {
                                            val attachment = FileUtils.getPathAndSizeFromUri(item.uri,
                                                    contentResolver, this)
                                            if (attachment != null)
                                                attachmentList.add(attachment)
                                        }
                                    }
                                }
                                if (attachmentList.isNotEmpty())
                                    return IntentExtrasData.IntentExtrasSend(intent.action, attachmentList)
                            }else{
                                return IntentExtrasData.IntentErrorMessage(Intent.ACTION_APP_ERROR, UIMessage(R.string.too_many_files))
                            }
                        }
                    }
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
            is ExternalActivityParams.FilePicker -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    putExtra("remaining", params.remaining)
                    type = "*/*"
                }
                startActivityForResult(intent, FilePickerConst.REQUEST_CODE_DOC)
            }
            is ExternalActivityParams.ImagePicker -> {
                FilePickerBuilder.getInstance()
                        .enableVideoPicker(true)
                        .setMaxCount(params.remaining)
                        .setActivityTheme(R.style.AppTheme)
                        .enableCameraSupport(false)
                        .pickPhoto(this)
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
                val intent = Intent(this, LockScreenActivity::class.java)
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK)
                startActivityForResult(intent, ExternalActivityParams.PIN_REQUEST_CODE)
            }
            is ExternalActivityParams.InviteFriend -> {
                val share = Intent(android.content.Intent.ACTION_SEND)
                share.type = "text/plain"
                share.putExtra(Intent.EXTRA_SUBJECT, "Invite a Friend")
                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.invite_text))
                startActivity(Intent.createChooser(share, getString(R.string.invite_title)))

                val bundle = Bundle()
                bundle.putString("app_source", "Unknown")
                mFirebaseAnalytics?.logEvent("invite_friend", bundle)
            }
        }
    }

    override fun getContentResolver(): ContentResolver? {
        return this.applicationContext.contentResolver
    }

    override fun getHandler(): Handler? {
        return handler
    }

    override fun checkPermissions(requestCode: Int, permission: String): Boolean =
        if (PermissionUtilsCompat.checkPermission(applicationContext, permission)) {
            true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            false
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
            cachedModels[SignUpActivity::class.java] = SignUpSceneModel()
            cachedModels[SettingsActivity::class.java] = SettingsModel()
            cachedModels[ChangePasswordActivity::class.java] = ChangePasswordModel()
        }

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun setCachedModel(clazz: Class<*>, model: Any) {
            cachedModels[clazz] = model
        }
    }

    enum class RequestCode {
        writeAccess, readAccess
    }
}
