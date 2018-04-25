package com.email.scenes.composer.mocks

import com.email.db.dao.AccountDao
import com.email.db.models.Account

/**
 * Created by jigl on 4/24/18.
 */

class MockedAccountDao: AccountDao{

    override fun insertAll(accounts: List<Account>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insert(account: Account) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAll(): List<Account> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLoggedInAccount(): Account? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteAll(accounts: List<Account>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}