package com.criptext.mail.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
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