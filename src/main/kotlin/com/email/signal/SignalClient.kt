package com.email.signal

import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle
import org.whispersystems.libsignal.state.SignalProtocolStore
import java.nio.charset.Charset

/**
 * Created by gabriel on 3/16/18.
 */
interface SignalClient {
    /**
     * Takes a list of keybundles and creates new sessions. with each
     */
    fun  createSessionsFromBundles(bundles: List<PreKeyBundleShareData.DownloadBundle>)

    /**
     * encrypts a message using Signal.
     * @param recipientId the username of the only Criptext user that can decrypt this message
     * @param deviceId The id of the only device owned by `recipientId` that can decrypt this
     * message. Make sure that you have already established a session with this recipientId + deviceId.
     * @param plainText A plain message to encrypt, encoded in UTF-8.
     * @returns A Base64 string with the encrypted bytes.
     */
    fun encryptMessage(recipientId: String, deviceId: Int, plainText: String): String

    /**
     * decrypts a message using Signal.
     * @param recipientId the username of the Criptext user that encrypted this message
     * @param deviceId The id of the device owned by `recipientId` that encrypted this
     * message.
     * @param encryptedB64 A Base64 string with the encrypted bytes.
     * @returns The original UTF-8 plain text message.
     */
    fun decryptMessage(recipientId: String, deviceId: Int, encryptedB64: String): String

    class Default(private val store: SignalProtocolStore) : SignalClient {
        private val createNewSessionParams: (PreKeyBundleShareData.DownloadBundle) -> NewSessionParams =
            { downloadBundle ->
                val shareData = downloadBundle.shareData
                val registrationId = shareData.registrationId
                val deviceId = shareData.deviceId
                val preKeyId = downloadBundle.preKey?.id ?: 0
                val publicPreKeyString = downloadBundle.preKey?.byteString
                val signedPreKeyId = shareData.signedPreKeyId
                val signedPreKeyPublicString = shareData.signedPreKeyPublic
                val signedPreKeySignatureString = shareData.signedPreKeySignature
                val identityPublicKeyString = shareData.identityPublicKey

                val publicPreKeyBytes = if (publicPreKeyString != null)
                                            Encoding.stringToByteArray(publicPreKeyString)
                                        else null
                val publicPreKey = if(publicPreKeyBytes != null)
                                            Curve.decodePoint(publicPreKeyBytes, 0)
                                        else null

                val signedPreKeyPublicBytes = Encoding.stringToByteArray(signedPreKeyPublicString)
                val signedPreKeyPublic = Curve.decodePoint(signedPreKeyPublicBytes, 0)

                val signedPreKeySignature = Encoding.stringToByteArray(signedPreKeySignatureString)

                val identityPublicKeyBytes = Encoding.stringToByteArray(identityPublicKeyString)
                val identityPublicKey = IdentityKey(identityPublicKeyBytes, 0)

                val preKeyBundle = PreKeyBundle(registrationId, deviceId, preKeyId, publicPreKey,
                                   signedPreKeyId, signedPreKeyPublic, signedPreKeySignature,
                                   identityPublicKey)
                NewSessionParams(recipientId = downloadBundle.shareData.recipientId,
                               preKeyBundle = preKeyBundle)
            }

        private val buildNewSession: (NewSessionParams) -> Unit = { params ->
            val sessionBuilder = SessionBuilder(store, SignalProtocolAddress(params.recipientId,
                    params.preKeyBundle.deviceId))
            sessionBuilder.process(params.preKeyBundle)
        }

        override fun createSessionsFromBundles(bundles: List<PreKeyBundleShareData.DownloadBundle>) {
            bundles.map(createNewSessionParams).forEach(buildNewSession)
        }

        override fun encryptMessage(recipientId: String, deviceId: Int, plainText: String): String {
            val cipher = SessionCipher(store, SignalProtocolAddress(recipientId, deviceId))
            val cipherText = cipher.encrypt(plainText.toByteArray(Charset.forName("UTF-8")))
            return Encoding.byteArrayToString(cipherText.serialize())
        }

        override fun decryptMessage(recipientId: String, deviceId: Int, encryptedB64: String): String {
            val encryptedBytes = Encoding.stringToByteArray(encryptedB64)
            val signalMessage = PreKeySignalMessage(encryptedBytes)
            val cipher = SessionCipher(store, SignalProtocolAddress(recipientId, deviceId))
            return cipher.decrypt(signalMessage).toString(Charset.forName("UTF-8"))
        }

    }

    private data class NewSessionParams(val recipientId: String, val preKeyBundle: PreKeyBundle)
}