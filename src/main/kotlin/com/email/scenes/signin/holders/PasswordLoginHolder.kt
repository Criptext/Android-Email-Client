package com.email.scenes.signin.holders

import android.support.design.widget.TextInputEditText
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.email.R
import com.email.scenes.signin.SignInSceneController

/**
 * Created by sebas on 3/8/18.
 */

class PasswordLoginHolder(
        val view: View,
        val user : String
) {

    private val username: TextView
    private val password: TextInputEditText
    private val buttonConfirm: Button

    var signInListener: SignInSceneController.SignInListener? = null

    init {
        username = view.findViewById(R.id.username)
        password = view.findViewById(R.id.password)
        buttonConfirm = view.findViewById(R.id.buttonConfirm)

        username.text  = "$user@criptext.com"

    }

    fun assignPasswordChangeListener() {

        password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                signInListener!!.onPasswordChangeListener(text.toString())
            }
        })
    }
    fun assignConfirmButtonListener() {
        buttonConfirm.setOnClickListener {
            signInListener!!.onPasswordLoginClick()
        }
    }

}
