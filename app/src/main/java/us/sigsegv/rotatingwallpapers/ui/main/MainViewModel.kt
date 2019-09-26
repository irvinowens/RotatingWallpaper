package us.sigsegv.rotatingwallpapers.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.FileUtils
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import android.util.SparseArray
import androidx.core.util.containsValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import java.io.*

class MainViewModel() : ViewModel() {

    private var inputPFD: ParcelFileDescriptor? = null
    private val fileList: SparseArray<File> = SparseArray(1000)
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
                fileList.append(i, files.next())
                i++
            }
        }
    }

    fun getFileCount() : Int {
        return fileList.size()
    }

    fun getImageFileForPosition(position: Int): File? {
        return fileList.get(position)
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
                    val bufferedOutputStream = BufferedOutputStream(FileOutputStream(file))
                    FileUtils.copy(bufferedInputStream, bufferedOutputStream)
                    bufferedInputStream.close()
                    bufferedOutputStream.close()
                    if(!fileList.containsValue(file)) {
                        fileList.append(fileList.size(), file)
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
            recyclerAdapter?.notifyItemInserted(fileList.size())
        }
    }

    fun deleteFile(file: File) {
        viewModelScope.launch {
            val deleting = async { asyncDeleteFile(file) }
            deleting.await()
            val index = fileList.indexOfValue(file)
            internalRecyclerAdapter?.notifyItemRemoved(index)
            fileList.removeAt(index)
        }
    }

    suspend fun asyncDeleteFile(file: File) = withContext(Dispatchers.IO) {
        file.delete()
        Log.d("MainViewModel", "File deleted")
    }
}
