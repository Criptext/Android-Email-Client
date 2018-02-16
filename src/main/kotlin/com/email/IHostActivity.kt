package com.email

import android.view.MenuItem
import com.email.scenes.params.SceneParams

/**
 * Created by sebas on 1/29/18.
 */
interface IHostActivity {
    fun refreshToolbarItems()
    fun goToScene(params: SceneParams)

    interface IActivityMenu {
        fun findItemById(id: Int): MenuItem?
    }

}
