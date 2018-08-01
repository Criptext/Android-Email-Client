package com.criptext.mail.utils.ui

import android.view.Menu
import android.view.MenuItem
import com.criptext.mail.IHostActivity

/**
 * Created by gabriel on 7/12/17.
 */

class ActivityMenu(val menu: Menu): IHostActivity.IActivityMenu {

    override fun findItemById(id: Int): MenuItem? = menu.findItem(id)

}