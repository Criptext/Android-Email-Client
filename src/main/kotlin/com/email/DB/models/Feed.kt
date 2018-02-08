package com.email.DB.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

/**
 * Created by danieltigse on 2/7/18.
 */

@Entity(tableName = "feed")
class Feed(
        @PrimaryKey(autoGenerate = true)
        var id:Int,

        @ColumnInfo(name = "feedType")
        var feedType: Int,

        @ColumnInfo(name = "feedTitle")
        var feedTitle: String,

        @ColumnInfo(name = "feedSubtitle")
        var feedSubtitle: String,

        @ColumnInfo(name = "feedDate")
        var feedDate: Date,

        @ColumnInfo(name = "isNew")
        var isNew: Boolean,

        @ColumnInfo(name = "isMuted")
        val isMuted: Boolean
)