package com.criptext.mail.scenes.syncing.holders

sealed class SyncingLayoutState {
    class SyncBegin: SyncingLayoutState()
    class SyncRejected: SyncingLayoutState()
    class SyncImport: SyncingLayoutState()
}