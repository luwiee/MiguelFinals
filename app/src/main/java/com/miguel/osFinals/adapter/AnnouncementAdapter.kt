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
import com.miguel.osFinals.model.Announcement
import com.miguel.osFinals.model.Chat
import com.miguel.osFinals.model.User

import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_chat.*
import java.util.Objects

class AnnouncementAdapter(private val context: Context, private val announcementList: ArrayList<Announcement>, private val userYearLevel:String, private val userCourse: String) :
    RecyclerView.Adapter<AnnouncementAdapter.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_announcement, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return announcementList.size
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUserName:TextView = view.findViewById(R.id.userName)
        val txtMessage:TextView = view.findViewById(R.id.message)
        val txtTargetYearCourse:TextView = view.findViewById(R.id.targetYearAndCourse)
        val imgUser:CircleImageView = view.findViewById(R.id.userImage)
        val layoutUser:LinearLayout = view.findViewById(R.id.layoutUser)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val announcement = announcementList[position]
        var sender: User? = null
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.child(announcement.senderId).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                sender = snapshot.getValue(User::class.java)
                holder.txtUserName.text = sender?.userName

                Glide.with(context).load(sender?.profileImage).placeholder(R.drawable.profile_image).into(holder.imgUser)
            }
        })


        holder.txtTargetYearCourse.text = announcement.yearLevel + "-" + announcement.course
        holder.txtMessage.text = announcement.message
    }

}
