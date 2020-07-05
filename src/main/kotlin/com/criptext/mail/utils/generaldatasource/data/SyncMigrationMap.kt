package com.criptext.mail.utils.generaldatasource.data

import com.criptext.mail.utils.exceptions.SyncFileException
import java.io.BufferedReader

abstract class SyncMigrationMap {
    abstract fun migrate(buffer: BufferedReader): BufferedReader
    protected lateinit var functionMap: MutableMap<Pair<Int, Int>, (buffer: BufferedReader) -> BufferedReader>

    class Default(private val fileVersion: Int, private val myVersion: Int): SyncMigrationMap() {
        private val fromCurrentToCurrentVersion: (BufferedReader) -> BufferedReader = {
            BufferedReader(it)
        }

        init {
            functionMap = mutableMapOf()
            for(i in 1..myVersion){
                functionMap[Pair(i, myVersion)] = when(i){
                    myVersion -> fromCurrentToCurrentVersion
                    else -> fromCurrentToCurrentVersion
                }
            }
        }

        override fun migrate(buffer: BufferedReader): BufferedReader {
            return functionMap[Pair(fileVersion, myVersion)]?.let { it(buffer) } ?: throw SyncFileException.MigrationNotFoundException()
        }
    }
}