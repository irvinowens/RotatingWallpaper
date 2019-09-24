package us.sigsegv.rotatingwallpapers.ui.main

import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.graphics.scale
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*
import android.view.Display
import androidx.core.content.ContextCompat.getSystemService
import android.view.WindowManager
import android.R.attr.y
import android.R.attr.x
import android.graphics.Point


class RotateWallpaperWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    /**
     * Override this method to do your actual background processing.  This method is called on a
     * background thread - you are required to **synchronously** do your work and return the
     * [androidx.work.ListenableWorker.Result] from this method.  Once you return from this
     * method, the Worker is considered to have finished what its doing and will be destroyed.  If
     * you need to do your work asynchronously on a thread of your own choice, see
     * [ListenableWorker].
     *
     *
     * A Worker is given a maximum of ten minutes to finish its execution and return a
     * [androidx.work.ListenableWorker.Result].  After this time has expired, the Worker will
     * be signalled to stop.
     *
     * @return The [androidx.work.ListenableWorker.Result] of the computation; note that
     * dependent work will not execute if you use
     * [androidx.work.ListenableWorker.Result.failure] or
     * [androidx.work.ListenableWorker.Result.failure]
     */
    override fun doWork(): Result {
        Log.d("RotateWallpaperWorker", "Rotating wallpaper")
        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
        if(wallpaperManager.isSetWallpaperAllowed && wallpaperManager.isWallpaperSupported) {
            val dir = applicationContext.filesDir?.list()
            if(dir == null || dir.isEmpty()) {
                Log.d("RotateWallpaperWorker", "Failed to find any images")
                return Result.failure()
            } else {
                val random = Random()
                val min = 0
                val max = dir.size - 1
                val randomInt = random.nextInt((max - min) + 1) + min
                val wm = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                val size = Point()
                display.getSize(size)
                val displayWidth = size.x
                val displayHeight = size.y
                wallpaperManager.setBitmap(Picasso.with(applicationContext).load(File(applicationContext.filesDir,
                    dir[randomInt]
                )).get().scale(displayWidth, displayHeight, false), null, true, WallpaperManager.FLAG_LOCK)
                wallpaperManager.setBitmap(Picasso.with(applicationContext).load(File(applicationContext.filesDir,
                    dir[randomInt]
                )).get().scale(displayWidth, displayHeight, false), null, true, WallpaperManager.FLAG_SYSTEM)
                Log.d("RotateWallpaperWorker", "Rotated wallpaper")
                return Result.success()
            }
        } else {
            Log.d("RotateWallpaperWorker", "Rotating wallpaper not allowed for this user")
            return Result.failure()
        }
    }
}