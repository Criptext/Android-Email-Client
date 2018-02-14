package com.email.scenes.mailbox

import com.email.scenes.mailbox.holders.ToolbarHolder

/**
 * Created by sebas on 2/6/18.
 */

class ToolbarController(val toolbarHolder: ToolbarHolder) {

    fun onStart(emailSize: Int) {
        updateNumerOfMails(emailSize)
    }

    fun showMultiModeBar(selectedThreadsQuantity: Int){
        toolbarHolder.showMultiModeBar(selectedThreadsQuantity)
    }

    fun hideMultiModeBar() {
        toolbarHolder.hideMultiModeBar()
    }

    fun updateToolbarTitle(title: String){
        toolbarHolder.updateToolbarTitle(title)
    }

    fun updateNumerOfMails(emailsSize: Int) {
        toolbarHolder.updateNumerOfMails(emailsSize)
    }
}
