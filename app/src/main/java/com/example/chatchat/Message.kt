package com.example.chatchat

import java.time.LocalDateTime

data class Message(val author: String = "",
                   val message: String = "",
                   val timeOfMessage: Long = 0) {

    //constructor() : this("", "", "")
}