package com.criptext.mail.utils.ui.data

object ImportMailboxAnimationData {
    val sendingKeysLoop = Pair(1, 120)
    val keysToWaitTransition = Pair(120, 180)
    val waitingLoop = Pair(180, 240)
    val waitingToDownloadTransition = Pair(240, 300)
    val downloadingLoop = Pair(300, 420)
    val downloadingToImportTransition = Pair(420, 480)
    val importingLoop = Pair(480, 500)
}

object ExportMailboxAnimationData {
    val encryptingLoop = Pair(1, 180)
    val encryptToReceiveKeysTransition = Pair(180, 240)
    val receivingKeysLoop = Pair(240, 360)
    val receivingKeysToExportTransition = Pair(360, 420)
    val exportingTransition = Pair(420, 540)
}