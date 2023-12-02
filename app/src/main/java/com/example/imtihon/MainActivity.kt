package com.example.imtihon

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.imtihon.databinding.ActivityMainBinding
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private lateinit var questionTextViews: Array<TextView?>
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var answersTextView: Array<TextView?>
    private var speechRecognizer: SpeechRecognizer? = null
    private var previousQuestions: MutableList<String>? = null
    private var textToSpeech: TextToSpeech? = null

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        textToSpeech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener {

            // if No error is found then only it will run
            if(it!=TextToSpeech.ERROR){
                // To Choose language of speech
                textToSpeech?.setLanguage(Locale.ENGLISH);
            }
        })

        binding.btnText.setOnClickListener {
            textToSpeech!!.speak(binding.textView.text.toString(), TextToSpeech.QUEUE_FLUSH, null)
        }



        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val languageResult = textToSpeech!!.setLanguage(Locale.getDefault())
                if (languageResult == TextToSpeech.LANG_MISSING_DATA ||
                    languageResult == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Log.e("TextToSpeech", "Language is not supported or missing data")
                } else {
                    Log.i("TextToSpeech", "TextToSpeech initialized successfully")
                }
            }
        }
        questionTextViews = arrayOfNulls(2)

        questionTextViews[0] = binding.textView
        questionTextViews[1] = binding.textView

        answersTextView = arrayOfNulls(2)

        answersTextView[0] = binding.textView
        answersTextView[1] = binding.textView


        binding.btnText.setOnClickListener {

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            try {
                startActivityForResult(intent, 1)
            } catch (a: ActivityNotFoundException) {
                Toast.makeText(this, "Error ${a.message}", Toast.LENGTH_SHORT).show()
            }
        }
        previousQuestions = ArrayList()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val result = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (result != null && result.size > 0) {
                val question = result[0]


                for (i in 0 until questionTextViews.size) {
                    if (i == previousQuestions!!.size) {
                        previousQuestions!!.add(question)
                        questionTextViews[i]!!.text = question
                        break
                    }
                }

                gen(question)

            }
        }
    }

    private fun generateAnswer(question: String): String {
        val indexOfMeningIsmim = question.indexOf("mening ismim")
        var text = ""
        var ism = ""

        if (indexOfMeningIsmim != -1) {
            // "mening ismim" so'zini o'zgaruvchiga olish
            val meningIsmimWord = question.substring(indexOfMeningIsmim, indexOfMeningIsmim + 12)

            // "mening ismim" so'zi o'chirib qolgan so'zni olish
            val remainingText = question.removeRange(indexOfMeningIsmim, indexOfMeningIsmim + 12)

            ism = remainingText.trim() // Trim ishlatib qolgan so'zni olib tashlash
            text = meningIsmimWord
        }else if (question == "salom"){
            text = "salom"
        }else if (question == "aytib ber"){
            text = "aytib ber"
        }

        return when (text) {
            "salom" -> "Va alaykum assalom"
            "mening ismim" -> "Labbay $ism"
            "aytib ber" -> " "
            else -> "Kechirasiz. Tushunmadim"
        }
    }

    private fun gen(question: String){
        val answer = generateAnswer(question)
        if (answer == " "){
            MediaPlayer.create(this, R.raw.sherlar).start()
        }else {
            answersTextView.get(previousQuestions!!.size - 1)?.setText(answer)
        }
        textToSpeech!!.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (speechRecognizer != null) {
            speechRecognizer!!.destroy()
        }
    }
}

