package com.criptext.mail.scenes.settings.labels.data

import com.criptext.mail.db.models.Label

sealed class LabelsResult{

    sealed class GetCustomLabels : LabelsResult() {
        data class Success(val labels: List<Label>): GetCustomLabels()
        class Failure: GetCustomLabels()
    }

    sealed class ChangeVisibilityLabel : LabelsResult() {
        class Success: ChangeVisibilityLabel()
        class Failure: ChangeVisibilityLabel()
    }

    sealed class CreateCustomLabel: LabelsResult() {
        data class Success(val label: Label): CreateCustomLabel()
        class Failure: CreateCustomLabel()
    }

    sealed class DeleteCustomLabel: LabelsResult() {
        data class Success(val uuid: String): DeleteCustomLabel()
        class Failure: DeleteCustomLabel()
    }

    sealed class EditCustomLabel: LabelsResult() {
        data class Success(val uuid: String, val newName: String): EditCustomLabel()
        class Failure: EditCustomLabel()
    }
}