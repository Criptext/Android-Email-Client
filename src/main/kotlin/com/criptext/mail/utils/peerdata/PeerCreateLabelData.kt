package com.criptext.mail.utils.peerdata

import com.criptext.mail.api.models.Event
import org.json.JSONObject

data class PeerCreateLabelData(val text: String, val color: String){
    fun toJSON(): JSONObject {
        val json = JSONObject()
        val jsonPut = JSONObject()
        jsonPut.put("cmd", Event.Cmd.peerLabelCreated)
        json.put("text", text)
        json.put("color", color)
        jsonPut.put("params", json)

        return jsonPut
    }
}