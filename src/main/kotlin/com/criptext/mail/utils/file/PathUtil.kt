package com.criptext.mail.utils.file

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import droidninja.filepicker.utils.FileUtils
import java.io.*
import java.net.URISyntaxException


object PathUtil {

    fun getPathFromImgSrc(src: String): String{
        return src.removePrefix("file:///")
    }

    /*
     * Gets the file path of the given Uri.
     */
    @Throws(URISyntaxException::class)
    fun getPath(context: Context, rawUri: Uri): String? {
        var uri = rawUri
        val needToCheckUri = Build.VERSION.SDK_INT >= 19
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.applicationContext, uri)) {
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    return Environment.getExternalStorageDirectory().absolutePath + "/" + split[1]
                }
                isDownloadsDocument(uri) -> {
                    val id = DocumentsContract.getDocumentId(uri)
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val name = context.contentResolver.query(uri, null, null, null, null)?.use {
                            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            it.moveToFirst()
                            it.getString(nameIndex)
                        }
                        val tempFilePath = context.cacheDir.absolutePath + "/" + name
                        val file = File(tempFilePath)
                        if(inputStream == null)
                            return null
                        writeFile(inputStream, file)
                        return file.absolutePath
                    }else {
                        uri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), id.toLong())
                    }
                }
                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]
                    if ("image" == type) {
                        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    selection = "_id=?"
                    selectionArgs = arrayOf(split[1])
                }
            }
        }
        if ("content".equals(uri.scheme!!, ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) {
                    return cursor.getString(columnIndex)
                }
                cursor.close()
            } catch (e: Exception) {
            }

        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
            return uri.path
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun getDataColumn(context: Context, uri: Uri, selection: String?,
                      selectionArgs: Array<String>?): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor!!.moveToFirst()) {
                val index = cursor!!.getColumnIndexOrThrow(column)
                val path =  cursor!!.getString(index)
                return path
            }
        } finally {
            if (cursor != null)
                cursor!!.close()
        }
        return null
    }

    private fun writeFile(`in`: InputStream, file: File) {
        var out: OutputStream? = null
        try {
            out = FileOutputStream(file)
            val buf = ByteArray(1024)
            var len: Int = `in`.read(buf)
            while (len > 0) {
                out.write(buf, 0, len)
                len = `in`.read(buf)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
                `in`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    private fun getDownloadPathFromUri(uri: Uri): String{
        return if(uri.toString().contains("android.externalstorage")){
            val filePath = removeColon(File(uri.path).absolutePath)
            if(filePath.startsWith("/"))
                Environment.getExternalStorageDirectory().absolutePath + filePath
            else
                Environment.getExternalStorageDirectory().absolutePath + "/" + filePath
        }else{
            removeColon(File(uri.path).absolutePath)
        }
    }

    private fun removeColon(path: String): String{
        return if (path.contains(":")) {
            path.substring(path.indexOf(":") + 1)
        } else {
            path
        }
    }
}