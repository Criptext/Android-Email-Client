package com.email.scenes.search.holders

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.email.R

/**
 * Created by danieltigse on 2/5/18.
 */

class SearchHolder(view: View): RecyclerView.ViewHolder(view){

    private val textViewSubject : TextView = view.findViewById(R.id.textViewSubject)
    val rootView: View = view.findViewById(R.id.rootView)

    fun bindWithSearch(search: String){
        textViewSubject.text = search
    }

}