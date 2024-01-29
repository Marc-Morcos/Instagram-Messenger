package com.example.instagram_messenger

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.instagram_messenger.databinding.FragmentFirstBinding
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource


//context for user throughout app
var globalContext: Context? = null


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment()  {



    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //https://stackoverflow.com/questions/45603682/file-upload-in-webview-android-studio
    private var _binding: FragmentFirstBinding? = null
    private lateinit var web_view_controller:WebViewController
    var mWebView: WebView? = null
    private var mUploadMessage: ValueCallback<Uri>? = null
    var uploadMessage: ValueCallback<Array<Uri>>? = null
    val REQUEST_SELECT_FILE = 100
    private val FILECHOOSER_RESULTCODE = 1

    //keep track of when app last stopped
    private var lastPausedTime = TimeSource.Monotonic.markNow()

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null) return
                if (intent != null && intent.clipData!=null && intent.clipData?.getItemAt(0)!=null) {
                    intent.setData(intent.clipData?.getItemAt(0)?.getUri())
                }
                uploadMessage!!.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        intent
                    )
                )
                Log.d("Image",intent.toString())
                uploadMessage = null
            }
        } else Toast.makeText(
            requireActivity().applicationContext,
            "Failed to Upload Image",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        val root: View = inflater.inflate(R.layout.fragment_first, container, false)

        globalContext = activity //get context

        // Created a WebView and used the loadurl method to give url to WebViewController class
        val webView = root.findViewById<WebView>(R.id.web_view_home)
        mWebView = webView;
        web_view_controller = WebViewController()
        webView.webViewClient = web_view_controller // WebViewController is used
        val settings: WebSettings = webView.getSettings()
        settings.domStorageEnabled = true
        settings.setJavaScriptEnabled(true);
        val mWebSettings = mWebView!!.settings
        mWebSettings.javaScriptEnabled = true
        mWebSettings.setSupportZoom(true)
        mWebSettings.builtInZoomControls = true
        mWebSettings.allowFileAccess = true
        mWebSettings.allowContentAccess = true

//      Load image on long press
        webView.setOnLongClickListener(View.OnLongClickListener { v ->
            val hr = (v as WebView).hitTestResult
            if(hr.type == WebView.HitTestResult.IMAGE_TYPE && hr.extra!=null){
                webView.loadUrl(hr.extra!!)
            }
            Log.i("LongClick", "Type=" + hr.type)
            false
        })

        //fix double click opening links and liking (only like)
        webView.setOnTouchListener(web_view_controller.onTouch);


        //https://stackoverflow.com/questions/45603682/file-upload-in-webview-android-studio
        mWebView!!.webChromeClient = object : WebChromeClient() {

            // For Lollipop 5.0+ Devices
            override fun onShowFileChooser(
                mWebView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                if (uploadMessage != null) {
                    uploadMessage!!.onReceiveValue(null)
                    uploadMessage = null
                }
                uploadMessage = filePathCallback
                val intent = fileChooserParams.createIntent()
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE)
                } catch (e: ActivityNotFoundException) {
                    uploadMessage = null
                    Toast.makeText(
                        activity!!.applicationContext,
                        "Cannot Open File Chooser",
                        Toast.LENGTH_LONG
                    ).show()
                    return false
                }
                return true
            }
            
        }

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


    override fun onPause() {
        super.onPause()
        lastPausedTime = TimeSource.Monotonic.markNow()
    }


    override fun onStart() {
        super.onStart()

        //reload home page when app has been closed for a while
        val reloadAfter: Duration = 5.minutes
        if(TimeSource.Monotonic.markNow()-lastPausedTime > reloadAfter){
            mWebView!!.loadUrl(web_view_controller.startPageUrl)
        }

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