package com.criptext.mail.scenes.emaildetail.ui.holders

import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.emaildetail.ui.FullEmailListAdapter
import com.criptext.mail.scenes.emaildetail.ui.labels.LabelsRecyclerView
import com.criptext.mail.utils.virtuallist.VirtualList
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso


class HeaderViewHolder(val view: View, val subject: String, val labels: VirtualList<Label>,
                       val isStarred: Boolean) : RecyclerView.ViewHolder(view) {

    private var labelsRecyclerView: LabelsRecyclerView

    val textViewSubject : TextView = view.findViewById(R.id.textViewSubject)
    val starredImage: ImageView = view.findViewById(R.id.starred)
    val recyclerLabelsView: RecyclerView = view.findViewById(R.id.labels_recycler)
    private var currentStar: Boolean

    init {

        labelsRecyclerView = LabelsRecyclerView(recyclerLabelsView, labels)

        textViewSubject.text = subject

        currentStar = isStarred

        setStarredColor(isStarred)

    }

    private fun setIconAndColor(drawable: Int, color: Int){
        Picasso.with(view.context).load(drawable).into(starredImage, object : Callback {
            override fun onError() {}
            override fun onSuccess() {
                DrawableCompat.setTint(starredImage.drawable,
                        ContextCompat.getColor(view.context, color))
            }
        })
    }

    private fun setStarredColor(starred: Boolean){
        if(starred){
            setIconAndColor(R.drawable.starred, R.color.starred)
        }else
            setIconAndColor(R.drawable.starred_empty, R.color.starred_empty)

    }

    fun notifyLabelsChanged(updatedLabels: VirtualList<Label>, updatedHasStar: Boolean){
        if(updatedHasStar && !currentStar){
            currentStar = true
        }else if(!updatedHasStar && currentStar){
            currentStar = false
        }
        setStarredColor(currentStar)
        labelsRecyclerView = LabelsRecyclerView(recyclerLabelsView, updatedLabels)
    }

    fun setListeners(emailListener: FullEmailListAdapter.OnFullEmailEventListener?) {
        starredImage.setOnClickListener({
            currentStar = !currentStar
            emailListener?.onStarredButtonPressed(currentStar)
            setStarredColor(currentStar)
        })
    }

}
