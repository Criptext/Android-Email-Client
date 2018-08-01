package com.criptext.mail.bgworker

/**
 * Created by gabriel on 2/19/18.
 */
sealed class WorkState<out V: Any> {
    class Working<out V: Any>: WorkState<V>()
    data class Done<out V: Any>(val result: V): WorkState<V>()
}
