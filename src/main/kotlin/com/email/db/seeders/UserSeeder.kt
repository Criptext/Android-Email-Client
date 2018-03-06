package com.email.db.seeders

import com.email.db.DAO.UserDao
import com.email.db.models.User

/**
 * Created by sebas on 2/7/18.
 */

class UserSeeder {

    companion object {
        var users : List<User> = mutableListOf<User>()

        fun seed(userDao: UserDao){
            users = userDao.getAll()
            userDao.deleteAll(users)
            users = mutableListOf<User>()
            for (a in 1..2){
                users += fillUser(a)
            }
            userDao.insertAll(users)
        }


        fun fillUser(iteration: Int): User {
            lateinit var user: User
            when (iteration) {
                1 -> user = User(name = "Andres" ,
                        nickname = "sebas",
                        email = "ascacere92@gmail.com",
                        registrationId = 1256,
                        rawIdentityKeyPair = "tgYgBA59Sg8gHpL93AxcA"
                        )

                2 -> user = User(name = "Sebas" ,
                        nickname = "xndres",
                        email = "xdres@gmail.com",
                        registrationId = 1255,
                        rawIdentityKeyPair = "tgYgBA59Sg8gHpL93AxcA"
                )
            }
            return user
        }
    }
}
