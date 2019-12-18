package com.example.za_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_connect.*
import kotlinx.android.synthetic.main.activity_inscription.*
import kotlinx.android.synthetic.main.app_bar_main.*


class ConnectActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    fun snack(s: String) {
        val snack = Snackbar.make(this.lay,s,
            Snackbar.LENGTH_LONG)
        snack.setAction("Ok!", View.OnClickListener {

        })
        snack.show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        val sharedPref: SharedPreferences = getSharedPreferences("Log", Context.MODE_PRIVATE)

        if (sharedPref.getString("resterConecter", null) == "true"){
            et_email1.setText(sharedPref.getString("lastEmail", null))
            et_password1.setText(sharedPref.getString("pass", null))
            switch1.isChecked = true
        }

        mAuth = FirebaseAuth.getInstance()


        login.setOnClickListener {


            if (et_email1.text.isNotEmpty() && et_password1.text.isNotEmpty()) {

                        mAuth.signInWithEmailAndPassword(et_email1.text.toString(), et_password1.text.toString())
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    val user = mAuth.currentUser
                                    val editor: SharedPreferences.Editor = sharedPref.edit()
                                    editor.putString("lastEmail", user!!.email.toString())
                                    editor.putString("pass", et_password1.text.toString())
                                    editor.commit()
                                    if (switch1.isChecked){
                                        editor.putString("resterConecter", "true")
                                        editor.commit()
                                    }else{
                                        editor.putString("resterConecter", "false")
                                        editor.commit()
                                    }
                                        val intent = Intent(this@ConnectActivity, MainActivity::class.java)
                                        startActivity(intent)

                                } else {
                                    snack("Identifiants invalides!")
                                    // If sign in fails, display a message to the user.
                                }
                            }

                }else{
                    snack("Veillez remplir tous les champs!")
                }


        }



        inscrip.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(this@ConnectActivity, InscriptionActivity::class.java)
                startActivity(intent)
            }
        })



    }


}
