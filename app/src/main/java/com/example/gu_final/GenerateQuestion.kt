/**
 * Martin MacDonald
 * 20/04/2024
 */
package com.example.gu_final

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley

class GenerateQuestion(val context: Context) {
    private val question = Question()
    /**
     * generateQuestion is used to send a GET request to the trivia API, receive that request and then parse it into separate variables within the Question class.
     * generateQuestion makes use of a callback,this ensures that the GET request is complete before the rest of the quiz game begins.
     * I received help with calling the API from https://www.delasign.com/blog/android-studio-kotlin-api-call/ as I've not worked with API's before.
     * I received help with creating the callback from https://medium.com/@agayevrauf/kotlin-callback-functions-with-code-examples-3eb1f6bcadcf as I've not worked with callbacks before.
     */
    fun generateQuestion(callback: (Question) -> Unit){
        val url = "https://the-trivia-api.com/v2/questions"
        val queue = Volley.newRequestQueue(context)
        //This request attempts to perform a GET on the supplied URL.
        //If successful then each Question field will be set using the received values.
        //If unsuccessful then an error message is logged to help with debugging.
        val request = JsonArrayRequest(Request.Method.GET, url, null, { response ->
            try{
                val triviaObject = response.getJSONObject(0)
                val correctAnswer = triviaObject.getString("correctAnswer")
                question.correctAnswer = correctAnswer

                val questionText = triviaObject.getJSONObject("question").getString("text")
                question.questionText = questionText

                val tempArray = triviaObject.getJSONArray("incorrectAnswers")
                for(i in 0 until tempArray.length()){
                    question.answers.add(tempArray.getString(i))
                }
                //The correct answer is added to the answers array and then shuffled so that the position of the correct answer in the array is randomized each time.
                question.answers.add(correctAnswer)
                question.shuffleAnswers()

                callback(question)
            } catch (e: Exception) {
                //This is an example of error handling/prevention
                Log.e("Volley Error", "Volley Error")
            }
        }, { error ->
            //This is an example of error handling/prevention
            Log.e("Volley Error", error.toString())
        })
        queue.add(request)
    }
}