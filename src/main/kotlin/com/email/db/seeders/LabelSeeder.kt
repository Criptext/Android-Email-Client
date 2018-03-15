package com.email.db.seeders

import com.email.db.ColorTypes
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
            for (a in 1..10){
                labels += fillLabel(a)
            }
            labelDao.insertAll(labels)
        }

        private fun fillLabel(iteration: Int): Label {
            lateinit var label : Label
            when (iteration) {
                1 -> label = Label(id = 1, color = ColorTypes.GREEN, text = "DRAFT")
                2 -> label = Label(id = 2, color = ColorTypes.BLUE, text = "INBOX")
                3 -> label = Label(id = 3, color = ColorTypes.RED, text = "NO SE QUE VA ACA")
                4 -> label = Label(id = 4, color = ColorTypes.RED, text = "4")
                5 -> label = Label(id = 5, color = ColorTypes.WHITE, text = "5")
                6 -> label = Label(id = 6, color = ColorTypes.GREEN, text = "6")
                7 -> label = Label(id = 7, color = ColorTypes.WHITE, text = "7")
                8 -> label = Label(id = 8, color = ColorTypes.GREEN, text = "8")
                9 -> label = Label(id = 9, color = ColorTypes.WHITE, text = "9")
                10 -> label = Label(id = 10, color = ColorTypes.GREEN, text = "10")
            }
            return label
        }
    }
}
