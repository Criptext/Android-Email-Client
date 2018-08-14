package com.criptext.mail.scenes.mailbox.ui.WelcomeTour

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.criptext.mail.R

class WelcomePagerAdapter(private val context: Context,
                          private val pageArray: List<WelcomePagerModel>): PagerAdapter() {

    private val  inflater: LayoutInflater = (context as AppCompatActivity).layoutInflater

    override fun instantiateItem(container: ViewGroup, position: Int) : Any {
        val view = inflater.inflate(R.layout.welcome_slide, container, false)
        val title = view.findViewById(R.id.welcome_title) as TextView
        title.setText(pageArray[position].title)
        val text = view.findViewById(R.id.welcome_text) as TextView
        text.setText(pageArray[position].text)
        val anim = view.findViewById(R.id.welcome_animation) as LottieAnimationView
        anim.setAnimation(pageArray[position].animation)
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