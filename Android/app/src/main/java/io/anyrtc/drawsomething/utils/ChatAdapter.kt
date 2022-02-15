package io.anyrtc.drawsomething.utils

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import androidx.cardview.widget.CardView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import io.anyrtc.drawsomething.R

class ChatAdapter
    : BaseQuickAdapter<MyConstants.ChatData, BaseViewHolder>(R.layout.item_chat_msg) {

    override fun convert(holder: BaseViewHolder, item: MyConstants.ChatData) {
        val msgContent = String.format("%s:%s", item.uid, item.msg)
        val sp = SpannableString(msgContent)

        sp.setSpan(
            ForegroundColorSpan(if (item.isSelf) Color.parseColor("#59BE6C") else Color.parseColor("#666666")),
            0,
            item.uid.length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        holder.setTextColor(R.id.chat_msg, if (item.isSelf) Color.parseColor("#49AE5C") else Color.parseColor("#333333"))
        holder.setText(R.id.chat_msg, sp)

        val cardView = holder.getView<CardView>(R.id.msg_item)
        cardView.setCardBackgroundColor(if (item.isSelf) Color.parseColor("#CFE6D6") else Color.WHITE)
    }

    override fun addData(data: MyConstants.ChatData) {
        super.addData(data)
        if (this.data.size > 50) {
            this.removeAt(0)
        }
        recyclerView.scrollToPosition(super.data.size - 1)
    }
}
