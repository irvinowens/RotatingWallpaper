package us.sigsegv.rotatingwallpapers.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.FileUtils
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MainViewModel() : ViewModel() {

    private var inputPFD: ParcelFileDescriptor? = null
    internal val fileList: ArrayList<File> = ArrayList(20)
    private var internalRecyclerAdapter: FilesRecyclerAdapter? = null
    var context: Context? = null


    fun load(recyclerAdapter: FilesRecyclerAdapter?){
        viewModelScope.launch {
            val loading = async {loadFiles()}
            loading.await()
            internalRecyclerAdapter = recyclerAdapter
            recyclerAdapter?.notifyDataSetChanged()
        }
    }

    private suspend fun loadFiles() = withContext(Dispatchers.IO) {
        val files: Iterator<File>? = context?.filesDir?.listFiles()?.iterator()
        if(files != null) {
            var i = 0
            while(files.hasNext()) {
                fileList.add(files.next())
                i++
            }
        }
    }

    fun getFileCount() : Int {
        return fileList.size
    }

    fun getImageFileForPosition(position: Int): File? {
        return fileList[position]
    }

    suspend fun getImageAndSave(data: Intent?) = withContext(Dispatchers.IO) {
        // Get the file's content URI from the incoming Intent
        if(context == null) {
            return@withContext
        }
        data?.also { returnUri ->
            /*
             * Try to open the file for "read" access using the
             * returned URI. If the file isn't found, write to the
             * error log and return.
             */
            val uri: Uri? = returnUri.data
            if(uri == null) {
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
            }

            // Get a regular file descriptor for the file
            val fd = inputPFD?.fileDescriptor
            if(uri != null && fd != null) {
                /*
                 * Get the column indexes of the data in the Cursor,
                 * move to the first row in the Cursor, get the data,
                 * and display it.
                 */
                val cursor = context!!.contentResolver?.query(uri,
                    null, null, null, null)
                if(cursor != null) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    val fileName = cursor.getString(nameIndex)
                    cursor.close()
                    val bufferedInputStream = BufferedInputStream(FileInputStream(fd))
                    val file = File(context!!.filesDir, fileName)
                    if(file.exists()) {
                        Log.i("MainViewModel", "File exists")
                        bufferedInputStream.close()
                        inputPFD?.close()
                        return@withContext
                    }
                    val bufferedOutputStream = BufferedOutputStream(FileOutputStream(file))
                    FileUtils.copy(bufferedInputStream, bufferedOutputStream)
                    bufferedInputStream.close()
                    bufferedOutputStream.close()
                    if(!fileList.contains(file)) {
                        fileList.add(file)
                    }
                }
            }
            inputPFD?.close()
        }

    }

    fun fetchImageAndSave(data: Intent?, recyclerAdapter: FilesRecyclerAdapter?) {
        viewModelScope.launch {
            val saving = async {getImageAndSave(data) }
            saving.await()
            recyclerAdapter?.notifyItemInserted(fileList.size)
        }
    }

    fun deleteFile(file: File) {
        viewModelScope.launch {
            val deleting = async { asyncDeleteFile(file) }
            deleting.await()
            val index = fileList.indexOf(file)
            if(index != -1) {
                internalRecyclerAdapter?.notifyItemRemoved(index)
                fileList.removeAt(index)
            } else {
                internalRecyclerAdapter?.notifyDataSetChanged()
            }
        }
    }

    suspend fun asyncDeleteFile(file: File) = withContext(Dispatchers.IO) {
        file.delete()
        Log.d("MainViewModel", "File deleted")
    }

    fun stopWork(context: Context) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork("Rotation")
    }

    fun startWork(context : Context){
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
