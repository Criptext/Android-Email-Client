package com.criptext.mail.scenes.settings.custom_domain.data

import com.criptext.mail.db.models.CustomDomain

data class DomainItem(val id: Long, val rowId: Long, val name: String, val validated: Boolean,
                      val accountId: Long): Comparable<DomainItem> {
    override fun compareTo(other: DomainItem): Int {
        return 0
    }
    constructor(domain: CustomDomain) : this(domain.id, domain.rowId, domain.name, domain.validated, domain.accountId)
}