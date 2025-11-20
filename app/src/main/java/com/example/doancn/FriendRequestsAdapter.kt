package com.example.doancn

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.checkerframework.checker.index.qual.Positive

class FriendRequestsAdapter(
    private var listFriendRequests: MutableList<User>,
    private val onAcceptClick: (User) -> Unit,
    private val onDeclineClick: (User) -> Unit

): RecyclerView.Adapter<FriendRequestsAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val imgAvatar: ImageView = view.findViewById(R.id.imgAvatar)
        val name: TextView = view.findViewById(R.id.tvName)
        val btnAccept: Button = view.findViewById(R.id.btnAccept)
        val btnDecline: Button = view.findViewById(R.id.btnDecline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_request, parent, false)
        return ViewHolder(view);
    }
    override fun getItemCount(): Int{
        return listFriendRequests.size;
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val friendRequest = listFriendRequests[position]

        holder.name.text = friendRequest.fullName
        if (friendRequest.profileImage.isNotEmpty()) {
            Glide.with(holder.itemView)
                .load(friendRequest.profileImage)
                .placeholder(R.drawable.person_circle_sharp)
                .into(holder.imgAvatar)
        } else {
            holder.imgAvatar.setImageResource(R.drawable.person_circle_sharp)
        }

        holder.btnAccept.setOnClickListener {
            onAcceptClick(friendRequest)
        }
        holder.btnDecline.setOnClickListener {
            onDeclineClick(friendRequest)
        }
    }
}