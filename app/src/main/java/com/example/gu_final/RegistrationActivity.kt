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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()
        setContentView(R.layout.activity_registration)

        //These lines set these two visual elements to values which can then be edited
        val txtRules = findViewById<TextView>(R.id.txtRegRules)
        txtRules.text = "Username Rules: \nMust start with capital letter. \nMust be longer than 3 characters. \nMust not be longer than 15 characters."
        val txtBox = findViewById<TextView>(R.id.txtboxRegEnterUsername)

        //This Regex was created with the help of https://regexr.com/ as this was my first time using Regex
        //The rules for this regex are: The first character must be a capital letter, subsequent characters must be either letters or numbers
        //and the length of the username cannot be below 3 character or exceed 15 characters.
        val regex = Regex("^[A-Z][a-zA-Z0-9]{2,14}$")

        //This button is used to take the player back to the TitleScreenActivity
        findViewById<Button>(R.id.btnRegBack)
            .setOnClickListener{
                val intent = Intent(this, TitleScreenActivity::class.java)
                startActivity(intent)
            }

        //This button is used to submit the username once it has been typed into the txtBox.
        //First checks that the txtBox text matches the regex, if it does then attempts to load a player using that name,
        //If no user is found then create a new player using the name submitted in the txtBox and load the LoginActivity.
        //If the txtBox text doesn't match the regex then display an error dialogue box indicating this to the player.
        //IF the player already exists with that username then display an error dialogue box indicating this to the player.
        findViewById<Button>(R.id.btnRegSubmit)
            .setOnClickListener{
                val name: String
                if(regex.matches(txtBox.text)){
                    name = txtBox.text.toString()
                    if(loadData(this, "$name.json") == null){
                        val player = Player(name)
                        val json: String = Json.encodeToString(player)
                        serializeData(this, player.name, json)
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }else{
                        //This is an example of error handling/prevention
                        txtBox.text = ""
                        popupBox("This user already exists, try again")
                    }
                }else{
                    //This is an example of error handling/prevention
                    txtBox.text = ""
                    popupBox("Username does not match rules, try again")
                }
            }
    }

    /**
     * This function is not in GU plan.
     * Takes in Context context, String? name and String json
     * This function attempts to write the passed in json String to a file using the name String? as part of the file name.
     * If the file is unable to be written then an error message is logged.
     */
    private fun serializeData(context: Context, name: String?, json: String){
        val file = File(context.filesDir, "$name.json")
        try{
            file.writeText(json)
            Log.d("Successful", "Successful")
        }catch(e: Exception){
            //This is an example of error handling/prevention
            Log.e("Failed to write", "Failed to write")
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
     * Is used to save space and helps avoid repetition of code
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