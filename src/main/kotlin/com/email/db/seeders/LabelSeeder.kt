package com.email.db.seeders

import com.email.db.ColorTypes
import com.email.db.LabelTextTypes
import com.email.db.dao.LabelDao
import com.email.db.models.Label

/**
 * Created by sebas on 1/24/18.
 */

class LabelSeeder {
    companion object {
        var labels : List<Label> = mutableListOf()

        fun seed(labelDao: LabelDao){
            labels = labelDao.getAll()
            labelDao.deleteMultipleLabels(labels)
            labels = mutableListOf()
            for (a in 1..7){
                labels += fillLabel(a)
            }
            labelDao.insertAll(labels)
        }

        private fun fillLabel(iteration: Int): Label {
            lateinit var label : Label
            when (iteration) {
                1 -> label = Label(id = 1, color = ColorTypes.GREEN, text = LabelTextTypes.DRAFT)
                2 -> label = Label(id = 2, color = ColorTypes.BLUE, text = LabelTextTypes.INBOX)
                3 -> label = Label(id = 3, color = ColorTypes.RED, text = LabelTextTypes.ARCHIVED)
                4 -> label = Label(id = 4, color = ColorTypes.RED, text = LabelTextTypes.SENT)
                5 -> label = Label(id = 5, color = ColorTypes.WHITE, text = LabelTextTypes.TRASH)
                6 -> label = Label(id = 6, color = ColorTypes.WHITE, text = LabelTextTypes.STARRED)
                7 -> label = Label(id = 7, color = ColorTypes.GREEN, text = LabelTextTypes.SPAM)
            }
            return label
        }
    }
}
