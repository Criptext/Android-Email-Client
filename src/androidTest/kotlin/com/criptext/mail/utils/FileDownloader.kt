package com.criptext.mail.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files

object FileDownloader {
    fun download(url: String, targetFile: File) {
        val httpClient = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = httpClient.newCall(request).execute()

        val byteStream = response.body()!!.byteStream()
        val fileStream = FileOutputStream(targetFile)

        byteStream.use { input ->
            fileStream.use { fileOut ->
                input.copyTo(fileOut)
            }
        }

        fileStream.close()
    }
}