package com.signaltest.crypto

import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyRecord

/**
 * Created by gabriel on 11/7/17.
 */

data class SignalInstallData(val identification: Identification,
                             val preKeyRecords: List<PreKeyRecord>) {

    constructor(identityKeyPair: IdentityKeyPair,
                registrationId: Int,
                signedPreKeyRecord: SignedPreKeyRecord,
                preKeyRecords: List<PreKeyRecord>)
            : this(Identification(identityKeyPair, registrationId, signedPreKeyRecord),
            preKeyRecords)

    data class Identification(val identityKeyPair: IdentityKeyPair,
                              val registrationId: Int,
                              val signedPreKeyRecord: SignedPreKeyRecord)
}


