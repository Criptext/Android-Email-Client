package com.criptext.mail.scenes.signin.holders

import com.criptext.mail.scenes.settings.devices.data.DeviceItem
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.validation.ProgressButtonState

/**
 * Created by gabriel on 5/18/18.
 */
sealed class SignInLayoutState {
    data class Start(val username: String, val firstTime: Boolean): SignInLayoutState()
    data class LoginValidation(val username: String, val domain: String, val hasTwoFA: Boolean = false): SignInLayoutState()
    data class InputPassword(val username: String, val password: String, val domain: String,
                             val buttonState: ProgressButtonState, val hasTwoFA: Boolean = false): SignInLayoutState()
    data class ChangePassword(val username: String, val oldPassword: String, val domain: String,
                             val buttonState: ProgressButtonState): SignInLayoutState()
    data class RemoveDevices(val username: String, val domain: String, val devices: List<DeviceItem>, val buttonState: ProgressButtonState): SignInLayoutState()
    data class WaitForApproval(val username: String, val domain: String, val authorizerType: DeviceUtils.DeviceType) : SignInLayoutState()
}