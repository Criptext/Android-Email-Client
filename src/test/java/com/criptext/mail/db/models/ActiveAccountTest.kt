package com.criptext.mail.db.models

import com.criptext.mail.db.AccountTypes
import org.amshove.kluent.`should equal`
import org.junit.Test

/**
 * Created by gabriel on 3/22/18.
 */

class ActiveAccountTest {

    @Test
    fun `serialize to JSON and deserialize from JSON`() {
        val original = ActiveAccount(name = "Gabriel", recipientId = "gabriel", deviceId = 3,
                jwt = "gI9Y4mXsww31qT", signature = "", refreshToken = "gI9Y4mXsww31qT", id = 1,
                domain = Contact.mainDomain, type = AccountTypes.STANDARD)
        val serialized = original.toJSON().toString()
        val deserialized = ActiveAccount.fromJSONString(serialized)

        deserialized `should equal` original

    }
}