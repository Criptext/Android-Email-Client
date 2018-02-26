package com.email

import com.email.DB.SignUpLocalDB
import com.email.scenes.SceneController
import com.email.scenes.signin.SignUpDataSource
import com.email.scenes.signin.SignUpScene
import com.email.scenes.signin.SignUpSceneController
import com.email.scenes.signup.SignUpSceneModel

/**
 * Created by sebas on 2/16/18.
 */

class SignUpActivity: BaseActivity() {
    override val layoutId: Int = R.layout.activity_sign_up

    override val toolbarId: Int?
        get() = null

    override fun initController(receivedModel: Any): SceneController {
        val db: SignUpLocalDB.Default = SignUpLocalDB.Default(this.applicationContext)
        val signUpSceneView = SignUpScene.SignUpSceneView(findViewById(R.id.signup_layout_container))
        val signUpSceneModel = receivedModel as SignUpSceneModel
        return SignUpSceneController(
                model = signUpSceneModel,
                scene = signUpSceneView,
                host = this,
                dataSource = SignUpDataSource(db))
    }
}
