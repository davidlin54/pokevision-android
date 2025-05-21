package com.pokevision.viewmodels

import android.app.Activity
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.google.android.play.core.review.ReviewManagerFactory

class ReviewViewModel : ViewModel() {

    fun maybeLaunchReviewRequest(activity: Activity) {
        val reviewManager = ReviewManagerFactory.create(activity)
        val request = reviewManager.requestReviewFlow()

        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener {
                    // The flow has finished. The user may or may not have left a review.
                    // You can’t tell — and shouldn’t ask.
                }
            } else {
                // Fallback: take user to Play Store
                val appPackageName = activity.packageName
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=$appPackageName".toUri()
                )
                activity.startActivity(intent)
            }
        }
    }
}