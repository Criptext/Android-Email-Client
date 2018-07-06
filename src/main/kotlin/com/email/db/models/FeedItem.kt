package com.email.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.ForeignKey.CASCADE
import android.arch.persistence.room.PrimaryKey
import com.email.db.FeedType
import java.util.*

/**
 * Created by danieltigse on 2/7/18.
 */

@Entity(tableName = "feedItem",
        foreignKeys = [
            ForeignKey(entity = Email::class,
                    parentColumns = ["id"],
                    onDelete = CASCADE,
                    childColumns = ["emailId"]
            )
        ])
class FeedItem(
        @PrimaryKey(autoGenerate = true)
        var id:Long,

        @ColumnInfo(name = "date")
        var date : Date,

        @ColumnInfo(name = "type")
        var feedType: FeedType,

        @ColumnInfo(name = "location")
        var location: String,

        @ColumnInfo(name = "seen")
        var seen: Boolean,

        @ColumnInfo(name = "emailId")
        var emailId: Long,

        @ColumnInfo(name = "contactId")
        var contactId: Long,

        @ColumnInfo(name = "fileId")
        var fileId: Long?
)