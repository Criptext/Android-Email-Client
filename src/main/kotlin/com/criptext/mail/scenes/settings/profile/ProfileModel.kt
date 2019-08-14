package com.criptext.mail.scenes.settings.profile

import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.scenes.settings.profile.data.ProfileUserData

class ProfileModel(val comesFromMailbox: Boolean) : SceneModel {
    var userData: ProfileUserData = ProfileUserData()
    var criptextFooterEnabled: Boolean = true
}