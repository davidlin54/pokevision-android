package com.pokevision.models

data class ImagePrediction(
    val item: Item,
    val itemDetails: ItemDetails,
    val set: Set,
    val prediction: Double,
)
