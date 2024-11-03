/**
 * Martin MacDonald
 * 20/04/2024
 */
package com.example.gu_final

import kotlinx.serialization.Serializable

@Serializable
data class Player(var name: String){
    var unlimitedGameScore = 0
    var unlimitedGameQuestionsCorrectInARow = 0
    var unlimitedGameHighestQuestionsCorrectInARow = 0
    var unlimitedGameQuestionsAnswered = 0
    var rapidGameScore = 0
    var rapidGameHighestScore = 0
    var rapidGameQuestionsCorrectInARow = 0
    var rapidGameHighestQuestionsCorrectInARow = 0
    var rapidGameQuestionsAnswered = 0
    var font = "arial"
    var background = "#ffffff"
}