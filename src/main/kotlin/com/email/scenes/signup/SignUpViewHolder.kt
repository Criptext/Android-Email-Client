package com.email.scenes.signin

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatEditText
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.email.R
import com.email.scenes.signup.OnRecoveryEmailWarningListener

/**
 * Created by sebas on 2/15/18.
 */

class SignUpViewHolder(val mActivity: AppCompatActivity) {
    private val res = mActivity.resources
    private val username: AppCompatEditText
    private val usernameInput: TextInputLayout
    private val usernameSuccessImage: ImageView
    private val usernameErrorImage: ImageView

    private val fullName: AppCompatEditText
    private val fullNameInput: TextInputLayout

    private val password: AppCompatEditText
    private val passwordInput: TextInputLayout
    private val passwordSuccessImage: ImageView
    private val passwordErrorImage: ImageView

    private val confirmPassword: AppCompatEditText
    private val confirmPasswordInput: TextInputLayout
    private val confirmPasswordSuccessImage: ImageView
    private val confirmPasswordErrorImage: ImageView


    private val recoveryEmail: AppCompatEditText
    private val recoveryEmailInput: TextInputLayout

    private val checkboxTerms: CheckBox
    private val txtTermsAndConditions: TextView
    private val createAccount: Button

    private val recoveryEmailWarningDialog = RecoveryEmailWarningDialog(mActivity.layoutInflater.context)

    fun showRecoveryEmailWarningDialog(onRecoveryEmailWarningListener: OnRecoveryEmailWarningListener){
        recoveryEmailWarningDialog.showRecoveryEmailWarningDialog(onRecoveryEmailWarningListener)
    }

    fun showUsernameSucess() {
        usernameSuccessImage.visibility = View.VISIBLE
        usernameInput.hint = ""
    }

    fun hideUsernameSucess() {
        usernameSuccessImage.visibility = View.INVISIBLE
    }

    fun showPasswordSucess() {
        passwordSuccessImage.visibility = View.VISIBLE
        confirmPasswordSuccessImage.visibility = View.VISIBLE
    }

    fun hidePasswordSucess() {
        passwordSuccessImage.visibility = View.INVISIBLE
        confirmPasswordSuccessImage.visibility = View.INVISIBLE
    }

    @SuppressLint("RestrictedApi")
    fun hideUsernameErrors() {
        usernameErrorImage.visibility = View.INVISIBLE
        usernameInput.error = ""
    }

    @SuppressLint("RestrictedApi")
    fun hidePasswordErrors() {
        passwordErrorImage.visibility = View.GONE
        confirmPasswordErrorImage.visibility = View.GONE

        passwordInput.setHintTextAppearance(R.style.textinputlayout_login)
        password.supportBackgroundTintList = ColorStateList.valueOf(res.getColor(R.color.white))

        confirmPasswordInput.error = ""

        confirmPasswordInput.setHintTextAppearance(R.style.textinputlayout_login)
        confirmPassword.supportBackgroundTintList = ColorStateList.valueOf(res.getColor(R.color.white))
    }

    @SuppressLint("RestrictedApi")
    fun showPasswordErrors() {
        passwordErrorImage.visibility = View.VISIBLE
        confirmPasswordErrorImage.visibility = View.VISIBLE

        confirmPasswordInput.error = "Passwords do not match"
        passwordInput.setHintTextAppearance(R.style.textinputlayout_login_error)
        password.supportBackgroundTintList = ColorStateList.valueOf(res.getColor(R.color.black))
        confirmPassword.supportBackgroundTintList = ColorStateList.valueOf(res.getColor(R.color.black))
    }

    @SuppressLint("RestrictedApi")
    fun showUsernameErrors() {
        usernameErrorImage.visibility = View.VISIBLE
        usernameInput.error = "Username is not available"
        usernameInput.hint = ""
    }

    fun disableCreateAccountButton() {
        createAccount.isEnabled = false
    }

    fun enableCreateAccountButton() {
        createAccount.isEnabled = true
    }

    fun isPasswordErrorShown(): Boolean {
        return passwordErrorImage.visibility == View.VISIBLE
    }


    fun isUsernameErrorShown(): Boolean {
        return usernameErrorImage.visibility == View.VISIBLE
    }

    private fun assignPasswordTextChangeListener(onPasswordChangeListener: TextWatcher) {
        password.addTextChangedListener(onPasswordChangeListener)
    }

    private fun assignConfirmPasswordTextChangeListener(onPasswordChangeListener: TextWatcher) {
        confirmPassword.addTextChangedListener(onPasswordChangeListener)
    }

    private fun assignCheckTermsAndConditionsListener(onCheckedOptionChanged: CompoundButton.OnCheckedChangeListener) {
        checkboxTerms.setOnCheckedChangeListener(onCheckedOptionChanged)
    }

    private fun assignUsernameTextChangeListener(onUsernameChangeListener: TextWatcher) {
        username.addTextChangedListener(onUsernameChangeListener)
    }

    private fun assignfullNameTextChangeListener(onFullnameChangeListener: TextWatcher) {
        fullName.addTextChangedListener(onFullnameChangeListener)
    }

    private fun assignTermsAndConditionsClickListener
            (onTermsAndConditionsClick : View.OnClickListener){
        txtTermsAndConditions.setOnClickListener(onTermsAndConditionsClick)
    }

    fun toggleUsernameError(userAvailable: Boolean){
        if(userAvailable) {
            showUsernameSucess()
            hideUsernameErrors()
        } else {
            hideUsernameSucess()
            showUsernameErrors()
        }
    }
    init {
        mActivity.setContentView(R.layout.activity_sign_up)

        username = mActivity.findViewById(R.id.username)
        usernameInput = mActivity.findViewById(R.id.input_username)
        usernameSuccessImage = mActivity.findViewById(R.id.success_username)
        usernameErrorImage = mActivity.findViewById(R.id.error_username)

        fullName = mActivity.findViewById(R.id.full_name)
        fullNameInput = mActivity.findViewById(R.id.full_name_input)

        password = mActivity.findViewById(R.id.password)
        passwordInput = mActivity.findViewById(R.id.password_input)
        passwordSuccessImage = mActivity.findViewById(R.id.success_password)
        passwordErrorImage = mActivity.findViewById(R.id.error_password)

        confirmPassword = mActivity.findViewById(R.id.password_repeat)
        confirmPasswordInput = mActivity.findViewById(R.id.password_repeat_input)
        confirmPasswordSuccessImage = mActivity.findViewById(R.id.success_password_repeat)
        confirmPasswordErrorImage = mActivity.findViewById(R.id.error_password_repeat)


        recoveryEmail = mActivity.findViewById(R.id.recovery_email)
        recoveryEmailInput = mActivity.findViewById(R.id.recovery_email_input)

        checkboxTerms = mActivity.findViewById(R.id.chkTermsAndConditions)
        txtTermsAndConditions = mActivity.findViewById(R.id.txt_terms_and_conditions)
        createAccount = mActivity.findViewById(R.id.create_account)

        drawNormalTint()
    }

    fun drawNormalTint() {
        setHintAppearences()
        setBackgroundTintLists()
    }
    fun areThereFieldsEmpty(): Boolean {
        return username.text.isEmpty() ||
                fullName.text.isEmpty() ||
                password.text.isEmpty() ||
                confirmPassword.text.isEmpty()
    }

    fun isSetRecoveryEmail(): Boolean {
        return !recoveryEmail.text.isEmpty()
    }

    fun assignCreateAccountClickListener(onCreateAccountClick: View.OnClickListener) {
        createAccount.setOnClickListener(onCreateAccountClick)
    }

    @SuppressLint("RestrictedApi")
    fun setBackgroundTintLists() {
        username.supportBackgroundTintList = ColorStateList.valueOf(res.getColor(R.color.signup_hint_color))
        fullName.supportBackgroundTintList = ColorStateList.valueOf(res.getColor(R.color.signup_hint_color))
        password.supportBackgroundTintList = ColorStateList.valueOf(res.getColor(R.color.signup_hint_color))
        confirmPassword.supportBackgroundTintList = ColorStateList.valueOf(res.getColor(R.color.signup_hint_color))
        recoveryEmail.supportBackgroundTintList = ColorStateList.valueOf(res.getColor(R.color.signup_hint_color))
    }

    private fun setHintAppearences(){
        usernameInput.setHintTextAppearance(
                R.style.NormalTextAppearenceUsernameInput)
        passwordInput.setHintTextAppearance(
                R.style.NormalTextAppearenceUsernameInput)
        confirmPasswordInput.setHintTextAppearance(
                R.style.NormalTextAppearenceUsernameInput)
        recoveryEmailInput.setHintTextAppearance(
                R.style.NormalTextAppearenceUsernameInput)
        fullNameInput.setHintTextAppearance(
                R.style.NormalTextAppearenceUsernameInput)
    }

    fun initListeners(signUpListener: SignUpSceneController.SignUpListener.Default) {
        assignPasswordTextChangeListener(signUpListener.
                passwordListener.
                onPasswordChangedListener())
        assignConfirmPasswordTextChangeListener(signUpListener.
                passwordListener.
                onConfirmPasswordChangedListener())
        assignUsernameTextChangeListener(signUpListener.
                usernameListener.
                onUsernameChangedListener())
        assignCheckTermsAndConditionsListener(signUpListener.
                checkTermsAndConditionsListener.
                onCheckedOptionChanged())
        assignTermsAndConditionsClickListener(signUpListener.
                textTermsAndConditionsListener.
                onTermsAndConditionsClick())
        assignfullNameTextChangeListener(signUpListener.
                fullNameListener.
                onFullNameTextChangeListener())
        assignCreateAccountClickListener(signUpListener.
                createAccountButtonListener.
                onCreateAccountClick())
    }
}
