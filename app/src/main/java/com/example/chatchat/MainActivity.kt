package com.example.chatchat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object val db = Firebase.firestore
    private val storage = Firebase.storage
    private val RC_SIGN_IN: Int = 100
    private val RC_GET_IMAGE: Int = 101

    private lateinit var editor: SharedPreferences.Editor
    private lateinit var auth: FirebaseAuth
    private lateinit var etTextOfMessage: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var attachFileButton: ImageButton
    private lateinit var myAdapter: MessageAdapter

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_items, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.itemSignOut) {
            auth.signOut()
            signOut()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sharedPreferences  = getSharedPreferences("username_preferences", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        etTextOfMessage = findViewById(R.id.etTextOfMessage)
        sendButton = findViewById(R.id.sendButton)
        attachFileButton = findViewById(R.id.attachFileButton)
        auth = Firebase.auth

        if (auth.currentUser != null) {
            sendButton.setOnClickListener {
                sendMessage(sharedPreferences.getString("currentUser", "Anonim").toString(), etTextOfMessage.text.toString().trim())
            }
        } else {
            signOut()
        }
    }

    override fun onResume() {
        super.onResume()
        db.collection("users")
            .orderBy("timeOfMessage")
            .addSnapshotListener { value, error ->
                if (value != null) {
                myAdapter = MessageAdapter(messages = value.toObjects(Message::class.java))
                recyclerViewMessageList.layoutManager = LinearLayoutManager(this)
                recyclerViewMessageList.adapter = myAdapter
                recyclerViewMessageList.scrollToPosition(myAdapter.itemCount - 1)
                } else {
                    error?.message.toString()
                }
            }
    }

    private fun sendMessage(author: String, textOfMessage: String) {
        val message = Message(author, textOfMessage, System.currentTimeMillis())
        if (textOfMessage.isNotEmpty()) {
            db.collection("users")
                .add(message)
                .addOnSuccessListener { documentReference ->
                    etTextOfMessage.setText("")
                    myAdapter.notifyDataSetChanged()
                    recyclerViewMessageList.scrollToPosition(myAdapter.itemCount - 1)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this, "Error! ${e.message.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var storageRef = storage.reference
        var imagesRef: StorageReference? = storageRef.child("image")

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (requestCode == Activity.RESULT_OK) {
                var user = auth.currentUser
                if (user != null) {
                    editor.putString("currentUser", auth.currentUser!!.displayName.toString()).apply()
                }
            }
        }
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val providers = arrayListOf(
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build())

                    startActivityForResult(
                        AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                        RC_SIGN_IN)
                }
            }
    }
}