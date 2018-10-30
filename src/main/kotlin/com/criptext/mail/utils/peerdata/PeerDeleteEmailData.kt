package com.criptext.mail.utils.peerdata

import com.criptext.mail.api.models.Event
import com.criptext.mail.api.toJSONLongArray
import org.json.JSONObject

data class PeerDeleteEmailData(val metadataKeys: List<Long>){
    fun toJSON(): JSONObject{
        val json = JSONObject()
        val jsonPost = JSONObject()
        jsonPost.put("cmd", Event.Cmd.peerEmailDeleted)
        json.put("metadataKeys", metadataKeys.toJSONLongArray())
        jsonPost.put("params", json)

        return jsonPost
    }
}