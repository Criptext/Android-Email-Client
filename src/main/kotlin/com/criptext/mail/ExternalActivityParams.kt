package com.criptext.mail

sealed class ExternalActivityParams {
    class FilePicker: ExternalActivityParams()
    class ImagePicker: ExternalActivityParams()
    class Camera: ExternalActivityParams()
    class FilePresent(val filepath: String, val mimeType: String): ExternalActivityParams()
}