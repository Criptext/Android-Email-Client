package com.criptext.mail.utils.file

import com.criptext.mail.R
import droidninja.filepicker.FilePickerBuilder


fun FilePickerBuilder.addExtensions(): FilePickerBuilder{
    val zipTypes = arrayOf(".zip", ".rar")
    val musicTypes = arrayOf(".mp3", ".ogg", ".wav", ".mp4", ".flac")
    this.addFileSupport("ZIP", zipTypes, R.drawable.zip)
    this.addFileSupport("AUDIO", musicTypes, R.drawable.audio)
    return this
}
