package com.stefanchurch.ferryservices.additional

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.stefanchurch.ferryservices.databinding.AdditionalFragmentBinding

class AdditionalFragment: Fragment() {

    private val args: AdditionalFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = AdditionalFragmentBinding.inflate(inflater, container, false)
        val styledHtml = """
            <!DOCTYPE html>
            <html>
                <head>
                    <meta name='viewport' content='width=device-width, initial-scale=1'>
                    <style type='text/css'>
                        body { color: #606060; }
                        a { color: #21BFAA; }
                    </style>
                </head>
                <body>
                    ${args.service.additionalInfo!!}
                </body>
            </html>
            """
        val base64Html = Base64.encodeToString(styledHtml.toByteArray(charset("UTF-8")), Base64.DEFAULT)
        binding.webView.loadData(base64Html, "text/html; charset=utf-8", "base64")
        return binding.root
    }

}