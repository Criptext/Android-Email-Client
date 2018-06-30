package com.email

import com.email.scenes.ActivityMessage
import com.email.scenes.params.SceneParams
import com.email.utils.UIMessage

/**
 * Created by sebas on 1/29/18.
 */
interface IHostActivity {
    fun refreshToolbarItems()
    /**
     * Launches a new activity to show the new scene.
     * @param params object used to create the new scene.
     * @param keep if true, the current activity will not be finished after the transition, so
     * that the user can return by pressing back.
     */
    fun goToScene(params: SceneParams, keep: Boolean)

    /**
     * Finishes the current activity and opens a new one using a "return" animation.
     * @param params object used to create the new scene. If the activity for the scene is used,
     * it will be reused.
     * @param activityMessage an object to pass to the activity that will take the foreground. This
     * is useful when you want to return to an activity that already exists so params won't work.
     */
    fun exitToScene(params: SceneParams, activityMessage: ActivityMessage?)
    /**
     * Finishes the current activity.
     */
    fun finishScene()
    fun getLocalizedString(message: UIMessage): String
    fun showDialog(message: UIMessage)
    fun dismissDialog()
    /**
     * Launch an activity for a result, and then pass that result as an ActivityMessage
     * @param params Object with the necessary data to launch the correct activity.
     */
    fun launchExternalActivityForResult(params: ExternalActivityParams)

    fun checkPermissions(requestCode: Int, permission: String): Boolean
}
