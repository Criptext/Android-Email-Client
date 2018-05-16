package com.email.utils.form

/**
 * Created by gabriel on 5/17/18.
 */
data class TextInput(val value: String, val state: FormInputState) {
    companion object {
        fun blank() = TextInput("", FormInputState.Unknown())
    }
}