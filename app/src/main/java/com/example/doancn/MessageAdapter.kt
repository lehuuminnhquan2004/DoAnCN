package com.example.doancn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val messages:List<Message>, private val currentUid:String)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    companion object{
        private const val VIEW_TYPE_MESSAGE_SENT=1
        private const val VIEW_TYPE_MESSAGE_RECEIVED=2
    }

    override fun getItemViewType(position:Int): Int{
        return if(messages[position].senderId==currentUid)
            VIEW_TYPE_MESSAGE_SENT
        else
            VIEW_TYPE_MESSAGE_RECEIVED
    }

    class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtMessage: TextView = view.findViewById(R.id.txtMessageReceived)
    }

    class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtMessage: TextView = view.findViewById(R.id.txtMessageSent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType== VIEW_TYPE_MESSAGE_SENT){
            val view= LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent,parent,false)
            SentViewHolder(view)
        }else{
            val view= LayoutInflater.from(parent.context).inflate(R.layout.item_message_received,parent,false)
            ReceivedViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentViewHolder) holder.txtMessage.text = message.message
        else if (holder is ReceivedViewHolder) holder.txtMessage.text = message.message
    }
    override fun getItemCount()=messages.size

}


