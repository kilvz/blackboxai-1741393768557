package com.vpnforms

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.vpnforms.utils.Constants

class WebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        // Setup toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)

        // Configure WebView
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            
            // Security settings
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            allowContentAccess = false
            allowFileAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false
            
            // Disable text zoom
            textZoom = 100
            
            // Disable caching
            cacheMode = WebSettings.LOAD_NO_CACHE
        }

        // Disable text selection and context menus
        webView.setOnLongClickListener { true }
        webView.isLongClickable = false

        // Inject JavaScript to disable copy/paste
        webView.addJavascriptInterface(object {}, "Android")
        
        webView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun onPageFinished(view: WebView?, url: String?) {
                // Inject JavaScript to disable copy/paste
                val script = """
                    javascript:(function() {
                        // Disable text selection
                        document.documentElement.style.webkitUserSelect = 'none';
                        document.documentElement.style.userSelect = 'none';
                        
                        // Disable context menu
                        document.addEventListener('contextmenu', function(e) {
                            e.preventDefault();
                            return false;
                        });
                        
                        // Disable copy
                        document.addEventListener('copy', function(e) {
                            e.preventDefault();
                            return false;
                        });
                        
                        // Disable cut
                        document.addEventListener('cut', function(e) {
                            e.preventDefault();
                            return false;
                        });
                        
                        // Disable paste
                        document.addEventListener('paste', function(e) {
                            e.preventDefault();
                            return false;
                        });
                        
                        // Disable selection
                        document.addEventListener('selectstart', function(e) {
                            e.preventDefault();
                            return false;
                        });
                        
                        // Disable drag and drop
                        document.addEventListener('dragstart', function(e) {
                            e.preventDefault();
                            return false;
                        });
                    })()
                """.trimIndent()
                
                webView.evaluateJavascript(script, null)
                progressBar.visibility = View.GONE
                webView.visibility = View.VISIBLE
                errorText.visibility = View.GONE
            }

            @Deprecated("Deprecated in Java")
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                progressBar.visibility = View.GONE
                webView.visibility = View.GONE
                errorText.visibility = View.VISIBLE
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
            }
        }

        // Load URL
        val url = intent.getStringExtra(Constants.EXTRA_URL)
        if (url != null) {
            webView.loadUrl(url)
        } else {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        webView.clearCache(true)
        webView.clearHistory()
        super.onDestroy()
    }
}
