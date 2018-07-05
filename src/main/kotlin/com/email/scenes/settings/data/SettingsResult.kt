package com.email.scenes.settings.data

import com.email.db.models.Label

sealed class SettingsResult{

    sealed class ChangeContactName : SettingsResult() {
        data class Success(val fullName: String): ChangeContactName()
        class Failure: ChangeContactName()
    }

    sealed class GetCustomLabels : SettingsResult() {
        data class Success(val labels: List<Label>): GetCustomLabels()
        class Failure: GetCustomLabels()
    }

}