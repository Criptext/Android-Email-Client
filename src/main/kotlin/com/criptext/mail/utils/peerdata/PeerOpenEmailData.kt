package com.criptext.mail.utils.peerdata

import com.criptext.mail.api.models.Event
import com.criptext.mail.api.toJSONLongArray
import org.json.JSONObject

data class PeerOpenEmailData(val metadataKeys: List<Long>){
    fun toJSON(): JSONObject{
        val json = JSONObject()
        json.put("metadataKeys", metadataKeys.toJSONLongArray())
        val jsonPut = JSONObject()
        jsonPut.put("cmd", Event.Cmd.openEmail)
        jsonPut.put("params", json)

        return jsonPut
    }
}