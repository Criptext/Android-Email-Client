package com.criptext.mail.utils.peerdata

import com.criptext.mail.api.models.Event
import com.criptext.mail.api.toJSONLongArray
import org.json.JSONArray
import org.json.JSONObject

data class PeerChangeEmailLabelData(val metadataKeys: List<Long>, val labelsRemoved: List<String>,
                                    val labelsAdded: List<String>){
    fun toJSON(): JSONObject{
        val json = JSONObject()
        val jsonPost = JSONObject()
        jsonPost.put("cmd", Event.Cmd.peerEmailChangedLabels)
        json.put("metadataKeys", metadataKeys.toJSONLongArray())
        json.put("labelsRemoved", JSONArray(labelsRemoved))
        json.put("labelsAdded", JSONArray(labelsAdded))
        jsonPost.put("params", json)

        return jsonPost
    }
}