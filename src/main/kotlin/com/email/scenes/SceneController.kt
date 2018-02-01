package com.email.scenes

import android.app.Activity

/**
 * Created by sebas on 1/30/18.
 */
abstract class SceneController {

    abstract fun onStart()

    abstract fun onStop()

    abstract fun onBackPressed(activity: Activity)

}
