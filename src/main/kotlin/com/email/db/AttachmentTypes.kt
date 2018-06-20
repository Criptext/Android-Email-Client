package com.email.db

import com.email.R

/**
 * Created by sebas on 3/19/18.
 */


enum class AttachmentTypes {
    EXCEL, WORD, PDF, PPT, IMAGE, DEFAULT;

    fun getDrawableImage(): Int{
        return when(this){
            EXCEL -> R.drawable.xls
            WORD -> R.drawable.word
            PDF -> R.drawable.pdf
            PPT -> R.drawable.ppt
            IMAGE -> R.drawable.img
            DEFAULT -> R.drawable.generic
        }
    }
}
