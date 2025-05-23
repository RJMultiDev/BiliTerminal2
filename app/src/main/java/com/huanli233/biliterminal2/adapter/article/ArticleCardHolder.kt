package com.huanli233.biliterminal2.adapter.article

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.bean.ArticleCard
import com.huanli233.biliterminal2.util.GlideUtil.loadPicture
import com.huanli233.biliterminal2.util.htmlToString

class ArticleCardHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var title: TextView =
        itemView.findViewById(R.id.listArticleTitle)
    var upName: TextView =
        itemView.findViewById(R.id.text_upname)
    var readTimes: TextView =
        itemView.findViewById(R.id.listReadTimes)
    var cover: ImageView =
        itemView.findViewById(R.id.img_cover)
    var readIcon: ImageView =
        itemView.findViewById(R.id.imageView3)
    var upIcon: ImageView =
        itemView.findViewById(R.id.avatarIcon)

    fun showArticleCard(articleCard: ArticleCard, context: Context) {
        title.text = articleCard.title.htmlToString()
        val upNameStr = articleCard.upName
        if (upNameStr.isEmpty()) {
            upName.visibility = View.GONE
            upIcon.visibility = View.GONE
        } else upName.text = upNameStr

        if (articleCard.view.isEmpty()) {
            readIcon.visibility = View.GONE
            readTimes.visibility = View.GONE
        } else readTimes.text = articleCard.view

        cover.loadPicture(articleCard.cover.ifEmpty { R.mipmap.article_placeholder })
    }

}
