package com.criptext.mail.signal

import com.criptext.mail.utils.Encoding
import com.criptext.mail.utils.file.ChunkFileReader
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.protocol.SignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle
import org.whispersystems.libsignal.state.SignalProtocolStore
import java.io.File
import java.nio.charset.Charset

/**
 * Created by gabriel on 3/16/18.
 */
interface SignalClient {
    /**
     * Takes a list of keybundles and creates new sessions. with each
     */
    fun  createSessionsFromBundles(bundles: List<PreKeyBundleShareData.DownloadBundle>)

    fun  deleteSessions(addresses: List<SignalProtocolAddress>)

    /**
     * encrypts a message using Signal.
     * @param recipientId the username of the only Criptext user that can decrypt this message
     * @param deviceId The id of the only device owned by `recipientId` that can decrypt this
     * message. Make sure that you have already established a session with this recipientId + deviceId.
     * @param plainText A plain message to encrypt, encoded in UTF-8.
     * @returns A SignalEncryptedData object with the base64 string of the encrypted data and
     * the encryption type (preKey or normal)
     */
    fun encryptMessage(recipientId: String, deviceId: Int, plainText: String): SignalEncryptedData

    fun encryptBytes(recipientId: String, deviceId: Int, byteArray: ByteArray): SignalEncryptedData

    fun encryptFileByChunks(fileToEncrypt: File, recipientId: String, deviceId: Int, chunkSize: Int, outputFileName: String = "encrypted_user_data"): String
    fun decryptFileByChunks(fileToDecrypt: File, recipientId: String, deviceId: Int,  outputFileName: String = "decrypted_user_data"): String

    /**
     * decrypts a message using Signal.
     * @param recipientId the username of the Criptext user that encrypted this message
     * @param deviceId The id of the device owned by `recipientId` that encrypted this
     * message.
     * @param encryptedData The data to decrypt.
     * @returns The original UTF-8 plain text message.
     */
    fun decryptMessage(recipientId: String, deviceId: Int, encryptedData: SignalEncryptedData): String

    fun decryptBytes(recipientId: String, deviceId: Int, encryptedData: SignalEncryptedData): ByteArray

    class Default(private val store: SignalProtocolStore) : SignalClient {
        private val createNewSessionParams: (PreKeyBundleShareData.DownloadBundle) -> NewSessionParams =
            { downloadBundle ->
                val shareData = downloadBundle.shareData
                val registrationId = shareData.registrationId
                val deviceId = shareData.deviceId
                val preKeyId = downloadBundle.preKey?.preKeyId ?: -1
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

        override fun deleteSessions(addresses: List<SignalProtocolAddress>) {
            addresses.forEach { store.deleteSession(it) }
        }

        override fun encryptMessage(recipientId: String, deviceId: Int, plainText: String): SignalEncryptedData {
            val cipher = SessionCipher(store, SignalProtocolAddress(recipientId, deviceId))
            val cipherText = cipher.encrypt(plainText.toByteArray(Charset.forName("UTF-8")))
            cipherText.type
            val encryptedB64 = Encoding.byteArrayToString(cipherText.serialize())
            val encryptionType = if (cipherText is PreKeySignalMessage)
                                     SignalEncryptedData.Type.preKey
                                 else
                                     SignalEncryptedData.Type.normal

            return SignalEncryptedData(encryptedB64, encryptionType)
        }

        override fun encryptBytes(recipientId: String, deviceId: Int, byteArray: ByteArray): SignalEncryptedData {
            val cipher = SessionCipher(store, SignalProtocolAddress(recipientId, deviceId))
            val cipherText = cipher.encrypt(byteArray)
            cipherText.type
            val encryptedB64 = Encoding.byteArrayToString(cipherText.serialize())
            val encryptionType = if (cipherText is PreKeySignalMessage)
                SignalEncryptedData.Type.preKey
            else
                SignalEncryptedData.Type.normal

            return SignalEncryptedData(encryptedB64, encryptionType)
        }

        override fun decryptBytes(recipientId: String, deviceId: Int, encryptedData: SignalEncryptedData): ByteArray {
            val encryptedBytes = Encoding.stringToByteArray(encryptedData.encryptedB64)
            val senderAddress = SignalProtocolAddress(recipientId, deviceId)
            val cipher = SessionCipher(store, senderAddress)

            return if (encryptedData.type == SignalEncryptedData.Type.preKey)
                cipher.decrypt(PreKeySignalMessage(encryptedBytes))
            else
                cipher.decrypt(SignalMessage(encryptedBytes))
        }

        override fun encryptFileByChunks(fileToEncrypt: File, recipientId: String, deviceId: Int, chunkSize: Int, outputFileName: String): String {
            val encryptedFile = createTempFile(outputFileName)
            val onNewChunkRead: (ByteArray, Int) -> Unit = { chunk, _ -> encryptedFile.appendText(encryptBytes(recipientId, deviceId,chunk).encryptedB64.plus("\n")) }
            ChunkFileReader.read(fileToEncrypt, chunkSize, onNewChunkRead)
            return encryptedFile.absolutePath
        }

        override fun decryptFileByChunks(fileToDecrypt: File, recipientId: String, deviceId: Int, outputFileName: String): String {
            val decryptedFile = createTempFile(outputFileName)
            fileToDecrypt.forEachLine {decryptedFile.appendText(decryptMessage(recipientId, deviceId,SignalEncryptedData(it,SignalEncryptedData.Type.preKey))) }
            return decryptedFile.absolutePath
        }

        override fun decryptMessage(recipientId: String, deviceId: Int,
                                    encryptedData: SignalEncryptedData): String {
            val encryptedBytes = Encoding.stringToByteArray(encryptedData.encryptedB64)
            val senderAddress = SignalProtocolAddress(recipientId, deviceId)
            val cipher = SessionCipher(store, senderAddress)
            val decryptedMessage =
                    if (encryptedData.type == SignalEncryptedData.Type.preKey)
                        cipher.decrypt(PreKeySignalMessage(encryptedBytes))
                    else
                        cipher.decrypt(SignalMessage(encryptedBytes))

            return decryptedMessage.toString(Charset.forName("UTF-8"))
        }

    }

    private data class NewSessionParams(val recipientId: String, val preKeyBundle: PreKeyBundle)
}