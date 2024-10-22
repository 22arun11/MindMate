package com.example.applicationlogin

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.chatapp.Message
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ChatbotActivity : ComponentActivity() {
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        val userInputEditText: EditText = findViewById(R.id.et_user_input)
        val askButton: Button = findViewById(R.id.btn_ask)
        val messagesLayout: LinearLayout = findViewById(R.id.ll_messages)
        val scrollView: ScrollView = findViewById(R.id.scroll_view)

        // Align messagesLayout at the bottom
        messagesLayout.gravity = Gravity.BOTTOM

        askButton.setOnClickListener {
            val userInput = userInputEditText.text.toString()
            modelCall(userInput) { response ->
                messages.add(Message(userInput, response))
                addMessageToLayout(userInput, response, messagesLayout)
                userInputEditText.text.clear()
                scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        }
    }

    private fun addMessageToLayout(prompt: String, response: String, layout: LinearLayout) {
        val promptTextView = TextView(this).apply {
            text = prompt
            setBackgroundResource(R.drawable.send_round_box)
            setBackgroundTintList(getColorStateList(R.color.prompt_background))
            setTextColor(getColor(R.color.white))
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                width = resources.displayMetrics.widthPixels / 2
                gravity = Gravity.END or Gravity.BOTTOM
                setMargins(8, 8, 8, 8)
            }
        }

        val responseTextView = TextView(this).apply {
            text = response
            setBackgroundResource(R.drawable.receive_round_box)
            setBackgroundTintList(getColorStateList(R.color.response_background))
            setTextColor(getColor(R.color.white))
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                width = resources.displayMetrics.widthPixels / 2
                gravity = Gravity.START or Gravity.BOTTOM
                setMargins(8, 8, 8, 8)
            }
        }

        layout.addView(promptTextView)
        layout.addView(responseTextView)
    }

    fun modelCall(prompt: String, onResponse: (String) -> Unit) {
        val apiKey = "AIzaSyCcFHvj9xJTiBoKiqXufuzQsR-n4_i3lF0"
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
        MainScope().launch {
            val response = generativeModel.generateContent(prompt)
            response.text?.let { onResponse(it) }
        }
    }
}