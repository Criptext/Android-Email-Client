package com.criptext.mail.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.criptext.mail.db.models.PendingEvent

@Dao
interface PendingEventDao {
    @Insert
    fun insert(pendingEvent: PendingEvent)

    @Delete
    fun delete(pendingEvent: PendingEvent)

    @Query("""SELECT * FROM pendingEvent LIMIT :batch""")
    fun getByBatch(batch: Int): List<PendingEvent>

    @Query("""DELETE FROM pendingEvent
                    WHERE id IN (:ids)""")
    fun deleteByBatch(ids: List<Long>)

    @Query("DELETE FROM pendingEvent")
    fun nukeTable()

}