package com.criptext.mail.mocks

/**
 * Created by gabriel on 5/1/18.
 */
object MockedJSONData {
    val sampleNewEmailEvent = """
          {
            "rowid": 1,
            "cmd": 101,
            "params":
                "{\"threadId\":\"<15221916.12518@jigl.com>\",\"subject\":\"hello\",\"from\":\"Mayer Mizrachi <mayer@jigl.com>\",\"to\":\"gabriel@jigl.com\",\"cc\":\"\",\"bcc\":\"\",\"messageId\":\"<15221916.12518@jigl.com>\",\"date\":\"2018-03-27 23:00:13\",\"metadataKey\":81,\"messageType\":3,\"senderDeviceId\":2}"
}"""
    val sampleNewEmailEventPlainText = """
          {
            "rowid": 1,
            "cmd": 101,
            "params":
                "{\"threadId\":\"<15221916.12520@jigl.com>\",\"subject\":\"hello\",\"from\":\"Some One <someone@gmail.com>\",\"to\":\"gabriel@jigl.com\",\"cc\":\"\",\"bcc\":\"\",\"messageId\":\"<15221916.12520@jigl.com>\",\"date\":\"2018-03-27 23:00:13\",\"metadataKey\":81}"
}"""
    val sample2NewEmailEvents = """
          [{
            "rowid": 1,
            "cmd": 101,
            "params":
                "{\"threadId\":\"<15221916.12518@jigl.com>\",\"subject\":\"hello\",\"from\":\"Mayer Mizrachi <mayer@jigl.com>\",\"to\":\"gabriel@jigl.com\",\"cc\":\"\",\"bcc\":\"\",\"messageId\":\"<15221916.12518@jigl.com>\",\"date\":\"2018-03-27 23:00:13\",\"metadataKey\":81,\"messageType\":3,\"senderDeviceId\":1}"
},{
            "rowid": 2,
            "cmd": 101,
            "params":
                "{\"threadId\":\"<15221916.12519@jigl.com>\",\"subject\":\"hello again\",\"from\":\"Gianni Carlo <gianni@jigl.com>\",\"to\":\"gabriel@jigl.com\",\"cc\":\"\",\"bcc\":\"\",\"messageId\":\"<15221916.12519@jigl.com>\",\"date\":\"2018-03-27 23:00:13\",\"metadataKey\":82,\"messageType\":3,\"senderDeviceId\":1}"
}
]"""
    val sampleNewTrackingUpdateEvent = """
            {
                "rowid":39820,
                "cmd":102,
                "params":
                    "{\"type\":7,\"from\":\"lmiguel\",\"date\":\"2018-07-12T04:19:43.623Z\",\"metadataKey\":1937}"
            }
            """
}