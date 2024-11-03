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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class UnlimitedGameActivity : AppCompatActivity(), GameMode {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_unlimited_game)
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
        setContentView(R.layout.activity_unlimited_game)
        play()
    }

    /**
     * This function is in the GU plan
     * Takes in Player player
     * This function adds one to the passed in player's score and their questions correct in a row variable.
     * Checks if the current amount of questions correct in a row variable is larger than the highest amount of questions correct in a row variable.
     * If true than set highest amount of questions correct in a row variable to current amount of questions correct in a row variable.
     */
    override fun addScore(player: Player) {
        player.unlimitedGameQuestionsAnswered = player.unlimitedGameQuestionsAnswered + 1
        player.unlimitedGameScore = player.unlimitedGameScore + 1
        player.unlimitedGameQuestionsCorrectInARow = player.unlimitedGameQuestionsCorrectInARow + 1
        if(player.unlimitedGameQuestionsCorrectInARow > player.unlimitedGameHighestQuestionsCorrectInARow){
            player.unlimitedGameHighestQuestionsCorrectInARow = player.unlimitedGameQuestionsCorrectInARow
        }
    }

    /**
     * This function is in the GU plan
     * This function runs the majority of functionality in the Unlimited Game.
     * Instantiates all elements on the screen, disables all buttons initially until a question is loaded.
     * When a question is generated, the question text is displayed and each button's text is set to an answer.
     * After the user chooses an answer, it is checked if that answer is correct.
     * If it is correct then add 1 score to the users score and highlight the answer green, then reload this activity.
     * If the answer is incorrect then highlight the users selected answer red and the correct answer green, then reload this activity.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun play() {
        //Sets each visual element to a variable/value, sets context to this and creates a generateQuestion object using context in the constructor
        //sets each button to be disabled
        val context = this
        val questionlbl = findViewById<TextView>(R.id.lblQuestion)
        val generateQuestion = GenerateQuestion(context)
        val btnOne = findViewById<TextView>(R.id.btnAnswerOne)
        val btnTwo = findViewById<TextView>(R.id.btnAnswerTwo)
        val btnThree = findViewById<TextView>(R.id.btnAnswerThree)
        val btnFour = findViewById<TextView>(R.id.btnAnswerFour)
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

        //This button is used to return the player to the ChooseGameMode activity screen
        findViewById<Button>(R.id.btnUnGameBack)
            .setOnClickListener{
                val intent = Intent(this, ChooseGameModeActivity::class.java)
                intent.putExtra("name", player?.name)
                startActivity(intent)
            }
        //This button is used to take the player to the Options activity screen
        findViewById<Button>(R.id.btnUnGameOptions)
            .setOnClickListener{
                val intent = Intent(this, OptionsActivity::class.java)
                intent.putExtra("name", player?.name)
                intent.putExtra("comingFrom", "unlimitedGame")
                startActivity(intent)
            }
        //This button is used to take the player to the Statistics activity screen
        findViewById<Button>(R.id.btnUnGameStats)
            .setOnClickListener{
                val intent = Intent(this, StatisticsActivity::class.java)
                intent.putExtra("name", player?.name)
                intent.putExtra("comingFrom", "unlimitedGame")
                startActivity(intent)
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
            //Then if the answer is correct, each other button is disabled, the player is awarded score, the player is saved to memory and the activity is recreated
            //If the player is incorrect, each other button is disabled, the players unlimitedGameQuestionsCorrectInARow variable is set to 0,
            //the players unlimitedQuestionsAnswered variable is increased by 1, the player is saved to memory, a delay of one second is run and then the correct answer
            //is displayed, finally the activity is recreated.
            findViewById<Button>(R.id.btnAnswerOne)
                .setOnClickListener{
                    changeColour(btnOne, checkPlayerAnswer(btnOne.text as String, question.correctAnswer))
                    if(checkPlayerAnswer(btnOne.text as String, question.correctAnswer)){
                        disableIncorrectButtons(btnTwo, btnThree, btnFour)
                        if (player != null) {
                            addScore(player)
                        }
                        val json: String = Json.encodeToString(player)
                        serializeData(this, player?.name, json)
                        recreateActivity()
                    }else{
                        disableIncorrectButtons(btnTwo, btnThree, btnFour)
                        player?.unlimitedGameQuestionsCorrectInARow = 0
                        if (player != null) {
                            player.unlimitedGameQuestionsAnswered = player.unlimitedGameQuestionsAnswered + 1
                        }
                        val json: String = Json.encodeToString(player)
                        serializeData(this, player?.name, json)
                        Handler(Looper.getMainLooper()).postDelayed({
                            displayCorrectAnswer(question.correctAnswer, btnTwo, btnThree, btnFour)
                        }, 1000)
                        recreateActivity()
                    }
                }
            //This button is the second answer button on the user interface
            //Once the player selects this button, its colour is changed to green if the answer is correct or red if incorrect.
            //Then if the answer is correct, each other button is disabled, the player is awarded score, the player is saved to memory and the activity is recreated
            //If the player is incorrect, each other button is disabled, the players unlimitedGameQuestionsCorrectInARow variable is set to 0,
            //the players unlimitedQuestionsAnswered variable is increased by 1, the player is saved to memory, a delay of one second is run and then the correct answer
            //is displayed, finally the activity is recreated.
            findViewById<Button>(R.id.btnAnswerTwo)
                .setOnClickListener{
                    changeColour(btnTwo, checkPlayerAnswer(btnTwo.text as String, question.correctAnswer))
                    if(checkPlayerAnswer(btnTwo.text as String, question.correctAnswer)){
                        disableIncorrectButtons(btnOne, btnThree, btnFour)
                        if (player != null) {
                            addScore(player)
                        }
                        val json: String = Json.encodeToString(player)
                        serializeData(this, player?.name, json)
                        recreateActivity()
                    }else{
                        disableIncorrectButtons(btnOne, btnThree, btnFour)
                        player?.unlimitedGameQuestionsCorrectInARow = 0
                        if (player != null) {
                            player.unlimitedGameQuestionsAnswered = player.unlimitedGameQuestionsAnswered + 1
                        }
                        val json: String = Json.encodeToString(player)
                        serializeData(this, player?.name, json)
                        Handler(Looper.getMainLooper()).postDelayed({
                            displayCorrectAnswer(question.correctAnswer, btnOne, btnThree, btnFour)
                        }, 1000)
                        recreateActivity()
                    }
                }
            //This button is the third answer button on the user interface
            //Once the player selects this button, its colour is changed to green if the answer is correct or red if incorrect.
            //Then if the answer is correct, each other button is disabled, the player is awarded score, the player is saved to memory and the activity is recreated
            //If the player is incorrect, each other button is disabled, the players unlimitedGameQuestionsCorrectInARow variable is set to 0,
            //the players unlimitedQuestionsAnswered variable is increased by 1, the player is saved to memory, a delay of one second is run and then the correct answer
            //is displayed, finally the activity is recreated.
            findViewById<Button>(R.id.btnAnswerThree)
                .setOnClickListener{
                    changeColour(btnThree, checkPlayerAnswer(btnThree.text as String, question.correctAnswer))
                    if(checkPlayerAnswer(btnThree.text as String, question.correctAnswer)){
                        disableIncorrectButtons(btnTwo, btnOne, btnFour)
                        if (player != null) {
                            addScore(player)
                        }
                        val json: String = Json.encodeToString(player)
                        serializeData(this, player?.name, json)
                        recreateActivity()
                    }else{
                        disableIncorrectButtons(btnTwo, btnOne, btnFour)
                        player?.unlimitedGameQuestionsCorrectInARow = 0
                        if (player != null) {
                            player.unlimitedGameQuestionsAnswered = player.unlimitedGameQuestionsAnswered + 1
                        }
                        val json: String = Json.encodeToString(player)
                        serializeData(this, player?.name, json)
                        Handler(Looper.getMainLooper()).postDelayed({
                            displayCorrectAnswer(question.correctAnswer, btnTwo, btnOne, btnFour)
                        }, 1000)
                        recreateActivity()
                    }
                }
            //This button is the fourth answer button on the user interface
            //Once the player selects this button, its colour is changed to green if the answer is correct or red if incorrect.
            //Then if the answer is correct, each other button is disabled, the player is awarded score, the player is saved to memory and the activity is recreated
            //If the player is incorrect, each other button is disabled, the players unlimitedGameQuestionsCorrectInARow variable is set to 0,
            //the players unlimitedQuestionsAnswered variable is increased by 1, the player is saved to memory, a delay of one second is run and then the correct answer
            //is displayed, finally the activity is recreated.
            findViewById<Button>(R.id.btnAnswerFour)
                .setOnClickListener{
                    changeColour(btnFour, checkPlayerAnswer(btnFour.text as String, question.correctAnswer))
                    if(checkPlayerAnswer(btnFour.text as String, question.correctAnswer)){
                        disableIncorrectButtons(btnTwo, btnThree, btnOne)
                        if (player != null) {
                            addScore(player)
                        }
                        val json: String = Json.encodeToString(player)
                        serializeData(this, player?.name, json)
                        recreateActivity()
                    }else{
                        disableIncorrectButtons(btnTwo, btnThree, btnOne)
                        player?.unlimitedGameQuestionsCorrectInARow = 0
                        if (player != null) {
                            player.unlimitedGameQuestionsAnswered = player.unlimitedGameQuestionsAnswered + 1
                        }
                        val json: String = Json.encodeToString(player)
                        serializeData(this, player?.name, json)
                        Handler(Looper.getMainLooper()).postDelayed({
                            displayCorrectAnswer(question.correctAnswer, btnTwo, btnThree, btnOne)
                        }, 1000)
                        recreateActivity()
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
     * Pauses time for 2 seconds and then loads UnlimitedGameActivity so that the next question can be asked.
     */
    fun recreateActivity(){
        Handler(Looper.getMainLooper()).postDelayed({
            this.recreate()
        }, 2000)
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