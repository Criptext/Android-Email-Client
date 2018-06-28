package com.email

sealed class ExternalActivityParams {
    class FilePicker: ExternalActivityParams()
    class FilePresent(val filepath: String, val mimeType: String): ExternalActivityParams()
}