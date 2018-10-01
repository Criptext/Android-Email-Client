package com.criptext.mail.utils

import com.criptext.mail.api.models.Event
import com.criptext.mail.scenes.mailbox.data.MailboxAPIClient
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import org.json.JSONArray

object EventLoader{

    fun getEvents(apiClient: MailboxAPIClient): Result<List<Event>, Exception>{
        return fetchPendingEvents(apiClient)
                .flatMap(parseEvents)
    }

    private fun fetchPendingEvents(apiClient: MailboxAPIClient): Result<String, Exception> {
        return Result.of {
            val responseText = apiClient.getPendingEvents()
            if (responseText.isEmpty()) "[]" else responseText
        }
    }

    private val parseEvents: (String) -> Result<List<Event>, Exception> = { jsonString ->
        Result.of {
            val eventsJSONArray = JSONArray(jsonString)
            val lastIndex = eventsJSONArray.length() - 1
            if (lastIndex > -1) {
                (0..lastIndex).map {
                    val eventJSONString = eventsJSONArray.get(it).toString()
                    Event.fromJSON(eventJSONString)
                }
            } else emptyList()

        }
    }
}