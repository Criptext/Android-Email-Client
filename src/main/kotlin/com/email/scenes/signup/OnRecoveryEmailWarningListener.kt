package com.email.scenes.signup

import com.email.scenes.signin.SignUpSceneController


/**
 * Created by sebas on 2/20/18.
 */

class OnRecoveryEmailWarningListener
    ( warningListener: SignUpSceneController.OnWarningListener ) {
    val willAssignRecoverEmail = {
        ->
        warningListener.willAssignRecoverEmail()
    }

    val  denyWillAssignRecoverEmail = {
        ->
         warningListener.denyWillAssignRecoverEmail()
    }
}