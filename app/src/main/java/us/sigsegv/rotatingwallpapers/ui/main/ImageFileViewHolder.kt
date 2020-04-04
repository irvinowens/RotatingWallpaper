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

import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import us.sigsegv.rotatingwallpapers.R
import java.io.File

class ImageFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    lateinit var model: MainViewModel
    lateinit var file: File
    val imageView: ImageView = itemView.findViewById(R.id.imageView)
    val textView: TextView = itemView.findViewById(R.id.fileDescription)
    init {
        itemView.setOnLongClickListener {
            Log.d("ImageFileViewHolder", "Long clicked!")
            val dialog = showDialogChoice(it, file)
            dialog.show()
            true
        }
    }

    private fun showDialogChoice(view: View, file: File) : AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(view.context)
            .setTitle(R.string.delete_image_title)
            .setMessage(R.string.delete_image_message)
            .setNegativeButton(R.string.cancel_button_text
            ) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.ok_button_text) { dialog, _ ->
                model.deleteFile(file)
                dialog.dismiss()
            }
        return builder.create()
    }
}