package com.apps.khubab.pharmaquiz

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock.sleep
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_start_screen.*

class StartScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_screen)

        val animationUtils = AnimationUtils.loadAnimation(this, R.anim.start_transition)

        tv_start.startAnimation(animationUtils)
        iv_start.startAnimation(animationUtils)

        val intent = Intent(this@StartScreenActivity, BeginActivity::class.java)

        val splashThread = Thread {
            ->
           try {
               sleep(3000)
           }
           catch (e: InterruptedException){
               e.printStackTrace()
           }
            finally {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Apply activity transition
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
                } else {
                    // Swap without transition
                    startActivity(intent)
                }
                this.finish()
            }
        }

        splashThread.start()
    }
}
