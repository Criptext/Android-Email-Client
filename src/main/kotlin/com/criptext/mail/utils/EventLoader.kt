package com.criptext.mail.utils

import com.criptext.mail.api.HttpResponseData
import com.criptext.mail.api.models.Event
import com.criptext.mail.scenes.mailbox.data.MailboxAPIClient
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import org.json.JSONArray

object EventLoader{

    fun getEvents(apiClient: MailboxAPIClient): Result<Pair<List<Event>, Boolean>, Exception>{
        return fetchPendingEvents(apiClient)
                .flatMap(parseEvents)
    }

    private fun fetchPendingEvents(apiClient: MailboxAPIClient): Result<HttpResponseData, Exception> {
        return Result.of {
            val responseText = apiClient.getPendingEvents()
            if (responseText.body.isEmpty()) HttpResponseData(ServerCodes.Success, "[]") else responseText
        }
    }

    private val parseEvents: (HttpResponseData) -> Result<Pair<List<Event>, Boolean>, Exception> = { responseData ->
        Result.of {
            val eventsJSONArray = JSONArray(responseData.body)
            val lastIndex = eventsJSONArray.length() - 1
            Pair(if (lastIndex > -1) {
                (0..lastIndex).map {
                    val eventJSONString = eventsJSONArray.get(it).toString()
                    Event.fromJSON(eventJSONString)
                }
            } else emptyList(), shouldCallGetEventsAgain(responseData.code))
        }
    }

    private fun shouldCallGetEventsAgain(respondeCode: Int): Boolean{
        return respondeCode == ServerCodes.SuccessAndRepeat
    }
}