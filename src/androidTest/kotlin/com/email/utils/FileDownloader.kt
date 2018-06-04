package com.email.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.nio.file.Files


object FileDownloader {
    fun download(url: String, targetFile: File) {
       val httpClient = OkHttpClient()
       val request = Request.Builder().url(url).build()
       val response = httpClient.newCall(request).execute()

       val byteStream = response.body()!!.byteStream()
        Files.copy(byteStream, targetFile.toPath())
    }
}