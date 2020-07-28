package com.example.chatchat

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageAdapter.MessageHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MessageHolder(inflater, parent)
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: MessageAdapter.MessageHolder, position: Int) {
        val message: Message = messages[position]
        holder.bind(message)
    }

class MessageHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.incoming_message_item, parent, false)) {
    private var mAuthor: TextView? = null
    private var mMessage: TextView? = null

    init {
        mAuthor = itemView.findViewById(R.id.tv_author)
        mMessage = itemView.findViewById(R.id.tv_message)
    }

    fun bind(message: Message) {
        mAuthor?.text = message.author
        mMessage?.text = message.message
    }

}
}