package com.criptext.mail.utils.ui

import android.view.View
import android.view.ViewGroup
import java.util.ArrayList
import android.support.v4.view.PagerAdapter

internal class ViewPagerAdapter : PagerAdapter() {

    private val mPresenterList = ArrayList<TabView>()

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj
    }

    override fun getCount(): Int {
        return mPresenterList.size
    }

    fun addView(tabView: TabView) {
        mPresenterList.add(tabView)
    }

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        return mPresenterList[position].view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {

    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mPresenterList[position].title
    }
}