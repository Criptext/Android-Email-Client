package com.email.scenes.emaildetail

import com.email.db.models.FileDetail
import com.email.db.models.FullEmail
import com.email.db.models.Label
import com.email.email_preview.EmailPreview
import com.email.scenes.SceneModel
import com.email.utils.virtuallist.VirtualList

/**
 * Created by sebas on 3/12/18.
 */

class EmailDetailSceneModel(val threadId: String,
                            val currentLabel: Label,
                            var threadPreview: EmailPreview) : SceneModel {
    val emails = ArrayList<FullEmail>()
    val fileDetails = HashMap<Long, List<FileDetail>>()
}
