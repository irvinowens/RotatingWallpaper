/*
 * Copyright (c) 2020. Irvin Owens Jr
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package us.sigsegv.rotatingwallpapers

import android.app.WallpaperManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import us.sigsegv.rotatingwallpapers.ui.main.DetailFragment
import us.sigsegv.rotatingwallpapers.ui.main.LicenseDisclosure
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

    /**
     * Dispatch incoming result to the correct fragment.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == Constants.SAVE_EDITED_FILE) {
            val editedImage: Uri? = data?.data
            Log.v("MainActivity", "Edited Image URI $editedImage")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if(supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            Log.v("MainActivity", "No fragments to pop, finishing activity")
            finish()
        }
        return true
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

        R.id.action_show_license -> {
            (this as AppCompatActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.container, LicenseDisclosure.newInstance())
                .addToBackStack("license")
                .commit()
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.context = this
    }

    private fun setWallpaper(uri : Uri) {
        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
        val picassoBitmap = Picasso.get().load(uri).get()
        val bitmap = viewModel.transform(picassoBitmap, applicationContext)
        wallpaperManager.setBitmap(bitmap, null, true,
            WallpaperManager.FLAG_LOCK or
                    WallpaperManager.FLAG_SYSTEM)
        Log.d("RotateWallpaperWorker", "Rotated wallpaper")
        showSnackbar("Wallpaper selected")
    }

    private fun showSnackbar(text: String) {
        val view : View? = findViewById(R.id.main)
        if(view != null) {
            Snackbar.make(view, text, Snackbar.LENGTH_LONG).show()
        }
    }

    class Constants {
        companion object {
            val SAVE_EDITED_FILE = 12721;
        }
    }
}
