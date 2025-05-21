package com.pokevision.viewmodels

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.pokevision.BuildConfig

class AdsViewModel : ViewModel() {
    val bannerAdId = BuildConfig.ADMOB_RESULTS_BANNER_ID
    val interstitialAdId = BuildConfig.ADMOB_INTERSTITIAL_ID

    val interstitialAdCooldownMillis = 30 * 1000L // 30 seconds cooldown
    var snapshotsTaken = 0
    var snapshotsBeforeInterstitialAd = 5
    var interstitialLastShown = 0L

    fun loadInterstitialAd(context: Context, callback : InterstitialAdLoadCallback) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            interstitialAdId,
            adRequest,
            callback
        )
    }
    fun maybeShowInterstitialAd(activity : Activity, interstitialAd: InterstitialAd?, onDismiss : () -> Unit) {
        snapshotsTaken++
        if (snapshotsTaken % snapshotsBeforeInterstitialAd != 0) {
            return
        }

        val now = System.currentTimeMillis()
        if (interstitialAd != null && (now - interstitialLastShown) > interstitialAdCooldownMillis) {
            interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    onDismiss()
                }
            }
            interstitialAd.show(activity)
            interstitialLastShown = now
        }
    }
}