package com.practicum.playlistmaker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SearchActivity : AppCompatActivity() {

    var searchedText: String = EMPTY_TEXT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Кнопка Назад
        val toMainButton = findViewById<ImageView>(R.id.button_to_main)
        toMainButton.setOnClickListener {
            val displayIntent = Intent(this, MainActivity::class.java)
            startActivity(displayIntent)
        }

        //Обработка введенного текста в EditText + очистка введенного текста (Х)
        val searchEditText = findViewById<EditText>(R.id.search_editText)
        val clearButton = findViewById<ImageView>(R.id.search_clear_imageView)

        val searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = clearButtonVisibility(s)
            }
            override fun afterTextChanged(s: Editable?) {
                searchedText = s.toString()
            }
        }
        searchEditText.addTextChangedListener(searchTextWatcher)

        clearButton.visibility = clearButtonVisibility(searchEditText.text)

        clearButton.setOnClickListener {
            searchEditText.setText("")
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    // Переопределяем сохранение Activity
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCHED_TEXT, searchedText)
    }

    // Переопределяем получение Activity
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        searchedText = savedInstanceState.getString(SEARCHED_TEXT, EMPTY_TEXT)
        val searchEditText = findViewById<EditText>(R.id.search_editText)
        searchEditText.setText(searchedText)
    }


    //Константные переменные для сохранения и получения значений
    companion object {
        const val SEARCHED_TEXT = "SEARCHED_TEXT"
        const val EMPTY_TEXT = ""
    }


}
