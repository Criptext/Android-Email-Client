package com.email

import com.email.DB.SignInLocalDB
import com.email.scenes.SceneController
import com.email.scenes.signin.SignInDataSource
import com.email.scenes.signin.SignInSceneController
import com.email.scenes.signin.SignInSceneModel
import com.email.scenes.signin.SignInViewHolder

/**
 * Created by sebas on 2/15/18.
 */

class SignInActivity : BaseActivity() {
    override val layoutId: Int = R.layout.activity_sign_in
    override val toolbarId: Int?
        get() = null

    override fun initController(receivedModel: Any): SceneController {
        val db: SignInLocalDB.Default = SignInLocalDB.Default(this.applicationContext)
        val viewHolder = SignInViewHolder(this)
        val signInSceneModel = receivedModel as SignInSceneModel
        return SignInSceneController(
                model = signInSceneModel,
                holder = viewHolder,
                dataSource = SignInDataSource(db))
    }
}
