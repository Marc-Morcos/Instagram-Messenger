package com.example.instagram_messenger

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.instagram_messenger.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {


//        if (checkCallingOrSelfPermission(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE) == PackageManager.PERMISSION_GRANTED) {
            // START SERVICE AS STICKY - BY EXPLICIT INTENT
            // to prevent being started by the system (without the sticky flag)
            val intent = Intent(applicationContext, NLService::class.java)
            startService(intent);
//        } else {
//            //Launch notification access in the settings...
//            val intent = Intent(
//                "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
//            getApplicationContext().startActivity(intent);
//        }


        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

}