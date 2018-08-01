package com.criptext.mail.api.models


import org.json.JSONObject

/**
 * * data class for per email status updates. This is received as params of a "peer thread read status update" event (302).
 */

data class PeerReadThreadStatusUpdate (val threadIds: List<String>, val unread: Boolean) {
    companion object {

        fun fromJSON(jsonString: String): PeerReadThreadStatusUpdate {
            val json = JSONObject(jsonString)
            val listOfMetaKeys: MutableList<String> = mutableListOf()
            val jsonListOfMetaKeys = json.getJSONArray("threadIds")
            for(i in 0 until jsonListOfMetaKeys.length()) listOfMetaKeys.add(jsonListOfMetaKeys[i].toString())
            return PeerReadThreadStatusUpdate(
                    threadIds =  listOfMetaKeys,
                    unread = json.getInt("unread") == 1
            )
        }
    }
}