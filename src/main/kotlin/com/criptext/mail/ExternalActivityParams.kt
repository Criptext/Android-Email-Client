package com.criptext.mail

sealed class ExternalActivityParams {
    class FilePicker: ExternalActivityParams()
    class FilePresent(val filepath: String, val mimeType: String): ExternalActivityParams()
}