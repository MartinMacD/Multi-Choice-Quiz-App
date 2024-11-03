/**
 * Martin MacDonald
 * 20/04/2024
 */
package com.example.gu_final

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class OptionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_options)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        setContentView(R.layout.activity_options)

        //Used to load the player using the intent from the previous activity
        val player: Player?
        val intent = intent
        val received = intent.getStringExtra("name")


        if(received != null){
            player = loadData(this, "$received.json")
        }else{
            //This is an example of error handling/prevention
            player = Player("Default")
        }

        //Sets each visual element to a variable/value, sets them to the player's selected typeface
        //and changes the background to the players selected colour
        var fontId = resources.getIdentifier(player?.font, "font", packageName)
        var typeface = resources.getFont(fontId)
        val optionsTitle = findViewById<TextView>(R.id.lblOptionsTitle)
        val fontTitle = findViewById<TextView>(R.id.txtChangeFont)
        val backgroundTitle = findViewById<TextView>(R.id.txtChangeBackground)
        val resetProgressBtn = findViewById<TextView>(R.id.btnResetProgress)
        optionsTitle.setTypeface(typeface)
        fontTitle.setTypeface(typeface)
        backgroundTitle.setTypeface(typeface)
        resetProgressBtn.setTypeface(typeface)
        val targetView = findViewById<View>(R.id.main)
        targetView.setBackgroundColor(Color.parseColor(player?.background))

        //Sets up the fonts and backgrounds to be used in the spinner
        val spinnerFont = findViewById<Spinner>(R.id.spinnerFont)
        val spinnerBackground = findViewById<Spinner>(R.id.spinnerBackGround)
        val fonts = arrayOf("Choose font", "Arial", "Times New Roman", "Comic Sans")
        val backgrounds = arrayOf("Choose background color", "White", "Yellow", "Green")

        //This spinner is used to display each available font choice to the player
        //After the player makes their choice, fontChanger is called and the player and selectedItem are passed in
        //The UI elements are then updated to reflect the players chosen font
        //I received help with spinners from https://www.geeksforgeeks.org/spinner-in-kotlin/
        //as I've not worked with spinners before.
        val fontAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fonts)
        spinnerFont.adapter = fontAdapter
        spinnerFont.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position) as String
                fontChanger(player, selectedItem)

                fontId = resources.getIdentifier(player?.font, "font", packageName)
                typeface = resources.getFont(fontId)
                optionsTitle.setTypeface(typeface)
                fontTitle.setTypeface(typeface)
                backgroundTitle.setTypeface(typeface)
                resetProgressBtn.setTypeface(typeface)

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        //This spinner is used to display each available background choice to the player
        //After the player makes their choice, backgroundChanger is called and the player and selectedItem are passed in
        //The UI elements are then updated to reflect the players chosen background
        //I received help with spinners from https://www.geeksforgeeks.org/spinner-in-kotlin/
        val backgroundAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, backgrounds)
        spinnerBackground.adapter = backgroundAdapter
        spinnerBackground.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position) as String
                backgroundChanger(player, selectedItem)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        //This button is used to reset the progress of the currently logged in player.
        //Upon pressing the button, a dialogue box will appear asking if you want to reset your progress
        //If yes then the all the players statistics will be reset back to 0
        findViewById<Button>(R.id.btnResetProgress)
            .setOnClickListener{
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Are you sure you want to reset your progress?")
                builder.setTitle("Reset Progress")
                builder.setPositiveButton("Yes"){ _, _ ->
                    player?.unlimitedGameScore = 0
                    player?.unlimitedGameQuestionsCorrectInARow = 0
                    player?.unlimitedGameHighestQuestionsCorrectInARow = 0
                    player?.unlimitedGameQuestionsAnswered = 0
                    player?.rapidGameScore = 0
                    player?.rapidGameHighestScore = 0
                    player?.rapidGameQuestionsCorrectInARow = 0
                    player?.rapidGameHighestQuestionsCorrectInARow = 0
                    player?.rapidGameQuestionsAnswered = 0
                    val json: String = Json.encodeToString(player)
                    serializeData(this, player?.name, json)

                }
                builder.setNegativeButton("Cancel", null)

                val dialog = builder.create()
                dialog.show()
            }

        //This button is used to return the player to the UnlimitedGame activity or ChooseGameMode activity depending on where they came from
        findViewById<Button>(R.id.btnOptionsBack)
            .setOnClickListener{
                val receivedBack = intent.getStringExtra("comingFrom")

                if(receivedBack.equals("unlimitedGame")){
                    val intent = Intent(this, UnlimitedGameActivity::class.java)
                    intent.putExtra("name", player?.name)
                    startActivity(intent)
                }else if(receivedBack.equals("chooseGame")){
                    val intent = Intent(this, ChooseGameModeActivity::class.java)
                    intent.putExtra("name", player?.name)
                    startActivity(intent)
                }

            }
    }

    /**
     * This function is in the GU plan but is called changeFont there
     * Takes in Player player and String font
     * Checks the font passed in against each font available and sets the players font variable to the corresponding font String
     * player is then saved to file
     */
    private fun fontChanger(player: Player?, font: String){
        if(font.equals("Arial")){
            player?.font = "arial"
        }else if(font.equals("Times New Roman")){
            player?.font = "times"
        }else if(font.equals("Comic Sans")){
            player?.font = "comic"
        }else{
            if (player != null) {
                player.font = player.font
            }
        }
        val json: String = Json.encodeToString(player)
        serializeData(this, player?.name, json)
    }

    /**
     * This function is in the GU plan but is called changeBackgroundColour there
     * Takes in Player player and String background
     * Checks the background passed in against each background available and sets the players background variable to the corresponding background String
     * player is then saved to file
     */
    private fun backgroundChanger(player: Player?, background: String){
        if(background.equals("White")){
            player?.background = "#ffffff"
        }else if(background.equals("Yellow")){
            player?.background = "#ffee70"
        }else if(background.equals("Green")){
            player?.background = "#8fff70"
        }else{
            if (player != null) {
                player.background = player.background
            }
        }
        val json: String = Json.encodeToString(player)
        serializeData(this, player?.name, json)
        val targetView = findViewById<View>(R.id.main)
        targetView.setBackgroundColor(Color.parseColor(player?.background))
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
}