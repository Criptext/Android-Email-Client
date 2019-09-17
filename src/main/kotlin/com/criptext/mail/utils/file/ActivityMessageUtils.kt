package com.criptext.mail.utils.file

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.ParcelFileDescriptor
import com.criptext.mail.scenes.ActivityMessage

object ActivityMessageUtils  {
    fun getAddAttachmentsActivityMessage(data: Intent, contentResolver: ContentResolver?, ctx: Context): ActivityMessage.AddAttachments? {
        val clipData = data.clipData
        if(clipData != null && data.data == null){
            val attachmentList = mutableListOf<Pair<String, Long>>()
            for (i in 0 until clipData.itemCount) {
                clipData.getItemAt(i).also { item ->
                    val attachment = FileUtils.getPathAndSizeFromUri(item.uri, contentResolver,
                            ctx)
                    if (attachment != null)
                        attachmentList.add(attachment)
                }
            }
            if (attachmentList.isNotEmpty())
                return ActivityMessage.AddAttachments(attachmentList)
        } else if(data.data != null) {
            data.data?.also { uri ->
                val attachment = FileUtils.getPathAndSizeFromUri(uri, contentResolver, ctx)
                if (attachment != null)
                    return ActivityMessage.AddAttachments(listOf(attachment))
            }
        }
        return null
    }
}