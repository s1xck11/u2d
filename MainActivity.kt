package com.example.ebikedisplay

import android.os.Bundle
import android.widget.TextView
import android.app.Activity
import android.graphics.Color

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this)
        tv.text = "EBike Display работает!"
        tv.textSize = 24f
        tv.setTextColor(Color.WHITE)
        tv.setBackgroundColor(Color.parseColor("#0D0D0D"))
        setContentView(tv)
    }
}