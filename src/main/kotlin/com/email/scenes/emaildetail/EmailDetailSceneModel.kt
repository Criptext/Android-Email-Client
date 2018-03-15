package com.email.scenes.emaildetail

import com.email.db.models.FullEmail
import com.email.scenes.SceneModel
import com.email.utils.VirtualList

/**
 * Created by sebas on 3/12/18.
 */

class EmailDetailSceneModel(val threadId: String) : SceneModel {
    lateinit var fullEmailList : VirtualList<FullEmail>
}
