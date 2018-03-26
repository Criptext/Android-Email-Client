package com.email.utils

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.amshove.kluent.shouldBeGreaterThan
import org.amshove.kluent.shouldEndWith
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldStartWith
import org.json.JSONObject

/**
 * Created by gabriel on 3/22/18.
 */

data class ExpectedRequest(val needsJwt: Boolean, val method: String, val path: String, val assertBodyFn: ((JSONObject) -> Unit)?)

fun MockWebServer.enqueueSuccessfulResponses(responses: List<String>) {
    responses.forEach { resBody ->
        this.enqueue(MockResponse().setResponseCode(200).setBody(resBody))
    }
}

private fun parseJSONRequestBody(recordedRequest: RecordedRequest): JSONObject {
    val bodyString = recordedRequest.body.readUtf8()
    return JSONObject(bodyString)
}

private fun assertRequestContainsJwt(recordedRequest: RecordedRequest) {
    val authorizationHeader = recordedRequest.getHeader("Authorization")
    authorizationHeader shouldStartWith "Bearer "
    authorizationHeader.length shouldBeGreaterThan 10
}

fun MockWebServer.assertSentRequests(expectedRequests: List<ExpectedRequest>) {
    expectedRequests.forEach { expectedRequest ->
        val recordedRequest = this.takeRequest()
        recordedRequest.path shouldEndWith expectedRequest.path
        recordedRequest.method shouldEqual expectedRequest.method

        if (expectedRequest.needsJwt) assertRequestContainsJwt(recordedRequest)

        val assertFn = expectedRequest.assertBodyFn
        if (assertFn != null) assertFn(parseJSONRequestBody(recordedRequest))
    }
}