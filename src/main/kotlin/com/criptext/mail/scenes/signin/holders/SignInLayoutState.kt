package com.criptext.mail.scenes.signin.holders

import com.criptext.mail.scenes.settings.devices.data.DeviceItem
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.validation.ProgressButtonState

/**
 * Created by gabriel on 5/18/18.
 */
sealed class SignInLayoutState {
    data class Start(val firstTime: Boolean): SignInLayoutState()
    data class Login(val username: String, val recipientId: String, val password: String, val domain: String,
                     val buttonState: ProgressButtonState, val firstTime: Boolean): SignInLayoutState()
    data class LoginValidation(val username: String, val domain: String, val recoveryCode: String,
                               val needToRemoveDevices: Boolean, val password: String, val recoveryAddress: String? = null): SignInLayoutState()
    data class ForgotPassword(val username: String): SignInLayoutState()
    data class DeniedValidation(val username: String, val domain: String): SignInLayoutState()
    data class ChangePassword(val username: String, val oldPassword: String, val domain: String,
                             val buttonState: ProgressButtonState): SignInLayoutState()
    data class RemoveDevices(val username: String, val domain: String, val password: String, val devices: List<DeviceItem>, val buttonState: ProgressButtonState): SignInLayoutState()
    data class Connection(val username: String, val domain: String, val authorizerType: DeviceUtils.DeviceType) : SignInLayoutState()
}