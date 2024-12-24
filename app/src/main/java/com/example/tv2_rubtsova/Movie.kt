package com.example.tv2_rubtsova

import com.google.gson.annotations.SerializedName
data class MovieResponse(
    val docs: List<Movie>
)


data class Movie(
    @SerializedName("alternativeName") val title: String,
    @SerializedName("year") val year: Int,
    @SerializedName("poster") val poster: Poster
)

data class Poster(
    @SerializedName("url") val image: String? // URL постера может быть null
)