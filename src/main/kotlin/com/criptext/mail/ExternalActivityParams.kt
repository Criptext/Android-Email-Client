package com.criptext.mail


sealed class ExternalActivityParams {
    data class FilePicker(val remaining: Int): ExternalActivityParams()
    class ProfileImagePicker: ExternalActivityParams()
    data class ImagePicker(val remaining: Int): ExternalActivityParams()
    data class PinScreen(val isFirstTime: Boolean): ExternalActivityParams()
    class Camera: ExternalActivityParams()
    class InviteFriend: ExternalActivityParams()
    class OpenGooglePlay: ExternalActivityParams()
    class FilePresent(val filepath: String, val mimeType: String): ExternalActivityParams()

    companion object {
        const val PIN_REQUEST_CODE = 2018
    }
}