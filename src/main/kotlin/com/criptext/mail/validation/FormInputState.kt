package com.criptext.mail.validation

import com.criptext.mail.utils.UIMessage

/**
 * Created by gabriel on 5/16/18.
 */
sealed class FormInputState {
    class Valid: FormInputState() {
        override fun equals(other: Any?): Boolean = other is Valid

        override fun hashCode(): Int = 1
    }
    data class Error(val message: UIMessage): FormInputState()
    class Unknown: FormInputState() {

        override fun equals(other: Any?): Boolean = other is Unknown

        override fun hashCode(): Int = 1
    }
}