package com.criptext.mail.scenes.emaildetail.ui.labels

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.criptext.mail.R
import com.criptext.mail.db.models.Label
import com.criptext.mail.utils.virtuallist.VirtualList

/**
 * Created by sebas on 3/14/18.
 */

class LabelListAdapter(private val mContext: Context,
                       private val labels: VirtualList<Label>
) : RecyclerView.Adapter<LabelHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelHolder{
        val mView = LayoutInflater.from(mContext).inflate(R.layout.label_holder, null)
        return LabelHolder(mView)
    }

    override fun getItemCount(): Int {
        return labels.size
    }

    override fun onBindViewHolder(holder: LabelHolder, position: Int) {
        val label = labels[position]
        holder.bindLabel(label)
    }
}
