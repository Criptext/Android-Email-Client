package com.criptext.mail.scenes.signup.holders

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import com.google.android.material.textfield.TextInputLayout
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import com.criptext.mail.R
import com.criptext.mail.scenes.signup.RecoveryEmailWarningDialog
import com.criptext.mail.scenes.signup.OnRecoveryEmailWarningListener
import com.criptext.mail.scenes.signup.SignUpSceneController
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.validation.TextInput
import com.criptext.mail.utils.getLocalizedUIMessage

/**
 * Created by sebas on 3/2/18.
 */

class SignUpFormHolder(val view: View) {

    private val createAccount: Button = view.findViewById(R.id.create_account)

    private val username: AppCompatEditText = view.findViewById(R.id.username)
    private val usernameInput: FormInputViewHolder = FormInputViewHolder(
            textInputLayout = view.findViewById(R.id.input_username),
            editText = username,
            validView = view.findViewById(R.id.success_username),
            errorView = view.findViewById(R.id.error_username),
            disableSubmitButton = { -> createAccount.isEnabled = false })

    private val fullName: AppCompatEditText = view.findViewById(R.id.full_name)
    private val fullNameInput: FormInputViewHolder = FormInputViewHolder(
            textInputLayout = view.findViewById(R.id.full_name_input),
            editText = fullName,
            validView = view.findViewById(R.id.success_fullname),
            errorView = view.findViewById(R.id.error_fullname),
            disableSubmitButton = { -> createAccount.isEnabled = false })

    private val password: AppCompatEditText = view.findViewById(R.id.password)
    private val passwordInput: TextInputLayout = view.findViewById(R.id.password_input)
    private val passwordSuccessImage: ImageView = view.findViewById(R.id.success_password)
    private val passwordErrorImage: ImageView = view.findViewById(R.id.error_password)

    private val confirmPassword: AppCompatEditText = view.findViewById(R.id.password_repeat)
    private val confirmPasswordInput: TextInputLayout = view.findViewById(R.id.password_repeat_input)
    private val confirmPasswordSuccessImage: ImageView = view.findViewById(R.id.success_password_repeat)
    private val confirmPasswordErrorImage: ImageView = view.findViewById(R.id.error_password_repeat)

    private val recoveryEmail: AppCompatEditText = view.findViewById(R.id.recovery_email)
    private val recoveryEmailInput: FormInputViewHolder = FormInputViewHolder(
            textInputLayout = view.findViewById(R.id.recovery_email_input),
            editText = recoveryEmail,
            validView = view.findViewById(R.id.success_recovery),
            errorView = view.findViewById(R.id.error_recovery),
            disableSubmitButton = { -> createAccount.isEnabled = false })

    private val checkboxTerms: CheckBox = view.findViewById(R.id.chkTermsAndConditions)
    private val txtTermsAndConditions: TextView = view.findViewById(R.id.txt_terms_and_conditions)
    private val imageBack: ImageView = view.findViewById(R.id.icon_back)
    private val recoveryEmailWarningDialog = RecoveryEmailWarningDialog(view.context)

    var uiObserver: SignUpSceneController.SignUpUIObserver? = null

    init {
        setPasswordEyeToggle()
        setHintAppearences()
        setBackgroundTintLists()
    }

    fun showRecoveryEmailWarningDialog(onRecoveryEmailWarningListener: OnRecoveryEmailWarningListener){
        recoveryEmailWarningDialog.showRecoveryEmailWarningDialog(onRecoveryEmailWarningListener)
    }

    fun showPasswordSuccess() {
        passwordSuccessImage.visibility = View.VISIBLE
        confirmPasswordSuccessImage.visibility = View.VISIBLE
    }

    fun hidePasswordSuccess() {
        passwordSuccessImage.visibility = View.INVISIBLE
        confirmPasswordSuccessImage.visibility = View.INVISIBLE
    }


    @SuppressLint("RestrictedApi")
    fun hidePasswordError() {
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
    fun showPasswordError(message: UIMessage) {
        passwordErrorImage.visibility = View.VISIBLE
        confirmPasswordErrorImage.visibility = View.VISIBLE

        confirmPasswordInput.error = view.context.getLocalizedUIMessage(message)
        passwordInput.setHintTextAppearance(R.style.textinputlayout_login_error)
        confirmPasswordInput.setHintTextAppearance(R.style.textinputlayout_login_error)
        password.supportBackgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(view.context, R.color.black))
        confirmPassword.supportBackgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(view.context, R.color.black))
    }

    fun setPasswordError(message: UIMessage?) {
        if (message == null) hidePasswordError()
        else {
            showPasswordError(message)
            disableCreateAccountButton()
        }
    }

    fun setFullNameState(state: FormInputState) {
        fullNameInput.setState(state)
    }

    fun setRecoveryEmailState(state: FormInputState) {
        recoveryEmailInput.setState(state)
    }

    fun setUsernameState(state: FormInputState) {
        usernameInput.setState(state)
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


    fun assignPasswordTextListener() {
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

    fun assignConfirmPasswordTextChangeListener() {
        confirmPassword.addTextChangedListener( object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                uiObserver?.onConfirmPasswordChangedListener(text.toString())
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
                uiObserver?.onCheckedOptionChanged(state)
            }
        })
    }

    fun assignUsernameTextChangeListener() {
        username.addTextChangedListener( object : TextWatcher {

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                uiObserver?.onUsernameChangedListener(text.toString())
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
                uiObserver?.onFullNameTextChangeListener(text.toString())
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
                uiObserver?.onRecoveryEmailTextChangeListener(text.toString())
            }
        })
    }

    fun assignTermsAndConditionsClickListener() {
        txtTermsAndConditions.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                uiObserver?.onTermsAndConditionsClick()
            }
        })
    }

    fun assignBackButtonListener() {
        imageBack.setOnClickListener {
            uiObserver?.onBackPressed()
        }
    }

    fun assignCreateAccountClickListener() {
        createAccount.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                uiObserver?.onCreateAccountClick()
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

    private fun setPasswordEyeToggle(){
        passwordInput.isPasswordVisibilityToggleEnabled = true
        passwordInput.setPasswordVisibilityToggleTintList(
                AppCompatResources.getColorStateList(view.context, R.color.login_password_eye))
        confirmPasswordInput.isPasswordVisibilityToggleEnabled = true
        confirmPasswordInput.setPasswordVisibilityToggleTintList(
                AppCompatResources.getColorStateList(view.context, R.color.login_password_eye))
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

    fun fillSceneWidgets(
            username: TextInput,
            fullName: TextInput,
            password: String,
            confirmPassword: String,
            recoveryEmail: TextInput,
            isChecked: Boolean) {

        this.username.setText(username.value, TextView.BufferType.EDITABLE)
        this.fullName.setText(fullName.value, TextView.BufferType.EDITABLE)
        this.password.setText(password, TextView.BufferType.EDITABLE)
        this.confirmPassword.setText(confirmPassword, TextView.BufferType.EDITABLE)
        this.recoveryEmail.setText(recoveryEmail.value, TextView.BufferType.EDITABLE)
        this.checkboxTerms.isChecked = isChecked

        setUsernameState(username.state)
        setFullNameState(fullName.state)
        setRecoveryEmailState(recoveryEmail.state)
    }
}