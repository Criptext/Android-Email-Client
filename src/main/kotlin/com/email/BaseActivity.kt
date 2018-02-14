package com.email

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.email.scenes.SceneController

/**
 * Created by gabriel on 2/14/18.
 */

abstract class BaseActivity: AppCompatActivity() {


    abstract val layoutId: Int
    abstract val toolbarId: Int

    private lateinit var controller: SceneController
    abstract fun initController(): SceneController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)

        val toolbar = findViewById<Toolbar>(toolbarId)
        setSupportActionBar(toolbar)

        controller = initController()
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
        if(controller.onBackPressed())
            super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val activeSceneMenu = controller.menuResourceId
        menuInflater.inflate(activeSceneMenu, menu)
        return true
    }


}