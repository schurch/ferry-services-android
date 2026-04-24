package com.stefanchurch.ferryservicesandroid.ui.screens.webinfo

import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun WebInfoScreen(
    html: String,
    onBack: () -> Unit,
) {
    val darkTheme = isSystemInDarkTheme()
    val backgroundColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val linkColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disruption Information") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        val styledHtml = """
            <!DOCTYPE html>
            <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1" />
                    <style>
                        :root { color-scheme: ${if (darkTheme) "dark" else "light"}; }
                        body {
                            font-family: sans-serif;
                            padding: 16px;
                            background: ${backgroundColor.toCssColor()};
                            color: ${textColor.toCssColor()};
                        }
                        a { color: ${linkColor.toCssColor()}; }
                        img, iframe, video { max-width: 100%; height: auto; }
                    </style>
                </head>
                <body>$html</body>
            </html>
        """.trimIndent()
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val url = request?.url ?: return false
                            if (url.scheme in setOf("http", "https", "mailto", "tel")) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, url))
                                return true
                            }
                            return false
                        }

                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            val parsedUrl = url?.let(Uri::parse) ?: return false
                            if (parsedUrl.scheme in setOf("http", "https", "mailto", "tel")) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, parsedUrl))
                                return true
                            }
                            return false
                        }
                    }
                    settings.javaScriptEnabled = false
                    setBackgroundColor(backgroundColor.toArgb())
                    if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                        WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, darkTheme)
                    }
                    loadDataWithBaseURL("about:blank", styledHtml, "text/html", "utf-8", null)
                }
            },
            update = { webView ->
                webView.setBackgroundColor(backgroundColor.toArgb())
                if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, darkTheme)
                }
                webView.loadDataWithBaseURL("about:blank", styledHtml, "text/html", "utf-8", null)
            },
        )
    }
}

private fun Color.toCssColor(): String = String.format("#%06X", 0xFFFFFF and toArgb())
