package com.criptext.mail.scenes.signup.data

import com.criptext.mail.signal.PreKeyBundleShareData
import com.criptext.mail.signal.SignalKeyGenerator
import com.criptext.mail.utils.DeviceUtils

/**
 * Created by gabriel on 5/18/18.
 */

object RegisterUserTestUtils {
    fun createRegistrationBundles(recipientId: String, deviceId: Int): SignalKeyGenerator.RegistrationBundles {
        val preKeys = mapOf(Pair(1, "__PK_1__"), Pair(2, "__PK_2__"), Pair(3, "__PK_3__"))
        val registrationId = 54378
        return SignalKeyGenerator.RegistrationBundles(
                privateBundle = SignalKeyGenerator.PrivateBundle(identityKeyPair = "__IDENTITY_KEY_PAIR__",
                        signedPreKeyId = 1, signedPreKey = "__SIGNED_PRE_KEY__",
                        registrationId = registrationId, preKeys = preKeys),
                uploadBundle = PreKeyBundleShareData.UploadBundle(
                        shareData = PreKeyBundleShareData(recipientId = recipientId,
                                deviceId = deviceId, signedPreKeyId = 1,
                                signedPreKeyPublic = "__SIGNED_PRE_KEY_PUBLIC__",
                                signedPreKeySignature = "__SIGNED_PRE_KEY_SIGNATURE__",
                                identityPublicKey = "__IDENTITY_PUBLIC_KEY__",
                                registrationId = registrationId), preKeys = preKeys,
                                deviceName = "__TEST__", deviceFriendlyName = "__TEST__",
                                deviceType = DeviceUtils.DeviceType.Phone)
        )
    }
}