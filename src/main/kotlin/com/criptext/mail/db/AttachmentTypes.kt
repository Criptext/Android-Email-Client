package com.criptext.mail.db

import com.criptext.mail.R

/**
 * Created by sebas on 3/19/18.
 */


enum class AttachmentTypes {
    EXCEL, WORD, PDF, PPT, IMAGE, UNSEND, DEFAULT, AUDIO, VIDEO;

    fun getDrawableImage(): Int{
        return when(this){
            EXCEL -> R.drawable.xls
            WORD -> R.drawable.word
            PDF -> R.drawable.pdf
            PPT -> R.drawable.ppt
            IMAGE -> R.drawable.img
            UNSEND -> R.drawable.unsent
            DEFAULT -> R.drawable.generic
            AUDIO -> R.drawable.audio
            VIDEO -> R.drawable.video
        }
    }
}
