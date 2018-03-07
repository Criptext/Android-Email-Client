package com.email

import com.email.api.SignalKeyGenerator
import com.email.bgworker.AsyncTaskWorkRunner
import com.email.db.SignUpLocalDB
import com.email.scenes.SceneController
import com.email.scenes.signin.SignUpDataSource
import com.email.scenes.signup.SignUpScene
import com.email.scenes.signup.SignUpSceneController
import com.email.scenes.signup.SignUpSceneModel
import com.email.scenes.signup.data.SignUpAPIClient

/**
 * Created by sebas on 2/16/18.
 */

class SignUpActivity: BaseActivity() {
    override val layoutId: Int = R.layout.activity_sign_up

    override val toolbarId: Int?
        get() = null

    override fun initController(receivedModel: Any): SceneController {
        val db: SignUpLocalDB.Default = SignUpLocalDB.Default(this.applicationContext)
        val signalKeyGenerator = SignalKeyGenerator.Default()
        val signUpSceneView = SignUpScene.SignUpSceneView(findViewById(R.id.signup_layout_container))
        val signUpSceneModel = receivedModel as SignUpSceneModel
        return SignUpSceneController(
                model = signUpSceneModel,
                scene = signUpSceneView,
                host = this,
                dataSource = SignUpDataSource(runner = AsyncTaskWorkRunner(),
                        signUpAPIClient = SignUpAPIClient.Default(),
                        signUpLocalDB = db,
                        signalKeyGenerator = signalKeyGenerator))
    }
}
