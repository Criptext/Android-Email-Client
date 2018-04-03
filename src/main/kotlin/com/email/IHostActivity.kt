package com.email

import com.email.scenes.ActivityMessage
import com.email.scenes.params.SceneParams
import com.email.utils.UIMessage

/**
 * Created by sebas on 1/29/18.
 */
interface IHostActivity {
    fun refreshToolbarItems()
    fun goToScene(params: SceneParams)
    fun exitToScene(params: SceneParams, activityMessage: ActivityMessage?)
    fun finishScene()
    fun getLocalizedString(message: UIMessage): String
    fun showDialog(message: UIMessage)
    fun dismissDialog()
}
