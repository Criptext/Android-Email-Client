package com.criptext.mail.scenes.mailbox.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.R
import com.criptext.mail.db.models.Account
import com.criptext.mail.scenes.mailbox.DrawerMenuItemListener

class AccountAdapter(private val mContext : Context,
                     private var drawerMenuItemListener: DrawerMenuItemListener?,
                     private val accountList: List<Account>,
                     private val badgeCountList: List<Int>)
    : RecyclerView.Adapter<AccountHolder>() {

    override fun onBindViewHolder(holder: AccountHolder, position: Int) {
        val account = accountList[position]
        holder.bindAccount(account, badgeCountList.getOrNull(position) ?: 0)
        holder.setOnClickedListener {
            drawerMenuItemListener?.onAccountClicked(account)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountHolder{
        val mView = LayoutInflater.from(mContext).inflate(R.layout.account_item, null)
        return AccountHolder(mView)
    }

    override fun getItemCount(): Int {
        return accountList.size
    }
}
