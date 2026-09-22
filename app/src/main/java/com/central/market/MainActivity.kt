package com.central.market

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this)
        textView.text = "CENTRAL MARKET\n\nالتطبيق يعمل بنجاح"
        textView.textSize = 24f
        textView.setPadding(32, 32, 32, 32)

        setContentView(textView)
    }
}
