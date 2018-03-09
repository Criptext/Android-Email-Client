package com.email.db.seeders

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
                1 -> label = Label(id = 1, color = "red", text = "1")
                2 -> label = Label(id = 2, color = "green", text = "2")
                3 -> label = Label(id = 3, color = "blue", text = "3")
                4 -> label = Label(id = 4, color = "blue", text = "4")
                5 -> label = Label(id = 5, color = "blue", text = "5")
                6 -> label = Label(id = 6, color = "green", text = "6")
                7 -> label = Label(id = 7, color = "red", text = "7")
                8 -> label = Label(id = 8, color = "red", text = "8")
                9 -> label = Label(id = 9, color = "red", text = "9")
                10 -> label = Label(id = 10, color = "red", text = "10")
            }
            return label
        }
    }
}
