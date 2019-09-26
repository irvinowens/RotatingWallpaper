package us.sigsegv.rotatingwallpapers

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Point
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.WindowManager
import androidx.constraintlayout.widget.Constraints
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModelProviders
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.squareup.picasso.Picasso
import us.sigsegv.rotatingwallpapers.ui.main.DetailFragment
import us.sigsegv.rotatingwallpapers.ui.main.MainFragment
import us.sigsegv.rotatingwallpapers.ui.main.MainViewModel
import us.sigsegv.rotatingwallpapers.ui.main.RotateWallpaperWorker
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.context = this
        WorkManager.getInstance(this).cancelAllWorkByTag("Rotation")
        val constraints = androidx.work.Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(true)
            .build()

        val saveRequest =
            androidx.work.PeriodicWorkRequest.Builder(RotateWallpaperWorker::class.java,
                24, TimeUnit.HOURS, 12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .addTag("Rotation")
                .build()

        WorkManager.getInstance(this)
            .enqueue(saveRequest)
    }

    private fun setWallpaper(uri : Uri) {
        val wm = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        val displayWidth = size.x
        val displayHeight = size.y
        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
        wallpaperManager.setBitmap(
            Picasso.with(applicationContext).load(uri).get().scale(displayWidth, displayHeight, false), null, true, WallpaperManager.FLAG_LOCK)
        wallpaperManager.setBitmap(
            Picasso.with(applicationContext).load(uri).get().scale(displayWidth, displayHeight, false), null, true, WallpaperManager.FLAG_SYSTEM)
        Log.d("RotateWallpaperWorker", "Rotated wallpaper")
    }

}
