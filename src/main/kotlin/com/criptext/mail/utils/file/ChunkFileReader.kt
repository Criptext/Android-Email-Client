package com.criptext.mail.utils.file

import java.io.File
import java.io.FileInputStream


object ChunkFileReader {

    /**
     * Read a file by chunks of the specified size.
     * @param file The file to read
     * @param chunkSize The size in bytes of each chunk
     * @param onNewChunkRead a callback to execute every time a new chunk is read. The first
     * parameter is the byte array with the read chunk, it should always be of size chunkSize
     * except for the last chunk which may be smaller. The second parameter is then index of the
     * chunk.
     */
    fun read(file: File, chunkSize: Int, onNewChunkRead: (ByteArray, Int) -> Unit) {
        val length = file.length()
        val inputStream = FileInputStream(file)
        val totalChunks = length / chunkSize + 1
        var index = 0
        val newChunkBuffer = ByteArray(chunkSize)

        while (index < totalChunks) {
            val readBytes = inputStream.read(newChunkBuffer, 0, chunkSize)
            if (readBytes in 1..(chunkSize - 1)) {
                val lastChunk = newChunkBuffer.copyOf(readBytes)
                onNewChunkRead(lastChunk, index)
                break // last chunk got processed so we exit
            } else if (readBytes == chunkSize)
                onNewChunkRead(newChunkBuffer, index)
            index += 1
        }

        inputStream.close()
    }
}