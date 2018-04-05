package com.email.scenes.emailDetail.mocks

import com.email.db.EmailDetailLocalDB
import com.email.db.models.Email
import com.email.db.models.FullEmail

/**
 * Created by sebas on 3/29/18.
 */

class MockedEmailDetailLocalDB: EmailDetailLocalDB {

    override fun getFullEmailsFromThreadId(threadId: String): List<FullEmail> {
        return nextLoadedEmailItems!!
    }

    override fun unsendEmail(emailId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var nextLoadedEmailItems: List<FullEmail>? = null


}
