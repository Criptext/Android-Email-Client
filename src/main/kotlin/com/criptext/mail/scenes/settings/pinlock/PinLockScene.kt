package com.criptext.mail.scenes.settings.pinlock

import android.view.View
import android.widget.*
import com.criptext.mail.R
import com.criptext.mail.api.models.UntrustedDeviceInfo
import com.criptext.mail.scenes.linking.LinkingActivity
import com.criptext.mail.scenes.settings.pinlock.pinscreen.LockScreenActivity
import com.criptext.mail.scenes.signin.SignInActivity
import com.criptext.mail.scenes.signup.SignUpActivity
import com.criptext.mail.splash.SplashActivity
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.ConfirmPasswordDialog
import com.criptext.mail.utils.ui.ForgotPasswordDialog
import com.criptext.mail.utils.ui.LinkNewDeviceAlertDialog
import com.criptext.mail.utils.uiobserver.UIObserver
import com.github.omadahealth.lollipin.lib.managers.LockManager


interface PinLockScene{

    fun attachView(uiObserver: PinLockUIObserver,
                   keyboardManager: KeyboardManager, model: PinLockModel)
    fun showMessage(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: UntrustedDeviceInfo)
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun dismissConfirmPasswordDialog()
    fun setConfirmPasswordError(message: UIMessage)
    fun showForgotPasswordDialog(email: String)
    fun setPinLockStatus(isEnabled: Boolean)
    fun togglePinOptions(isEnabled: Boolean)

    class Default(val view: View): PinLockScene{
        private lateinit var pinLockUIObserver: PinLockUIObserver

        private val context = view.context

        private val pinEnableSwitch: Switch by lazy {
            view.findViewById<Switch>(R.id.pin_lock_switch)
        }

        private val changePinButton: View by lazy {
            view.findViewById<View>(R.id.change_pin)
        }

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val autoText: View by lazy {
            view.findViewById<View>(R.id.pin_auto_option_text)
        }

        private val changeText: View by lazy {
            view.findViewById<View>(R.id.change_pin_option_text)
        }

        private val autoLockSpinner: Spinner by lazy {
           view.findViewById(R.id.auto_lock_spinner) as Spinner
        }

        private val confirmPasswordDialog = ConfirmPasswordDialog(context)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)

        private val selectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                pinLockUIObserver.onAutoTimeSelected(p2)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        override fun attachView(uiObserver: PinLockUIObserver, keyboardManager: KeyboardManager,
                                model: PinLockModel) {

            pinLockUIObserver = uiObserver

            backButton.setOnClickListener {
                pinLockUIObserver.onBackButtonPressed()
            }

            pinEnableSwitch.setOnCheckedChangeListener {_, isChecked ->
                if(isChecked)
                    pinActivate()
                pinLockUIObserver.onPinSwitchChanged(isChecked)
            }

            changePinButton.setOnClickListener {
                pinLockUIObserver.onPinChangePressed()
            }

            ArrayAdapter.createFromResource(
                    context,
                    R.array.pin_lock_auto_options,
                    android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                autoLockSpinner.adapter = adapter
            }
            autoLockSpinner.setSelection(model.pinTimeOut)
            autoLockSpinner.onItemSelectedListener = selectedListener
        }

        private fun pinActivate(){
            val lockManager = LockManager.getInstance()
            lockManager.enableAppLock(context, LockScreenActivity::class.java)
            lockManager.appLock.setOnlyBackgroundTimeout(true)
            lockManager.appLock.addIgnoredActivity(SplashActivity::class.java)
            lockManager.appLock.addIgnoredActivity(SignInActivity::class.java)
            lockManager.appLock.addIgnoredActivity(SignUpActivity::class.java)
            lockManager.appLock.addIgnoredActivity(LinkingActivity::class.java)
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

        override fun setPinLockStatus(isEnabled: Boolean) {
            pinEnableSwitch.setOnCheckedChangeListener { _, _ ->  }
            pinEnableSwitch.isChecked = isEnabled
            pinEnableSwitch.setOnCheckedChangeListener {_, isChecked ->
                pinLockUIObserver.onPinSwitchChanged(isChecked)
            }
        }

        override fun togglePinOptions(isEnabled: Boolean) {
            changePinButton.isEnabled = isEnabled
            autoLockSpinner.isEnabled = isEnabled
            autoText.isEnabled = isEnabled
            changeText.isEnabled = isEnabled
        }

        override fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: UntrustedDeviceInfo) {
            if(linkAuthDialog.isShowing() != null && linkAuthDialog.isShowing() == false)
                linkAuthDialog.showLinkDeviceAuthDialog(pinLockUIObserver, untrustedDeviceInfo)
            else if(linkAuthDialog.isShowing() == null)
                linkAuthDialog.showLinkDeviceAuthDialog(pinLockUIObserver, untrustedDeviceInfo)
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