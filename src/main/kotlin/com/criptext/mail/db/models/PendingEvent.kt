package com.criptext.mail.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "pendingEvent",
        indices = [ (Index("id")) ])
data class  PendingEvent(
        @PrimaryKey(autoGenerate = true)
        var id: Long,
        @ColumnInfo(name = "data")
        var data: String
)
