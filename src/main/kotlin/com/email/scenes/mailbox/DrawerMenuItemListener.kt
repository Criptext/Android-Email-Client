package com.email.scenes.mailbox

import com.email.db.models.Label

interface DrawerMenuItemListener {
    fun onNavigationItemClick(navigationMenuOptions: NavigationMenuOptions)
    fun onCustomLabelClicked(label: Label)
    fun onSettingsOptionClicked()
}