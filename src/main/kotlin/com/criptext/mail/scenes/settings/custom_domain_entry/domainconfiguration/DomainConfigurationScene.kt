package com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration

import android.content.ClipboardManager
import android.graphics.Color
import android.text.Editable
import android.text.Layout
import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatEditText
import android.view.View
import android.widget.*
import androidx.viewpager.widget.ViewPager
import com.beardedhen.androidbootstrap.BootstrapProgressBar
import com.beardedhen.androidbootstrap.api.attributes.BootstrapBrand
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration.ui.MXRecordsPagerAdapter
import com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration.ui.MXRecordsPagerModel
import com.criptext.mail.scenes.settings.recovery_email.holders.FormInputViewHolder
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.UIUtils
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.*
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.uiobserver.UIObserver
import com.criptext.mail.validation.FormInputState
import com.viewpagerindicator.CirclePageIndicator


interface DomainConfigurationScene{

    fun attachView(domainConfigurationUIObserver: DomainConfigurationUIObserver,
                   keyboardManager: KeyboardManager, model: DomainConfigurationModel)
    fun showMessage(message: UIMessage)
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun dismissConfirmPasswordDialog()
    fun setConfirmPasswordError(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
    fun dismissLinkDeviceDialog()
    fun dismissSyncDeviceDialog()
    fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType)
    fun dismissAccountSuspendedDialog()
    fun enableNextButton(enable: Boolean)
    fun hideBackButton(hide: Boolean)
    fun progressNextButton(isInProgress: Boolean)
    fun initState(model: DomainConfigurationModel)
    fun setProgress(progress: Int)
    fun setProgressError(errorCode: Int?, domainName: String)

    class Default(val view: View): DomainConfigurationScene{
        private lateinit var uiObserver: DomainConfigurationUIObserver

        private val context = view.context

        private val toolbarTitle: TextView by lazy {
            view.findViewById<TextView>(R.id.domain_configuration_toolbar_title)
        }

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val hintButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.info)
        }

        private val nextButton: Button by lazy {
            view.findViewById<Button>(R.id.next)
        }

        private val nextButtonProgress: ProgressBar by lazy {
            view.findViewById<ProgressBar>(R.id.next_progress_button)
        }

        private var pageArr: List<MXRecordsPagerModel> = listOf()

        private val confirmPassword = ConfirmPasswordDialog(context)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)
        private val syncAuthDialog = SyncDeviceAlertDialog(context)
        private val accountSuspended = AccountSuspendedDialog(context)

        override fun attachView(domainConfigurationUIObserver: DomainConfigurationUIObserver, keyboardManager: KeyboardManager,
                                model: DomainConfigurationModel) {
            this.uiObserver = domainConfigurationUIObserver

            backButton.setOnClickListener {
                this.uiObserver.onBackButtonPressed()
            }

            nextButton.setOnClickListener {
                this.uiObserver.onNextButtonPressed()
            }

            initState(model)
        }

        override fun initState(model: DomainConfigurationModel){
            when(model.state.first){
                DomainConfigurationModel.StepState.FIRST -> {
                    toolbarTitle.text = context.getLocalizedUIMessage(UIMessage(R.string.domain_configuration_step_1))
                    nextButton.text = context.getLocalizedUIMessage(UIMessage(R.string.btn_next))
                    val stepInfo = view.findViewById<LinearLayout>(R.id.step_information)
                    stepInfo.removeAllViews()
                    View.inflate(context, model.state.second, stepInfo)
                    hintButton.setOnClickListener {
                        GeneralMessageOkDialog(
                                context = view.context,
                                dialogData = DialogData.DialogMessageData(
                                        title = UIMessage(R.string.title_custom_domain),
                                        type = DialogType.Message(),
                                        message = listOf(UIMessage(R.string.custom_domain_help)),
                                        onOkPress = {}
                                )
                        ).showDialog()
                    }
                }
                DomainConfigurationModel.StepState.SECOND -> {
                    toolbarTitle.text = context.getLocalizedUIMessage(UIMessage(R.string.domain_configuration_step_2))
                    nextButton.text = context.getLocalizedUIMessage(UIMessage(R.string.btn_next))
                    val stepInfo = view.findViewById<LinearLayout>(R.id.step_information)
                    stepInfo.removeAllViews()
                    View.inflate(context, model.state.second, stepInfo)
                    hintButton.setOnClickListener {
                        GeneralMessageOkDialog(
                                context = view.context,
                                dialogData = DialogData.DialogMessageData(
                                        title = UIMessage(R.string.domain_configuration_dialog_help_step_2_title),
                                        type = DialogType.Message(),
                                        message = listOf(UIMessage(R.string.domain_configuration_dialog_help_step_2_message)),
                                        onOkPress = {}
                                )
                        ).showDialog()
                    }
                    pageArr = model.mxRecords.map { MXRecordsPagerModel(it) }

                    val viewPager: ViewPager = view.findViewById(R.id.view_pager)

                    val pageIndicator: CirclePageIndicator = view.findViewById(R.id.circle_indicator)

                    val adapter = MXRecordsPagerAdapter(context, pageArr, uiObserver)
                    viewPager.adapter = adapter

                    viewPager.addOnPageChangeListener(pageChangeListener)

                    pageIndicator.setViewPager(viewPager)
                    pageIndicator.setCurrentItem(0)
                }
                DomainConfigurationModel.StepState.THIRD -> {
                    toolbarTitle.text = context.getLocalizedUIMessage(UIMessage(R.string.domain_configuration_step_3))
                    val stepInfo = view.findViewById<LinearLayout>(R.id.step_information)
                    stepInfo.removeAllViews()
                    View.inflate(context, model.state.second, stepInfo)
                    nextButton.text = context.getLocalizedUIMessage(UIMessage(R.string.btn_back))
                }
            }
        }

        private val pageChangeListener = object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                nextButton.isEnabled = position == pageArr.lastIndex
            }

        }

        override fun setProgress(progress: Int) {
            val progressBar = view.findViewById<BootstrapProgressBar>(R.id.progressBar) ?: return
            val progressBarNumber = view.findViewById<TextView>(R.id.percentage_advanced) ?: return
            val isSuccess = progress == 100
            val anim = UIUtils.animationForProgressBar(progressBar, progress, progressBarNumber, if(isSuccess || progress == 10) 1000L else 28000L)
            anim.start()
            if(isSuccess){
                nextButton.text = context.getLocalizedUIMessage(UIMessage(R.string.btn_next))
                progressBar.isStriped = false
                progressBar.bootstrapBrand = DefaultBootstrapBrand.SUCCESS
            }
        }

        override fun setProgressError(errorCode: Int?, domainName: String) {
            val step3View = view.findViewById<RelativeLayout>(R.id.step_3) ?: return
            setProgress(10)
            val progressBar = step3View.findViewById<BootstrapProgressBar>(R.id.progressBar)
            progressBar.isStriped = false
            progressBar.bootstrapBrand = DefaultBootstrapBrand.DANGER
            step3View.findViewById<RelativeLayout>(R.id.progress_bar_percentage).visibility = View.GONE
            step3View.findViewById<TextView>(R.id.warning_text).visibility = View.GONE
            when(errorCode){
                null -> {
                    step3View.findViewById<TextView>(R.id.title).text =
                            context.getLocalizedUIMessage(UIMessage(R.string.domain_verification_mx_records_error_title))
                    val errorMessage = step3View.findViewById<TextView>(R.id.error_message)
                    errorMessage.text =
                            context.getLocalizedUIMessage(UIMessage(R.string.domain_verification_mx_records_error_message))
                    errorMessage.visibility = View.VISIBLE
                }
                400 -> {
                    step3View.findViewById<TextView>(R.id.title).text =
                            context.getLocalizedUIMessage(UIMessage(R.string.domain_verification_mx_records_not_found_title))
                    val errorMessage = step3View.findViewById<TextView>(R.id.error_message)
                    errorMessage.text =
                            context.getLocalizedUIMessage(UIMessage(R.string.domain_verification_mx_records_not_found_message, arrayOf(domainName)))
                    errorMessage.visibility = View.VISIBLE
                }
                else -> {
                    step3View.findViewById<TextView>(R.id.title).text =
                            context.getLocalizedUIMessage(UIMessage(R.string.domain_verification_mx_records_dont_match_title))
                    val errorMessage = step3View.findViewById<TextView>(R.id.error_message)
                    errorMessage.text =
                            context.getLocalizedUIMessage(UIMessage(R.string.domain_verification_mx_records_dont_match_message))
                    errorMessage.visibility = View.VISIBLE
                }
            }
        }

        override fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            if(linkAuthDialog.isShowing() != null && linkAuthDialog.isShowing() == false)
                linkAuthDialog.showLinkDeviceAuthDialog(uiObserver, untrustedDeviceInfo)
            else if(linkAuthDialog.isShowing() == null)
                linkAuthDialog.showLinkDeviceAuthDialog(uiObserver, untrustedDeviceInfo)
        }

        override fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            if(syncAuthDialog.isShowing() != null && syncAuthDialog.isShowing() == false)
                syncAuthDialog.showLinkDeviceAuthDialog(uiObserver, trustedDeviceInfo)
            else if(syncAuthDialog.isShowing() == null)
                syncAuthDialog.showLinkDeviceAuthDialog(uiObserver, trustedDeviceInfo)
        }

        override fun enableNextButton(enable: Boolean) {
            nextButton.isEnabled = enable
        }

        override fun hideBackButton(hide: Boolean) {
            if(hide) backButton.visibility = View.INVISIBLE
            else backButton.visibility = View.VISIBLE
        }

        override fun progressNextButton(isInProgress: Boolean) {
            if(isInProgress){
                nextButtonProgress.visibility = View.VISIBLE
                nextButton.visibility = View.GONE
            } else {
                nextButtonProgress.visibility = View.GONE
                nextButton.visibility = View.VISIBLE
            }
        }

        override fun dismissLinkDeviceDialog() {
            linkAuthDialog.dismiss()
        }

        override fun dismissSyncDeviceDialog() {
            syncAuthDialog.dismiss()
        }

        override fun dismissAccountSuspendedDialog() {
            accountSuspended.dismissDialog()
        }

        override fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType) {
            accountSuspended.showDialog(observer, email, dialogType)
        }

        override fun showConfirmPasswordDialog(observer: UIObserver) {
            confirmPassword.showDialog(observer)
        }

        override fun dismissConfirmPasswordDialog() {
            confirmPassword.dismissDialog()
        }

        override fun setConfirmPasswordError(message: UIMessage) {
            confirmPassword.setPasswordError(message)
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