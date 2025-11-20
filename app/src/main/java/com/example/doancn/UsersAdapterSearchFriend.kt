package com.example.doancn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class UsersAdapterSearchFriend(
    private val users:MutableList<User>,
    private val onAddFriendClick: (User)-> Unit
) : RecyclerView.Adapter<UsersAdapterSearchFriend.UserVH>(){

    inner class UserVH(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val imgAvatar: ImageView=itemView.findViewById(R.id.imgAvatar)
        private val tvName: TextView=itemView.findViewById(R.id.tvName)

        val btnAdd: Button = itemView.findViewById(R.id.btnAdd)

        fun bind(user: User){
            tvName.text=user.fullName
            if(user.profileImage.isNotEmpty()){
                Glide.with(itemView.context)
                    .load(user.profileImage)
                    .centerCrop()
                    .placeholder(R.drawable.person_circle_sharp)
                    .into(imgAvatar)
            }else{
                imgAvatar.setImageResource(R.drawable.person_circle_sharp)
            }
            val currentUid = FirebaseAuth.getInstance().uid ?: return
            Firebase.firestore.collection("friend_requests")
                .document("${currentUid}_${user.uid}")
                .get()
                .addOnSuccessListener { doc1 ->
                    Firebase.firestore.collection("friend_requests")
                        .document("${user.uid}_$currentUid")
                        .get()
                        .addOnSuccessListener { doc2 ->
                            if (doc1.exists()) {
                                // Đã gửi lời mời
                                btnAdd.text = "Đã gửi"
                                btnAdd.isEnabled = false
                                btnAdd.alpha = 0.7f
                            }else if(doc2.exists()){
                                // Nguoi kia gui
                                btnAdd.text = "Chấp nhan"
                                btnAdd.isEnabled = false
                                btnAdd.alpha = 1f
                            }
                            else {
                                // Chưa gửi → cho phép gửi
                                btnAdd.text = "Kết bạn"
                                btnAdd.isEnabled = true
                                btnAdd.alpha = 1f
                            }

                    }

                }

            btnAdd.setOnClickListener {
                onAddFriendClick(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVH {
        val v= LayoutInflater.from(parent.context).inflate(R.layout.item_user_search,parent,false)
        return UserVH(v)
    }
    override fun onBindViewHolder(holder: UserVH, position: Int) {
        holder.bind(users[position])
    }
    override fun getItemCount(): Int = users.size
    fun updateList(newList: List<User>){
        users.clear()
        users.addAll(newList)
        notifyDataSetChanged()
    }

}