package com.criptext.mail.scenes.settings.custom_domain.data

import com.criptext.mail.db.models.Alias
import com.criptext.mail.db.models.CustomDomain
import com.criptext.mail.scenes.settings.aliases.data.AliasItem

data class DomainItem(val id: Long, val rowId: Long, val name: String, val validated: Boolean,
                      val accountId: Long, var aliases: ArrayList<AliasItem>): Comparable<DomainItem> {
    override fun compareTo(other: DomainItem): Int {
        return 0
    }
    constructor(domain: CustomDomain, aliases: List<Alias>) : this(domain.id, domain.rowId,
            domain.name, domain.validated, domain.accountId, ArrayList(aliases.map { AliasItem(it) }))
}