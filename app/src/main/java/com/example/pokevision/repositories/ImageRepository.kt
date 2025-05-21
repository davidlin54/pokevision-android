package com.example.pokevision.repositories

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.viewModelScope
import com.example.pokevision.models.ImagePrediction
import com.example.pokevision.models.ImagePredictionRequest
import com.example.pokevision.models.Item
import com.example.pokevision.models.Set
import com.example.pokevision.models.ItemDetails
import com.example.pokevision.models.NetworkResult
import com.example.pokevision.services.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Response

class ImageRepository(private val api: ApiService) {

    suspend fun getImagePredictions(base64ImageEncoding: String) :
            NetworkResult<Array<ImagePrediction>> {
        return try {
            val imagePredictionResponse = api.getImagePredictions(ImagePredictionRequest(base64ImageEncoding))

            val resultString = imagePredictionResponse.body()?.string()
            if (!imagePredictionResponse.isSuccessful || resultString.isNullOrEmpty()) {
                NetworkResult.Error(imagePredictionResponse.message())
            } else {
                val parsedResponse = parseImagePredictionResponse(resultString)
                NetworkResult.Success(parsedResponse)
            }
        } catch (e: Exception) {
            NetworkResult.Error("Failed: $e")
        }
    }

    private fun parseImagePredictionResponse(jsonString: String) : Array<ImagePrediction> {
        val jsonObject = JSONObject(jsonString)
        val dataArray = jsonObject.getJSONArray("data")

        val result : MutableList<ImagePrediction> = mutableListOf()
        for (i in 0 until dataArray.length()) {
            val dataItem = dataArray.getJSONObject(i)

            // Parse "item" object
            val item = dataItem.getJSONObject("item")
            val itemId = item.getInt("id")
            val itemName = item.getString("name")
            val itemUrl = item.getString("url")
            val itemSetId = item.getInt("set_id")

            val itemObject = Item(itemId, itemName, itemUrl)

            // Parse "item_details" object
            val itemDetails = dataItem.getJSONObject("item_details")
            val ungradedPrice = itemDetails.getDouble("ungraded_price")
            val psa1Pop = itemDetails.optInt("psa_1_pop", -1)
            val psa1Price = itemDetails.optDouble("psa_1_price", 0.0)

            val psa2Pop = itemDetails.optInt("psa_2_pop", -1)
            val psa2Price = itemDetails.optDouble("psa_2_price", 0.0)

            val psa3Pop = itemDetails.optInt("psa_3_pop", -1)
            val psa3Price = itemDetails.optDouble("psa_3_price", 0.0)

            val psa4Pop = itemDetails.optInt("psa_4_pop", -1)
            val psa4Price = itemDetails.optDouble("psa_4_price", 0.0)

            val psa5Pop = itemDetails.optInt("psa_5_pop", -1)
            val psa5Price = itemDetails.optDouble("psa_5_price", 0.0)

            val psa6Pop = itemDetails.optInt("psa_6_pop", -1)
            val psa6Price = itemDetails.optDouble("psa_6_price", 0.0)

            val psa7Pop = itemDetails.optInt("psa_7_pop", -1)
            val psa7Price = itemDetails.optDouble("psa_7_price", 0.0)

            val psa8Pop = itemDetails.optInt("psa_8_pop", -1)
            val psa8Price = itemDetails.optDouble("psa_8_price", 0.0)

            val psa9Pop = itemDetails.optInt("psa_9_pop", -1)
            val psa9Price = itemDetails.optDouble("psa_9_price", 0.0)

            val psa10Pop = itemDetails.optInt("psa_10_pop", -1)
            val psa10Price = itemDetails.optDouble("psa_10_price", 0.0)

            val itemDetailsObject = ItemDetails(ungradedPrice,
                psa1Pop, psa1Price,
                psa2Pop, psa2Price,
                psa3Pop, psa3Price,
                psa4Pop, psa4Price,
                psa5Pop, psa5Price,
                psa6Pop, psa6Price,
                psa7Pop, psa7Price,
                psa8Pop, psa8Price,
                psa9Pop, psa9Price,
                psa10Pop, psa10Price)

            // Parse "set" object
            val set = dataItem.getJSONObject("set")
            val setId = set.getInt("id")
            val setName = set.getString("name")
            val setUrl = set.getString("url")

            val setObject = Set(setId, setName, setUrl)

            // Parse "probability"
            val probability = dataItem.getDouble("probability")

            result.add(ImagePrediction(itemObject, itemDetailsObject, setObject, probability))
        }

        return result.toTypedArray()
    }

    suspend fun getImageForItem(item: Item) : NetworkResult<Bitmap> {
        val client = OkHttpClient()
        try {
            val request = Request.Builder().url(item.url).build()
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            if (response.isSuccessful) {
                val html = response.body?.string() ?: ""
                val doc: Document = Jsoup.parse(html)

                // Find the div with id 'extra-images'
                val extraImagesDiv = doc.getElementById("extra-images")
                    ?: throw Exception("Cannot find image div")

                // Find all <a> elements inside it
                val link = extraImagesDiv.select("a")[0].attr("href")

                // Extract href attributes
                val imageRequest = Request.Builder().url(link).build()

                val imageResponse = withContext(Dispatchers.IO) {
                    client.newCall(imageRequest).execute()
                }

                if (imageResponse.isSuccessful) {
                    imageResponse.body?.byteStream()?.use { stream ->
                        return withContext(Dispatchers.IO) {
                            NetworkResult.Success(BitmapFactory.decodeStream(stream))
                        }
                    }
                }
            } else {
                return NetworkResult.Error("Failed: ${response.code}")
            }
        } catch (e: Exception) {
            return NetworkResult.Error("Error loading image: ${e.message}")
        }


        return NetworkResult.Error("Image loading failed")
    }
}