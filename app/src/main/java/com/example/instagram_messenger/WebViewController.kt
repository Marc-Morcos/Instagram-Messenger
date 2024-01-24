package com.example.instagram_messenger
import android.net.Uri
import android.util.Log
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

    val startPageUrl = "https://www.instagram.com/direct/inbox/"

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
            //to previous page if possible
            view.loadUrl(history.removeLast());
        } else {
            //to start page if no history
            Log.d("navAlert", "Empty history, going home")
            view.loadUrl(startPageUrl)
        }
    }

    //do redirects and return true if redirected
    fun doRedirect(view: WebView?, url: String?): Boolean
    {
        if (view != null && linkMap.containsKey(url)) {
            val urlNew = linkMap[url]!!
            view.loadUrl(urlNew)

            return true
        }

        return false
    }

    //handle external sites and block sites
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        if(url==null){return true}

        val host = (Uri.parse(url).getHost())
        if(host!=null){
            Log.d("navAlert, host:", host)} //print host

        //if external thing, open in other browser
        if (globalContext!= null && !instagramHostNames.contains(host)) {
            val tabIntentBuilder = CustomTabsIntent.Builder()
            val tabsIntent = tabIntentBuilder.build()
            tabsIntent.launchUrl(globalContext!!, Uri.parse(url))
            return true
        }

        //if instagram link, do normal redirects
        return doRedirect(view,url)
    }

    //block sites
    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {

        if(isReload){
            super.doUpdateVisitedHistory(view, url, isReload)
            return
        }

        if (url != null) Log.d("navAlert", url) //print site we are currently navigating to

        if (doRedirect(view,url)) {
            //doUpdateVisitedHistory will be called again due to loadUrl
        } else {
            //add to history
            if(view != null && url!=null){
                //filter duplicates
                val last = history.lastOrNull()
                if(last == null || last != url) {
                    Log.d("navAlert", "Add " + url + " to history")
                    history.add(url)
                }
            }

            super.doUpdateVisitedHistory(view, url, isReload)
        }
    }
}