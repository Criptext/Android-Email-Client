package com.email.DB.seeders

import android.util.Log
import com.email.DB.DAO.EmailDao
import com.email.DB.models.Email
import java.text.SimpleDateFormat

/**
 * Created by sebas on 1/24/18.
 */

public class EmailSeeder {
    companion object {
        var emails : List<Email> = mutableListOf<Email>()
        var sdf : SimpleDateFormat = SimpleDateFormat( "yyyy-MM-dd HH:mm:dd")

        fun seed(emailDao: EmailDao){
            emails = emailDao.getAll()
            Log.d("SEEDER - EMAILS", emails.size.toString())
            emailDao.deleteAll(emails)
            emails = mutableListOf<Email>()
            for (a in 1..10){
                emails += fillEmail(a)
            }
            emailDao.insertAll(emails)
        }


        fun fillEmail(iteration: Int): Email {
            lateinit var email: Email
            when (iteration) {
                1 -> email = Email( id = 1,
                        content = "Contenido 1",
                        date = sdf.parse("1992-05-23 20:12:58"),
                        delivered = 1,
                        isDraft = 0,
                        isTrash = 0,
                        key = "key",
                        preview = "preview 1" ,
                        secure = 1,
                        subject = "Subject",
                        threadid = "threadid",
                        unread = 0)

                2 -> email = Email(id = 2,
                        content = "Contenido 2",
                        date = sdf.parse("1993-03-02 18:12:29"),
                        delivered = 1,
                        isDraft = 0,
                        isTrash = 0,
                        key = "key",
                        preview = "preview 2" ,
                        secure = 1,
                        subject = "Subject 2",
                        threadid = "threadid",
                        unread = 1)
                3 -> email = Email( id = 3,
                        content = "Contenido 3",
                        date = sdf.parse("2013-05-23 20:12:58"),
                        delivered = 1,
                        isDraft = 0,
                        isTrash = 0,
                        key = "key",
                        preview = "preview" ,
                        secure = 1,
                        subject = "Subject",
                        threadid = "threadid",
                        unread = 0)
                4 -> email = Email(id = 4,
                        content = "Contenido 4",
                        date = sdf.parse("2017-05-21 20:12:58"),
                        delivered = 1,
                        isDraft = 0,
                        isTrash = 0,
                        key = "key",
                        preview = "preview" ,
                        secure = 1,
                        subject = "Subject",
                        threadid = "threadid",
                        unread = 0)
                5 -> email = Email(id = 5,
                        content = "Contenido 5",
                        date = sdf.parse("1992-05-23 20:12:58"),
                        delivered = 1,
                        isDraft = 0,
                        isTrash = 0,
                        key = "key",
                        preview = "preview" ,
                        secure = 1,
                        subject = "Subject",
                        threadid = "threadid",
                        unread = 0)
                6 -> email = Email(id = 6,
                        content = "Contenido 6",
                        date = sdf.parse("1992-05-23 20:12:58"),
                        delivered = 1,
                        isDraft = 0,
                        isTrash = 0,
                        key = "key",
                        preview = "preview" ,
                        secure = 1,
                        subject = "Subject",
                        threadid = "threadid",
                        unread = 0)
                7 -> email = Email(id = 7,
                        content = "Contenido 7",
                        date = sdf.parse("1992-05-23 20:12:58"),
                        delivered = 1,
                        isDraft = 0,
                        isTrash = 0,
                        key = "key",
                        preview = "preview" ,
                        secure = 1,
                        subject = "Subject",
                        threadid = "threadid",
                        unread = 0)
                8 -> email = Email(id = 8,
                        content = "Contenido 8",
                        date = sdf.parse("1992-05-23 20:12:58"),
                        delivered = 1,
                        isDraft = 0,
                        isTrash = 0,
                        key = "key",
                        preview = "preview" ,
                        secure = 1,
                        subject = "Subject",
                        threadid = "threadid",
                        unread = 0)
                9 -> email = Email(id = 9,
                        content = "Contenido 9",
                        date = sdf.parse("1992-05-23 20:12:58"),
                        delivered = 1,
                        isDraft = 0,
                        isTrash = 0,
                        key = "key",
                        preview = "preview" ,
                        secure = 1,
                        subject = "Subject",
                        threadid = "threadid",
                        unread = 0)
                10 -> email = Email(id = 10,
                        content = "Contenido 10",
                        date = sdf.parse("1992-05-23 20:12:58"),
                        delivered = 1,
                        isDraft = 0,
                        isTrash = 0,
                        key = "key",
                        preview = "preview" ,
                        secure = 1,
                        subject = "Subject",
                        threadid = "threadid",
                        unread = 0)
            }
            return email
        }
    }
}
