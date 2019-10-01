package us.sigsegv.rotatingwallpapers

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import us.sigsegv.rotatingwallpapers.ui.main.DetailFragment
import us.sigsegv.rotatingwallpapers.ui.main.MainFragment
import us.sigsegv.rotatingwallpapers.ui.main.MainViewModel


class MainActivity : AppCompatActivity(),
    DetailFragment.OnFragmentInteractionListener {
    private lateinit var viewModel: MainViewModel
    private val ht = HandlerThread("background ops")
    init {
        ht.start()
    }
    private val handler = Handler(ht.looper)

    override fun onFragmentInteraction(uri: Uri) {
        Log.v("MainActivity", "User did something on fragment")
        handler.post {
            setWallpaper(uri)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_play -> {
            // User chose the "Play" item, show the app settings UI...
            viewModel.startWork(this)
            showSnackbar("Started daily rotation")
            true
        }

        R.id.action_pause -> {
            // User chose the "Pause" action
            viewModel.stopWork(this)
            showSnackbar("Stopped daily rotation")
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.context = this
    }

    private fun setWallpaper(uri : Uri) {
        val wm = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        val displayWidth = size.x
        val displayHeight = size.y
        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
        val bitmap = Picasso.with(applicationContext).load(uri).get().scale(displayWidth, displayHeight, false)
        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
        Log.d("RotateWallpaperWorker", "Rotated wallpaper")
        showSnackbar("Wallpaper selected")
    }

    private fun showSnackbar(text: String) {
        val view : View? = findViewById(R.id.main)
        if(view != null) {
            Snackbar.make(view, text, Snackbar.LENGTH_LONG).show()
        }
    }

}
