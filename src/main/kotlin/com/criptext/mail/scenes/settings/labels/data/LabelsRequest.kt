package com.criptext.mail.scenes.settings.labels.data

sealed class LabelsRequest{
    class GetCustomLabels: LabelsRequest()
    data class CreateCustomLabel(val labelName: String): LabelsRequest()
    data class ChangeVisibilityLabel(val labelId: Long, val isVisible: Boolean): LabelsRequest()
    data class DeleteCustomLabel(val labelUUID: String): LabelsRequest()
    data class EditCustomLabel(val labelUUID: String, val newName: String): LabelsRequest()
}