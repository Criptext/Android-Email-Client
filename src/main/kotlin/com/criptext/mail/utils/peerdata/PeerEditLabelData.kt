package com.criptext.mail.utils.peerdata

import com.criptext.mail.api.models.Event
import org.json.JSONObject

data class PeerEditLabelData(val uuid: String, val newName: String, val color: String){
    fun toJSON(): JSONObject {
        val json = JSONObject()
        val jsonPut = JSONObject()
        jsonPut.put("cmd", Event.Cmd.peerLabelEdited)
        json.put("uuid", uuid)
        json.put("text", newName)
        json.put("color", color)
        jsonPut.put("params", json)

        return jsonPut
    }
}