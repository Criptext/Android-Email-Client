package com.criptext.mail.utils.file

import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.junit.AfterClass
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.util.*

class ChunkFileReaderTest {

    private fun createTestTextFile(numberOfChars: Int) {
        val file = File(testFilePath)
        val outputStream = FileOutputStream(file)

        val textToWrite = String(CharArray(numberOfChars, { _ -> 'a'}))
        file.writeText(textToWrite, Charset.defaultCharset())
        outputStream.close()
    }

    @Test
    fun `should read the file completely with readByChunks()`() {
        val numberOfCharsWritten = 2_000
        val chunkSize = 512
        createTestTextFile(numberOfCharsWritten)
        val expectedFileContents = File(testFilePath).readText()
        val readCharacters = StringBuilder("")


        val onNewChunkRead: (ByteArray, Int) -> Unit  = { newChunk, _ ->
            val readText = newChunk.toString(Charset.defaultCharset())
            readCharacters.append(readText)
        }

        ChunkFileReader.read(file = File(testFilePath), chunkSize = chunkSize,
                onNewChunkRead = onNewChunkRead)

        readCharacters.toString() `should equal` expectedFileContents
    }

    @Test
    fun `readByChunks() should invoke the callback with sequential indexes`() {
        val numberOfCharsWritten = 2_000
        val chunkSize = 512
        createTestTextFile(numberOfCharsWritten)

        val receivedIndexes = mutableListOf<Int>()

        val onNewChunkRead: (ByteArray, Int) -> Unit = { _: ByteArray, index: Int ->
            receivedIndexes.add(index)
        }

        ChunkFileReader.read(file = File(testFilePath), chunkSize = chunkSize,
                onNewChunkRead = onNewChunkRead)

        receivedIndexes `should equal` mutableListOf(0, 1, 2, 3)
    }

    @Test
    fun should_identify_file_type_by_mime_type(){
        FileUtils.getExtension(testFilePath) `should equal` "txt"
        FileUtils.getExtension(testImagePath) `should equal` "png"
        FileUtils.getExtension(testVideoPath) `should equal` "mp4"
        FileUtils.getExtension(testAudioPath) `should equal` "mp3"

        FileUtils.getMimeType(testFilePath) `should equal` "text/plain"
        FileUtils.getMimeType(testImagePath) `should equal` "image/png"
        FileUtils.getMimeType(testVideoPath) `should equal` "video/mp4"
        FileUtils.getMimeType(testAudioPath) `should equal` "audio/mpeg"
    }

    companion object {
        private const val testFilePath = "./build/tmp/test.txt"
        private const val testImagePath = "./build/tmp/image.png"
        private const val testVideoPath = "./build/tmp/video.mp4"
        private const val testAudioPath = "./build/tmp/audio.mp3"
        @AfterClass
        fun tearDown() {
            File(testFilePath).delete()
        }
    }
}