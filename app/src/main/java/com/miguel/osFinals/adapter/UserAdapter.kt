package com.miguel.osFinals.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.miguel.osFinals.R
import com.miguel.osFinals.activity.ChatActivity
import com.miguel.osFinals.model.Chat
import com.miguel.osFinals.model.User
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_chat.*

class UserAdapter(private val context: Context, private val userList: ArrayList<User>, private val currentUser: User) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var theLastMessage = ""
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Chat")
        val user = userList[position]
        holder.txtUserName.text = user.userName

        Glide.with(context).load(user.profileImage).placeholder(R.drawable.profile_image).into(holder.imgUser)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val chat = dataSnapShot.getValue(Chat::class.java)

                    if (chat!!.senderId.equals(user.userId) && chat!!.receiverId.equals(currentUser.userId) ||
                        chat!!.senderId.equals(currentUser.userId) && chat!!.receiverId.equals(user.userId)
                    ) {
                        theLastMessage = chat.message
                    }
                }

                holder.txtMessage.text = theLastMessage
            }
        })
        holder.layoutUser.setOnClickListener {
            val intent = Intent(context,ChatActivity::class.java)
            intent.putExtra("userId",user.userId)
            intent.putExtra("userName",user.userName)
            intent.putExtra("profileImage", currentUser.profileImage)
            context.startActivity(intent)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val txtUserName:TextView = view.findViewById(R.id.userName)
        val txtMessage:TextView = view.findViewById(R.id.message)
        val imgUser:CircleImageView = view.findViewById(R.id.userImage)
        val layoutUser:LinearLayout = view.findViewById(R.id.layoutUser)
    }
}