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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.serialization.json.Json
import java.io.File

class ChooseGameModeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choose_game_mode)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        setContentView(R.layout.activity_choose_game_mode)

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
        val fontId = resources.getIdentifier(player?.font, "font", packageName)
        val typeface = resources.getFont(fontId)
        val chooseTitleLbl = findViewById<TextView>(R.id.lblChooseTitle)
        val unGameChoiceBtn = findViewById<TextView>(R.id.btnUnGameChoice)
        val rapidGameChoiceBtn = findViewById<TextView>(R.id.btnRapidGameChoice)
        chooseTitleLbl.setTypeface(typeface)
        unGameChoiceBtn.setTypeface(typeface)
        rapidGameChoiceBtn.setTypeface(typeface)
        val targetView = findViewById<View>(R.id.main)
        targetView.setBackgroundColor(Color.parseColor(player?.background))

        //This button places the players name into the intent extra and then loads the Unlimited Game activity using that intent
        findViewById<Button>(R.id.btnUnGameChoice)
            .setOnClickListener{
                val name = intent.getStringExtra("name")
                val intent = Intent(this, UnlimitedGameActivity::class.java)
                intent.putExtra("name", name)
                startActivity(intent)
            }

        //This button places the players name into the intent extra and then loads the Rapid Game activity using that intent
        findViewById<Button>(R.id.btnRapidGameChoice)
            .setOnClickListener{
                val name = intent.getStringExtra("name")
                val intent = Intent(this, RapidFireGameActivity::class.java)
                intent.putExtra("name", name)
                startActivity(intent)
            }

        //This button displays a dialogue box to the player asking if they are sure they want to log out and go back to the title screen.
        //If yes load the Title Screen activity without including the players name in the intent extra, ensuring that the player is logged out.
        //If no then stay on this screen and don't log out.
        findViewById<Button>(R.id.btnChooseModeBack)
            .setOnClickListener{
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Are you sure you want to log out?")
                builder.setTitle("Logout")
                builder.setPositiveButton("OK"){ _, _ ->
                    val intent = Intent(this, TitleScreenActivity::class.java)
                    startActivity(intent)
                }
                builder.setNegativeButton("Cancel", null)
                val dialog = builder.create()
                dialog.show()
            }

        //This button places the players name and chooseGame into the intent extra so that the Stats activity knows it was loaded from the Choose game mode activity.
        //Then loads the Statistics Game activity using that intent
        findViewById<Button>(R.id.btnChooseGameStats)
            .setOnClickListener{
                val name = intent.getStringExtra("name")
                val intent = Intent(this, StatisticsActivity::class.java)
                intent.putExtra("name", name)
                intent.putExtra("comingFrom", "chooseGame")
                startActivity(intent)
            }

        //This button places the players name and chooseGame into the intent extra so that the Options activity knows it was loaded from the Choose game mode activity.
        //Then loads the Options Game activity using that intent
        findViewById<Button>(R.id.btnChooseGameOptions)
            .setOnClickListener{
                val name = intent.getStringExtra("name")
                val intent = Intent(this, OptionsActivity::class.java)
                intent.putExtra("name", name)
                intent.putExtra("comingFrom", "chooseGame")
                startActivity(intent)
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