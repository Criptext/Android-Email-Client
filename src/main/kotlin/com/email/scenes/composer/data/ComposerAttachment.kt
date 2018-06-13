package com.email.scenes.composer.data

import com.email.db.AttachmentTypes
import com.email.utils.Utility

data class ComposerAttachment(val filepath: String, var uploadProgress: Int,
                              var filetoken: String, val type: AttachmentTypes, var size: Long) {
    constructor(filepath: String): this (filepath, -1, filetoken = "",
            type = Utility.getAttachmentTypeFromPath(filepath), size = 0L)
}