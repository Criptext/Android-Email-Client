package com.email.db.models

import com.email.db.AttachmentTypes
import com.email.utils.file.FileUtils

data class FileDetail(val file: CRFile){

    val name: String
        get() = file.name
    val token: String
        get() = file.token
    val size: Long
        get() = file.size
    val type = FileUtils.getAttachmentTypeFromPath(file.name)
    var progress = -1

}