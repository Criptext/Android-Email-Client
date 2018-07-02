package com.email.scenes.mailbox.data

import java.util.*

/**
 * Created by gabriel on 5/2/18.
 */
sealed class LoadParams {
    data class NewPage(val size: Int, val startDate: Date?): LoadParams()
    data class Reset(val size: Int): LoadParams()
}