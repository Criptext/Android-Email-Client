package com.criptext.mail.scenes.signin

import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.scenes.signin.data.LinkDeviceState
import com.criptext.mail.scenes.signin.holders.SignInLayoutState
import com.criptext.mail.utils.DeviceUtils

/**
 * Created by sebas on 2/23/18.
 */
class SignInSceneModel(val isMultiple: Boolean = false) : SceneModel {
    var state: SignInLayoutState = SignInLayoutState.Start(username = "", firstTime = true)
    var ephemeralJwt: String = ""
    var hasTwoFA = false
    var realSecurePassword: String? = null
    var activeAccount: ActiveAccount? = null
    var linkDeviceState: LinkDeviceState = LinkDeviceState.Begin()
    var name = ""
    var randomId = 0
    var key = ""
    var dataAddress = ""
    var authorizerId = 0
    var authorizerType = DeviceUtils.DeviceType.Android
    var retryTimeLinkDataReady = 0
    var retryTimeLinkStatus = 0
}
