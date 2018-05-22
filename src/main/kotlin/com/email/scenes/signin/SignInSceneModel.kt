package com.email.scenes.signin

import com.email.scenes.SceneModel
import com.email.scenes.signin.holders.SignInLayoutState

/**
 * Created by sebas on 2/23/18.
 */
class SignInSceneModel : SceneModel {
    var state: SignInLayoutState = SignInLayoutState.Start(username = "")
}
