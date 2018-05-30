package com.email.scenes.composer.data

data class ComposerAttachment(val filepath: String, var uploadProgress: Int) {
    constructor(filepath: String): this (filepath, -1)
}