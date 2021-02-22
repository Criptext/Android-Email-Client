package com.criptext.mail.scenes.restorebackup.holders

sealed class RestoreBackupLayoutState {
    class Searching(): RestoreBackupLayoutState()
    class Found(): RestoreBackupLayoutState()
    class Restoring: RestoreBackupLayoutState()
    class NotFound(): RestoreBackupLayoutState()
    class Error(): RestoreBackupLayoutState()
}