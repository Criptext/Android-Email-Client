package com.criptext.mail.scenes.settings

import com.criptext.mail.scenes.settings.custom_domain.data.DomainItem

interface DomainListItemListener {
    fun onCustomDomainTrashClicked(domain: DomainItem, position: Int) : Boolean
    fun onCustomDomainValidateClicked(domain: DomainItem, position: Int)
}