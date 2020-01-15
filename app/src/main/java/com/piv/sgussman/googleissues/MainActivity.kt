package com.piv.sgussman.googleissues

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TO-DO: Move this to some OnConnected (to wi-fi or cellular) event
        val intent = Intent(this, RepoViewerActivity::class.java).apply{
            // I don't need to send any state over
        }
        startActivity(intent)
    }

    // TO-DO: implement interface to handle network change events (display different images and
    // text based on whether the user is connected to the internet / data (especially so a user
    // with a connection issue upon opening the app has it take care of itself once the device
    // connects (without needing to restart the app)
}
