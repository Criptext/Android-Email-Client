package com.email.db.models

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.CASCADE
import com.email.db.FeedType
import java.util.*

/**
 * Created by danieltigse on 2/7/18.
 */

@Entity(tableName = "feedItem",
        indices = [ (Index(value = ["emailId", "contactId"], unique = true)) ],
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