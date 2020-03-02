package com.criptext.mail.scenes.settings

import com.criptext.mail.scenes.settings.aliases.data.AliasItem

interface AliasListItemListener {
    fun onAliasTrashClicked(alias: AliasItem, position: Int) : Boolean
    fun onAliasActiveSwitched(alias: AliasItem, position: Int, enabled: Boolean)
}