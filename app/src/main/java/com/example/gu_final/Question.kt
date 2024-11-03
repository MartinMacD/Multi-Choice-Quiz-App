/**
 * Martin MacDonald
 * 20/04/2024
 */
package com.example.gu_final

class Question (){
    var questionText = "Default"
    var correctAnswer = "Default"
    var answers = mutableListOf<String>()

    /**
     * This function shuffles the answers List to ensure that the order of the answers is randomized for each question
     */
    fun shuffleAnswers(){
        answers.shuffle()
    }
}