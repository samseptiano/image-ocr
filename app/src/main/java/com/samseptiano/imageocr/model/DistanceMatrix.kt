package com.samseptiano.imageocr.model

import com.google.gson.annotations.SerializedName

data class DistanceMatrix (
    @SerializedName("destinationAddresses")
    val destinationAddresses: List<String>? = listOf(),
    @SerializedName("originAddresses")
    val originAddresses: List<String>? = listOf(),
    @SerializedName("rows")
    val rows: List<Row>? = listOf(),
    @SerializedName("status")
    val status: String? = ""
)

data class Row (
    @SerializedName("elements")
    val elements: List<Element>
)

data class Element (
    @SerializedName("distance")
    val distance: Distance,
    @SerializedName("duration")
    val duration: Distance,
    @SerializedName("status")
    val status: String
)

data class Distance (
    @SerializedName("text")
    val text: String,
    @SerializedName("value")
    val value: Long
)
