package com.criptext.mail.scenes.emaildetail.data

import com.criptext.mail.api.HttpClient
import com.gaumala.kotlinsnapshot.Camera
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import org.json.JSONObject
import org.junit.Before
import org.junit.Test

/**
 * Created by gabriel on 6/27/18.
 */
class EmailDetailAPIClientTest {

    private lateinit var httpClient: HttpClient
    private lateinit var apiClient: EmailDetailAPIClient
    private val camera = Camera()

    @Before
    fun setup() {
        httpClient = mockk()
        apiClient = EmailDetailAPIClient(httpClient = httpClient, authToken = "__TOKEN__")

    }

    @Test
    fun `should send request to post open events with correct shape`() {
        val bodySlot = CapturingSlot<JSONObject>()
        every {
            httpClient.post("/event/open", authToken = "__TOKEN__", body = capture(bodySlot)).body
        } returns "Ok"

        apiClient.postOpenEvent(metadataKeys = listOf(1L, 3L, 7L))

        camera.matchWithSnapshot("should send request to post open events with correct shape",
                bodySlot.captured.toString(4))
    }
}