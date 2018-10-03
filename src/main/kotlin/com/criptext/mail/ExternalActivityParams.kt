package com.criptext.mail

sealed class ExternalActivityParams {
    data class FilePicker(val remaining: Int): ExternalActivityParams()
    data class ImagePicker(val remaining: Int): ExternalActivityParams()
    class Camera: ExternalActivityParams()
    class InviteFriend: ExternalActivityParams()
    class FilePresent(val filepath: String, val mimeType: String): ExternalActivityParams()
}