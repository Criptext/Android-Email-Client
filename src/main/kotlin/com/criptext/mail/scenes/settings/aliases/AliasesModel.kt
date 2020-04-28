package com.criptext.mail.scenes.settings.aliases

import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.scenes.settings.aliases.data.AliasItem
import com.criptext.mail.scenes.settings.custom_domain.data.DomainItem

class AliasesModel: SceneModel{
    var criptextAliases: ArrayList<AliasItem> = ArrayList()
    var domains: ArrayList<DomainItem> = ArrayList()
}