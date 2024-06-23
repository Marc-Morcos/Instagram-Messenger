package com.example.instagram_messenger

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent


//import android.content.Context
//import android.net.ConnectivityManager
//
//fun checkInternetConnection(context: Context): Boolean {
//    val con_manager =
//        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//    return (con_manager.activeNetworkInfo != null && con_manager.activeNetworkInfo!!.isAvailable
//            && con_manager.activeNetworkInfo!!.isConnected)
//}

// class is extended to WebViewClient to access the WebView
class WebViewController : WebViewClient() {
    private var history = ArrayDeque<String>()
    @Volatile private var lastClickType = 0

    val startPageUrl = "https://www.instagram.com/direct/inbox/"

    val IndividualPostPrefix = "https://www.instagram.com/p/" //instagram posts start with this prefix

    private val linkMap = mapOf(
        "https://www.instagram.com/" to "https://www.instagram.com/notifications/",
        "https://www.instagram.com/explore/" to "https://www.instagram.com/explore/search/",
        "https://www.instagram.com/reels/" to "https://instagram.com/users/self")

    private val instagramHostNames = setOf<String>("www.instagram.com","https://www.instagram.com","http://www.instagram.com")

    //return to previous page
    fun returnToPrevious(view: WebView?) {
        if(view == null){return}

        if (!history.isEmpty()) {
            history.removeLast()
        } //last entry is current page, remove

        //goBack command doesn't give me enough control, so keep my own history
        if (!history.isEmpty()) {
            val previousUrl = history.removeLast()
            if( view.getUrl() != previousUrl) {
                view.loadUrl(previousUrl)
            }
        } else {
            //to start page if no history
//            Log.d("navAlert", "Empty history, going home")
            if( view.getUrl() != startPageUrl) {
                view.loadUrl(startPageUrl)
            }
        }
    }

    //do redirects and return true if redirected
    fun doRedirect(view: WebView?, url: String?): Boolean
    {
        if (view != null && linkMap.containsKey(url)) {
            val urlNew = linkMap[url]!!
            if( view.getUrl() != urlNew) {
                view.loadUrl(urlNew)
            }

            return true
        }

        return false
    }

    //handle external sites and block sites
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        if(url==null){return true}

        val host = (Uri.parse(url).getHost())
        if(host!=null){
//            Log.d("navAlert, host:", host)} //print host

        //if external thing, open in other browser
        if (globalContext!= null && !instagramHostNames.contains(host)) {
            val tabIntentBuilder = CustomTabsIntent.Builder()
            val tabsIntent = tabIntentBuilder.build()
            tabsIntent.launchUrl(globalContext!!, Uri.parse(url))
            return true
        }

        //if instagram post and double click, reject
        if(url.startsWith(IndividualPostPrefix)) {
            if(lastClickType==0){
                Thread { //wait to confirm its a single click
                    while(lastClickType==0){
                        Thread.sleep(100);
                    }
                    if(lastClickType==1) {
                        Handler(Looper.getMainLooper()).post {
                            view?.loadUrl(url);
                        }
                    }
                }.start()
                return true;
            }
//            Log.i("Instagram Messenger Double Click","Read double click "+lastClickType.toString())
            if (lastClickType == 2) { 
                return true
            }
        }

        //if instagram link and not double click, do normal redirects
        return doRedirect(view,url)
    }

    //block sites
    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {

        if(isReload){
            super.doUpdateVisitedHistory(view, url, isReload)
            return
        }

//        if (url != null) Log.d("navAlert", url) //print site we are currently navigating to

        if (doRedirect(view,url)) {
            //doUpdateVisitedHistory will be called again due to loadUrl
        } else {
            //add to history
            if(view != null && url!=null){
                //filter duplicates
                val last = history.lastOrNull()
                if(last == null || last != url) {
//                    Log.d("navAlert", "Add " + url + " to history")
                    history.add(url)
                }
            }

            super.doUpdateVisitedHistory(view, url, isReload)
        }
    }

    //fix double click opening links and liking (only like)
    //https://stackoverflow.com/questions/10418757/make-a-double-click-work-in-a-webview
    private var gs: GestureDetector? = null

    val onTouch = OnTouchListener { _, event ->
        if (gs == null) {
            gs = GestureDetector(
                object : SimpleOnGestureListener() {

                    //detect double click
                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        lastClickType = 2
//                        Log.i("Instagram Messenger Double Click","Write double click 2"+lastClickType.toString())
                        return super.onDoubleTap(e)
                    }

                    //wait for single click
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        lastClickType = 0
//                        Log.i("Instagram Messenger Double Click","Write double click 0"+lastClickType.toString())

                        return super.onSingleTapUp(e)
                    }

                    //detect single click
                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        lastClickType = 1
//                        Log.i("Instagram Messenger Double Click","Write double click 1"+lastClickType.toString())
                        return super.onSingleTapConfirmed(e)
                    }
                })
        }
        gs!!.onTouchEvent(event)
        false
    }
}

