package com.criptext.mail.utils.eventhelper

import com.criptext.mail.api.HttpResponseData
import com.criptext.mail.api.models.Event
import com.criptext.mail.scenes.mailbox.data.MailboxAPIClient
import com.criptext.mail.utils.ServerCodes
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import org.json.JSONArray
import org.json.JSONObject

object EventLoader{

    fun getEvents(apiClient: MailboxAPIClient): Result<Pair<List<Event>, Boolean>, Exception>{
        return fetchPendingEvents(apiClient)
                .flatMap(parseEvents)
    }

    fun getEvent(apiClient: MailboxAPIClient, rowId: Int): Result<Event, Exception>{
        return fetchPendingEvent(apiClient, rowId)
                .flatMap(parseEvent)
    }

    private fun fetchPendingEvent(apiClient: MailboxAPIClient, rowId: Int): Result<HttpResponseData, Exception> {
        return Result.of {
            val responseText = apiClient.getPendingEvent(rowId)
            if (responseText.body.isEmpty()) HttpResponseData(ServerCodes.Success, "{}") else responseText
        }
    }

    private fun fetchPendingEvents(apiClient: MailboxAPIClient): Result<HttpResponseData, Exception> {
        return Result.of {
            val responseText = apiClient.getPendingEvents()
            if (responseText.body.isEmpty()) HttpResponseData(ServerCodes.Success, "[]") else responseText
        }
    }

    private val parseEvent: (HttpResponseData) -> Result<Event, Exception> = { responseData ->
        Result.of {
            val eventJSONString = JSONObject(responseData.body).toString()
            if (eventJSONString.isNotEmpty()) {
                Event.fromJSON(eventJSONString)
            } else {
                if(noContentFound(responseData.code))
                    throw EventHelper.NoContentFoundException()
                else
                    throw Exception()
            }
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

    private fun shouldCallGetEventsAgain(responseCode: Int): Boolean{
        return responseCode == ServerCodes.SuccessAndRepeat
    }

    private fun noContentFound(responseCode: Int): Boolean{
        return responseCode == ServerCodes.NoContent
    }
}