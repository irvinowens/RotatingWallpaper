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

package us.sigsegv.rotatingwallpapers.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.os.FileUtils
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import us.sigsegv.rotatingwallpapers.R
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class MainViewModel : ViewModel() {

    private var inputPFD: ParcelFileDescriptor? = null
    internal val fileList: ArrayList<File> = ArrayList(20)
    private var internalRecyclerAdapter: FilesRecyclerAdapter? = null
    var context: Context? = null


    fun load(recyclerAdapter: FilesRecyclerAdapter?) {
        viewModelScope.launch {
            val loading = async { loadFiles() }
            loading.await()
            internalRecyclerAdapter = recyclerAdapter
            recyclerAdapter?.notifyDataSetChanged()
        }
    }

    fun loadLicense(ctx: Context, tv: TextView) {
        viewModelScope.launch {
            val loading = async { loadLicenseContentIntoTextView(ctx, tv) }
            loading.await()
            Log.v("LicenseDisclosure", "Finished loading license")
        }
    }

    private suspend fun loadLicenseContentIntoTextView(ctx: Context, tv: TextView) = withContext(Dispatchers.IO) {
        Log.v("LicenseDisclosure", "Loading the license disclosure")
        try {
            val inputStream: InputStream = ctx.resources.openRawResource(R.raw.license)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.readText()
            tv.text = content
            Log.v("LicenseDisclosure", String.format(Locale.ENGLISH, "The content: %s", content))
            reader.close()
            inputStream.close()
        } catch (ex: IOException) {
            Log.w("LicenseDisclosure", "Couldn't load the license text")
        }
    }

    private suspend fun loadFiles() = withContext(Dispatchers.IO) {
        val files: Iterator<File>? = context?.filesDir?.listFiles()?.iterator()
        if (files != null) {
            var i = 0
            while (files.hasNext()) {
                fileList.add(files.next())
                i++
            }
        }
    }

    fun getFileCount(): Int {
        return fileList.size
    }

    fun getImageFileForPosition(position: Int): File? {
        return fileList[position]
    }

    private suspend fun getImageAndSave(data: Intent?) = withContext(Dispatchers.IO) {
        // Get the file's content URI from the incoming Intent
        if (context == null) {
            return@withContext
        }
        data?.also { returnUri ->
            /*
             * Try to open the file for "read" access using the
             * returned URI. If the file isn't found, write to the
             * error log and return.
             */
            val uri: Uri? = returnUri.data
            if (uri == null) {
                Log.e("MainViewModel", "URI was null")
            }
            try {
                /*
                 * Get the content resolver instance for this context, and use it
                 * to get a ParcelFileDescriptor for the file.
                 */
                inputPFD =
                    returnUri.data?.let {
                        context!!.contentResolver?.openFileDescriptor(it, "r")
                    }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Log.e("MainViewModel", "File not found")
                return@withContext
            }

            // Get a regular file descriptor for the file
            val fd = inputPFD?.fileDescriptor
            if (uri != null && fd != null) {
                /*
                 * Get the column indexes of the data in the Cursor,
                 * move to the first row in the Cursor, get the data,
                 * and display it.
                 */
                val cursor = context!!.contentResolver?.query(
                    uri,
                    null, null, null, null
                )
                if (cursor != null) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    val fileName = cursor.getString(nameIndex)
                    cursor.close()
                    val bufferedInputStream = BufferedInputStream(FileInputStream(fd))
                    val file = File(context!!.filesDir, fileName)
                    if (fileExists(fd)) {
                        Log.i("MainViewModel", "File exists")
                        bufferedInputStream.close()
                        inputPFD?.close()
                        return@withContext
                    }
                    var bufferedOutputStream: BufferedOutputStream? = null
                    try {
                        bufferedOutputStream = BufferedOutputStream(FileOutputStream(file))
                        FileUtils.copy(bufferedInputStream, bufferedOutputStream)
                    } catch (ex: IOException) {
                        Log.e("MainViewModel", "Could not interface with the file")
                        inputPFD?.close()
                        file.delete()
                        return@withContext
                    } catch (ea: SecurityException) {
                        Log.e("MainViewModel", "Security violation accessing the file")
                        inputPFD?.close()
                        file.delete()
                        return@withContext
                    } finally {
                        bufferedInputStream.close()
                        bufferedOutputStream?.close()
                    }
                    if (!fileList.contains(file)) {
                        fileList.add(file)
                    }
                }
            }
            inputPFD?.close()
        }

    }

    private fun fileExists(file: FileDescriptor): Boolean {
        val files: Iterator<File>? = context?.filesDir?.listFiles()?.iterator()
        if (files != null) {
            var i = 0
            while (files.hasNext()) {
                val existingAbsolutePath: String = files.next().absolutePath
                val bitmap: Bitmap? = BitmapFactory.decodeFile(existingAbsolutePath)
                Log.d(
                    "MainViewModel",
                    String.format(Locale.ENGLISH, "Saved File Path: %s", existingAbsolutePath)
                )
                val givenBitmap: Bitmap? = BitmapFactory.decodeFileDescriptor(file)
                if (bitmap != null && givenBitmap != null && bitmap.sameAs(givenBitmap)) {
                    return true
                }
                i++
            }
        }
        return false
    }

    fun fetchImageAndSave(data: Intent?, recyclerAdapter: FilesRecyclerAdapter?) {
        viewModelScope.launch {
            val saving = async { getImageAndSave(data) }
            saving.await()
            recyclerAdapter?.notifyItemInserted(fileList.size)
        }
    }

    fun deleteFile(file: File) {
        viewModelScope.launch {
            val index = fileList.indexOf(file)
            val deleting = async { asyncDeleteFile(file) }
            deleting.await()
            if (index != -1) {
                fileList.removeAt(index)
                internalRecyclerAdapter?.notifyItemRemoved(index)
            } else {
                internalRecyclerAdapter?.notifyDataSetChanged()
            }
        }
    }

    private suspend fun asyncDeleteFile(file: File) = withContext(Dispatchers.IO) {
        file.delete()
        Log.d("MainViewModel", "File deleted")
    }

    fun stopWork(context: Context) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork("Rotation")
    }

    fun loadScaledImage(context: Context, iv: ImageView, uri: Uri) {
        viewModelScope.launch {
            val result = async {
                loadScaledImageIntoImageView(context ,iv, uri)
            }
            result.await()
            Log.v("MainViewModel", "WYSIWYG version loading started")
        }
    }

    private suspend fun loadScaledImageIntoImageView(context: Context, iv: ImageView, uri: Uri) = withContext(Dispatchers.IO) {
        val picassoBitmap = Picasso.with(context).load(uri).get()
        val bitmap = transformAndCrop(picassoBitmap, context)
        launch(Dispatchers.Main) {
            iv.setImageBitmap(bitmap)
            Log.v("MainViewModel", "WYSIWYG version loading completed")
        }
    }

    fun startWork(context: Context) {
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

    private fun transformAndCrop(source: Bitmap, context: Context) :Bitmap {
        val bitmap = transform(source, context)
        val windowManager: WindowManager = context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val windowSizePoint = Point()
        windowManager.defaultDisplay.getSize(windowSizePoint)
        return if(bitmap.width > windowSizePoint.x || bitmap.height > windowSizePoint.y) {
            Bitmap.createBitmap(bitmap, 0, 0, windowSizePoint.x, windowSizePoint.y)
        } else {
            bitmap
        }
    }

    fun transform(source: Bitmap, context: Context): Bitmap {
        val windowManager: WindowManager = context
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
}
