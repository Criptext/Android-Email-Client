package com.criptext.mail.scenes.composer.data

import com.criptext.mail.db.AttachmentTypes
import com.criptext.mail.utils.file.FileUtils
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

data class ComposerAttachment(val id: Long, val uuid: String, val filepath: String, var uploadProgress: Int,
                              var filetoken: String, val type: AttachmentTypes, var size: Long,
                              val fileKey: String, val cid: String?) {
    constructor(uuid: String, filepath: String, size: Long, fileKey: String): this (0, uuid ,filepath, -1, filetoken = "",
            type = FileUtils.getAttachmentTypeFromPath(filepath), size = size, fileKey = fileKey, cid = null)

    companion object{
        fun fromJSON(jsonString: String): ComposerAttachment{
            val json = JSONObject(jsonString)
            return ComposerAttachment(
                    filepath = json.getString("filepath"),
                    uuid = json.getString("uuid"),
                    size = json.getLong("size"),
                    fileKey = json.getString("fileKey")
            )
        }

        fun fromJSONArray(jsonString: String): List<ComposerAttachment> {
            val array = JSONArray(jsonString)
            val attachmentList = mutableListOf<ComposerAttachment>()
            for (i in 0 until array.length()){
                attachmentList.add(fromJSON(array.getJSONObject(i).toString()))
            }
            return attachmentList
        }

        fun toJSON(attachments: List<ComposerAttachment>): JSONArray {
            val array = JSONArray()
            attachments.forEach {
                val json = JSONObject()
                json.put("id", it.id)
                json.put("uuid", it.uuid)
                json.put("filepath", it.filepath)
                json.put("uploadProgress", it.uploadProgress)
                json.put("filetoken", it.filetoken)
                json.put("type", it.type.name)
                json.put("size", it.size)
                json.put("fileKey", it.fileKey)
                json.put("cid", it.cid)
                array.put(json)
            }
            return array
        }
    }

}