package com.criptext.mail.scenes.settings.custom_domain

import com.criptext.mail.db.AccountTypes
import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.scenes.settings.custom_domain.data.DomainItem

class CustomDomainModel: SceneModel{
    var domains: ArrayList<DomainItem> = ArrayList()
    var accountType = AccountTypes.STANDARD
}