package com.example.sabi.ui.activity.dictionary

import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.sabi.R

class DetailDictionaryActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_dictionary)

        webView = findViewById(R.id.webview)

        val id = intent.getIntExtra("DICTIONARY_ID", 0)
        Log.d("DetailDictionaryActivity", "Received Dictionary ID: $id")

        if (id != 0) {
            val url = "http://34.128.124.247:8080/readings/$id"
            setupWebView(url)
        } else {
            Log.e("DetailDictionaryActivity", "Invalid Dictionary ID")
        }
    }

    private fun setupWebView(url: String) {
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT

        webView.loadUrl(url)
    }
}
