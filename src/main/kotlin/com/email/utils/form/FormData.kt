package com.email.utils.form

import com.email.utils.UIMessage

/**
 * Created by gabriel on 5/15/18.
 */

sealed class FormData<out T>{
    data class Valid<out T>(val value: T): FormData<T>()
    data class Error<out T>(val message: UIMessage): FormData<T>()
}