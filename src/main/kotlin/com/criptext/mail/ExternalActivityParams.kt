package com.criptext.mail


sealed class ExternalActivityParams {
    class FilePicker: ExternalActivityParams()
    class ProfileImagePicker: ExternalActivityParams()
    class ImagePicker: ExternalActivityParams()
    data class PinScreen(val isFirstTime: Boolean): ExternalActivityParams()
    class Camera: ExternalActivityParams()
    class InviteFriend: ExternalActivityParams()
    class OpenGooglePlay: ExternalActivityParams()
    class FilePresent(val filepath: String, val mimeType: String): ExternalActivityParams()

    companion object {
        const val PIN_REQUEST_CODE = 2018
    }
}