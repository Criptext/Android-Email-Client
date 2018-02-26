package com.email

import com.email.DB.SignInLocalDB
import com.email.scenes.SceneController
import com.email.scenes.signin.*

/**
 * Created by sebas on 2/15/18.
 */

class SignInActivity : BaseActivity() {
    override val layoutId: Int = R.layout.activity_sign_in
    override val toolbarId: Int?
        get() = null

    override fun initController(receivedModel: Any): SceneController {
        val db: SignInLocalDB.Default = SignInLocalDB.Default(this.applicationContext)
        val signInSceneView = SignInScene.SignInSceneView(findViewById(R.id.signin_layout_container))
        val signInSceneModel = receivedModel as SignInSceneModel
        return SignInSceneController(
                model = signInSceneModel,
                scene = signInSceneView,
                host = this,
                dataSource = SignInDataSource(db)
                )
    }
}
