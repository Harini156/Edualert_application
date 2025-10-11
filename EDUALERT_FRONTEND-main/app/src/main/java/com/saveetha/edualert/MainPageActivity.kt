package com.saveetha.edualert

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.saveetha.edualert.Login
import kotlin.jvm.java

class MainPageActivity : AppCompatActivity() {

    lateinit var getStartedBtn : Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        getStartedBtn = findViewById(R.id.getStartedBtn)

        getStartedBtn.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
}
