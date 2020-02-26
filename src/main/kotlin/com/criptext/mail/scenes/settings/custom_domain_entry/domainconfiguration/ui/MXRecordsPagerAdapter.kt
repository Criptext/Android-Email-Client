package com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.criptext.mail.R
import com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration.DomainConfigurationUIObserver

class MXRecordsPagerAdapter(private val context: Context,
                            private val pageArray: List<MXRecordsPagerModel>,
                            private val uiObserver: DomainConfigurationUIObserver): PagerAdapter() {

    private val  inflater: LayoutInflater = (context as AppCompatActivity).layoutInflater

    override fun instantiateItem(container: ViewGroup, position: Int) : Any {
        val view = inflater.inflate(R.layout.activity_domain_configuration_step_2_page, container, false)
        val type = view.findViewById(R.id.type) as TextView
        type.text = pageArray[position].mxRecords.type
        val priority = view.findViewById(R.id.priority) as TextView
        priority.text = pageArray[position].mxRecords.priority.toString()
        val at = view.findViewById(R.id.at) as TextView
        at.text = pageArray[position].mxRecords.host
        val value = view.findViewById(R.id.value) as TextView
        value.text = pageArray[position].mxRecords.pointsTo

        val copyLink = view.findViewById(R.id.copy_link) as TextView
        copyLink.setOnClickListener {
            uiObserver.onCopyButtonClicked(value.text.toString())
        }
        container.addView(view)
        return view
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView((`object` as View))
    }

    override fun getCount(): Int {
        return pageArray.size
    }

}