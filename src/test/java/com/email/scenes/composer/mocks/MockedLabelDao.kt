package com.email.scenes.composer.mocks

import com.email.db.MailFolders
import com.email.db.dao.LabelDao
import com.email.db.models.Label

/**
 * Created by gabriel on 4/24/18.
 */

class MockedLabelDao: LabelDao {
    override fun insertAll(labels: List<Label>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAll(): List<Label> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteMultipleLabels(labels: List<Label>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(label: Label) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(labelTextType: MailFolders): Label {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(labelTextTypes: List<String>): List<Label> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}