package com.miguel.osFinals

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.miguel.osFinals.adapter.AnnouncementAdapter
import com.miguel.osFinals.firebase.FirebaseService
import com.miguel.osFinals.model.Announcement
import com.miguel.osFinals.model.User
import kotlinx.android.synthetic.main.activity_announcement.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_users.*
import kotlinx.android.synthetic.main.activtiy_profile.*

class AnnouncementActivity : AppCompatActivity() {
    var announcementList = ArrayList<Announcement>()

    // Lazy Initialization Variables for Storing User and the Database Reference
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var databaseReference: DatabaseReference
    private lateinit var databaseReferenceUser: DatabaseReference

    private lateinit var userYearLevel: String
    private lateinit var userCourse: String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcement)

        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token = it.token
        }

        var intent = intent
        userYearLevel = intent.getStringExtra("userYearLevel").toString()
        userCourse = intent.getStringExtra("userCourse").toString()

        announcementRecycleView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        getAnnouncementList()
        btnSubmitAnnouncement.setOnClickListener{
            val yearLevel:String= spinnerYearLevel.selectedItem.toString()
            val course:String = spinnerCourse.selectedItem.toString()
            val message:String = etAnnouncement.text.toString()
            val senderID :String = firebaseUser.uid

            etAnnouncement.setText("")
            // Add push notification feature TODO
            sendAnnouncement(senderID, message,yearLevel,course)
        }



        databaseReferenceUser =
            FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)

        databaseReferenceUser.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    if (user.admin != "yes"){
                        adminAnnouncementLayout.visibility = View.INVISIBLE
                    }
                }
            }
        })




    }

    private fun sendAnnouncement(senderId: String, message:String, yearLevel:String, course:String){
        var reference: DatabaseReference? = FirebaseDatabase.getInstance().getReference()

        var hashMap: HashMap<String, String> = HashMap()
        hashMap.put("senderId", senderId)
        hashMap.put("message", message)
        hashMap.put("yearLevel", yearLevel)
        hashMap.put("course", course)
        reference!!.child("Announcements").push().setValue(hashMap)
    }
    private fun getAnnouncementList() {

        var userid = firebaseUser.uid
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userid")


        databaseReference = FirebaseDatabase.getInstance().getReference("Announcements")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                announcementList.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val announcement = dataSnapShot.getValue(Announcement::class.java)

                    // Checking of Year Level and Course
                    if ((announcement!!.yearLevel == userYearLevel && announcement!!.course == userCourse) || announcement.senderId == userid) {
                        announcement?.let { announcementList.add(it) }
                    }
                }
                // Create Users using the User adapter
                val userAdapter = AnnouncementAdapter(this@AnnouncementActivity, announcementList, userYearLevel, userCourse)

                announcementRecycleView.adapter = userAdapter
            }

        })
    }
}