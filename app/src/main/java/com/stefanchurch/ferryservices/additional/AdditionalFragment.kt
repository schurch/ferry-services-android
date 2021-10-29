package com.stefanchurch.ferryservices.additional

import android.content.res.Configuration
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebSettingsCompat.FORCE_DARK_ON
import androidx.webkit.WebSettingsCompat.FORCE_DARK_OFF
import androidx.webkit.WebViewFeature
import com.stefanchurch.ferryservices.databinding.AdditionalFragmentBinding

class AdditionalFragment: Fragment() {

    private val args: AdditionalFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = AdditionalFragmentBinding.inflate(inflater, container, false)
        val webView = binding.webView

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    WebSettingsCompat.setForceDark(webView.settings, FORCE_DARK_ON)
                }
                else -> {
                    WebSettingsCompat.setForceDark(webView.settings, FORCE_DARK_OFF)
                }
            }
        }

        val styledHtml = """
            <!DOCTYPE html>
            <html>
                <head>
                    <meta name='viewport' content='width=device-width, initial-scale=1'>
                    <meta name="color-scheme" content="dark light">
                    <style type='text/css'>
                        body { color: #000000; background-color: #ffffff; }
                        a { color: #21BFAA; }
                        
                        @media (prefers-color-scheme: dark) {
                            body { color: #ffffff; background-color: #303030; }
                        }
                    </style>
                </head>
                <body>
                    ${args.service.additionalInfo!!}
                </body>
            </html>
            """
        val base64Html = Base64.encodeToString(styledHtml.toByteArray(charset("UTF-8")), Base64.DEFAULT)
        webView.loadData(base64Html, "text/html; charset=utf-8", "base64")

        return binding.root
    }

}