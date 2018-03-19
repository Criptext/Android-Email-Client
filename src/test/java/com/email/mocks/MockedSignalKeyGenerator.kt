package com.email.mocks

import com.email.signal.PreKeyBundleShareData
import com.email.signal.SignalKeyGenerator

/**
 * Created by sebas on 3/6/18.
 */

class MockedSignalKeyGenerator : SignalKeyGenerator {
    override fun register(recipientId: String, deviceId: Int): SignalKeyGenerator.RegistrationBundles {
        val shareData = PreKeyBundleShareData(
                deviceId = deviceId,
                recipientId = recipientId,
                signedPreKeySignature = "",
                signedPreKeyPublic = "",
                identityPublicKey = "",
                signedPreKeyId = 1,
                registrationId = 1)

        val uploadBundle = PreKeyBundleShareData.UploadBundle(shareData, emptyMap())
        val privateBundle = SignalKeyGenerator.PrivateBundle(identityKeyPair = "",
                signedPreKeyId = 1, signedPreKey = "", registrationId = 5, preKeys = emptyMap())

        return SignalKeyGenerator.RegistrationBundles(privateBundle, uploadBundle)
    }
}
