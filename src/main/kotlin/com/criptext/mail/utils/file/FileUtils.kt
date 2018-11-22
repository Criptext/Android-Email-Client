package com.criptext.mail.utils.file

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.criptext.mail.db.AttachmentTypes
import java.io.File
import java.net.URLConnection

/**
 * Anything you need to know about a file given its filename
 * Created by gesuwall on 5/2/17.
 */

class FileUtils {
    companion object {
        /**
         * The extension separator character.
         * @since Commons IO 1.4
         */
        val EXTENSION_SEPARATOR = '.'

        /**
         * The Unix separator character.
         */
        private val UNIX_SEPARATOR = '/'

        /**
         * Returns the index of the last directory separator character.
         *
         *
         * This method will handle a file in either Unix or Windows format.
         * The position of the last forward or backslash is returned.
         *
         *
         * The output will be the same irrespective of the machine that the code is running on.

         * @param filename  the filename to find the last path separator in, null returns -1
         * *
         * @return the index of the last separator character, or -1 if there
         * * is no such character
         */
        fun indexOfLastSeparator(filename: String?): Int {
            if (filename == null) {
                return -1
            }
            return filename.lastIndexOf(UNIX_SEPARATOR)
        }
        /**
         * Returns the index of the last extension separator character, which is a dot.
         *
         *
         * This method also checks that there is no directory separator after the last dot.
         * To do this it uses [.indexOfLastSeparator] which will
         * handle a file in either Unix or Windows format.
         *
         *
         * The output will be the same irrespective of the machine that the code is running on.

         * @param filename  the filename to find the last path separator in, null returns -1
         * *
         * @return the index of the last separator character, or -1 if there
         * * is no such character
         */
        private fun indexOfExtension(filename: String?): Int {
            if (filename == null) {
                return -1
            }
            val extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR)
            val lastSeparator = indexOfLastSeparator(filename)
            return if (lastSeparator > extensionPos) -1 else extensionPos
        }
        /**
         * Gets the extension of a filename.
         *
         *
         * This method returns the textual part of the filename after the last dot.
         * There must be no directory separator after the dot.
         *
         * foo.txt      --> "txt"
         * a/b/c.jpg    --> "jpg"
         * a/b.txt/c    --> ""
         * a/b/c        --> ""
         *
         *
         *
         * The output will be the same irrespective of the machine that the code is running on.

         * @param filename the filename to retrieve the extension of.
         * *
         * @return the extension of the file or an empty string if none exists.
         */
        fun getExtension(filename: String): String {
            val index = indexOfExtension(filename)
            return if (index == -1) {
                ""
            } else {
                filename.substring(index + 1)
            }
        }
        /**
         *
         * @param filename the filename to retrieve the extension of.
         * *
         * @return a pair with the basename and extension of the filename. The extension separator is
         * not included in neither string
         */
        fun getBasenameAndExtension(filename: String): Pair<String, String> {
            val index = indexOfExtension(filename)
            return if (index == -1) {
                Pair(filename, "")
            } else {
                Pair(filename.substring(0, index), filename.substring(index + 1))
            }
        }

        /**
         * Gets the name minus the path from a full filename.
         *
         *
         * This method will handle a file in either Unix or Windows format.
         * The text after the last forward or backslash is returned.
         *
         * a/b/c.txt --> c.txt
         * a.txt     --> a.txt
         * a/b/c     --> c
         * a/b/c/    --> ""
         *
         *
         *
         * The output will be the same irrespective of the machine that the code is running on.

         * @param filename  the filename to query, null returns null
         * *
         * @return the name of the file without the path, or an empty string if none exists
         */
        fun getName(filename: String): String {
            val index = indexOfLastSeparator(filename)
            return filename.substring(index + 1)
        }

        fun getFileTypeFromName(filename: String) : String{
            val extension = getExtension(filename)
            return when (extension.toLowerCase()) {
                "xlsx", "xls" -> "excel"
                "docx", "doc" -> "word"
                "pptx", "ppt" -> "ppt"
                "zip", "rar", "tar", "gz", "7z" -> "zip"
                "jpg", "jpeg", "png", "gif" -> "image"
                "mp4", "m4v", "ogv", "webm" -> "video"
                "mp3", "m4a", "ogg", "wav" -> "video"
                "pdf" -> "pdf"
                else -> "default"
            }
        }

        fun isAPicture(filename: String) = getFileTypeFromName(filename) == "image"

        fun getMimeType(filename: String): String {
            // There does not seem to be a way to ask the OS or file itself for this
            // information, so unfortunately resorting to extension sniffing.
            val ext = getExtension(filename)

            if (ext.equals("mp3", ignoreCase = true))
                return "audio/mpeg"
            if (ext.equals("aac", ignoreCase = true))
                return "audio/aac"
            if (ext.equals("wav", ignoreCase = true))
                return "audio/wav"
            if (ext.equals("ogg", ignoreCase = true))
                return "audio/ogg"
            if (ext.equals("mid", ignoreCase = true))
                return "audio/midi"
            if (ext.equals("midi", ignoreCase = true))
                return "audio/midi"
            if (ext.equals("wma", ignoreCase = true))
                return "audio/x-ms-wma"

            if (ext.equals("mp4", ignoreCase = true))
                return "video/mp4"
            if (ext.equals("avi", ignoreCase = true))
                return "video/x-msvideo"
            if (ext.equals("wmv", ignoreCase = true))
                return "video/x-ms-wmv"

            if (ext.equals("png", ignoreCase = true))
                return "image/png"
            if (ext.equals("jpg", ignoreCase = true))
                return "image/jpeg"
            if (ext.equals("jpe", ignoreCase = true))
                return "image/jpeg"
            if (ext.equals("jpeg", ignoreCase = true))
                return "image/jpeg"
            if (ext.equals("gif", ignoreCase = true))
                return "image/gif"

            if (ext.equals("xml", ignoreCase = true))
                return "text/xml"
            if (ext.equals("txt", ignoreCase = true))
                return "text/plain"
            if (ext.equals("cfg", ignoreCase = true))
                return "text/plain"
            if (ext.equals("csv", ignoreCase = true))
                return "text/plain"
            if (ext.equals("conf", ignoreCase = true))
                return "text/plain"
            if (ext.equals("rc", ignoreCase = true))
                return "text/plain"
            if (ext.equals("htm", ignoreCase = true))
                return "text/html"
            if (ext.equals("html", ignoreCase = true))
                return "text/html"

            if (ext.equals("pdf", ignoreCase = true))
                return "application/pdf"
            if (ext.equals("apk", ignoreCase = true))
                return "application/vnd.android.package-archive"

            // Additions and corrections are welcomed.
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
        }

        fun getAttachmentTypeFromPath(filepath: String): AttachmentTypes {
            val mimetype = getMimeType(filepath)
            val type = when {
                mimetype.contains("image") -> AttachmentTypes.IMAGE
                mimetype.contains("word") -> AttachmentTypes.WORD
                mimetype.contains("powerpoint") || mimetype.contains("presentation") -> AttachmentTypes.PPT
                mimetype.contains("excel") || mimetype.contains("sheet") -> AttachmentTypes.EXCEL
                mimetype.contains("pdf") -> AttachmentTypes.PDF
                mimetype.contains("audio") -> AttachmentTypes.AUDIO
                mimetype.contains("video") -> AttachmentTypes.VIDEO
                else -> AttachmentTypes.DEFAULT
            }
            return type
        }

        fun readableFileSize(size: Long): String{
            val unit = 1024
            if (size < unit) return "$size B"
            val exp = (Math.log(size.toDouble()) / Math.log(unit.toDouble())).toInt()
            val pre = ("KMGTPE")[exp - 1]
            return String.format("%.2f %sB", size / Math.pow(unit.toDouble(), exp.toDouble()), pre)
        }

        fun getPathAndSizeFromUri(uri: Uri, contentResolver: ContentResolver?,
                                            context: Context): Pair<String, Long>?{
            if(uri.toString().contains("com.google.android")){
                return Pair(uri.toString(), -1L)
            }else {
                contentResolver?.query(uri, null, null, null, null)?.use {
                    val absolutePath = PathUtil.getPath(context, uri)
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    it.moveToFirst()
                    if(absolutePath != null)
                        return Pair(absolutePath, it.getLong(sizeIndex))
                }
            }
            return null
        }
    }
}
