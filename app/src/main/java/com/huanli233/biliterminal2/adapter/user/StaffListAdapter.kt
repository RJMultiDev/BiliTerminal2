package com.huanli233.biliterminal2.adapter.user

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.huanli233.biliterminal2.R
import com.huanli233.biliwebapi.bean.user.UserInfo

class StaffListAdapter(
    context: Context,
    userList: List<UserInfo> = emptyList()
) : UserListAdapter(context, userList) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(this.context).inflate(R.layout.item_uploader, parent, false)
        return Holder(view)
    }
}
