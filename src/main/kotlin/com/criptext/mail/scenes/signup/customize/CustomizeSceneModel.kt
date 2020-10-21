package com.criptext.mail.scenes.signup.customize

import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.scenes.signup.customize.holder.CustomizeLayoutState
import com.criptext.mail.scenes.signup.holders.SignUpLayoutState
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.validation.TextInput
import com.google.api.services.drive.Drive

class CustomizeSceneModel(val recoveryEmail: String) : SceneModel {
    var isRecoveryEmailVerified = false
    var hasAllowedContacts = false
    var hasDarkTheme: Boolean = false
    var hasSetPicture = false
    var mDriveServiceHelper: Drive? = null
    var state: CustomizeLayoutState? = null
}