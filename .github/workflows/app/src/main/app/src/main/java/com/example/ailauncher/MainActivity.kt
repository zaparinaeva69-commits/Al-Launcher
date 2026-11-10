package com.example.ailauncher

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class MainActivity : Activity() {

    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.setBackgroundColor(0xFF0F1115.toInt())
        web.webViewClient = WebViewClient()
        web.addJavascriptInterface(Bridge(), "Android")
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }

    override fun onBackPressed() {
        // launcher stays on the home screen
    }

    private fun iconToBase64(d: Drawable): String {
        val size = 96
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        d.setBounds(0, 0, size, size)
        d.draw(canvas)
        val out = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    inner class Bridge {

        @JavascriptInterface
        fun getApps(): String {
            val pm = packageManager
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val items = pm.queryIntentActivities(intent, 0)
                .filter { it.activityInfo.packageName != packageName }
                .sortedBy { it.loadLabel(pm).toString().lowercase() }
            val arr = JSONArray()
            for (ri in items) {
                val o = JSONObject()
                o.put("name", ri.loadLabel(pm).toString())
                o.put("pkg", ri.activityInfo.packageName)
                o.put("icon", iconToBase64(ri.loadIcon(pm)))
                arr.put(o)
            }
            return arr.toString()
        }

        @JavascriptInterface
        fun launch(pkg: String) {
            val i = packageManager.getLaunchIntentForPackage(pkg)
            if (i != null) {
                runOnUiThread { startActivity(i) }
            }
        }
    }
}
