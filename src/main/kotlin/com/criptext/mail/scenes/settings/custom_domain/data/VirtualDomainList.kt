package com.criptext.mail.scenes.settings.custom_domain.data

import com.criptext.mail.scenes.settings.custom_domain.CustomDomainModel
import com.criptext.mail.scenes.settings.devices.DevicesModel
import com.criptext.mail.utils.virtuallist.VirtualList

class VirtualDomainList(val model: CustomDomainModel): VirtualList<DomainItem>{

    override fun get(i: Int): DomainItem {
        return model.domains[i]
    }

    override val size: Int
        get() = model.domains.size

    override val hasReachedEnd = true

}