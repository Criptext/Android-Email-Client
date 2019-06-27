package com.criptext.mail.scenes.settings.profile

import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.scenes.settings.profile.data.ProfileUserData

class ProfileModel(var userData: ProfileUserData) : SceneModel {
    var comesFromMailbox = false
}