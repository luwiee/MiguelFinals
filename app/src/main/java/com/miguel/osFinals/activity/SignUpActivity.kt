package com.miguel.osFinals.activity

import android.app.DownloadManager
import android.content.ContentResolver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.miguel.osFinals.R
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.apache.poi.*
import org.apache.poi.xssf.usermodel.*
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream
import java.io.IOException


class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        btnSignUp.setOnClickListener {
            val userName = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()
            val yearLevel = spnYearLevel.selectedItem.toString()
            val course = spnCourse.selectedItem.toString()
            if (TextUtils.isEmpty(userName)){
                Toast.makeText(applicationContext,"username is required",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(email)){
                Toast.makeText(applicationContext,"email is required",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)){
                Toast.makeText(applicationContext,"password is required",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(confirmPassword)){
                Toast.makeText(applicationContext,"confirm password is required",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword){
                Toast.makeText(applicationContext,"password not match",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            registerUser(userName=userName,email=email, password = password,yearLevel = yearLevel, course = course, autologin = true)

        }

       var myFileIntent: Intent;
        btnImportFile.setOnClickListener {
            myFileIntent =  Intent(Intent.ACTION_GET_CONTENT)
            myFileIntent.type = "*/*"
            startActivityForResult(myFileIntent, 10)
        }

        btnLogin.setOnClickListener {
            val intent = Intent(this@SignUpActivity,
                LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnDownloadTemplate.setOnClickListener{
            val pathReference = storageRef.child("files/Import Template.xlsx")


            //val localFile = File.createTempFile("importTemplate", "xlsx")

            pathReference.downloadUrl.addOnSuccessListener { 
                val url: String = it.toString()
                downloadFile(applicationContext, "importTemplate", ".xlsx", Environment.DIRECTORY_DOWNLOADS, url )
                Toast.makeText(applicationContext,"Template Downloading",Toast.LENGTH_SHORT).show()
            }

        }

    }

    public fun downloadFile(context: Context, fileName: String, fileExtension: String, destinationDirectory: String, url: String){
        val downloadManager: DownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri:Uri = Uri.parse(url)
        val request: DownloadManager.Request = DownloadManager.Request(uri)

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(destinationDirectory, fileName +fileExtension)
        request.setTitle(fileName)

        downloadManager.enqueue(request)

    }

    private fun registerUser(userName:String,email:String,password:String,yearLevel:String,course:String, autologin: Boolean){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){ userCreation ->
                if (userCreation.isSuccessful){
                    val user: FirebaseUser? = auth.currentUser
                    val userId:String = user!!.uid

                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

                    val hashMap:HashMap<String,String> = HashMap()
                    hashMap["userId"] = userId
                    hashMap["userName"] = userName
                    hashMap["profileImage"] = ""
                    hashMap["yearLevel"] = yearLevel
                    hashMap["course"] = course
                    hashMap["admin"] = "False"

                    databaseReference.setValue(hashMap).addOnCompleteListener(this){
                        if (it.isSuccessful){
                            Toast.makeText(applicationContext, "Account created: $userName", Toast.LENGTH_SHORT).show()
                        }
                        if (it.isSuccessful && autologin){
                            //open home activity
                            etName.setText("")
                            etEmail.setText("")
                            etPassword.setText("")
                            etConfirmPassword.setText("")
                            val intent = Intent(this@SignUpActivity,
                                UsersActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
    }
    private fun getFileFromUri(contentResolver: ContentResolver, uri: Uri, directory: File): File {
        val file =
            File.createTempFile("suffix", "prefix", directory)
        file.outputStream().use {
            contentResolver.openInputStream(uri)?.copyTo(it)
        }

        return file
    }
    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10) {
            val uri: Uri? = data?.data
            Toast.makeText(applicationContext, "File selected${uri!!.path}",Toast.LENGTH_SHORT).show()

            var file =  getFileFromUri(applicationContext.contentResolver, uri, applicationContext.cacheDir)
            var fileInputStream: FileInputStream? = null

            try {
                fileInputStream = FileInputStream(file);
                Toast.makeText(applicationContext, fileInputStream.toString(), Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Reading from Excel" + file);

                // Create instance having reference to .xls file
                var workbook: Workbook = WorkbookFactory.create(fileInputStream)
                // Fetch sheet at position 'i' from the workbook
                var sheet: Sheet = workbook.getSheetAt(0)

                // Iterate through each row
                for (row: Row in sheet) {

                    if (row.getRowNum() > 0) {
                        var sArray = Array<String?>(5) { null }
                        var index: Int = 0
                        // Iterate through all the cells in a row (Excluding header row)
                       var cellIterator: Iterator<Cell> = row.cellIterator()


                        while (cellIterator.hasNext()) {
                            var cell:Cell = cellIterator.next()
                            Log.e(TAG,"Cell Value: "+cell.getStringCellValue())
                            sArray.set(index, cell.getStringCellValue())
                            index+=1
                        }

                        val userName = sArray[0]
                        val email = sArray[1]
                        val password = sArray[2]
                        val yearLevel = sArray[3]
                        val course = sArray[4]
                        registerUser(userName=userName!!,email=email!!, password = password!!,yearLevel = yearLevel!!, course = course!!, autologin = false)

                    }
                }
            }catch (e:IOException) {
                Log.e(TAG, "Error Reading Exception: ", e);
            } catch (e:java.lang.Exception) {
                Log.e(TAG, "Failed to read file due to Exception: ", e);
            } finally {
                try {
                    fileInputStream?.close()
                } catch (ex:java.lang.Exception) {
                    ex.printStackTrace();
                }
            }
        }

    }
}
