package com.example.tv2_rubtsova

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.*
import java.io.IOException
import com.squareup.picasso.Picasso
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import android.view.Gravity
class SearchActivity : AppCompatActivity() {

    private lateinit var searchBar: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var searchButton: Button
    private lateinit var resultsLayout: LinearLayout
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Инициализация элементов интерфейса
        searchBar = findViewById(R.id.searchBar)
        categorySpinner = findViewById(R.id.categorySpinner)
        searchButton = findViewById(R.id.searchButton)
        resultsLayout = findViewById(R.id.resultsLayout)

        // Настройка категорий
        val categories = arrayOf("Все", "Боевик", "Комедия", "Драма", "Ужасы", "Фантастика")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Обработка нажатия на кнопку поиска
        searchButton.setOnClickListener {
            val query = searchBar.text.toString()
            val category = categorySpinner.selectedItem.toString()
            searchMovies(query, category)
        }
    }

    private fun searchMovies(query: String, category: String) {
        // Формирование URL для запроса
        val url = "https://api.kinopoisk.dev/v1.4/movie/search?page=1&limit=10&query=$query&language=ru"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("accept", "application/json")
            .addHeader("X-API-KEY", "Q5R3EPC-BRZMKJ2-Q7H0HKQ-D5MPGJ4")
            .build()

        // Выполнение запроса
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@SearchActivity, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val movies = parseMoviesJson(responseData)

                    runOnUiThread {
                        if (movies.isEmpty()) {
                            // Если фильмы не найдены, показываем сообщение
                            Toast.makeText(this@SearchActivity, "Фильмы не найдены", Toast.LENGTH_SHORT).show()
                        } else {
                            displayMovies(movies)
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@SearchActivity, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun parseMoviesJson(jsonString: String?): List<Movie> {
        if (jsonString.isNullOrEmpty()) {
            return emptyList()
        }
        val gson = Gson()
        val movieResponse = gson.fromJson(jsonString, MovieResponse::class.java)
        return movieResponse.docs
    }

    private fun displayMovies(movies: List<Movie>) {
        resultsLayout.removeAllViews()
        movies.forEach { movie ->
            // Создаем линейный макет для каждого фильма
            val movieLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(8, 8, 8, 8)
                gravity = Gravity.CENTER // Устанавливаем gravity для всего макета
            }

            // Добавляем обложку фильма
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(300, 450) // Установите размеры по вашему усмотрению
                scaleType = ImageView.ScaleType.FIT_CENTER
                movie.poster.image?.let {
                    Picasso.get().load(it).into(this) // Загрузка изображения с помощью Picasso
                }
            }
            movieLayout.addView(imageView)

            // Добавляем название фильма
            val titleTextView = TextView(this).apply {
                text = "${movie.title} (${movie.year})"
                setTextColor(Color.WHITE)
                textSize = 16f
                gravity = Gravity.CENTER // Устанавливаем gravity для текста
            }
            titleTextView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER // Устанавливаем gravity для текста
            }
            movieLayout.addView(titleTextView)

            // Добавляем линейный макет в основной layout
            resultsLayout.addView(movieLayout)
        }
    }

    data class MovieResponse(
        @SerializedName("docs") val docs: List<Movie>
    )
}