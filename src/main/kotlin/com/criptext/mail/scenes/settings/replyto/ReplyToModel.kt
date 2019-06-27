package com.criptext.mail.scenes.settings.replyto

import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.scenes.settings.profile.data.ProfileUserData

class ReplyToModel(var userData: ProfileUserData): SceneModel{
    var newReplyToEmail = ""
    var comesFromMailbox = false
}