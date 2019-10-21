package com.criptext.mail.utils.peerdata

import com.criptext.mail.api.models.Event
import org.json.JSONObject

data class PeerDeleteLabelData(val uuid: String){
    fun toJSON(): JSONObject {
        val json = JSONObject()
        val jsonPut = JSONObject()
        jsonPut.put("cmd", Event.Cmd.peerLabelDeleted)
        json.put("uuid", uuid)
        jsonPut.put("params", json)

        return jsonPut
    }
}