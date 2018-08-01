package com.criptext.mail.db.typeConverters

import android.arch.persistence.room.TypeConverter
import com.criptext.mail.db.FeedType

/**
 * Created by danieltigse on 7/5/18.
 */

class FeedTypeConverter {

    @TypeConverter
    fun getFeedType(value: Int) : FeedType {
        return when(value) {
            1 -> FeedType.OPEN_EMAIL
            2 -> FeedType.OPEN_FILE
            else -> FeedType.DOWNLOAD_FILE
        }
    }

    @TypeConverter
    fun parseFeedType(value: FeedType): Int {
        return when(value) {
            FeedType.OPEN_EMAIL -> 1
            FeedType.OPEN_FILE-> 2
            FeedType.DOWNLOAD_FILE -> 3
        }
    }

}
