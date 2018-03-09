package com.email.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

/**
 * Created by danieltigse on 2/7/18.
 */

@Entity(tableName = "feedItem")
class FeedItem(
        @PrimaryKey(autoGenerate = true)
        var id:Int?,

        @ColumnInfo(name = "type")
        var feedType: Int,

        @ColumnInfo(name = "title")
        var feedTitle: String,

        @ColumnInfo(name = "subtitle")
        var feedSubtitle: String,

        @ColumnInfo(name = "date")
        var feedDate: Date,

        @ColumnInfo(name = "isNew")
        var isNew: Boolean,

        @ColumnInfo(name = "isMuted")
        var isMuted: Boolean
)