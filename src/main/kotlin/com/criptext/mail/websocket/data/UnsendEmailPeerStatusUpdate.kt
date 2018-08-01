package com.criptext.mail.websocket.data

import com.criptext.mail.api.models.PeerUnsendEmailStatusUpdate

data class UnsendEmailPeerStatusUpdate(val emailId: Long, val peerUnsendEmailStatusUpdate: PeerUnsendEmailStatusUpdate)