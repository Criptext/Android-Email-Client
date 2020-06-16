package com.criptext.mail.scenes.composer

import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.scenes.composer.data.ComposerAttachment
import com.criptext.mail.scenes.composer.data.ComposerType
import com.criptext.mail.scenes.composer.data.ContactDomainCheckData
import java.util.*

/**
 * Created by gabriel on 2/26/18.
 */

class ComposerModel(val type: ComposerType, val currentLabel: Label): SceneModel {

    val isReplyOrDraft: Boolean = type is ComposerType.Reply
            || type is ComposerType.ReplyAll || type is ComposerType.Draft

    val isSupport: Boolean = type is ComposerType.Support

    var attachments: ArrayList<ComposerAttachment> = ArrayList()

    val filesExceedingMaxEmailSize = mutableListOf<String>()
    val filesExceedingMaxFileSize = mutableListOf<Pair<String, String>>()

    val isUploadingAttachments = when {
        attachments.isEmpty() -> false
        attachments.any { it.uploadProgress == -1 } -> true
        attachments.any { it.uploadProgress in 0..99 } -> true
        else -> false
    }

    var attachmentsChanged = false

    var to = LinkedList<Contact>()
    var cc = LinkedList<Contact>()
    var bcc = LinkedList<Contact>()

    var checkedDomains = mutableListOf<ContactDomainCheckData>()

    var firstTime = true
    var initialized = type is ComposerType.Empty
    var subject = ""
    var body = ""
    var originalBody = ""

    var passwordText: String = ""

    var fileKey: String? = null

    var filesSize: Long = 0L

    var accounts = listOf<ActiveAccount>()
    var selectedAddress: String = ""
    var shareActivityMessage: ActivityMessage.AddAttachments? = null
    var fromAddresses = mutableListOf<String>()
    var groupId: String? = null
    var hasRecoveryEmail: Boolean = false
    var isEmailConfirmed: Boolean = false
}