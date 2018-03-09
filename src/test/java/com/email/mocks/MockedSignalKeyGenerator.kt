package com.email.mocks

import com.email.signal.PreKeyBundleShareData
import com.email.signal.SignalKeyGenerator
import org.whispersystems.libsignal.state.PreKeyRecord

/**
 * Created by sebas on 3/6/18.
 */

class MockedSignalKeyGenerator: SignalKeyGenerator {
    override fun createKeyBundle(deviceId: Int): PreKeyBundleShareData.UploadBundle {
        val mutableList : MutableList<PreKeyRecord> = arrayListOf()
        val shareData = PreKeyBundleShareData(
                deviceId = deviceId,
                signedPrekey = "",
                identityKeyPair = "",
                signedPreKeySignature = "",
                signedPreKeyPublic = "",
                identityPublicKey = "",
                signedPreKeyId = 1,
                registrationId = 1,
                prekeys = mutableList)
        return PreKeyBundleShareData.UploadBundle(shareData, emptyMap())
    }

}
