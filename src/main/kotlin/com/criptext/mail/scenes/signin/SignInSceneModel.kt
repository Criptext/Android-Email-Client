package com.criptext.mail.scenes.signin

import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.scenes.signin.holders.SignInLayoutState

/**
 * Created by sebas on 2/23/18.
 */
class SignInSceneModel : SceneModel {
    var state: SignInLayoutState = SignInLayoutState.Start(username = "", firstTime = true)
    var ephemeralJwt: String = ""
}
