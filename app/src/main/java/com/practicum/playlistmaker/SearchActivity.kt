package com.practicum.playlistmaker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsetsAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    var searchedText: String = EMPTY_TEXT

    //
    private val itunesBaseUrl = "https://itunes.apple.com"
    var trackList: MutableList<Track> = mutableListOf()

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

        //Кнопка Обновить
        val searchErrorRefreshButton = findViewById<Button>(R.id.search_error_refresh_button)

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

        //Список песен
        val recyclerView = findViewById<RecyclerView>(R.id.trackList)
        //val trackList: ArrayList<Track> = createExampleTrackList()
        recyclerView.layoutManager = LinearLayoutManager(this)
        //recyclerView.adapter = TrackAdapter(trackList)
        val retrofit = Retrofit.Builder()
            .baseUrl(itunesBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val searchTrackService = retrofit.create(Itunes::class.java)


        clearButton.visibility = clearButtonVisibility(searchEditText.text)

        clearButton.setOnClickListener {
            searchEditText.setText("")
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            //
            showSearchResults(TrackSearchResultsType.EMPTY, recyclerView)
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                getTrackSearchResults(searchTrackService, searchEditText.text.toString(), recyclerView)
            }
            false
        }


        searchErrorRefreshButton.setOnClickListener {
            getTrackSearchResults(searchTrackService, searchEditText.text.toString(), recyclerView)
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


    //Мок-объект для списка песен, чтобы наполнить trackList. Больше не нужен
//    private fun createExampleTrackList(): ArrayList<Track> {
//        val track1 = Track("Smells Like Teen Spirit", "Nirvana", 0, "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg")
//        val track2 = Track("Billie Jean", "Michael Jackson", 0, "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg")
//        val track3 = Track("Stayin' Alive", "Bee Gees", 0, "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg")
//        val track4 = Track("Whole Lotta Love", "Led Zeppelin", 0, "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg")
//        val track5 = Track("Sweet Child O'Mine", "Guns N' Roses", 0, "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg")
//
//        val trackList: ArrayList<Track> = arrayListOf<Track>(track1, track2, track3, track4, track5)
//        return trackList
//    }


//Покажем резулататы поиска
    fun showSearchResults(trackSearchResultsType: TrackSearchResultsType, recyclerView: RecyclerView){

        //Плейсхолдеры для ошибочного результата поиска и отсутствия результата
        val searchErrorPlaceholder = findViewById<FrameLayout>(R.id.search_error_placeholder)
        val searchNoResultsPlaceholder = findViewById<FrameLayout>(R.id.search_no_results_placeholder)


        when (trackSearchResultsType){
            //нашли
            TrackSearchResultsType.SUCCESS -> {

                val trackAdapter = TrackAdapter(trackList)
                recyclerView.adapter = trackAdapter

                recyclerView.visibility = View.VISIBLE
                searchErrorPlaceholder.visibility = View.GONE
                searchNoResultsPlaceholder.visibility = View.GONE
            }

            //очистили строку поиска
            TrackSearchResultsType.EMPTY -> {

                trackList.clear()
                val trackAdapter = TrackAdapter(ArrayList<Track>(0))
                recyclerView.adapter = trackAdapter

                recyclerView.visibility = View.VISIBLE
                searchErrorPlaceholder.visibility = View.GONE
                searchNoResultsPlaceholder.visibility = View.GONE
            }

            //ошибка
            TrackSearchResultsType.ERROR -> {
                recyclerView.visibility = View.GONE
                searchErrorPlaceholder.visibility = View.VISIBLE
                searchNoResultsPlaceholder.visibility = View.GONE
            }

            //ничего не найдено
            TrackSearchResultsType.NO_RESULTS -> {
                recyclerView.visibility = View.GONE
                searchErrorPlaceholder.visibility = View.GONE
                searchNoResultsPlaceholder.visibility = View.VISIBLE
            }

        }
    }

    fun getTrackSearchResults(searchTrackService: Itunes, expression: String, recyclerView: RecyclerView) {
        searchTrackService
            .search(expression)
            .enqueue(object : Callback<SearchTrackResponse> {

                //сервер дал ответ для запроса
                override fun onResponse(
                    call: Call<SearchTrackResponse>,
                    response: Response<SearchTrackResponse>
                )

                {
                    when (response.code()) {

                        200 -> {

                            val searchTrackResults = response.body()?.results
                            trackList.clear()

                            if (searchTrackResults != null) {
                                if (searchTrackResults.size > 0) {
                                    trackList.addAll(searchTrackResults)
                                    showSearchResults(TrackSearchResultsType.SUCCESS, recyclerView)
                                } else {
                                    showSearchResults(TrackSearchResultsType.NO_RESULTS, recyclerView)
                                }
                            } else {
                                showSearchResults(TrackSearchResultsType.NO_RESULTS, recyclerView)
                            }
                        }
                        else -> {
                            showSearchResults(TrackSearchResultsType.ERROR, recyclerView)
                        }

                    }
                }

                // критическая ошибка обработки запроса
                override fun onFailure(call: Call<SearchTrackResponse>, t: Throwable) {
                    showSearchResults(TrackSearchResultsType.ERROR, recyclerView)
                }
            })
    }

    //Константные переменные для сохранения и получения значений
    companion object {
        const val SEARCHED_TEXT = "SEARCHED_TEXT"
        const val EMPTY_TEXT = ""
    }


}