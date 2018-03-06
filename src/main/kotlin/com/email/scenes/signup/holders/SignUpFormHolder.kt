package com.email.scenes.signup.holders

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.email.R
import com.email.scenes.signin.RecoveryEmailWarningDialog
import com.email.scenes.signin.SignUpSceneController
import com.email.scenes.signup.OnRecoveryEmailWarningListener

/**
 * Created by sebas on 3/2/18.
 */

class SignUpFormHolder(val view: View) {

    private val username: AppCompatEditText
    private val usernameInput: TextInputLayout
    private val usernameSuccessImage: ImageView
    private val usernameErrorImage: ImageView

    private val fullName: AppCompatEditText
    private  val fullNameInput: TextInputLayout

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
    private val imageBack: ImageView
    private val recoveryEmailWarningDialog = RecoveryEmailWarningDialog(view.context)

    var signUpListener : SignUpSceneController.SignUpListener? = null

    init {
        username = view.findViewById(R.id.username)
        usernameInput = view.findViewById(R.id.input_username)
        usernameSuccessImage = view.findViewById(R.id.success_username)
        usernameErrorImage = view.findViewById(R.id.error_username)
        fullName = view.findViewById(R.id.full_name)
        fullNameInput = view.findViewById(R.id.full_name_input)
        password = view.findViewById(R.id.password)
        passwordInput = view.findViewById(R.id.password_input)
        passwordSuccessImage = view.findViewById(R.id.success_password)
        passwordErrorImage = view.findViewById(R.id.error_password)
        confirmPassword = view.findViewById(R.id.password_repeat)
        confirmPasswordInput = view.findViewById(R.id.password_repeat_input)
        confirmPasswordSuccessImage = view.findViewById(R.id.success_password_repeat)
        confirmPasswordErrorImage = view.findViewById(R.id.error_password_repeat)
        recoveryEmail = view.findViewById(R.id.recovery_email)
        recoveryEmailInput = view.findViewById(R.id.recovery_email_input)
        checkboxTerms = view.findViewById(R.id.chkTermsAndConditions)
        txtTermsAndConditions = view.findViewById(R.id.txt_terms_and_conditions)
        createAccount = view.findViewById(R.id.create_account)
        imageBack = view.findViewById(R.id.icon_back)

        setHintAppearences()
        setBackgroundTintLists()
    }


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
        password.supportBackgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(view.context, R.color.signup_hint_color))

        confirmPasswordInput.error = ""

        confirmPasswordInput.setHintTextAppearance(R.style.textinputlayout_login)
        confirmPassword.supportBackgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(view.context, R.color.signup_hint_color))
    }

    @SuppressLint("RestrictedApi")
    fun showPasswordErrors() {
        passwordErrorImage.visibility = View.VISIBLE
        confirmPasswordErrorImage.visibility = View.VISIBLE

        confirmPasswordInput.error = "Passwords do not match"
        passwordInput.setHintTextAppearance(R.style.textinputlayout_login_error)
        confirmPasswordInput.setHintTextAppearance(R.style.textinputlayout_login_error)
        password.supportBackgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(view.context, R.color.black))
        confirmPassword.supportBackgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(view.context, R.color.black))
    }

    @SuppressLint("RestrictedApi")
    fun showUsernameErrors() {
        usernameErrorImage.visibility = View.VISIBLE
        usernameInput.error = "Username not available"
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

    fun assignPasswordTextListener() {
        password.addTextChangedListener( object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                signUpListener?.onPasswordChangedListener(text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    fun assignConfirmPasswordTextChangeListener() {
        confirmPassword.addTextChangedListener( object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                signUpListener?.onConfirmPasswordChangedListener(text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    fun assignCheckTermsAndConditionsListener() {
        checkboxTerms.setOnCheckedChangeListener(object :
                CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, state: Boolean) {
                signUpListener?.onCheckedOptionChanged(state)
            }
        })
    }

    fun assignUsernameTextChangeListener() {
        username.addTextChangedListener( object : TextWatcher {

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                signUpListener?.onUsernameChangedListener(text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    fun assignfullNameTextChangeListener() {
        fullName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                signUpListener?.onFullNameTextChangeListener(text.toString())
            }
        })
    }

    fun assignRecoveryEmailTextChangeListener() {

        recoveryEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                signUpListener?.onRecoveryEmailTextChangeListener(text.toString())
            }
        })
    }

    fun assignTermsAndConditionsClickListener() {
        txtTermsAndConditions.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                signUpListener?.onTermsAndConditionsClick()
            }
        })
    }

    fun assignBackButtonListener() {
        imageBack.setOnClickListener {
            signUpListener?.onBackPressed()
        }
    }

    fun assignCreateAccountClickListener() {
        createAccount.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                signUpListener?.onCreateAccountClick()
            }
        })
    }

    @SuppressLint("RestrictedApi")
    private fun setBackgroundTintLists() {
        username.supportBackgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(view.context, R.color.signup_hint_color))
        fullName.supportBackgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(view.context, R.color.signup_hint_color))
        password.supportBackgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(view.context, R.color.signup_hint_color))
        confirmPassword.supportBackgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(view.context, R.color.signup_hint_color))
        recoveryEmail.supportBackgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(view.context, R.color.signup_hint_color))
    }

    fun setHintAppearences(){
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

    fun fillSceneWidgets(
            username: String,
            fullName: String,
            password: String,
            recoveryEmail: String) {
        this.username.setText(username, TextView.BufferType.EDITABLE)
        this.fullName.setText(fullName, TextView.BufferType.EDITABLE)
        this.password.setText(password, TextView.BufferType.EDITABLE)
        this.confirmPassword.setText(password, TextView.BufferType.EDITABLE)
        this.recoveryEmail.setText(recoveryEmail, TextView.BufferType.EDITABLE)
        this.checkboxTerms.isChecked = true
    }
}