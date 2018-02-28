package com.signaltest.crypto

import com.signaltest.api.PreKeyBundleShareData
import org.whispersystems.curve25519.Curve25519
import org.whispersystems.curve25519.Curve25519KeyPair
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.protocol.CiphertextMessage
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.state.*
import java.nio.charset.Charset


/**
 * Created by gabriel on 11/7/17.
 */

class SignalClient(private val identityKeyStore: IdentityKeyStore,
                   private val preKeyStore: PreKeyStore,
                   private val signedPreKeyStore: SignedPreKeyStore,
                   private val sessionStore: SessionStore) {


    fun buildSession(recipientId: String, deviceId: Int, preKeyBundle: PreKeyBundle) {
        val sessionBuilder = SessionBuilder(sessionStore, preKeyStore, signedPreKeyStore,
                identityKeyStore, SignalProtocolAddress(recipientId, deviceId))
        sessionBuilder.process(preKeyBundle)
    }

    fun encryptMessage(recipientId: String, deviceId: Int, message: String): CiphertextMessage {
        val cipher = SessionCipher(sessionStore, preKeyStore, signedPreKeyStore, identityKeyStore,
                SignalProtocolAddress(recipientId, deviceId))
        return cipher.encrypt(message.toByteArray(Charset.forName("UTF-8")))
    }

    fun decryptMessage(recipientId: String, deviceId: Int, messageBytes: ByteArray): String {
        val message = PreKeySignalMessage(messageBytes)
        val cipher = SessionCipher(sessionStore, preKeyStore, signedPreKeyStore, identityKeyStore,
                SignalProtocolAddress(recipientId, deviceId))
        return cipher.decrypt(message).toString(Charset.forName("UTF-8"))
    }

    companion object {

        fun createPreKeyBundleFromInstallData(data: SignalInstallData, deviceId: Int): PreKeyBundle {
            val preKey = data.preKeyRecords.first()
            val signedPreKey = data.identification.signedPreKeyRecord
            val identityKey = data.identification.identityKeyPair.publicKey
            return PreKeyBundle(data.identification.registrationId, deviceId, preKey.id,
                    preKey.keyPair.publicKey, signedPreKey.id, signedPreKey.keyPair.publicKey,
                    signedPreKey.signature, identityKey)
        }

        fun createPreKeyBundleFromDownloadedBundle(
                downloadBundle: PreKeyBundleShareData.DownloadBundle): PreKeyBundle {

            val shareData = downloadBundle.shareData
            val registrationId = shareData.registrationId
            val deviceId = shareData.deviceId
            val preKeyId = downloadBundle.preKeyId
            val publicPreKeyString = downloadBundle.publicPreKey
            val signedPreKeyId = shareData.signedPreKeyId
            val signedPreKeyPublicString = shareData.signedPreKeyPublic
            val signedPreKeySignatureString = shareData.signedPreKeySignature
            val identityPublicKeyString = shareData.identityPublicKey

            val publicPreKeyBytes = Encoding.stringToByteArray(publicPreKeyString)
            val publicPreKey = Curve.decodePoint(publicPreKeyBytes, 0)

            val signedPreKeyPublicBytes = Encoding.stringToByteArray(signedPreKeyPublicString)
            val signedPreKeyPublic = Curve.decodePoint(signedPreKeyPublicBytes, 0)

            val signedPreKeySignature = Encoding.stringToByteArray(signedPreKeySignatureString)

            val identityPublicKeyBytes = Encoding.stringToByteArray(identityPublicKeyString)
            val identityPublicKey = IdentityKey(identityPublicKeyBytes, 0)

            return PreKeyBundle(registrationId, deviceId, preKeyId, publicPreKey, signedPreKeyId,
                    signedPreKeyPublic, signedPreKeySignature, identityPublicKey)
        }

        fun createFullShareBundleFromInstallData(data: SignalInstallData, recipientId: String,
                                                 deviceId: Int) : PreKeyBundleShareData.UploadBundle {
            val userData = data.identification

            val shareData = PreKeyBundleShareData(
                recipientId = recipientId,
                registrationId = userData.registrationId,
                deviceId = deviceId,
                signedPreKeyId = userData.signedPreKeyRecord.id,
                signedPreKeySignature = Encoding.byteArrayToString(
                        userData.signedPreKeyRecord.signature),
                identityPublicKey = Encoding.byteArrayToString(
                        userData.identityKeyPair.publicKey.serialize()),
                signedPreKeyPublic = Encoding.byteArrayToString(
                        userData.signedPreKeyRecord.keyPair.publicKey.serialize())
            )


            val preKeysMap = data.preKeyRecords
                    .map { preKeyRecord ->
                        Pair(preKeyRecord.id,
                             Encoding.byteArrayToString(preKeyRecord.keyPair.publicKey.serialize())
                        )
                    }.toMap()

            return PreKeyBundleShareData.UploadBundle(shareData, preKeysMap)
        }
    }

}


