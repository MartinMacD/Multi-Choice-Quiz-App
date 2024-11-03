/**
 * Martin MacDonald
 * 20/04/2024
 */
package com.example.gu_final

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.serialization.json.Json
import java.io.File

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()
        setContentView(R.layout.activity_login)

        val txtBox = findViewById<TextView>(R.id.txtboxEnterUsername)

        //This button is used to return the player to the title screen
        findViewById<Button>(R.id.btnBack)
            .setOnClickListener{
                val intent = Intent(this, TitleScreenActivity::class.java)
                startActivity(intent)
            }

        //This button is used to log the player in if their username matches a stored username
        //Checks that the name field is not empty and that a player with that username actually exists in memory
        //If it does then load the ChooseGameMode activity and pass the players name along
        //If it does not exist then clear the text box and display a dialogue box with an error to the user
        //This is an example of error handling/prevention
        findViewById<Button>(R.id.btnSubmit)
            .setOnClickListener{
                val name = txtBox.getText().toString()

                if(name.isNotEmpty() && loadData(this, "$name.json") != null){

                    val intent = Intent(this, ChooseGameModeActivity::class.java)

                    intent.putExtra("name", name)

                    startActivity(intent)
                }else{
                    //This is an example of error handling/prevention
                    txtBox.text = ""
                    popupBox("This user does not exist")
                }
            }
    }


    /**
     * This function is not in GU plan.
     * Takes in Context context and String filename
     * This function attempts to load a file using the filename passed in.
     * If successful returns a Player object, if unsuccessful returns null.
     */
    private fun loadData(context: Context, filename: String): Player? {
        val file = File(context.filesDir, filename)
        return if(file.exists()){
            try{
                val jsonString = file.readText()
                Json.decodeFromString<Player>(jsonString)
            }catch (e: Exception){
                //This is an example of error handling/prevention
                Log.e("Load Error", e.toString())
                null
            }
        }else{
            //This is an example of error handling/prevention
            Log.e("File does not exist", "File does not exist")
            null
        }
    }

    /**
     * This function is not in GU plan.
     * popupBox is used to construct a dialogue box where the text displayed is passed in as a String
     * Is used to save space and can be used in future if any other dialogue boxes are required
     */
    private fun popupBox(errorText: String){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(errorText)
        builder.setTitle("Error")
        builder.setPositiveButton("OK", null)

        val dialog = builder.create()
        dialog.show()
    }
}