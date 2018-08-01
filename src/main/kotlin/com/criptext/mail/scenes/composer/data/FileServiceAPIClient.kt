package com.criptext.mail.scenes.composer.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.MultipartFormItem
import org.json.JSONObject


class FileServiceAPIClient(private val client: HttpClient, private val authToken: String) {

    fun registerFile(fileName: String, fileSize: Int, chunkSize: Int, totalChunks: Int): String {
        val json = JSONObject()
        json.put("filename", fileName)
        json.put("filesize", fileSize)
        json.put("chunkSize", chunkSize)
        json.put("totalChunks", totalChunks)

        return client.post(path = "/file/upload", authToken = authToken, body = json)
    }

    fun uploadChunk(chunk: ByteArray, fileName: String, part: Int, fileToken: String): String {
        val formBody = HashMap<String, MultipartFormItem>()
        formBody["chunk"] = MultipartFormItem.ByteArrayItem(fileName, chunk)
        formBody["part"] = MultipartFormItem.StringItem(part.toString())
        formBody["filetoken"] = MultipartFormItem.StringItem(fileToken)

        return client.post(path = "/file/chunk", authToken = authToken, body = formBody)
    }

    fun getFileMetadata(fileToken: String): String {
        return client.get(path = "/file/$fileToken", authToken = authToken)
    }

    fun downloadChunk(fileToken: String, part: Int): ByteArray {
        return client.getFile(path = "/file/$fileToken/chunk/$part", authToken = authToken)
    }

}