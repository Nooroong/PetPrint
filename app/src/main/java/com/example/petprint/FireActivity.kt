package com.example.petprint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_fire.*

class FireActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fire)


        // Access a Cloud Firestore instance from your Activity
        val db = FirebaseFirestore.getInstance()

        db.collection("todos")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("hangnyang", "${document.id} => ${document.data}")
                    testText.setText(document.id)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("hangnyang", "Error getting documents.", exception)
            }
    }
}