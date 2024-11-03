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
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
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

class RapidFireGameActivity : AppCompatActivity(), GameMode {
    //running is used to activate the timer when true and pause it when false
    var running = true
    //These four variables mimic those found in the Player class, I've created these here so that
    //they can track the players different stats without updating actually updating them in the player
    //object until the end
    var score = 0
    var questionsAnswered = 0
    var questionsCorrectInARow = 0
    var highestQuestionsCorrectInARow = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rapid_fire_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * This function is not in GU plan.
     * This function runs when this screen is opened, not just when this screen is created.
     * onStart() sets the content view to the current activity and runs the play() function.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        setContentView(R.layout.activity_rapid_fire_game)
        play()
    }

    /**
     * This function is in the GU plan
     * Takes in Player player
     * This function is designed to add the different stats tracked as variables to the stats tracked in the player object.
     * Adds the questionsAnswered variable to the players rapidGameQuestionsAnswered variable.
     * Checks if the score variable is higher than the players rapidGameHighestScore variable
     * if it is then set the players rapidGameHighestScore to the score variable.
     * Check if the highestQuestionsCorrectInARow variable is higher than the players rapidGameHighestQuestionsCorrectInARow variable
     * If it is then set the players rapidGameHighestQuestionsCorrectInARow to the rapidGameHighestQuestionsCorrectInARow
     */
    override fun addScore(player: Player) {
        player.rapidGameQuestionsAnswered = player.rapidGameQuestionsAnswered + questionsAnswered

        if(score > player.rapidGameHighestScore){
            player.rapidGameHighestScore = score
        }

        if(highestQuestionsCorrectInARow > player.rapidGameHighestQuestionsCorrectInARow){
            player.rapidGameHighestQuestionsCorrectInARow = highestQuestionsCorrectInARow
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun play() {
        //Sets each visual element to a variable/value, sets context to this and creates a generateQuestion object using context in the constructor
        //sets each button to be disabled
        val context = this
        val questionlbl = findViewById<TextView>(R.id.lblRapidQuestion)
        val generateQuestion = GenerateQuestion(context)
        val btnOne = findViewById<TextView>(R.id.btnRapidAnswerOne)
        val btnTwo = findViewById<TextView>(R.id.btnRapidAnswerTwo)
        val btnThree = findViewById<TextView>(R.id.btnRapidAnswerThree)
        val btnFour = findViewById<TextView>(R.id.btnRapidAnswerFour)
        val timerlbl = findViewById<TextView>(R.id.lblTimer)
        btnOne.setEnabled(false)
        btnTwo.setEnabled(false)
        btnThree.setEnabled(false)
        btnFour.setEnabled(false)

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

        //Attempts to load in local stats from the previous activity
        //If that previous activity was RapidFireGame then these will be loaded and set to those values
        score = intent.getIntExtra("score", 0)
        questionsAnswered = intent.getIntExtra("questionsAnswered", 0)
        questionsCorrectInARow = intent.getIntExtra("questionsCorrectInARow", 0)
        highestQuestionsCorrectInARow = intent.getIntExtra("highestQuestionsCorrectInARow", 0)

        //This button is used to return the player to the ChooseGameMode activity screen
        //When the player attempts to go back, they are warned that their game will end early
        findViewById<Button>(R.id.btnRapidGameBack)
            .setOnClickListener{
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Are you sure you want to exit? Your game will end early")
                builder.setTitle("End game")
                builder.setPositiveButton("Yes"){ _, _ ->
                    val intent = Intent(this, ChooseGameModeActivity::class.java)
                    intent.putExtra("name", player?.name)
                    startActivity(intent)
                }
                builder.setNegativeButton("Cancel", null)

                val dialog = builder.create()
                dialog.show()
            }

        //Sets each visual element to a variable/value, sets them to the player's selected typeface
        //and changes the background to the players selected colour
        val fontId = resources.getIdentifier(player?.font, "font", packageName)
        val typeface = resources.getFont(fontId)
        questionlbl.setTypeface(typeface)
        btnOne.setTypeface(typeface)
        btnTwo.setTypeface(typeface)
        btnThree.setTypeface(typeface)
        btnFour.setTypeface(typeface)

        val targetView = findViewById<View>(R.id.main)
        targetView.setBackgroundColor(Color.parseColor(player?.background))

        //This is where the generateQuestion function is ran in the GenerateQuestion class.
        //When the question has been fetched from the API and formatted
        //The code inside this block is ran
        generateQuestion.generateQuestion { question ->
            //timeRemaining attempts to load from the previous activity
            //If that previous activity was RapidFireGame then the remaining time left is loaded
            //If not then the time is set to 180 seconds
            var timeRemaining: Long? = intent.getLongExtra("time", 180000)

            //choseGameIntent is used to load the chooseGameActivity in the onFinish function
            val chooseGameIntent = Intent(this, ChooseGameModeActivity::class.java)

            //This is where the CountDownTimer is ran for this activity
            //The timer is created using the timeRemaining and the countdown interval is set to 1 second
            //I received help creating this timer from https://www.geeksforgeeks.org/countdowntimer-in-android-using-kotlin/
            //as i've never used a timer before.
            object : CountDownTimer(timeRemaining!!, 1000) {

                //This onTick function is overridden to allow the time remaining to be displayed
                //on the timerlbl, timeRemaining is then set to millisUntilFinished
                override fun onTick(millisUntilFinished: Long) {
                    if(running){
                        timerlbl.setText("" + millisUntilFinished / 1000)
                        timeRemaining = millisUntilFinished
                    }
                }

                //This onFinish function is overridden so that when the timer has ran out, a dialogue box appears
                //that displays how the player did this game, the players score is then added to the current player object
                //and then that object is saved to memory and the ChooseGameMode activity is then ran.
                override fun onFinish() {
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage("Time is up. \nYou managed to answer $questionsAnswered questions. \nYou got $score questions correct. \nYour highest streak of correct answers was $highestQuestionsCorrectInARow")
                    builder.setTitle("Game Over")
                    builder.setCancelable(false)
                    builder.setPositiveButton("Ok"){ _, _ ->
                        if (player != null) {
                            addScore(player)
                        }
                        chooseGameIntent.putExtra("name", player?.name)
                        val json: String = Json.encodeToString(player)
                        serializeData(context, player?.name, json)
                        startActivity(chooseGameIntent)
                    }

                    val dialog = builder.create()
                    dialog.show()
                }
            }.start()

            //Each visual element is updated to reflect the newly fetched question and answers from generateQuestion
            //Each button is set to enabled
            questionlbl.text = question.questionText
            changeTextSize(questionlbl)
            btnOne.text = question.answers.elementAt(0)
            btnTwo.text = question.answers.elementAt(1)
            btnThree.text = question.answers.elementAt(2)
            btnFour.text = question.answers.elementAt(3)
            btnOne.setEnabled(true)
            btnTwo.setEnabled(true)
            btnThree.setEnabled(true)
            btnFour.setEnabled(true)

            //This button is the first answer button on the user interface
            //Once the player selects this button, its colour is changed to green if the answer is correct or red if incorrect.
            //Then if the answer is correct, the timer is paused, each other button is disabled,
            //score is added to and stored in this Activity, and then the activity is recreated
            //If the player is incorrect, the timer is paused, each other button is disabled, questionsCorrectInARow is set to 0
            //questionsAnswered has 1 added to it, a delay of one second is run and then the correct answer
            //is displayed, finally the activity is recreated.
            findViewById<Button>(R.id.btnRapidAnswerOne)
                .setOnClickListener{
                    changeColour(btnOne, checkPlayerAnswer(btnOne.text as String, question.correctAnswer))
                    if(checkPlayerAnswer(btnOne.text as String, question.correctAnswer)){
                        pauseTimer()
                        disableIncorrectButtons(btnTwo, btnThree, btnFour)
                        addGameScore()
                        recreateActivity(player, timeRemaining)
                    }else{
                        pauseTimer()
                        disableIncorrectButtons(btnTwo, btnThree, btnFour)
                        questionsCorrectInARow = 0
                        questionsAnswered = questionsAnswered + 1
                        Handler(Looper.getMainLooper()).postDelayed({
                            displayCorrectAnswer(question.correctAnswer, btnTwo, btnThree, btnFour)
                        }, 1000)
                        recreateActivity(player, timeRemaining)
                    }
                }
            //This button is the second answer button on the user interface
            //Once the player selects this button, its colour is changed to green if the answer is correct or red if incorrect.
            //Then if the answer is correct, the timer is paused, each other button is disabled,
            //score is added to and stored in this Activity, and then the activity is recreated
            //If the player is incorrect, the timer is paused, each other button is disabled, questionsCorrectInARow is set to 0
            //questionsAnswered has 1 added to it, a delay of one second is run and then the correct answer
            //is displayed, finally the activity is recreated.
            findViewById<Button>(R.id.btnRapidAnswerTwo)
                .setOnClickListener{
                    changeColour(btnTwo, checkPlayerAnswer(btnTwo.text as String, question.correctAnswer))
                    if(checkPlayerAnswer(btnTwo.text as String, question.correctAnswer)){
                        pauseTimer()
                        disableIncorrectButtons(btnOne, btnThree, btnFour)
                        addGameScore()
                        recreateActivity(player, timeRemaining)
                    }else{
                        pauseTimer()
                        disableIncorrectButtons(btnOne, btnThree, btnFour)
                        questionsCorrectInARow = 0
                        questionsAnswered = questionsAnswered + 1
                        Handler(Looper.getMainLooper()).postDelayed({
                            displayCorrectAnswer(question.correctAnswer, btnOne, btnThree, btnFour)
                        }, 1000)
                        recreateActivity(player, timeRemaining)

                    }
                }
            //This button is the third answer button on the user interface
            //Once the player selects this button, its colour is changed to green if the answer is correct or red if incorrect.
            //Then if the answer is correct, the timer is paused, each other button is disabled,
            //score is added to and stored in this Activity, and then the activity is recreated
            //If the player is incorrect, the timer is paused, each other button is disabled, questionsCorrectInARow is set to 0
            //questionsAnswered has 1 added to it, a delay of one second is run and then the correct answer
            //is displayed, finally the activity is recreated.
            findViewById<Button>(R.id.btnRapidAnswerThree)
                .setOnClickListener{
                    changeColour(btnThree, checkPlayerAnswer(btnThree.text as String, question.correctAnswer))
                    if(checkPlayerAnswer(btnThree.text as String, question.correctAnswer)){
                        pauseTimer()
                        disableIncorrectButtons(btnTwo, btnOne, btnFour)
                        addGameScore()
                        recreateActivity(player, timeRemaining)
                    }else{
                        pauseTimer()
                        disableIncorrectButtons(btnTwo, btnOne, btnFour)
                        questionsCorrectInARow = 0
                        questionsAnswered = questionsAnswered + 1
                        Handler(Looper.getMainLooper()).postDelayed({
                            displayCorrectAnswer(question.correctAnswer, btnTwo, btnOne, btnFour)
                        }, 1000)
                        recreateActivity(player, timeRemaining)

                    }
                }
            //This button is the fourth answer button on the user interface
            //Once the player selects this button, its colour is changed to green if the answer is correct or red if incorrect.
            //Then if the answer is correct, the timer is paused, each other button is disabled,
            //score is added to and stored in this Activity, and then the activity is recreated
            //If the player is incorrect, the timer is paused, each other button is disabled, questionsCorrectInARow is set to 0
            //questionsAnswered has 1 added to it, a delay of one second is run and then the correct answer
            //is displayed, finally the activity is recreated.
            findViewById<Button>(R.id.btnRapidAnswerFour)
                .setOnClickListener{
                    changeColour(btnFour, checkPlayerAnswer(btnFour.text as String, question.correctAnswer))
                    if(checkPlayerAnswer(btnFour.text as String, question.correctAnswer)){
                        pauseTimer()
                        disableIncorrectButtons(btnTwo, btnThree, btnOne)
                        addGameScore()
                        recreateActivity(player, timeRemaining)
                    }else{
                        pauseTimer()
                        disableIncorrectButtons(btnTwo, btnThree, btnOne)
                        questionsCorrectInARow = 0
                        questionsAnswered = questionsAnswered + 1
                        Handler(Looper.getMainLooper()).postDelayed({
                            displayCorrectAnswer(question.correctAnswer, btnTwo, btnThree, btnOne)
                        }, 1000)
                        recreateActivity(player, timeRemaining)

                    }
                }

        }


    }

    /**
     * This function is not here in GU plan, added after I realised it would work better here then Game
     * Takes in String playerAnswer and String correctAnswer, compares the text on playerAnswer button and correctAnswer
     * if text is equal then return Boolean true (Correct), if not equal then return Boolean false (wrong)
     */
    fun checkPlayerAnswer(playerAnswer: String, correctAnswer: String): Boolean{
        if(playerAnswer.equals(correctAnswer)){
            return true
        }else{
            return false
        }
    }

    /**
     * This function is not in GU plan.
     * Takes in TextView text.
     * if the length of text is greater than 65 characters, changes the font of text to 20.
     * This is done to ensure the question fits on the screen without being cropped.
     */
    fun changeTextSize(text: TextView){
        if(text.length() > 65){
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
        }
    }

    /**
     * This function is not in GU plan, added because it saves repetition in onStart
     * Takes in TextView button and Boolean correctOrWrong (From checkPlayerAnswer).
     * if true then set button background colour to green and button text color to black, if false then set button background colour to red
     */
    fun changeColour(button: TextView, correctOrWrong: Boolean){
        if(correctOrWrong){
            button.setBackgroundColor(Color.GREEN)
            button.setTextColor(Color.BLACK)
        }else{
            button.setBackgroundColor(Color.RED)
        }
    }

    /**
     * This function is not in GU plan, added because it saves repetition in onStart
     * Takes in TextView button1, TextView button2 and TextView button3.
     * Disables each button passed in
     */
    fun disableIncorrectButtons(button1: TextView, button2: TextView, button3: TextView){
        button1.setEnabled(false)
        button2.setEnabled(false)
        button3.setEnabled(false)
    }

    /**
     * This function is not in GU plan, added because it saves repetition in onStart
     * Takes in String correctAnswer, TextView button1, TextView button2 and TextView button3.
     * Checks to see if the text on each button is the same as the correctAnswer.
     * If yes then set that buttons background colour to green and set its text colour to black.
     */
    fun displayCorrectAnswer(correctAnswer: String, button1: TextView, button2: TextView, button3: TextView){
        if(button1.text.equals(correctAnswer)){
            button1.setBackgroundColor(Color.GREEN)
            button1.setTextColor(Color.BLACK)
        }else if(button2.text.equals(correctAnswer)){
            button2.setBackgroundColor(Color.GREEN)
            button2.setTextColor(Color.BLACK)
        }else if(button3.text.equals(correctAnswer)){
            button3.setBackgroundColor(Color.GREEN)
            button3.setTextColor(Color.BLACK)
        }
    }

    /**
     * This function is not in GU plan, added because it saves repetition in onStart
     * Pauses time for 2 seconds, puts each important variable into intent's putExtra
     * and then loads RapidFireGameActivity so that the next question can be asked.
     */
    fun recreateActivity(player: Player?, time: Long?){
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, RapidFireGameActivity::class.java)
            intent.putExtra("time", time)
            intent.putExtra("name", player?.name)
            intent.putExtra("score", score)
            intent.putExtra("questionsAnswered", questionsAnswered)
            intent.putExtra("questionsCorrectInARow", questionsCorrectInARow)
            intent.putExtra("highestQuestionsCorrectInARow", highestQuestionsCorrectInARow)
            startActivity(intent)
        }, 2000)
    }

    /**
     * This function is not in the GU plan
     * This function is used to pause the timer
     */
    fun pauseTimer(){
        running = false
    }

    /**
     * This function is not in the GU plan, thought it's similar to addScore
     * This function is used to increase the local stats variables being tracked in this class which will later
     * be used to add score to the Player class.
     * Adds 1 to questionsAnswered, score and questionsCorrectInARow
     * Checks if questionsCorrectInARow is higher than highestQuestionsCorrectInARow
     * if yes then set highestQuestionsCorrectInARow to questionsCorrectInARow
     */
    fun addGameScore(){
        questionsAnswered = questionsAnswered + 1
        score = score + 1
        questionsCorrectInARow = questionsCorrectInARow + 1

        if(questionsCorrectInARow > highestQuestionsCorrectInARow){
            highestQuestionsCorrectInARow = questionsCorrectInARow
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
}