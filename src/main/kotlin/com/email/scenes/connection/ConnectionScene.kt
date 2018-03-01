package com.email.scenes.connection

import android.widget.ProgressBar

/**
 * Created by sebas on 3/1/18.
 */

interface ConnectionScene {

        fun updateProgress(progress: Int)

        class ConnectionSceneView(private val view: View): ConnectionScene {

            private val res = view.context.resources
            private val progressBar: ProgressBar
            private val percentageAdvanced: TextView

            override fun updateProgress(progress: Int) {
                percentageAdvanced.text = progress.toString()
                progressBar.progress = progress
            }

            init {
                progressBar = view.findViewById(R.id.progressBar)
                percentageAdvanced = view.findViewById(R.id.percentage_advanced)
            }
        }

    }
}