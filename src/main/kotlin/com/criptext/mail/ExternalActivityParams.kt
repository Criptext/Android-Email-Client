package com.criptext.mail


sealed class ExternalActivityParams {
    class FilePicker: ExternalActivityParams()
    class ProfileImagePicker: ExternalActivityParams()
    class ImagePicker: ExternalActivityParams()
    data class PinScreen(val isFirstTime: Boolean): ExternalActivityParams()
    class Camera: ExternalActivityParams()
    class InviteFriend: ExternalActivityParams()
    data class ShareFile(val filePath: String): ExternalActivityParams()
    class SignInGoogleDrive: ExternalActivityParams()
    class SignOutGoogleDrive: ExternalActivityParams()
    class ChangeAccountGoogleDrive: ExternalActivityParams()
    class OpenGooglePlay: ExternalActivityParams()
    class FilePresent(val filepath: String, val mimeType: String): ExternalActivityParams()

    companion object {
        const val PIN_REQUEST_CODE = 2018
        const val REQUEST_CODE_SIGN_IN = 2019
    }
}