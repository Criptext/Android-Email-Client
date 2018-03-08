package com.email.scenes.signup.data

import com.email.api.ApiCall
import com.email.signal.PreKeyBundleShareData
import com.email.api.ServerErrorException
import com.email.scenes.signup.IncompleteAccount
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Created by sebas on 2/26/18.
 */

interface SignUpAPIClient {

    fun createUser(
            account: IncompleteAccount,
            recipientId: String,
            keybundle : PreKeyBundleShareData.UploadBundle)
            : String

    class Default : SignUpAPIClient {
        private val client = OkHttpClient().
                newBuilder().
                connectTimeout(20, TimeUnit.SECONDS).
                readTimeout(20, TimeUnit.SECONDS).
                build()

        override fun createUser(
                account: IncompleteAccount,
                recipientId: String,
                keybundle : PreKeyBundleShareData.UploadBundle
        ): String {
            val request = ApiCall.createUser(
                    recipientId = recipientId,
                    name = account.name,
                    password = account.password,
                    recoveryEmail = account.recoveryEmail,
                    keyBundle = keybundle
            )
            val response = client.newCall(request).execute()
            if(!response.isSuccessful) throw(ServerErrorException(response.code()))
            return response.message()
        }
    }
}
