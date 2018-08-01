package com.email.api.models


import org.json.JSONObject

/**
 * * data class for per email status updates. This is received as params of a "peer thread read status update" event (302).
 */

data class PeerEmailDeletedStatusUpdate (val metadataKeys: List<Long>) {
    companion object {

        fun fromJSON(jsonString: String): PeerEmailDeletedStatusUpdate {
            val json = JSONObject(jsonString)
            val listOfMetaKeys: MutableList<Long> = mutableListOf()
            val jsonListOfMetaKeys = json.getJSONArray("metadataKeys")
            for(i in 0 until jsonListOfMetaKeys.length()) listOfMetaKeys.add(jsonListOfMetaKeys[i].toString().toLong())
            return PeerEmailDeletedStatusUpdate(
                    metadataKeys =  listOfMetaKeys
            )
        }
    }
}