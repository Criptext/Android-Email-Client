package com.email.scenes.composer.data

data class ComposerAttachment(val filepath: String, var uploadProgress: Int, var filetoken: String) {
    constructor(filepath: String): this (filepath, -1, filetoken = "")
}