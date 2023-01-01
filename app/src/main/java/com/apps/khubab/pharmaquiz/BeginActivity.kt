package com.apps.khubab.pharmaquiz

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import kotlinx.android.synthetic.main.activity_begin.*

class BeginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_begin)

        val intent = Intent(this@BeginActivity, MainActivity::class.java)

        card_begin.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Apply activity transition
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            } else {
                // Swap without transition
                startActivity(intent)
            }
        }
//        card_score.setOnClickListener {
//            // Scoreboard
//            Toast.makeText(this@BeginActivity,"ScoreBoard Under Development" , Toast.LENGTH_LONG).show()
//        }
        card_about.setOnClickListener {
            // About App
            showAboutAlert()
        }
        card_exit.setOnClickListener {
            // Exit
            showExitAlert()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun showAboutAlert(){
        showAlert(getString(R.string.about_item), getString(R.string.about_app), false)
    }

    private fun showExitAlert(){
        showAlert(getString(R.string.exit_item), getString(R.string.exit_msg), true)
    }

    private fun showAlert(title: String, message: String, is_exit: Boolean){
        val builder = AlertDialog.Builder(this@BeginActivity)
//        val builderView = LayoutInflater.from(this).inflate(R.layout.activity_dialog, null)
//
//        builder.setView(builderView)
        builder.setTitle(title)
        builder.setMessage(message)

        if (is_exit) {
            builder.setPositiveButton(getString(R.string.yes_option)) { dialog, _ ->
                dialog.dismiss()
                this.finish()
            }
            builder.setNegativeButton(getString(R.string.no_option)) { dialog, _ ->
                dialog.dismiss()
            }
        }
        else {
            builder.setPositiveButton(getString(R.string.dismiss_option)) { dialog, _ ->
                dialog.dismiss()
            }
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()

    }

}
