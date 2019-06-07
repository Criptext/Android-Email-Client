package com.criptext.mail.mocks

/**
 * Created by gabriel on 6/28/18.
 */
/**
 * Created by gabriel on 5/1/18.
 */
object MockJSONData {
    val sampleNewEmailEvent = """
          {
            "rowid": 1,
            "cmd": 101,
            "params":
                "{\"threadId\":\"<15221916.12518@criptext.com>\",\"subject\":\"hello\",\"from\":\"Mayer Mizrachi <mayer@criptext.com>\",\"to\":\"gabriel@criptext.com\",\"cc\":\"\",\"bcc\":\"\",\"messageId\":\"<15221916.12518@criptext.com>\",\"date\":\"2018-03-27 23:00:13\",\"metadataKey\":81,\"messageType\":3,\"senderDeviceId\":2}"
}"""
    val sampleNewEmailEventPlainText = """
          {
            "rowid": 1,
            "cmd": 101,
            "params":
                "{\"threadId\":\"<15221916.12520@criptext.com>\",\"subject\":\"hello\",\"from\":\"Some One <someone@gmail.com>\",\"to\":\"gabriel@criptext.com\",\"cc\":\"\",\"bcc\":\"\",\"messageId\":\"<15221916.12520@criptext.com>\",\"date\":\"2018-03-27 23:00:13\",\"metadataKey\":81}"
}"""
    val sample2NewEmailEvents = """
          [{
            "rowid": 4,
            "cmd": 101,
            "params":
                "{\"threadId\":\"<15221916.12518@criptext.com>\",\"subject\":\"hello\",\"from\":\"Mayer Mizrachi <mayer@criptext.com>\",\"senderId\":\"mayer\",\"senderDomain\":\"criptext.com\",\"to\":\"gabriel@criptext.com\",\"cc\":\"\",\"bcc\":\"\",\"messageId\":\"<15221916.12518@criptext.com>\",\"date\":\"2018-03-27 23:00:13\",\"metadataKey\":81,\"messageType\":3,\"senderDeviceId\":1,\"guestEncryption\":1}"
},{
            "rowid": 5,
            "cmd": 101,
            "params":
                "{\"threadId\":\"<15221916.12519@criptext.com>\",\"subject\":\"hello again\",\"from\":\"Gianni Carlo <gianni@criptext.com>\",\"senderId\":\"gianni\",\"senderDomain\":\"criptext.com\",\"to\":\"gabriel@criptext.com\",\"cc\":\"\",\"bcc\":\"\",\"messageId\":\"<15221916.12519@criptext.com>\",\"date\":\"2018-03-27 23:00:13\",\"metadataKey\":82,\"messageType\":3,\"senderDeviceId\":1,\"guestEncryption\":1}"
}
]"""
    val sample2TrackingUpdateEvents = """
          [{
            "rowid": 1,
            "cmd": 102,
            "params":
                "{\"type\":7,\"metadataKey\":101,\"from\":\"mayer\",\"fromDomain\":{\"recipientId\": \"mayer\", \"domain\":\"criptext.com\"},\"date\":\"1522191612518\"}"
},{
            "rowid": 2,
            "cmd": 102,
            "params":
                "{\"type\":7,\"metadataKey\":102,\"from\":\"mayer\",\"fromDomain\":{\"recipientId\": \"mayer\", \"domain\":\"criptext.com\"},\"date\":\"1522191612519\"}"
}
]"""
}