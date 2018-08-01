package com.criptext.mail.websocket.data

import com.criptext.mail.api.models.PeerReadEmailStatusUpdate

data class ReadEmailPeerStatusUpdate(val emailId: List<Long>, val peerReadEmailStatusUpdate: PeerReadEmailStatusUpdate)