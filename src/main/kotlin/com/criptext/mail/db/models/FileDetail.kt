package com.criptext.mail.db.models

import com.criptext.mail.db.AttachmentTypes
import com.criptext.mail.utils.file.FileUtils

data class FileDetail(val file: CRFile){

    val fileKey: String
        get() = file.fileKey
    val name: String
        get() = file.name
    val token: String
        get() = file.token
    val status: Int
        get() = file.status
    val size: Long
        get() = file.size
    val type = FileUtils.getAttachmentTypeFromPath(file.name)
    var progress = -1

}