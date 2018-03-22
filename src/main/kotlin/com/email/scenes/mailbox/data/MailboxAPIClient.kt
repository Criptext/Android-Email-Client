package com.email.scenes.mailbox.data

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Created by sebas on 3/20/18.
 */


interface MailboxAPIClient {

    class Default : MailboxAPIClient {
        private val client = OkHttpClient().
                newBuilder().
                connectTimeout(20, TimeUnit.SECONDS).
                readTimeout(20, TimeUnit.SECONDS).
                build()

    }
}
