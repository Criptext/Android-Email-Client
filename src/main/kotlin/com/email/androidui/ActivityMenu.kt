package com.email.androidui

import android.view.Menu
import com.email.IHostActivity

/**
 * Created by sebas on 2/7/18.
 */

class ActivityMenu(val menu: Menu): IHostActivity.IActivityMenu {

    override fun findItemById(id: Int) = menu.findItem(id)

}
