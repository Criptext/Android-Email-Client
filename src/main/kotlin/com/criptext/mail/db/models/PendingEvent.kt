package com.criptext.mail.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "pendingEvent",
        indices = [ (Index("id")) ])
data class  PendingEvent(
        @PrimaryKey(autoGenerate = true)
        var id: Long,
        @ColumnInfo(name = "data")
        var data: String
)
