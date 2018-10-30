package com.criptext.mail.utils.peerdata

import com.criptext.mail.api.models.Event
import org.json.JSONArray
import org.json.JSONObject

data class PeerThreadReadData(val threadIds: List<String>, val unread: Boolean){
    fun toJSON(): JSONObject {
        val json = JSONObject()
        val jsonPost = JSONObject()
        jsonPost.put("cmd", Event.Cmd.peerEmailThreadReadStatusUpdate)
        json.put("threadIds", JSONArray(threadIds))
        json.put("unread", if(unread) 1 else 0)
        jsonPost.put("params", json)

        return jsonPost
    }
}