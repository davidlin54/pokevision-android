package com.example.pokevision.services

import com.example.pokevision.models.ImagePredictionRequest
import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("pokevision_evaluate")
    suspend fun getImagePredictions(@Body imagePredictionRequest: ImagePredictionRequest):
            Response<ResponseBody>
}