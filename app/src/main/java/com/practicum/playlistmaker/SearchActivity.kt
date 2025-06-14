package com.practicum.playlistmaker


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import retrofit2.Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    private val itunesBaseUrl = "https://itunes.apple.com"
    var searchedText: String = ""

    //
    var trackList: MutableList<Track> = mutableListOf()


    var trackSearchHistory: MutableList<Track> = mutableListOf()
    var searchFieldHasFocus = false



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
        val toMainButton = findViewById<ImageView>(R.id.buttonToMain)
        toMainButton.setOnClickListener {
            //val displayIntent = Intent(this, MainActivity::class.java)
            //startActivity(displayIntent)
            finish()
        }



        val sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
        val searchEditText = findViewById<EditText>(R.id.searchEditText)


        val searchResultsRecyclerView = findViewById<RecyclerView>(R.id.trackList)
        val searchHistoryRecyclerView = findViewById<RecyclerView>(R.id.trackHistorySearchList)
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchHistoryRecyclerView.layoutManager = LinearLayoutManager(this)

        val retrofit = Retrofit.Builder()
            .baseUrl(itunesBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        //Очистка поиска
        val clearButton = findViewById<ImageView>(R.id.searchClear)
        clearButton.visibility = clearButtonVisibility(searchEditText.text)
        clearButton.setOnClickListener {
            searchEditText.setText("")
            showSearchResults(TrackSearchResultsType.EMPTY, sharedPreferences)
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }

        //Очистка истории поиска
        val historyResetButton = findViewById<Button>(R.id.historyReset)
        historyResetButton.setOnClickListener {
            trackSearchHistory.clear()
            sharedPreferences.edit().putString(TRACKS_SEARCH_HISTORY, Gson().toJson(trackSearchHistory)).apply()
            showSearchResults(TrackSearchResultsType.EMPTY, sharedPreferences)
        }

        val searchTrackService = retrofit.create(Itunes::class.java)
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                getTrackSearchResults(searchTrackService, searchEditText.text.toString(), sharedPreferences)
            }
            false
        }

        //Обновить после ошибки
        val searchErrorRefreshButton = findViewById<Button>(R.id.searchRefresh)
        searchErrorRefreshButton.setOnClickListener {
            getTrackSearchResults(searchTrackService, searchEditText.text.toString(), sharedPreferences)
        }

        val searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = clearButtonVisibility(s)
                if (s.isNullOrEmpty()){
                    showSearchResults(TrackSearchResultsType.EMPTY, sharedPreferences)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                searchedText = s.toString()
            }
        }


        searchEditText.addTextChangedListener(searchTextWatcher)
        searchEditText.setOnFocusChangeListener { view, hasFocus ->
            searchFieldHasFocus = hasFocus
            if (hasFocus && searchEditText.text.isEmpty())
            {
                showSearchResults(TrackSearchResultsType.EMPTY, sharedPreferences)
            }
        }

        showSearchResults(TrackSearchResultsType.EMPTY, sharedPreferences)
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

        val searchErrorPlaceholderFrame = findViewById<FrameLayout>(R.id.searchErrorPlaceholder)
        val searchNoResultsPlaceholderFrame = findViewById<FrameLayout>(R.id.searchNoResultsPlaceholder)

        outState.putBoolean(SHOW_ERROR_SEARCH, searchErrorPlaceholderFrame.visibility == View.VISIBLE)
        outState.putBoolean(SHOW_ERROR_NO_RESULTS, searchNoResultsPlaceholderFrame.visibility == View.VISIBLE)
        outState.putString(TRACK_LIST, Gson().toJson(trackList))
    }

    // Переопределяем получение Activity
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchedText = savedInstanceState.getString(SEARCHED_TEXT, EMPTY_TEXT)

        val showSearchErrorPlaceHolder = savedInstanceState.getBoolean(SHOW_ERROR_SEARCH, false)
        val showSearchNoResultsPlaceHolder = savedInstanceState.getBoolean(SHOW_ERROR_NO_RESULTS, false)
        val sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)

        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        val searchErrorPlaceholderFrame = findViewById<FrameLayout>(R.id.searchErrorPlaceholder)
        val searchNoResultsPlaceholderFrame = findViewById<FrameLayout>(R.id.searchNoResultsPlaceholder)

        searchEditText.setText(searchedText)

        if (showSearchErrorPlaceHolder) {
            searchErrorPlaceholderFrame.visibility = View.VISIBLE
        }
        else {
            searchErrorPlaceholderFrame.visibility = View.GONE
        }
        if (showSearchNoResultsPlaceHolder) {
            searchNoResultsPlaceholderFrame.visibility = View.VISIBLE
        }
        else {
            searchNoResultsPlaceholderFrame.visibility = View.GONE
        }

        val stringTextList = savedInstanceState.getString(TRACK_LIST, EMPTY_TEXT)

        if (stringTextList != null && stringTextList != ""){
            val restoredTrackArray: Array<Track> = Gson().fromJson(stringTextList, Array<Track>::class.java)
            trackList = restoredTrackArray.toMutableList()
            if (trackList.size > 0) {
                //val recyclerView = findViewById<RecyclerView>(R.id.trackList)
                showSearchResults(TrackSearchResultsType.SUCCESS, sharedPreferences)
            }
        }
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
    fun showSearchResults(trackSearchResultsType: TrackSearchResultsType, sharedPreferences: SharedPreferences){

        //Плейсхолдеры для ошибочного результата поиска и отсутствия результата
        val searchErrorPlaceholderFrame = findViewById<FrameLayout>(R.id.searchErrorPlaceholder)
        val searchNoResultsPlaceholderFrame = findViewById<FrameLayout>(R.id.searchNoResultsPlaceholder)


        val searchResultFrame = findViewById<FrameLayout>(R.id.searchResultsFrame)
        val searchHistoryFrame = findViewById<LinearLayout>(R.id.searchHistoryFrame)
        val searchResultsRecyclerView = findViewById<RecyclerView>(R.id.trackList)
        val searchHistoryRecyclerView = findViewById<RecyclerView>(R.id.trackHistorySearchList)

        when (trackSearchResultsType){
            //как хорошо, что нашли
            TrackSearchResultsType.SUCCESS -> {

                val trackAdapter = TrackAdapter(trackList, getTrackOnClickListener(sharedPreferences))
                searchResultsRecyclerView.adapter = trackAdapter

                searchResultFrame.visibility                = View.VISIBLE
                searchHistoryFrame.visibility               = View.GONE
                searchErrorPlaceholderFrame.visibility      = View.GONE
                searchNoResultsPlaceholderFrame.visibility  = View.GONE
            }

            //очистили строку поиска
            TrackSearchResultsType.EMPTY -> {

                trackList.clear()

                val trackAdapter = TrackAdapter(ArrayList<Track>(0), getTrackOnClickListener(sharedPreferences))
                searchResultsRecyclerView.adapter = trackAdapter

                showTracksSearchHistory(sharedPreferences, searchHistoryRecyclerView)

                searchResultFrame.visibility                = View.GONE
                searchErrorPlaceholderFrame.visibility      = View.GONE
                searchNoResultsPlaceholderFrame.visibility  = View.GONE
            }

            //ошибка
            TrackSearchResultsType.ERROR -> {

                searchResultFrame.visibility                = View.GONE
                searchHistoryFrame.visibility               = View.GONE
                searchErrorPlaceholderFrame.visibility      = View.VISIBLE
                searchNoResultsPlaceholderFrame.visibility  = View.GONE
            }

            //ничего не найдено
            TrackSearchResultsType.NO_RESULTS -> {

                searchResultFrame.visibility                = View.GONE
                searchHistoryFrame.visibility               = View.GONE
                searchErrorPlaceholderFrame.visibility      = View.GONE
                searchNoResultsPlaceholderFrame.visibility  = View.VISIBLE
            }

        }
    }

    fun getTrackSearchResults(searchTrackService: Itunes, expression: String, sharedPreferences: SharedPreferences) {
        searchTrackService
            .search(expression)
            .enqueue(object : Callback<SearchTrackResponse> {
                override fun onResponse(
                    call: Call<SearchTrackResponse>,
                    response: Response<SearchTrackResponse>
                ) {

                    when (response.code()) {
                        //Есть ответ
                        200 -> {
                            val searchTrackResults = response.body()?.results
                            trackList.clear()

                            if (searchTrackResults != null) {
                                if (searchTrackResults.size > 0) {
                                    trackList.addAll(searchTrackResults)

                                    showSearchResults(TrackSearchResultsType.SUCCESS, sharedPreferences)
                                } else {
                                    showSearchResults(TrackSearchResultsType.NO_RESULTS, sharedPreferences)
                                }
                            } else {
                                showSearchResults(TrackSearchResultsType.NO_RESULTS, sharedPreferences)
                            }
                        }
                        else -> {
                            showSearchResults(TrackSearchResultsType.ERROR, sharedPreferences)
                        }
                    }
                }

                override fun onFailure(call: Call<SearchTrackResponse>, t: Throwable) {
                    showSearchResults(TrackSearchResultsType.ERROR, sharedPreferences)
                }
            }
            )
    }

    fun getTrackOnClickListener(sharedPreferences: SharedPreferences): TrackAdapter.OnTrackClickListener{

        // определяем слушателя нажатия элемента в списке
        val stateClickListener: TrackAdapter.OnTrackClickListener =
            object : TrackAdapter.OnTrackClickListener {
                override fun onTrackClick(track: Track, position: Int) {
                    updateTrackList(trackSearchHistory, track)
                    sharedPreferences.edit().putString(TRACKS_SEARCH_HISTORY, Gson().toJson(trackSearchHistory)).apply()
                }
            }

        return stateClickListener
    }

    fun updateTrackList(trackList: MutableList<Track>, newTrack: Track){

        // Ищем песню в сохраненном списке
        // Если такая есть, то удалим ее из списка. После этого нужно добавить ее в начало списка
        for (curTrack in trackList)
        {
            if  (curTrack.trackId == newTrack.trackId)
            {

                trackList.remove(curTrack)
                break

            }
        }

        // Добавляем в начало
        trackList.add(0, newTrack)

        // Проверим количество песен. Если больше 10-ти, то удалим с 11-ой
        if (trackList.size > 10)
        {
            for (i  in trackList.size-1..10 )
            {

                trackList.removeAt(10)

            }
        }

    }


    fun showTracksSearchHistory(sharedPreferences: SharedPreferences, recyclerView: RecyclerView){
        val stringSearchHistoryList = sharedPreferences.getString(TRACKS_SEARCH_HISTORY, "")
        val searchHistoryFrame = findViewById<LinearLayout>(R.id.searchHistoryFrame)


        if (stringSearchHistoryList != null && stringSearchHistoryList != "" && searchFieldHasFocus){
            val trackSearchHistoryArray: Array<Track> = Gson().fromJson(stringSearchHistoryList, Array<Track>::class.java)

            trackSearchHistory = trackSearchHistoryArray.toMutableList()
            if (trackSearchHistory.size > 0) {

                val trackAdapter = TrackAdapter(trackSearchHistory, getTrackOnClickListener(sharedPreferences))
                recyclerView.adapter = trackAdapter
                searchHistoryFrame.visibility = View.VISIBLE
            }
            else {
                searchHistoryFrame.visibility = View.GONE
            }
        }
        else {
            searchHistoryFrame.visibility = View.GONE
        }
    }



    //Константные переменные для сохранения и получения значений
    companion object {
        const val SEARCHED_TEXT = "SEARCHED_TEXT"

        const val SHOW_ERROR_SEARCH = "SHOW_SEARCH_ERROR_PLACEHOLDER"
        const val SHOW_ERROR_NO_RESULTS = "SHOW_NO_RESULTS_ERROR_PLACEHOLDER"
        const val TRACK_LIST = "TRACK_LIST"

        const val EMPTY_TEXT = ""
    }

}