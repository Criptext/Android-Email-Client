package com.criptext.mail.scenes.emaildetail

import com.criptext.mail.db.models.CRFile
import com.criptext.mail.db.models.FileDetail
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.utils.virtuallist.VirtualList

/**
 * Created by sebas on 3/12/18.
 */

class EmailDetailSceneModel(var threadId: String,
                            var currentLabel: Label,
                            var threadPreview: EmailPreview,
                            val doReply: Boolean = false) : SceneModel {
    val emails = ArrayList<FullEmail>()
    val fileDetails = hashMapOf<Long, List<FileDetail>>()
    val inlineImages = mutableListOf<CRFile>()
    var hasTriedToSaveImage = false
    var fileToDownload = Pair(-1, -1)
    var lastTouchedInlineSrc: String? = null
    var waitForAccountSwitch = false
    var exitToMailbox = false
}
