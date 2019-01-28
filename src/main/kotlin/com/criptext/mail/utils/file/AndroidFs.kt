package com.criptext.mail.utils.file

import android.content.Context
import android.os.Environment
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Methods for working with the android file system.
 * Created by gabriel on 7/3/17.
 */

class AndroidFs {

    companion object {
        private val IMAGE_DIR_NAME = "images"
        private val DOWNLOAD_DIR_NAME = "downloads"

        fun getFileFromDownloadsDir(filename: String): File {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, filename)
            return file
        }

        fun getEmailPathFromAppDir(filename: String, recipientId: String, fileDir: String, metadataKey: Long): File {
            val downloadsDir = "$fileDir/$recipientId/emails/$metadataKey/"
            val file = File(downloadsDir, filename)
            return file
        }

        fun fileExistsInDownloadsDir(filename: String, fileSize: Long): Boolean {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, filename)
            return file.exists() && file.length() == fileSize
        }

        fun fileExistsInDownloadsDir(filename: String): Boolean {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, filename)
            return file.exists()
        }

        fun fileExistsInAppDir(filePath: String): Boolean {
            val file = File(filePath)
            return file.exists()
        }

        fun fileExistsInAppDir(filename: String, recipientId: String, fileDir: String, metadataKey: Long, fileSize: Long): Boolean {
            val downloadsDir = "$fileDir/$recipientId/emails/$metadataKey/"
            val file = File(downloadsDir, filename)
            return file.exists() && file.length() == fileSize
        }

        fun writeByteArraysToFile(file: File, content: ByteArray) {
            val writer = BufferedOutputStream(FileOutputStream(file))
            writer.write(content)
            writer.flush()
            writer.close()
        }

        private fun getSubdirectory(parentDirectory: File, subDirectoryName: String): File {
            val subdirectory = File(parentDirectory, subDirectoryName)

            val mkSuccess = subdirectory.mkdir()
            if (!mkSuccess && !subdirectory.exists())
                throw IOException("Could not initialize subdirectory: $subDirectoryName inside $parentDirectory")

            return subdirectory
        }

        fun getImagesCacheDir(ctx: Context): File = getSubdirectory(ctx.cacheDir,
                "/$IMAGE_DIR_NAME")

        fun getDownloadsCacheDir(ctx: Context): File = getSubdirectory(ctx.cacheDir,
                "/$DOWNLOAD_DIR_NAME")

        fun getFileFromImageCache(ctx: Context, filename: String):File {
            val dir = getImagesCacheDir(ctx)
            return File(dir, filename)

        }
    }

}
