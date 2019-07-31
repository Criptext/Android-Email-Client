package com.criptext.mail.scenes.settings

class SettingsModel(var hasChangedTheme: Boolean = false){
    var showEmailPreview: Boolean = false

    var isWaitingForSync = false

    var retryTimeLinkStatus = 0
}