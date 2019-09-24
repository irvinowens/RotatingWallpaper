package us.sigsegv.rotatingwallpapers.ui.main

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.sample_image_cell_view.view.*
import us.sigsegv.rotatingwallpapers.R
import java.io.File

class ImageFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    lateinit var model: MainViewModel
    lateinit var file: File
    val imageView: ImageView = itemView.findViewById(R.id.imageView)
    val textView: TextView = itemView.findViewById(R.id.fileDescription)
    init {
        itemView.setOnLongClickListener(View.OnLongClickListener {
            Log.d("ImageFileViewHolder", "Long clicked!")
            model.deleteFile(file)
            true
        })
    }
}