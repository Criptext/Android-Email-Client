package com.email.scenes

/**
 * Created by sebas on 1/30/18.
 */
abstract class SceneController {
    abstract val menuResourceId: Int?

    abstract fun onStart()

    abstract fun onStop()

    abstract fun onBackPressed(): Boolean

    abstract fun onOptionsItemSelected(itemId: Int)

}
