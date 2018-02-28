package com.email

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.email.scenes.SceneController
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import com.email.scenes.mailbox.MailboxSceneModel
import com.email.scenes.params.SceneParams
import com.email.scenes.params.SearchParams
import com.email.scenes.params.SignUpParams
import com.email.scenes.search.SearchSceneModel
import com.email.scenes.signin.SignInSceneModel
import com.email.scenes.signup.SignUpSceneModel
import com.email.utils.UIMessage

/**
 * Base class for all of our activities. If you extend this class you don't need to implement
 * `onCreate`, `onStart` or `onStop`. This class will create your controller with `initController`
 * and then forward `onStart` and `onStop` events to it.
 * Created by gabriel on 2/14/18.
 */

abstract class BaseActivity: AppCompatActivity(), IHostActivity {

    /**
     * Resource Id of the layout to be used by this activity. This value will be used on `onCreate`
     * to inflate the activity's views. Your layout must contain a toolbar somewhere.
     */
    abstract val layoutId: Int

    /**
     * Resource Id of your activity's toolbar. After the layout is inflated, BaseActivity will call
     * `findViewById` with this value to get the toolbar and set it as action bar. If no toolbar
     * is found your activity will crash.
     */
    abstract val toolbarId: Int?

    private lateinit var controller: SceneController

    /**
     * Called during `onCreate` to create a controller for this activity given the current active
     * model which is passed as parameter. `BaseActivity` will call this once and keep a private
     * reference to the controller in order to forward events like `onStart` and `onStop`. There
     * is no need for your activity to keep another reference to the controller.
     * @param receivedModel The model that your controller should use. You should coerce this value
     * into the type that your controller expects.
     */
    abstract fun initController(receivedModel: Any): SceneController

    private fun getCachedModelOrThrow(): Any {
        return cachedModels[javaClass]
            ?: throw IllegalStateException("No model found for $javaClass. Perhaps you opened the wrong activity")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)

        val toolbarId = this.toolbarId
        if(toolbarId != null) {
            val toolbar = findViewById<Toolbar>(toolbarId)
            setSupportActionBar(toolbar)
        }

        val currentModel = getCachedModelOrThrow()
        controller = initController(currentModel)
    }

    override fun onStart() {
        super.onStart()
        controller.onStart()
    }

    override fun onStop() {
        super.onStop()
        controller.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        controller.onOptionsItemSelected(itemId)
        return true
    }

    override fun onBackPressed() {
        val shouldCallSuper = controller.onBackPressed()
        if (shouldCallSuper) super.onBackPressed()
    }

    private fun startActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val activeSceneMenu = controller.menuResourceId
        if(activeSceneMenu != null) menuInflater.inflate(activeSceneMenu, menu)
        return true
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    private fun createNewSceneFromParams(params: SceneParams): Any {
        return when(params) {
            is SearchParams -> SearchSceneModel()
            is SignUpParams -> SignUpSceneModel()
            else -> throw IllegalArgumentException("Don't know how to create a model from ${params.javaClass}")
        }
    }

    override fun refreshToolbarItems() {
        this.invalidateOptionsMenu()
    }

    override fun getLocalizedString(message: UIMessage): String {
        return getString(message.resId, *message.args)
    }

    override fun goToScene(params: SceneParams) {
        val newSceneModel = createNewSceneFromParams(params)
        cachedModels[params.activityClass] = newSceneModel
        startActivity(params.activityClass)
    }

    override fun finishScene() {
        finish()
    }

    private companion object {
        val cachedModels = HashMap<Class<*>, Any>()

        init {
            // set initial state
            cachedModels[MailboxActivity::class.java] = MailboxSceneModel()
            cachedModels[SignInActivity::class.java] = SignInSceneModel()
            cachedModels[SignUpActivity::class.java] = SignUpSceneModel()
        }
    }

}
