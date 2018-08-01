package com.criptext.mail.scenes.settings.data

import com.criptext.mail.db.models.Label

sealed class SettingsResult{

    sealed class CreateCustomLabel: SettingsResult() {
        data class Success(val label: Label): CreateCustomLabel()
        class Failure: CreateCustomLabel()
    }

    sealed class ChangeContactName : SettingsResult() {
        data class Success(val fullName: String): ChangeContactName()
        class Failure: ChangeContactName()
    }

    sealed class GetCustomLabels : SettingsResult() {
        data class Success(val labels: List<Label>): GetCustomLabels()
        class Failure: GetCustomLabels()
    }

    sealed class ChangeVisibilityLabel : SettingsResult() {
        class Success: ChangeVisibilityLabel()
        class Failure: ChangeVisibilityLabel()
    }

}