package com.criptext.mail.db

/**
 * Created by danieltigse on 5/28/18.
 */

enum class LabelTypes{
    SYSTEM, CUSTOM;

    companion object {
        fun getLabelType(ordinal: Int): LabelTypes{
            return when(ordinal){
                0 -> SYSTEM
                else -> CUSTOM
            }
        }
    }
}