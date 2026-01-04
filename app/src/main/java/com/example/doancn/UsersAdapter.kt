package com.example.doancn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class UsersAdapter (
    private val users:MutableList<User>,
    private val onUserClick: (User)-> Unit
) : RecyclerView.Adapter<UsersAdapter.UserVH>(){

    private val statusFormat = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())

    inner class UserVH(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val imgAvatar: ImageView=itemView.findViewById(R.id.imgAvatar)
        private val tvName: TextView=itemView.findViewById(R.id.tvName)
        private val tvEmail: TextView=itemView.findViewById(R.id.tvEmail)
        private val viewOnline: View = itemView.findViewById(R.id.viewOnline)
        private val tvStatus: TextView=itemView.findViewById(R.id.tvStatus)

        fun bind(user: User){
            tvName.text=user.fullName
            tvEmail.text=user.email
            viewOnline.visibility = if (user.online) View.VISIBLE else View.GONE
            tvStatus.text = when {
                user.online -> "Đang hoạt động"
                user.lastSeen != null -> "Hoạt động cuối: ${statusFormat.format(user.lastSeen)}"
                else -> "Ngoại tuyến"
            }

            if(user.profileImage.isNotEmpty()){
                Glide.with(itemView.context)
                    .load(user.profileImage)
                    .centerCrop()
                    .placeholder(R.drawable.person_circle_sharp)
                    .into(imgAvatar)
            }else{
                imgAvatar.setImageResource(R.drawable.person_circle_sharp)
            }
            itemView.setOnClickListener { onUserClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVH {
        val v= LayoutInflater.from(parent.context).inflate(R.layout.item_user,parent,false)
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
