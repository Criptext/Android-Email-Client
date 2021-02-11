package com.criptext.mail.utils.apputils

import com.criptext.mail.BaseActivity
import com.criptext.mail.IHostActivity


object AppRater {
    fun startReviewFlow(host: IHostActivity){
        val manager = host.getReviewManager()
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener {
            if (it.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = it.result
                manager.launchReviewFlow(host as BaseActivity, reviewInfo)
            }
        }
    }
}