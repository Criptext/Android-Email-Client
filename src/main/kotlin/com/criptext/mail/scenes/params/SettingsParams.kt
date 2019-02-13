package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.settings.SettingsActivity

class SettingsParams(val hasChangedTheme: Boolean = false): SceneParams(){
    override val activityClass = SettingsActivity::class.java
}