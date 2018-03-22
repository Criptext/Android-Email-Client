package com.email.scenes.emaildetail.data

/**
 * Created by sebas on 3/12/18.
 */

sealed class EmailDetailRequest{

    class LoadFullEmailsFromThreadId(
            val threadId: String): EmailDetailRequest()

    class UnsendFullEmailFromEmailId(
            val emailId: Int, val position: Int): EmailDetailRequest()
}
