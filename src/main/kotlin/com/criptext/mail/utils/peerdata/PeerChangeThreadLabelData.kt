package com.criptext.mail.utils.peerdata

import com.criptext.mail.api.models.Event
import org.json.JSONArray
import org.json.JSONObject

data class PeerChangeThreadLabelData(val threadIds: List<String>, val labelsRemoved: List<String>,
                                     val labelsAdded: List<String>){
    fun toJSON(): JSONObject{
        val json = JSONObject()
        val jsonPost = JSONObject()
        jsonPost.put("cmd", Event.Cmd.peerThreadChangedLabels)
        json.put("threadIds", JSONArray(threadIds))
        json.put("labelsRemoved", JSONArray(labelsRemoved))
        json.put("labelsAdded", JSONArray(labelsAdded))
        jsonPost.put("params", json)

        return jsonPost
    }
}