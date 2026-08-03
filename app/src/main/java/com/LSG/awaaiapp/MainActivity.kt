package com.LSG.awaaiapp

import android.annotation.SuppressLint
import android.net.http.SslError
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

// WEBSITE_URL_PLACEHOLDER is replaced at build time by the GitHub Actions workflow
private const val WEBSITE_URL = "WEBSITE_URL_PLACEHOLDER"

private const val DASHBOARD_URL = "DASHBOARD_URL_PLACEHOLDER"

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var ownerBtn: Button
    private var isOwnerMode = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //swipeRefresh = SwipeRefreshLayout(this)
        //webView = WebView(this)
        //swipeRefresh.addView(webView)
        //setContentView(swipeRefresh)

        val frame = FrameLayout(this)

        swipeRefresh = SwipeRefreshLayout(this)
        webView = WebView(this)
        swipeRefresh.addView(webView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        ))
        frame.addView(swipeRefresh, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        ))

// Small semi-transparent ⚙ button — bottom-right corner, owner only
        ownerBtn = Button(this)
        ownerBtn.text = "⚙"
        ownerBtn.textSize = 18f
        ownerBtn.setTextColor(Color.WHITE)
        ownerBtn.alpha = 0.65f
        ownerBtn.setPadding(0, 0, 0, 0)
        val btnBg = GradientDrawable()
        btnBg.shape = GradientDrawable.OVAL
        btnBg.setColor(0xCC101828.toInt())
        ownerBtn.background = btnBg
        val btnSize = dp(48)
        val btnParams = FrameLayout.LayoutParams(btnSize, btnSize, Gravity.BOTTOM or Gravity.END)
        btnParams.bottomMargin = dp(88)
        btnParams.marginEnd    = dp(20)
        frame.addView(ownerBtn, btnParams)

        setContentView(frame)




        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(false)
            mediaPlaybackRequiresUserGesture = false
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = false
            override fun onPageFinished(view: WebView, url: String) {
                swipeRefresh.isRefreshing = false
            }
            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                handler.proceed()
            }
        }

        webView.webChromeClient = WebChromeClient()
        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }
        ownerBtn.setOnClickListener {
            isOwnerMode = !isOwnerMode
            ownerBtn.text = if (isOwnerMode) "←" else "⚙"
            webView.loadUrl(if (isOwnerMode) DASHBOARD_URL else WEBSITE_URL)
        }

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl(WEBSITE_URL)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    webView.canGoBack() -> webView.goBack()
                    isOwnerMode -> {
                        isOwnerMode = false
                        ownerBtn.text = "⚙"
                        webView.loadUrl(WEBSITE_URL)
                    }
                    else -> {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                        isEnabled = true
                    }
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }


    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
}
