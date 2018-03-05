package com.email.db.models

import android.content.Context
import com.email.db.AppDatabase
import com.email.db.models.signal.RawSession

/**
 * Created by gabriel on 3/5/18.
 */

interface SessionDB {
    fun delete(rawSession: RawSession)
    fun deleteByRecipientId(recipientId: String)
    fun insert(rawSession: RawSession)
    fun find(recipientId: String, deviceId: Int): RawSession?
    fun findActiveDevicesByRecipientId(recipientId: String): List<Int>

    class Room(applicationContext: Context): SessionDB {
        private val rawSessionDao = AppDatabase.getAppDatabase(applicationContext).rawSessionDao()

        override fun delete(rawSession: RawSession) {
            rawSessionDao.delete(rawSession)
        }

        override fun deleteByRecipientId(recipientId: String) {
            rawSessionDao.deleteByRecipientId(recipientId)
        }

        override fun insert(rawSession: RawSession) {
            rawSessionDao.delete(rawSession)
        }

        override fun find(recipientId: String, deviceId: Int) =
            rawSessionDao.find(recipientId = recipientId, deviceId = deviceId)

        override fun findActiveDevicesByRecipientId(recipientId: String) =
            rawSessionDao.findActiveDevicesByRecipientId(recipientId)


    }
}