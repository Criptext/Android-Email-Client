package com.email.scenes.mailbox.data

/**
 * Created by gabriel on 5/2/18.
 */
sealed class LoadParams {
    data class NewPage(val size: Int, val oldestEmailThread: EmailThread?): LoadParams()
    data class Reset(val size: Int): LoadParams()
}