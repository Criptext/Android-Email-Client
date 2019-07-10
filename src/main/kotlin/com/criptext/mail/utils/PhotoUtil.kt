package com.criptext.mail.utils

import android.os.Environment
import java.io.File
import java.io.IOException
import java.util.*


interface PhotoUtil{
    var photoFile: File?
    fun getPhotoFileFromIntent(): File?
    fun getAlbumDir(): File?
    fun createImageFile(): File?

    class Default: PhotoUtil{
        override var photoFile: File? = null

        override fun getPhotoFileFromIntent(): File? {
            return if(photoFile != null) photoFile!!
            else null
        }

        override fun getAlbumDir(): File? {
            val path = Environment.getExternalStorageDirectory().toString() + PhotoUtil.ALBUM_DIR
            val storageDir = File(path)
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                if (! storageDir.mkdirs() && ! storageDir.exists())
                    return null
                return storageDir
            }
            return null
        }

        override fun createImageFile(): File? {
            // Create an image file name
            try {
                val timeStamp = DateAndTimeUtils.printDateWithServerFormat(Date(System.currentTimeMillis()))
                val imageFileName = "JPEG_" + timeStamp + "_"
                val storageDir = getAlbumDir()
                if (storageDir != null) {
                    val image = File.createTempFile(imageFileName, ".jpg")
                    photoFile = image
                    return image
                }
                return null
            } catch (e: IOException) {
                return null
            }
        }

    }

    companion object {
        val ALBUM_DIR = "/dcim/Criptext"
        val REQUEST_CODE_CAMERA = 1989
        val KEY_PHOTO_TAKEN = "PHOTO_TAKEN"
    }
}