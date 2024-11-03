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
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.serialization.json.Json
import java.io.File

class StatisticsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_statistics)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        setContentView(R.layout.activity_statistics)

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
        val lblOptionsTitle = findViewById<TextView>(R.id.lblStatisticsTitle)
        val btnUnlimitedGameStats = findViewById<Button>(R.id.btnUnGameStatsDisplay)
        val btnRapidGameStats = findViewById<Button>(R.id.btnRapidGameStatsDisplay)
        val lblStatsOne = findViewById<TextView>(R.id.lblStatsOne)
        val lblStatsTwo = findViewById<TextView>(R.id.lblStatsTwo)
        val lblStatsThree = findViewById<TextView>(R.id.lblStatsThree)
        val lblStatsUsername = findViewById<TextView>(R.id.lblStatsUsername)
        val statsSubHeadingTitle = findViewById<TextView>(R.id.lblStatsSubheadingTitle)
        lblOptionsTitle.setTypeface(typeface)
        btnUnlimitedGameStats.setTypeface(typeface)
        btnRapidGameStats.setTypeface(typeface)
        lblStatsOne.setTypeface(typeface)
        lblStatsTwo.setTypeface(typeface)
        lblStatsThree.setTypeface(typeface)
        lblStatsUsername.setTypeface(typeface)
        statsSubHeadingTitle.setTypeface(typeface)
        val targetView = findViewById<View>(R.id.main)
        targetView.setBackgroundColor(Color.parseColor(player?.background))

        //Sets the text when loaded to be the players unlimited game stats and sets the UnlimitedGameStats button to false
        statsSubHeadingTitle.text = "Unlimited Game"
        lblStatsOne.text = "Score: " + player?.unlimitedGameScore
        lblStatsTwo.text = "Questions Answered: " + player?.unlimitedGameQuestionsAnswered
        lblStatsThree.text = "Most questions correct in a row streak: " + player?.unlimitedGameHighestQuestionsCorrectInARow
        btnUnlimitedGameStats.isEnabled = false
        btnRapidGameStats.isEnabled = true

        //This button is used to display the players Unlimited Game statistics
        //When selected, each label will display either the score, questions answered or Most questions correct in a row streak
        //Then disables the UnlimitedGameStats button and enables RapidGameStats button
        findViewById<Button>(R.id.btnUnGameStatsDisplay)
            .setOnClickListener{
                statsSubHeadingTitle.text = "Unlimited Game"
                lblStatsOne.text = "Score: " + player?.unlimitedGameScore
                lblStatsTwo.text = "Questions Answered: " + player?.unlimitedGameQuestionsAnswered
                lblStatsThree.text = "Most questions correct in a row streak: " + player?.unlimitedGameHighestQuestionsCorrectInARow
                btnUnlimitedGameStats.isEnabled = false
                btnRapidGameStats.isEnabled = true
            }
        //This button is used to display the players Rapid Game statistics
        //When selected, each label will display either the high score, questions answered or Most questions correct in a row streak
        //Then disables the RapidGameStats button and enables UnlimitedGameStats button
        findViewById<Button>(R.id.btnRapidGameStatsDisplay)
            .setOnClickListener{
                statsSubHeadingTitle.text = "Rapid Fire Game"
                lblStatsOne.text = "High Score: " + player?.rapidGameHighestScore
                lblStatsTwo.text = "Questions Answered: " + player?.rapidGameQuestionsAnswered
                lblStatsThree.text = "Most questions correct in a row streak: " + player?.rapidGameHighestQuestionsCorrectInARow
                btnRapidGameStats.isEnabled = false
                btnUnlimitedGameStats.isEnabled = true
            }

        //Displays the players username in the lblUsername value.
        val lblUsername = findViewById<TextView>(R.id.lblStatsUsername)
        lblUsername.text = "Username: " + player?.name

        //This button is used to return the player to the screen they were previously on
        //The intentExtra is checked for a comingFrom string, if that string is unlimitedGame then the UnlimitedGameActivity is loaded
        //If the string is chooseGame then the ChooseGameModeActivity is loaded.
        findViewById<Button>(R.id.btnStatisticsBack)
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