package com.email

import android.view.MenuItem

/**
 * Created by sebas on 1/29/18.
 */
interface IHostActivity {
    fun refreshToolbarItems()

    interface IActivityMenu {
        fun findItemById(id: Int): MenuItem?
    }

}
