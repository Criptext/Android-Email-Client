package com.criptext.mail.db.models

import androidx.room.*

@Entity(tableName = "pendingEvent",
        indices = [ (Index(value = ["id"], name = "index_pending_event_id")) ],
        foreignKeys = [ForeignKey(entity = Account::class,
                parentColumns = ["id"],
                onDelete = ForeignKey.CASCADE,
                childColumns = ["accountId"])])
data class  PendingEvent(
        @PrimaryKey(autoGenerate = true)
        var id: Long,
        @ColumnInfo(name = "data")
        var data: String,
        @ColumnInfo(name = "accountId")
        var accountId: Long
)
