package com.email.utils

import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.rule.ActivityTestRule
import com.email.BaseActivity
import com.email.db.AppDatabase
import com.email.db.KeyValueStorage
import com.email.signal.InDBUser
import com.email.signal.SignalKeyGenerator
import com.email.signal.TestUser

/**
 * Created by gabriel on 4/5/18.
 */

class ActivityWithActiveUserTestRule<T: BaseActivity>(private val activityClass: Class<T>,
                                                      private val newModelFn: () -> Any): IntentsTestRule<T>(activityClass) {

    val keyGenerator = SignalKeyGenerator.Default()
    private lateinit var currentUser: TestUser

    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()
        val ctx = getInstrumentation().targetContext
        val db = AppDatabase.getAppDatabase(ctx)
        db.resetDao().deleteAllData(1)

        val storage = KeyValueStorage.SharedPrefs(ctx)
        currentUser = InDBUser(db, storage, keyGenerator, "gabriel", 1).setup()
        BaseActivity.setCachedModel(this.activityClass, newModelFn())
    }

}