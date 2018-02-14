package com.email.scenes.signin

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CompoundButton
import com.email.scenes.SceneController
import com.email.scenes.signup.OnRecoveryEmailWarningListener
import com.email.scenes.signup.SignUpSceneModel

/**
 * Created by sebas on 2/15/18.
 */

class SignUpSceneController(
        val model: SignUpSceneModel,
        val holder: SignUpViewHolder,
        val dataSource: SignUpDataSource): SceneController() {

    override val menuResourceId: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    val arePasswordsMatching: Boolean
        get() = model.password.equals(model.confirmPassword)

    val isPasswordErrorShown : Boolean
        get() = holder.isPasswordErrorShown()

    val isUsernameErrorShown : Boolean
        get() = holder.isUsernameErrorShown()

    val isCheckedTermsAndConditions : Boolean
        get() = model.checkTermsAndConditions

    val fieldsAreEmpty : Boolean
        get() = areFieldsEmpty()

    val isSetRecoveryEmail : Boolean
        get() = model.recoveryEmail.isNotEmpty()


    private fun shouldCreateButtonBeEnabled() : Boolean{
        return !isUsernameErrorShown
                && !isPasswordErrorShown
                && isCheckedTermsAndConditions
                && !fieldsAreEmpty
    }

    private val createAccountButtonListener = object : SignUpListener.CreateAccountButtonListener {
        override fun onCreateAccountClick(): View.OnClickListener {
            return object : View.OnClickListener{
                override fun onClick(view: View?) {
                    if(!isSetRecoveryEmail){
                        val warningListener = OnWarningListener.Default()
                        holder.showRecoveryEmailWarningDialog(
                                OnRecoveryEmailWarningListener(warningListener = warningListener)
                        )
                    } else {
                        TODO("GO TO LOGIN")
                    }
                }

            }
        }
    }
    private val passwordListener = object : SignUpListener.PasswordListener {
        override fun onConfirmPasswordChangedListener(): TextWatcher {
            return object : TextWatcher{
                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    model.confirmPassword = text.toString()
                    if(arePasswordsMatching && model.confirmPassword.length > 0) {
                        holder.hidePasswordErrors()
                        holder.showPasswordSucess()
                        if(shouldCreateButtonBeEnabled()) {
                            holder.enableCreateAccountButton()
                        }
                    } else if(arePasswordsMatching && model.confirmPassword.length == 0){
                        holder.hidePasswordSucess()
                        holder.hidePasswordErrors()
                        holder.disableCreateAccountButton()
                    }
                    else {
                        holder.showPasswordErrors()
                        holder.hidePasswordSucess()
                        holder.disableCreateAccountButton()
                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            }
        }
        override fun onPasswordChangedListener(): TextWatcher {
            return object : TextWatcher{
                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    model.password = text.toString()
                            if(arePasswordsMatching && model.password.length > 0) {
                                holder.hidePasswordErrors()
                                holder.showPasswordSucess()
                                if(shouldCreateButtonBeEnabled()) {
                                    holder.enableCreateAccountButton()
                                }
                            } else if(arePasswordsMatching && model.password.length == 0){
                                holder.hidePasswordSucess()
                                holder.hidePasswordErrors()
                                holder.disableCreateAccountButton()
                            }
                            else {
                                holder.showPasswordErrors()
                                holder.hidePasswordSucess()
                                holder.disableCreateAccountButton()
                            }
                }

                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            }
        }
    }
    private val usernameListener = object : SignUpListener.UsernameListener {
        override fun onUsernameChangedListener(): TextWatcher {
            return object : TextWatcher{
                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    model.username = text.toString()
                    val isUserAvailable = isUserAvailable()
                    holder.toggleUsernameError(userAvailable = isUserAvailable)
                    if(isUserAvailable && shouldCreateButtonBeEnabled()) {
                        holder.enableCreateAccountButton()
                    } else {
                        holder.disableCreateAccountButton()
                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            }
        }

    }
    private val checkTermsAndConditionsListener = object : SignUpListener.CheckTermsAndConditionsListener {
        override fun onCheckedOptionChanged(): CompoundButton.OnCheckedChangeListener {
            return object : CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(p0: CompoundButton?, state: Boolean) {
                    model.checkTermsAndConditions = state
                    if (model.checkTermsAndConditions) {
                        if (shouldCreateButtonBeEnabled()) {
                            holder.enableCreateAccountButton()
                        }
                    } else {
                        holder.disableCreateAccountButton()
                    }
                }
            }
        }
    }
    private val textTermsAndConditionsListener = object : SignUpListener.TextTermsAndConditionsListener {
        override fun onTermsAndConditionsClick(): View.OnClickListener {
            return object : View.OnClickListener{
                override fun onClick(view: View?) {
                    TODO("READ TERMS AND CONDITIONS.")
                }
            }
        }

    }
    private val fullNameListener = object : SignUpListener.FullNameListener {
        override fun onFullNameTextChangeListener(): TextWatcher {
            return object: TextWatcher{
                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    model.fullName = text.toString()
                    if(shouldCreateButtonBeEnabled()) {
                        holder.enableCreateAccountButton()
                    }
                }

            }
        }
    }

    val willAssignRecoverEmail = {
        ->
        willAssignRecoverEmail()
    }

    val denyWillAssignRecoverEmail = {
        ->
        denyWillAssignRecoverEmail()
    }

    fun willAssignRecoverEmail() {
        TODO("WILL ASSIGN RECOVER EMAIL")
    }

    fun denyWillAssignRecoverEmail() {
        TODO("DENY WILL ASSIGN RECOVER EMAIL")
    }


    private val signUpListener = SignUpListener.Default(
            createAccountButtonListener = createAccountButtonListener,
            passwordListener = passwordListener,
            usernameListener = usernameListener,
            checkTermsAndConditionsListener = checkTermsAndConditionsListener,
            textTermsAndConditionsListener = textTermsAndConditionsListener,
            fullNameListener = fullNameListener)

    fun isUserAvailable(): Boolean {
        return model.username == "sebas"
    }

    override fun onStart() {
        holder.disableCreateAccountButton()
        holder.initListeners(signUpListener = signUpListener)
    }

    override fun onStop() {
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onOptionsItemSelected(itemId: Int) {
    }

    private fun areFieldsEmpty() : Boolean {
        return model.username.isEmpty() ||
                model.fullName.isEmpty() ||
                model.password.isEmpty() ||
                model.confirmPassword.isEmpty()
    }

    interface OnWarningListener {
        fun willAssignRecoverEmail()
        fun denyWillAssignRecoverEmail()

        class Default :OnWarningListener {
            override fun willAssignRecoverEmail() {
                willAssignRecoverEmail()
            }

            override fun denyWillAssignRecoverEmail() {
                denyWillAssignRecoverEmail()
            }
        }
    }
    interface SignUpListener {
        interface CreateAccountButtonListener {
            fun onCreateAccountClick() : View.OnClickListener
        }

        interface PasswordListener {
            fun onPasswordChangedListener(): TextWatcher
            fun onConfirmPasswordChangedListener(): TextWatcher
        }

        interface UsernameListener {
            fun onUsernameChangedListener(): TextWatcher
        }

        interface CheckTermsAndConditionsListener {
            fun onCheckedOptionChanged(): CompoundButton.OnCheckedChangeListener
        }

        interface TextTermsAndConditionsListener {
            fun onTermsAndConditionsClick(): View.OnClickListener
        }

        interface FullNameListener {
            fun onFullNameTextChangeListener() : TextWatcher
        }

        class Default(
                val createAccountButtonListener: CreateAccountButtonListener,
                val passwordListener: PasswordListener,
                val usernameListener: UsernameListener,
                val checkTermsAndConditionsListener: CheckTermsAndConditionsListener,
                val textTermsAndConditionsListener: TextTermsAndConditionsListener,
                val fullNameListener: FullNameListener): SignUpListener
    }
}