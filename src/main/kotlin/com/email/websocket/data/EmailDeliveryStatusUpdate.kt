package com.email.websocket.data

import com.email.api.models.TrackingUpdate

/**
 * Created by gabriel on 7/3/18.
 */
data class EmailDeliveryStatusUpdate(val emailId: Long, val trackingUpdate: TrackingUpdate)