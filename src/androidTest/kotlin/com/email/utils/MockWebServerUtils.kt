package com.email.utils

import com.email.api.HttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.amshove.kluent.shouldBeGreaterThan
import org.amshove.kluent.shouldEndWith
import org.amshove.kluent.shouldEqual

/**
 * Created by gabriel on 3/22/18.
 */

data class ExpectedRequest(val expectedAuthScheme: ExpectedAuthScheme, val method: String, val path: String,
                           val assertBodyFn: ((String) -> Unit)?)

sealed class MockedResponse {
    class Ok(val body: String): MockedResponse()
    class ServerError: MockedResponse()
    class Timeout: MockedResponse()

}

fun MockWebServer.enqueueResponses(responses: List<MockedResponse>) {
    responses.forEach { res ->
        when (res) {
            is MockedResponse.Ok ->
                this.enqueue(MockResponse().setResponseCode(200).setBody(res.body))
            is MockedResponse.ServerError ->
                this.enqueue(MockResponse().setResponseCode(500).setBody("simulated server crash"))
            is MockedResponse.Timeout ->
                this.enqueue(MockResponse().setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.NO_RESPONSE))
        }
    }
}

private fun stringifyRequestBody(recordedRequest: RecordedRequest): String {
    return recordedRequest.body.readUtf8()
}

private fun assertRequestAuthorization(scheme: ExpectedAuthScheme, recordedRequest: RecordedRequest) {
    when (scheme) {
        is ExpectedAuthScheme.Jwt -> {
            val authorizationHeader = recordedRequest.getHeader("Authorization")
            authorizationHeader shouldEqual "Bearer ${scheme.token}"
        }
        is ExpectedAuthScheme.Basic -> {
            val authorizationHeader = recordedRequest.getHeader("Authorization")
            authorizationHeader shouldEqual "Basic ${scheme.token}"
        }
    }
}

fun MockWebServer.assertSentRequests(expectedRequests: List<ExpectedRequest>) {
    expectedRequests.forEachIndexed { i, expectedRequest ->
        try {
            val recordedRequest = this.takeRequest(0, java.util.concurrent.TimeUnit.HOURS)
            recordedRequest.path shouldEndWith expectedRequest.path
            recordedRequest.method shouldEqual expectedRequest.method
            assertRequestAuthorization(expectedRequest.expectedAuthScheme, recordedRequest)

            val assertFn = expectedRequest.assertBodyFn
            if (assertFn != null) assertFn(stringifyRequestBody(recordedRequest))
        } catch (e: Exception) {
            throw RequestAssertionException(pos = i, req = expectedRequest, throwable = e)
        }
    }
}

sealed class ExpectedAuthScheme {
    class None: ExpectedAuthScheme()
    data class Jwt(val token: String): ExpectedAuthScheme()
    data class Basic(val token: String): ExpectedAuthScheme()
}

class RequestAssertionException(pos: Int, req: ExpectedRequest, throwable: Throwable): Exception(
        "Failed to assert sent request $req at position $pos of input list.",  throwable)
