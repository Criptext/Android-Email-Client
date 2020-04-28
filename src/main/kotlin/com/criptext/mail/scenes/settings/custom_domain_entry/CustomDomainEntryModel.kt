package com.criptext.mail.scenes.settings.custom_domain_entry

import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.scenes.settings.profile.data.ProfileUserData
import com.criptext.mail.validation.TextInput

class CustomDomainEntryModel: SceneModel{
    var newDomain: TextInput = TextInput.blank()
}