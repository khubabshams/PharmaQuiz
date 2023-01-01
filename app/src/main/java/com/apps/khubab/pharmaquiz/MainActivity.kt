package com.apps.khubab.pharmaquiz


import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Chronometer
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.*
import kotlin.math.round

class MainActivity : AppCompatActivity() {

    lateinit var  context: Context

    var stringResult: String = String()
    var position: Int = -1
    var score: Int = 0
    var rightAnswers: ArrayList<Int> = ArrayList()
    var userWrongAnswers: ArrayList<Int> = ArrayList()
    var userAnswers: ArrayList<Int> = ArrayList()
    var questionList: MutableList<QuestionLine> = ArrayList()
    var timeStart: Long = 0
    var timeEnd: Long = 0
    var timeDelta: Long = 0
    var elapsedSeconds: Double = 0.0
    var elapsedMinutes: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this

        val updateQuestionLineThread = Thread {
            ->
            try {
                timeStart = System.currentTimeMillis()
                getQuestions().execute()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        updateQuestionLineThread.start()
    }

    private fun showDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this@MainActivity)

        builder.setTitle(title)
        builder.setMessage(message)
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle(R.string.exit_item)
        builder.setMessage(R.string.exit_msg)
        builder.setPositiveButton(R.string.yes_option) { dialog, _ ->
            dialog.dismiss()
            super.onBackPressed()
        }
        builder.setNegativeButton(R.string.no_option) { dialog, _ -> dialog?.dismiss() }
        builder.show()
    }

    private fun calculateScore(){
        score = 0
        for (i in 0 until rightAnswers.size) {
            if (userAnswers[i] == rightAnswers[i]){
                score++
            }
            else{
                userWrongAnswers.add(i)
            }
        }
    }

    private fun finishChallenge(){
        calculateScore()

        timeEnd = System.currentTimeMillis()
        timeDelta = timeEnd - timeStart
        elapsedSeconds = timeDelta / 1000.0
        elapsedMinutes = elapsedSeconds / 60

        var time: String = ""
        time = if (elapsedMinutes > 1){
            "${round(elapsedMinutes).toInt()} minutes"
        }
        else{
            "${round(elapsedSeconds).toInt()} seconds"
        }

        Toast.makeText(this@MainActivity,"Time you take to solve quiz is $time" , Toast.LENGTH_LONG).show()

        val intent = Intent(this@MainActivity, EndActivity::class.java)
        val preferenceManager: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val highScore = preferenceManager.getInt("highScore", 0)
        var isNewHighScore: Boolean = false

        if (score > highScore){
            val editor = preferenceManager.edit()
            editor.putInt("highScore", score)
            editor.apply()
            isNewHighScore = true
        }

        intent.putExtra("isNewHighScore", isNewHighScore)
        intent.putExtra("score", score)
        intent.putExtra("fullMark", questionList.size)
        intent.putExtra("wrongAnswers", userWrongAnswers)
        intent.putExtra("questions", stringResult)
        intent.putExtra("time", time)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Apply activity transition
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        } else {
            // Swap without transition
            startActivity(intent)
        }
        this.finish()
    }

    fun handleButtons(){
        if (position == 0 || position == -1){
            if (card_previous.visibility  == View.VISIBLE)
                card_previous.visibility = View.INVISIBLE
            if (position == 0){
                tv_next_ques.text = questionList[position + 1].question
            }
        }
        else if (position < questionList.size){
            if ((position + 1) == questionList.size){
                tv_next.text = getString(R.string.finish)
                tv_next_ques.text = getString(R.string.finish_desc)
            }
            else{
                if (tv_next.equals(getString(R.string.finish)))
                    tv_next.text = getString(R.string.next)
                tv_next_ques.text = questionList[position + 1].question
            }
            if (card_previous.visibility  == View.INVISIBLE)
                card_previous.visibility = View.VISIBLE
            tv_previous_ques.text = questionList[position - 1].question
        }
    }

    fun checkSelectedChoice(){
        if (userAnswers[position] != -1){

            val selectedChoice = userAnswers[position]

            when (selectedChoice){
                1 -> rb_choice1.isChecked = true
                2 -> rb_choice2.isChecked = true
                3 -> rb_choice3.isChecked = true
                4 -> rb_choice4.isChecked = true
            }
        }
    }

    fun initializeForm(){

        val animationUtils = AnimationUtils.loadAnimation(this, R.anim.change_question_transition)

        card_question.startAnimation(animationUtils)
        card_answers.startAnimation(animationUtils)

        rg_choice.clearCheck()

        val question = getString(R.string.letter_q)+" "+questionList[position].question
        val choice1 = getString(R.string.letter_a)+" "+questionList[position].choice1
        val choice2 = getString(R.string.letter_b)+" "+questionList[position].choice2
        val choice3 = getString(R.string.letter_c)+" "+questionList[position].choice3
        val choice4 = getString(R.string.letter_d)+" "+questionList[position].choice4

        tv_question.text = question
        rb_choice1.text = choice1
        rb_choice2.text = choice2
        rb_choice3.text = choice3
        rb_choice4.text = choice4
    }

    fun moveToQuestion(move_to: String) {
        val selected = rg_choice.checkedRadioButtonId
        when (selected) {
            rb_choice1.id -> {
                userAnswers[position] = 1
            }
            rb_choice2.id -> {
                userAnswers[position] = 2
            }
            rb_choice3.id -> {
                userAnswers[position] = 3
            }
            rb_choice4.id -> {
                userAnswers[position] = 4
            }
        }

        if (move_to.equals("next")) position++
        else position--

        if (position < questionList.size) {
            initializeForm()
            checkSelectedChoice()
            handleButtons()
        }
        else{
            finishChallenge()
        }

    }
    internal inner class getQuestions: AsyncTask<Void, Void, String>(){

        private var progressBar: ProgressBar = ProgressBar(context)

        override fun onPreExecute() {

            super.onPreExecute()



            l_questions.addView(progressBar)

            progressBar.visibility = View.VISIBLE

        }

        override fun doInBackground(vararg params: Void?): String {

            var lines: List<String> = listOf<String>()
            var result = listOf<JSONObject>()
            val inputStream: InputStream = resources.openRawResource(R.raw.data)
            val bufferedReader: BufferedReader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))

            try{

                //Bypass header
                bufferedReader.readLine()

                //Read all file lines
                lines = bufferedReader.readLines()

                val defaultQuestions = 50

                for (line in lines)
                {
                    val tokens: List<String> = line.split(",")
//                    Math.random().roundToInt().rangeTo(10)
                    val jsonObject: JSONObject = JSONObject()
                    jsonObject.put("sequence", tokens[0].toString())
                    jsonObject.put("question", tokens[1].toString())
                    jsonObject.put("choice1", tokens[2].toString())
                    jsonObject.put("choice2", tokens[3].toString())
                    jsonObject.put("choice3", tokens[4].toString())
                    jsonObject.put("choice4", tokens[5].toString())
                    jsonObject.put("answer", tokens[6].toString())

                    result += jsonObject
                }

            }
            catch(e: Exception){
                e.printStackTrace()
            }
            stringResult = result.toString()
            return result.toString()
        }

        private fun ClosedRange<Int>.rand() =
                Random().nextInt((endInclusive + 1) - start) +  start

        private fun makeString(string: String): String{

            val updatedString = string.toLowerCase().capitalize()

            updatedString.replaceFirst(" ", "", true)

            return updatedString
        }
        override fun onPostExecute(result: String?) {
            try {

                val resultArray = JSONArray(result)

                while(questionList.size < 50){
                    val random = (0 until 74).rand()
                    val currentObject = resultArray.getJSONObject(random)
                    val question = QuestionLine()

                    question.answer = currentObject.getString("answer").toInt()
                    question.sequence = currentObject.getString("sequence").toInt()
                    question.question = currentObject.getString("question").capitalize()
                    question.choice1 = currentObject.getString("choice1").capitalize()
                    question.choice2 = currentObject.getString("choice2").capitalize()
                    question.choice3 = currentObject.getString("choice3").capitalize()
                    question.choice4 = currentObject.getString("choice4").capitalize()

                    questionList.add(question)
                    rightAnswers.add(question.answer)
                    userAnswers.add(-1)
                }
//                for (i in 0 until resultArray.length()){
//                    val currentObject = resultArray.getJSONObject(i)
//                    val question = QuestionLine()
//
//                    question.answer = currentObject.getString("answer").toInt()
//                    question.sequence = currentObject.getString("sequence").toInt()
//                    question.question = currentObject.getString("question")
//                    question.choice1 = currentObject.getString("choice1")
//                    question.choice2 = currentObject.getString("choice2")
//                    question.choice3 = currentObject.getString("choice3")
//                    question.choice4 = currentObject.getString("choice4")
//
//                    questionList.add(question)
//                    rightAnswers.add(question.answer)
//                    userAnswers.add(-1)
//                }
                if (position == -1){
                    handleButtons()
                    position++
                    initializeForm()
                }

                card_next.alpha = 1.toFloat()
                card_next.setOnClickListener({ _: View? ->
                    if (position == -1){
                        handleButtons()
                        position++
                        initializeForm()
                    }
                    else{
                        moveToQuestion("next")
                    }
                })

                card_previous.alpha = 1.toFloat()
                card_previous.setOnClickListener({ _: View? ->
                    moveToQuestion("previous")
                })

            }
            catch(e: JSONException){
                e.printStackTrace()
            }

            progressBar.visibility = View.GONE
            super.onPostExecute(result)
        }
    }

}
