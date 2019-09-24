package us.sigsegv.rotatingwallpapers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.Constraints
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import us.sigsegv.rotatingwallpapers.ui.main.MainFragment
import us.sigsegv.rotatingwallpapers.ui.main.RotateWallpaperWorker
import java.time.Duration
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
        WorkManager.getInstance(this).cancelAllWorkByTag("Rotation")
        val constraints = androidx.work.Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(true)
            .build()

        val saveRequest =
            androidx.work.PeriodicWorkRequest.Builder(RotateWallpaperWorker::class.java,
                24, TimeUnit.HOURS, 1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .addTag("Rotation")
                .build()

        WorkManager.getInstance(this)
            .enqueue(saveRequest)
    }

}
