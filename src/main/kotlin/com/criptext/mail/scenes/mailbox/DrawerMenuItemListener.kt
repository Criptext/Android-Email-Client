package com.criptext.mail.scenes.mailbox

import com.criptext.mail.db.models.Label

interface DrawerMenuItemListener {
    fun onNavigationItemClick(navigationMenuOptions: NavigationMenuOptions)
    fun onCustomLabelClicked(label: Label)
    fun onSettingsOptionClicked()
    fun onInviteFriendOptionClicked()
    fun onSupportOptionClicked()
}