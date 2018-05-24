package com.email.db.models

import org.amshove.kluent.`should equal`
import org.junit.Test

/**
 * Created by gabriel on 3/22/18.
 */

class ActiveAccountTest {

    @Test
    fun `serialize to JSON and deserialize from JSON`() {
        val original = ActiveAccount(recipientId = "gabriel", deviceId = 3, jwt = "gI9Y4mXsww31qT")
        val serialized = original.toJSON().toString()
        val deserialized = ActiveAccount.fromJSONString(serialized)

        deserialized `should equal` original

    }
}