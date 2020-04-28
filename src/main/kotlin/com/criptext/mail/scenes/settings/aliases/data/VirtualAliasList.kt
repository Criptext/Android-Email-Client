package com.criptext.mail.scenes.settings.aliases.data

import com.criptext.mail.utils.virtuallist.VirtualList

class VirtualAliasList(val aliases: List<AliasItem>): VirtualList<AliasItem>{

    override fun get(i: Int): AliasItem {
        return aliases[i]
    }

    override val size: Int
        get() = aliases.size

    override val hasReachedEnd = true

}