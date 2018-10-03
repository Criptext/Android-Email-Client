package com.criptext.mail.api

import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.internal.Util
import okio.BufferedSink
import okio.Okio
import okio.Source
import java.io.File
import java.io.FileInputStream


class StreamRequest(private val contentType: MediaType?, filePath: String): RequestBody() {
    private val file = File(filePath)
    private val inputStream = FileInputStream(file)

    override fun contentType(): MediaType? {
        return contentType
    }

    override fun contentLength(): Long {
        return file.length()
    }

    override fun writeTo(sink: BufferedSink) {
        var source: Source? = null
        try {
            source = Okio.source(inputStream)
            sink.writeAll(source!!)
        } finally {
            Util.closeQuietly(source)
            inputStream.close()
        }
    }
}
