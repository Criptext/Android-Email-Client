package com.email.signal

import com.email.db.models.signal.CRPreKey
import org.whispersystems.libsignal.state.SignalProtocolStore

/**
 * Created by gabriel on 3/17/18.
 */

abstract class TestUser(generator: SignalKeyGenerator, recipientId: String, deviceId: Int) {

    val registrationBundles = generator.register(recipientId, deviceId)
    abstract val store: SignalProtocolStore
    private lateinit var client: SignalClient

    fun setup(): TestUser {
        client = SignalClient.Default(store)
        return this
    }

    fun fetchAPreKeyBundle(): PreKeyBundleShareData.DownloadBundle {
        val bundle = registrationBundles.uploadBundle
        val preKeyPublic = bundle.preKeys[1]!!
        val preKeyRecord = CRPreKey(1, preKeyPublic)

        return PreKeyBundleShareData.DownloadBundle(
                shareData = registrationBundles.uploadBundle.shareData,
                preKey = preKeyRecord)
    }

    fun buildSession(downloadBundle: PreKeyBundleShareData.DownloadBundle) {
        client.createSessionsFromBundles(listOf(downloadBundle))
    }

    fun encrypt(recipientId: String, deviceId: Int, text: String) =
        client.encryptMessage(recipientId, deviceId, text)

    fun decrypt(recipientId: String, deviceId: Int, encryptedData: SignalEncryptedData) =
            client.decryptMessage(recipientId, deviceId, encryptedData)

}