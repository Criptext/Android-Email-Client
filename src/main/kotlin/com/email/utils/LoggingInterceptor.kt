package com.email.utils

import com.squareup.okhttp.internal.Internal.logger
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException



/**
 * Created by gabriel on 3/23/18.
 */
class LoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val t1 = System.nanoTime()
        logger.info(String.format("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()))

        val response = chain.proceed(request)

        val t2 = System.nanoTime()
        logger.info(String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6, response.headers()))

        return response
    }

    companion object {
        private fun bodyToString(request: Request): String {
            return try {
                val copy = request.newBuilder().build()
                val buffer = Buffer()
                copy.body()!!.writeTo(buffer)
                buffer.readUtf8()
            } catch (e: IOException) {
                "did not work"
            }

        }
    }
}
