package com.email.websocket.data

import com.email.api.models.PeerEmailDeletedStatusUpdate

data class EmailDeletedPeerStatusUpdate(val emailIds: List<Long>, val peerEmailDeletedStatusUpdate: PeerEmailDeletedStatusUpdate)