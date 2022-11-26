package com.miguel.osFinals.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.miguel.osFinals.R
import com.miguel.osFinals.adapter.UserAdapter
import com.miguel.osFinals.firebase.FirebaseService
import com.miguel.osFinals.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.miguel.osFinals.AnnouncementActivity
import kotlinx.android.synthetic.main.activity_users.*

class UsersActivity : AppCompatActivity() {
    var userList = ArrayList<User>()
    // Lazy Initialization Variables for Storing User and the Database Reference
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var databaseReference: DatabaseReference
    var currentUser : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        FirebaseService.sharedPref = getSharedPreferences("sharedPref",Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token = it.token
        }

        userRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)


        // Add a Click Listener for the Profile Image
        imgProfile.setOnClickListener {
            val intent = Intent(
                this@UsersActivity,
                ProfileActivity::class.java
            )
            startActivity(intent)
        }
        btnAnnouncementButton.setOnClickListener{
            val intent = Intent(
                this@UsersActivity,
                AnnouncementActivity::class.java
            )
            // INTENT FOR YEARLEVEL COURSE
            intent.putExtra("userYearLevel", currentUser?.yearLevel)
            intent.putExtra("userCourse", currentUser?.course)
            startActivity(intent)
        }
        getUsersList()
    }

    /**
     * Function for Getting the List of Users from the Database and
     * Populating the User List by Calling the User Adapter
     */
    private fun getUsersList() {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        var userid = firebaseUser.uid
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userid")


        databaseReference = FirebaseDatabase.getInstance().getReference("Users")


        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val user = dataSnapShot.getValue(User::class.java)

                    if (user!!.userId != firebaseUser.uid) {
                        userList.add(user)
                    }else{
                        currentUser = user
                        if (user!!.profileImage == ""){
                            imgProfile.setImageResource(R.drawable.profile_image)
                        }else{
                            Glide.with(this@UsersActivity).load(user.profileImage).into(imgProfile)
                        }
                    }
                }
                // Create Users using the User adapter
                val userAdapter = UserAdapter(this@UsersActivity, userList, currentUser!!)

                userRecyclerView.adapter = userAdapter
            }

        })
    }
}