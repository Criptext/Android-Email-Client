package com.criptext.mail.utils.file

import android.net.Uri
import com.criptext.mail.R
import droidninja.filepicker.FilePickerBuilder
import java.io.File


fun FilePickerBuilder.addExtensions(): FilePickerBuilder{
    val zipTypes = arrayOf(".zip", ".rar")
    val musicTypes = arrayOf(".mp3", ".ogg", ".wav", ".mp4", ".flac")
    this.addFileSupport("ZIP", zipTypes, R.drawable.zip)
    this.addFileSupport("AUDIO", musicTypes, R.drawable.audio)
    return this
}

fun Uri.toFile(): File {
    require(scheme == "file") { "Uri lacks 'file' scheme: $this" }
    return File(path)
}
