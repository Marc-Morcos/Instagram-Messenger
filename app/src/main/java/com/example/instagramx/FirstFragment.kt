package com.example.instagramx

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.instagramx.databinding.FragmentFirstBinding

//context for user throughout app
var globalContext: Context? = null


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private lateinit var web_view_controller:WebViewController

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        val root: View = inflater.inflate(R.layout.fragment_first, container, false)

        globalContext = activity //get context

        // Created a WebView and used the loadurl method to give url to WebViewController class
        val webView = root.findViewById<WebView>(R.id.web_view_home)
        web_view_controller = WebViewController()
        webView.webViewClient = web_view_controller // WebViewController is used
        val settings: WebSettings = webView.getSettings()
        settings.domStorageEnabled = true
        settings.setJavaScriptEnabled(true);

        //load start page
        webView.loadUrl(web_view_controller.startPageUrl)

        //fix back gesture to work properly
        //https://stackoverflow.com/questions/55074497/how-to-add-onbackpressedcallback-to-fragment
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() { web_view_controller.returnToPrevious(webView) }
            }
        )

        return root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        globalContext = activity //get context
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}