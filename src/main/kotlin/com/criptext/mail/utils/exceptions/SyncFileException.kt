package com.criptext.mail.utils.exceptions

sealed class SyncFileException: Exception() {
    class OutdatedException: Exception()
    class UserNotValidException: Exception()
    class MigrationNotFoundException: Exception()
}

