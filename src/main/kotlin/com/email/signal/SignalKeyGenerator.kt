package com.email.signal

/**
 * Created by sebas on 3/6/18.
 */

interface SignalKeyGenerator {
    fun  createKeyBundle(deviceId: Int) : PreKeyBundleShareData.UploadBundle
    class Default : SignalKeyGenerator {
        override fun createKeyBundle(deviceId: Int): PreKeyBundleShareData.UploadBundle {
            return PreKeyBundleShareData.UploadBundle.createKeyBundle(deviceId = deviceId)
        }
    }
}
