package com.criptext.mail.scenes.settings.aliases.data

import com.criptext.mail.db.models.Alias

data class AliasItem(val id: Long, val rowId: Long, val name: String, val domain: String?,
                     val isActive: Boolean, val accountId: Long): Comparable<AliasItem> {
    override fun compareTo(other: AliasItem): Int {
        return 0
    }
    constructor(alias: Alias) : this(alias.id, alias.rowId, alias.name, alias.domain, alias.active,
            alias.accountId)
}