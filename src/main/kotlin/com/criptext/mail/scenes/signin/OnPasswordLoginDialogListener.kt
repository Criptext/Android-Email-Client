package com.criptext.mail.scenes.signin

/**
 * Created by sebas on 3/8/18.
 */

interface OnPasswordLoginDialogListener {
    fun acceptPasswordLogin(username: String, domain: String)
    fun cancelPasswordLogin()
}
