package com.example.za_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_inscription.*
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import androidx.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import android.R.attr.password
import android.os.Handler
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.modal.view.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.DocumentReference
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.FirebaseFirestore


class InscriptionActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    private val mDatabase = FirebaseDatabase.getInstance()

    fun snack(s: String) {
        val snack = Snackbar.make(this.layout1,s,
            Snackbar.LENGTH_LONG)

        snack.setAction("Ok!", View.OnClickListener {

        })
        snack.show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inscription)

        mAuth = FirebaseAuth.getInstance();

        var db = FirebaseFirestore.getInstance();

        btn_register.setOnClickListener {


            if (et_name.text.isNotEmpty() && et_email.text.isNotEmpty() && et_password.text.isNotEmpty()) {

                if ((et_password.text.toString()).equals(et_repassword.text.toString()) ){

                    if (checkBox.isChecked){
                        mAuth.createUserWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    val user = mAuth.currentUser

                                    val userTab : users = users(et_name.text.toString(), et_email.text.toString(),null)

                                    db.collection("users").document(et_email.text.toString().trim())
                                        .set(userTab)
                                    user?.sendEmailVerification()
                                        ?.addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Log.d("User", "Email sent.")
                                            }
                                        }

                                    snack("Bienvenu chez Za!")

                                    val handler = Handler()
                                    handler.postDelayed(Runnable {
                                        val intent = Intent(this@InscriptionActivity,
                                            ConnectActivity::class.java)
                                        startActivity(intent)
                                    }, (1000))


                                } else {
                                    snack("Oups! Une erreur s'est produite lors de l'enregistrement!")
                                    // If sign in fails, display a message to the user.
                                }
                            }
                    }else{
                        snack("Veillez lire et accepter les conditions d'utilisations svp!")
                    }

                }else{
                    snack("Veillez bien confirmer votre mot de passe!")
                }

            }else{
                snack("Vous n'avez pas complèter tous les champs!")
            }

        }

            connexion.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    val intent = Intent(this@InscriptionActivity, ConnectActivity::class.java)
                    startActivity(intent)
                }
            })

            swipeLeft.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    val intent = Intent(this@InscriptionActivity, ConnectActivity::class.java)
                    startActivity(intent)
                }
            })

    }


    override fun onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
    }





}
