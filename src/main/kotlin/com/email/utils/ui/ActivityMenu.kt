package com.email.utils.ui

import android.view.Menu
import android.view.MenuItem
import com.email.IHostActivity

/**
 * Created by gabriel on 7/12/17.
 */

class ActivityMenu(val menu: Menu): IHostActivity.IActivityMenu {

    override fun findItemById(id: Int): MenuItem = menu.findItem(id)

}