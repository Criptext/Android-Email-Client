package com.email.api.models


import org.json.JSONObject

/**
 * * data class for per email status updates. This is received as params of a "peer thread read status update" event (306).
 */

data class PeerThreadDeletedStatusUpdate (val threadIds: List<String>) {
    companion object {

        fun fromJSON(jsonString: String): PeerThreadDeletedStatusUpdate {
            val json = JSONObject(jsonString)
            val listOfThreadIds: MutableList<String> = mutableListOf()
            val jsonListOfMetaKeys = json.getJSONArray("threadIds")
            for(i in 0 until jsonListOfMetaKeys.length()) listOfThreadIds.add(jsonListOfMetaKeys[i].toString())
            return PeerThreadDeletedStatusUpdate(
                    threadIds =  listOfThreadIds
            )
        }
    }
}