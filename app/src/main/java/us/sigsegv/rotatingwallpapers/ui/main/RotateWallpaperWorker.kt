/*
 * Copyright (c) 2020. Irvin Owens Jr
 *
 *   This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package us.sigsegv.rotatingwallpapers.ui.main

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.util.Log
import android.view.WindowManager
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


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
                val bitmap = transform(Picasso.with(applicationContext).load(File(applicationContext.filesDir,
                    dir[randomInt]
                )).get())
                wallpaperManager.setBitmap(bitmap, null, true,
                    WallpaperManager.FLAG_LOCK or
                            WallpaperManager.FLAG_SYSTEM)
                Log.d("RotateWallpaperWorker", "Rotated wallpaper")
                bitmap.recycle()
                startWork(applicationContext)
                return Result.success()
            }
        } else {
            Log.d("RotateWallpaperWorker", "Rotating wallpaper not allowed for this user")
            return Result.failure()
        }
    }

    private fun transform(source: Bitmap): Bitmap {
        val windowManager: WindowManager = applicationContext
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val windowSizePoint = Point()
        windowManager.defaultDisplay.getSize(windowSizePoint)
        val isLandscape = source.width > source.height

        val newWidth: Int
        val newHeight: Int
        if (isLandscape) {
            newWidth = windowSizePoint.x
            newHeight = (newWidth.toFloat() / source.width * source.height).roundToInt()
        } else {
            newHeight = windowSizePoint.y
            newWidth = (newHeight.toFloat() / source.height * source.width).roundToInt()
        }

        return Bitmap.createScaledBitmap(source, newWidth, newHeight, false)
    }

    private fun startWork(context : Context){
        WorkManager.getInstance(context.applicationContext).cancelAllWork()
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        // set execution around 00:00:00
        dueDate.set(Calendar.HOUR_OF_DAY, 0)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

        val saveRequest =
            androidx.work.OneTimeWorkRequest.Builder(
                RotateWallpaperWorker::class.java
            )
                .addTag("Rotation")
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .build()

        WorkManager.getInstance(context.applicationContext)
            .enqueue(saveRequest)
        Log.d("MainViewModel", "Work started")
    }

}