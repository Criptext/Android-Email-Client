package com.email.scenes.keygeneration

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.email.R

/**
 * Created by sebas on 2/28/18.
 */

interface KeyGenerationScene {

    fun updateProgress(progress: Int)

    class KeyGenerationSceneView(private val view: View): KeyGenerationScene {

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
