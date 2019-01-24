package com.criptext.mail.scenes.emaildetail.data

data class EmailInlineDownloadData(val fileSize: Long,
                                   val fileName: String,
                                   val fileToken: String,
                                   val emailId: Long,
                                   val fileKey: String?)

data class EmailInlineData(val fileToken: String,
                           val emailId: Long,
                           val filePath: String)