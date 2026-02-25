package com.example.whoofpark.model

import com.google.gson.annotations.SerializedName


data class DogParkResponse(
    @SerializedName("features")
    val features: List<DogParkFeature>
)


data class DogParkFeature(
    @SerializedName("attributes")
    val attributes: DogParkAttributes,
    @SerializedName("geometry")
    val geometry: DogParkGeometry
)


data class DogParkAttributes(
    //when we get the data from Tel Aviv Municipality we will get a json with attributes like t_shem that is the name of the park
    // so we convert the name to understand with @SerializedName
    @SerializedName("shem_gina")
    val name: String?,

    @SerializedName("Full_Address")
    val address: String?,

    @SerializedName("UniqueId")
    val id: String?,

    @SerializedName("shaot")
    val hours: String?
)

data class DogParkGeometry(
    @SerializedName("x")
    val x: Double, // Longitude
    @SerializedName("y")
    val y: Double  // Latitude
)
