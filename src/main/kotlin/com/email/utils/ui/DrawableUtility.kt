package com.email.utils.ui

import com.email.R
import com.email.db.AttachmentTypes

class DrawableUtility {
    companion object {
        fun getDrawableAttachmentFromType(type: AttachmentTypes) = when (type) {
            AttachmentTypes.EXCEL ->
                R.drawable.xls

            AttachmentTypes.WORD ->
                R.drawable.word

            AttachmentTypes.PDF ->
                R.drawable.pdf

            AttachmentTypes.PPT ->
                R.drawable.ppt

            AttachmentTypes.IMAGE ->
                R.drawable.img

            AttachmentTypes.DEFAULT ->
                R.drawable.generic
        }
    }
}