package com.criptext.mail.websocket.data

import com.criptext.mail.api.models.PeerEmailDeletedStatusUpdate

data class EmailDeletedPeerStatusUpdate(val emailIds: List<Long>, val peerEmailDeletedStatusUpdate: PeerEmailDeletedStatusUpdate)