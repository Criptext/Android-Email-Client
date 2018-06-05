package com.email.scenes.composer.data

import com.email.api.HttpClient
import com.email.api.models.MultipartFormItem
import com.gaumala.kotlinsnapshot.Camera
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import org.json.JSONObject
import org.junit.Before
import org.junit.Test

/**
 * Created by gabriel on 6/5/18.
 */

class FileServiceAPIClientTest {

    lateinit var httpClient: HttpClient
    lateinit var fileServiceClient: FileServiceAPIClient
    val camera = Camera()

    @Before
    fun setup() {
        httpClient = mockk()
        fileServiceClient = FileServiceAPIClient(client = httpClient, authToken = "__TOKEN__")

    }

    @Test
    fun `should send request to register file with correct shape`() {
        val bodySlot = CapturingSlot<JSONObject>()
        every {
            httpClient.post("/file/upload", authToken = "__TOKEN__", body = capture(bodySlot))
        } returns "Ok"

        fileServiceClient.registerFile(fileName = "my_photo.png", fileSize =
        1024, chunkSize = 512, totalChunks = 2)

        camera.matchWithSnapshot("should send request to register file with correct shape",
                bodySlot.captured.toString(4))
    }

    @Test
    fun `should send request to upload chunk with correct shape`() {
        val bodySlot = CapturingSlot<Map<String, MultipartFormItem>>()
        every {
            httpClient.post("/file/chunk", authToken = "__TOKEN__", body = capture(bodySlot))
        } returns "Ok"

        fileServiceClient.uploadChunk(chunk = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 127),
                fileName = "my_photo.png", fileToken = "__FILE_TOKEN__", part = 2)

        camera.matchWithSnapshot("should send request to upload chunk with correct shape",
                bodySlot.captured.toString())
    }
}