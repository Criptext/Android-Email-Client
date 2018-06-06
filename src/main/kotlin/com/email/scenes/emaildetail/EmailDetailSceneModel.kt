package com.email.scenes.emaildetail

import com.email.db.models.FullEmail
import com.email.db.models.Label
import com.email.scenes.SceneModel
import com.email.utils.virtuallist.VirtualList

/**
 * Created by sebas on 3/12/18.
 */

class EmailDetailSceneModel(val threadId: String, val currentLabel: Label) : SceneModel {
    lateinit var fullEmailList : VirtualList<FullEmail>
}
