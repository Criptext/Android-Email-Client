package com.email.scenes

/**
 * Base class for all the main controllers.
 * Created by sebas on 1/30/18.
 */
abstract class SceneController {
    /**
     * Host activity will check this value every time it has to redraw the toolbar's menu. You
     * should return the resource id of the menu you wish to display.
     */
    abstract val menuResourceId: Int?

    /**
     * Called during the host activity's `onStart()`. This where your controller's "setup" code
     * should go.
     * @param activityMessage A message sent from another controller. The implementation should
     * try to handle it.
     * @return true if the message was handled correctly, otherwise false.
     */
    abstract fun onStart(activityMessage: ActivityMessage?): Boolean

    /**
     * Called during the host activity's `onStop()`. This where your controller's "teardown" code
     * should go.
     */
    abstract fun onStop()

    /**
     * Called during the host activity's `onBackPressed`. If this function returns true, host
     * activity with call `super.onBackPressed()`, potentially closing the activity.
     *
     * If you don't want your host activity to be closed after the account presses back, make this
     * function return false.
     */
    abstract fun onBackPressed(): Boolean

    /**
     * Called during the host activity's onOptionsItemSelected. You only get the selected item's
     * id number, to avoid coupling the controller code with android APIs.
     */
    abstract fun onOptionsItemSelected(itemId: Int)


}
