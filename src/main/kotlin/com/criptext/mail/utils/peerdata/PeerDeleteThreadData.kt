package com.criptext.mail.utils.peerdata

import com.criptext.mail.api.models.Event
import org.json.JSONArray
import org.json.JSONObject

data class PeerDeleteThreadData(val threadIds: List<String>){
    fun toJSON(): JSONObject{
        val json = JSONObject()
        val jsonPost = JSONObject()
        jsonPost.put("cmd", Event.Cmd.peerThreadDeleted)
        json.put("threadIds", JSONArray(threadIds))
        jsonPost.put("params", json)

        return jsonPost
    }
}