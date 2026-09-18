package com.rfaulkne.karoofueltimer

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        root.addView(TextView(this).apply {
            text = "Fuel Timer installed"
            textSize = 24f
            gravity = Gravity.CENTER
        })

        root.addView(TextView(this).apply {
            text = "Add the Fuel Timer data field to a Karoo ride profile."
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        })

        setContentView(root)
    }
}
