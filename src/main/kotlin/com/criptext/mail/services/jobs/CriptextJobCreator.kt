package com.criptext.mail.services.jobs

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator


class CriptextJobCreator: JobCreator {

    override fun create(tag: String): Job? {
        return when (tag) {
            CloudBackupJobService.JOB_TAG -> CloudBackupJobService()
            else -> null
        }
    }

}