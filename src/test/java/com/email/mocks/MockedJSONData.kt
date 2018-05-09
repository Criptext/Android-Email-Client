package com.email.mocks

/**
 * Created by gabriel on 5/1/18.
 */
object MockedJSONData {
    val sampleNewEmailEvent = """
          {
            "rowid": 1,
            "cmd": 1,
            "params":
                "{\"threadId\":\"<15221916.12518@jigl.com>\",\"subject\":\"hello\",\"from\":\"Mayer Mizrachi <mayer@jigl.com>\",\"to\":\"gabriel@jigl.com\",\"cc\":\"\",\"bcc\":\"\",\"messageId\":\"<15221916.12518@jigl.com>\",\"date\":\"2018-03-27 23:00:13\",\"metadataKey\":81}"
}"""
    val sample2NewEmailEvents = """
          [{
            "rowid": 1,
            "cmd": 1,
            "params":
                "{\"threadId\":\"<15221916.12518@jigl.com>\",\"subject\":\"hello\",\"from\":\"Mayer Mizrachi <mayer@jigl.com>\",\"to\":\"gabriel@jigl.com\",\"cc\":\"\",\"bcc\":\"\",\"messageId\":\"<15221916.12518@jigl.com>\",\"date\":\"2018-03-27 23:00:13\",\"metadataKey\":81}"
},{
            "rowid": 2,
            "cmd": 1,
            "params":
                "{\"threadId\":\"<15221916.12519@jigl.com>\",\"subject\":\"hello again\",\"from\":\"Gianni Carlo <gianni@jigl.com>\",\"to\":\"gabriel@jigl.com\",\"cc\":\"\",\"bcc\":\"\",\"messageId\":\"<15221916.12519@jigl.com>\",\"date\":\"2018-03-27 23:00:13\",\"metadataKey\":82}"
}
]"""
}