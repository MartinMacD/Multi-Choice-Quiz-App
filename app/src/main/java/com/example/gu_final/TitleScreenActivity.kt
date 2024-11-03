/**
 * Martin MacDonald
 * 20/04/2024
 */
package com.example.gu_final

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TitleScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_title_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    override fun onStart() {
        super.onStart()
        setContentView(R.layout.activity_title_screen)

        //Used to increase the size of the title to a size of 30.
        val title = findViewById<TextView>(R.id.lblGameTitle)
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)

        //This button is used to take the player to the LoginActivity
        findViewById<Button>(R.id.btnLoginScreen)
            .setOnClickListener{
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        //This button is used to take the player to the RegistrationActivity
        findViewById<Button>(R.id.btnRegisterScreen)
            .setOnClickListener{
                val intent = Intent(this, RegistrationActivity::class.java)
                startActivity(intent)
            }
    }


}