package com.apps.khubab.pharmaquiz

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.CardView
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_end.*
import org.json.JSONArray

class EndActivity : AppCompatActivity() {

    private var score: Int = 0
    private var fullMark: Int = 0
    private var isNewHighScore: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end)

        handleScoreView()

        card_back_main_menu.setOnClickListener {
            val beginIntent = Intent(this@EndActivity, BeginActivity::class.java)
            startActivityWithTransition(beginIntent)
        }

        card_restart.setOnClickListener {
            val mainIntent = Intent(this@EndActivity, MainActivity::class.java)
            startActivityWithTransition(mainIntent)
        }

        card_share.setOnClickListener {
            val shareIntent = Intent()
            val shareMessage = getString(R.string.share_message)+" $score/$fullMark"

            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)

            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_item)))
        }
    }

    private fun startActivityWithTransition(intent: Intent){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Apply activity transition
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        } else {
            // Swap without transition
            startActivity(intent)
        }
        this.finish()
    }

    private fun handleScoreView(){

        score = intent.getIntExtra("score", -1)
        isNewHighScore = intent.getBooleanExtra("isNewHighScore", false)
        fullMark = intent.getIntExtra("fullMark", -1)
        val userWrongAnswers: ArrayList<Int> = intent.getIntegerArrayListExtra("wrongAnswers")
        val questions = intent.getStringExtra("questions")

        val time = getString(R.string.time_msg)+" "+intent.getStringExtra("time")

        tv_score.text = score.toString()
        tv_full_mark.text = fullMark.toString()
        tv_time.text = time

        if (isNewHighScore){
            tv_high_score.visibility = View.VISIBLE
        }

        if(score * 2 >= fullMark){
            tv_end_msg.text = getString(R.string.congrats_item)
        }
        else{
            tv_end_msg.text = getString(R.string.bad_score_item)
        }

        if (userWrongAnswers.isNotEmpty()){
            l_wrong_answers.visibility = View.VISIBLE

            val questionsArray = JSONArray(questions)

            for (i in 0 until questionsArray.length()){
                if (i in userWrongAnswers){
                    val currentObject = questionsArray.getJSONObject(i)

                    val answer = currentObject.getString("answer").toInt()
                    val textAnswer = getString(R.string.answer_title)+" "+currentObject.getString("choice$answer")
                    val question = getString(R.string.question_title)+" "+currentObject.getString("question")

                    val linearLayout: LinearLayout = LinearLayout(this)
                    val cardView : CardView = CardView(this)
                    val tvQues: TextView = TextView(this)
                    val tvRightAnswer: TextView = TextView(this)
                    val vSeparator: View = TextView(this)

                    tvQues.text = question
                    tvRightAnswer.text = textAnswer

                    tvRightAnswer.setTextColor(resources.getColor(R.color.colorAccent))
                    cardView.isClickable = false
                    linearLayout.orientation = LinearLayout.VERTICAL

                    linearLayout.addView(tvQues)
                    linearLayout.addView(tvRightAnswer)
                    linearLayout.addView(vSeparator)

                    cardView.addView(linearLayout)
                    l_wrong_answers.addView(cardView)
                }
            }

        }
    }
}
