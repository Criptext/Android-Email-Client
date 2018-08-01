package com.criptext.mail.websocket.data

import com.criptext.mail.db.models.Email
import com.criptext.mail.utils.UIMessage

/**
 * Created by gabriel on 5/1/18.
 */
sealed class EventResult {
    sealed class InsertNewEmail: EventResult()  {
        data class Success(val newEmail: Email): InsertNewEmail()
        class Failure(val message: UIMessage): InsertNewEmail()
    }
    sealed class UpdateDeliveryStatus: EventResult()  {
        data class Success(val update: EmailDeliveryStatusUpdate?): UpdateDeliveryStatus()
        class Failure(val message: UIMessage): UpdateDeliveryStatus()
    }

    sealed class UpdatePeerUnsendEmailStatus: EventResult()  {
        data class Success(val update: UnsendEmailPeerStatusUpdate?): UpdatePeerUnsendEmailStatus()
        class Failure(val message: UIMessage): UpdatePeerUnsendEmailStatus()
    }

    sealed class UpdatePeerReadEmailStatus: EventResult()  {
        data class Success(val update: ReadEmailPeerStatusUpdate?): UpdatePeerReadEmailStatus()
        class Failure(val message: UIMessage): UpdatePeerReadEmailStatus()
    }

    sealed class UpdatePeerReadThreadStatus: EventResult()  {
        data class Success(val update: ReadThreadPeerStatusUpdate?): UpdatePeerReadThreadStatus()
        class Failure(val message: UIMessage): UpdatePeerReadThreadStatus()
    }

    sealed class UpdatePeerEmailDeletedStatus: EventResult()  {
        data class Success(val update: EmailDeletedPeerStatusUpdate?): UpdatePeerEmailDeletedStatus()
        class Failure(val message: UIMessage): UpdatePeerEmailDeletedStatus()
    }

    sealed class UpdatePeerThreadDeletedStatus: EventResult()  {
        data class Success(val update: ThreadDeletedPeerStatusUpdate?): UpdatePeerThreadDeletedStatus()
        class Failure(val message: UIMessage): UpdatePeerThreadDeletedStatus()
    }

    sealed class UpdatePeerEmailChangedLabelsStatus: EventResult()  {
        data class Success(val update: EmailChangedLabelsPeerStatusUpdate?): UpdatePeerEmailChangedLabelsStatus()
        class Failure(val message: UIMessage): UpdatePeerEmailChangedLabelsStatus()
    }

    sealed class UpdatePeerThreadChangedLabelsStatus: EventResult()  {
        data class Success(val update: ThreadChangedLabelsPeerStatusUpdate?): UpdatePeerThreadChangedLabelsStatus()
        class Failure(val message: UIMessage): UpdatePeerThreadChangedLabelsStatus()
    }

    sealed class UpdatePeerUsernameChangedStatus: EventResult()  {
        data class Success(val update: UsernameChangedPeerStatusUpdate?): UpdatePeerUsernameChangedStatus()
        class Failure(val message: UIMessage): UpdatePeerUsernameChangedStatus()
    }

    sealed class UpdatePeerLabelCreatedStatus: EventResult()  {
        data class Success(val update: LabelCreatedPeerStatusUpdate?): UpdatePeerLabelCreatedStatus()
        class Failure(val message: UIMessage): UpdatePeerLabelCreatedStatus()
    }

}