package com.email.websocket.data

import com.email.api.models.PeerUnsendEmailStatusUpdate

data class UnsendEmailPeerStatusUpdate(val emailId: Long, val peerUnsendEmailStatusUpdate: PeerUnsendEmailStatusUpdate)