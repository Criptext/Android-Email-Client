package com.email.websocket.data

import com.email.api.models.PeerEmailStatusUpdate

data class EmailPeerStatusUpdate(val emailId: Long, val peerEmailStatusUpdate: PeerEmailStatusUpdate)