package com.email.websocket.data

import com.email.api.models.PeerReadEmailStatusUpdate

data class ReadEmailPeerStatusUpdate(val emailId: List<Long>, val peerReadEmailStatusUpdate: PeerReadEmailStatusUpdate)