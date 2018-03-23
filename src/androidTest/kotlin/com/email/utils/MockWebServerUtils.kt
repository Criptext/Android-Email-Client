package com.email.utils

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

/**
 * Created by gabriel on 3/22/18.
 */

fun MockWebServer.enqueueSuccessfulResponses(responses: List<String>) {
    responses.forEach { resBody ->
        this.enqueue(MockResponse().setResponseCode(200).setBody(resBody))
    }
}