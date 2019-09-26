package us.sigsegv.rotatingwallpapers.ui.main

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import us.sigsegv.rotatingwallpapers.R
import java.io.File

class FilesRecyclerAdapter(val model: MainViewModel) : RecyclerView.Adapter<ImageFileViewHolder>() {
    init {
        model.load(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageFileViewHolder {
        return ImageFileViewHolder(LayoutInflater
                .from(parent.context)
                .inflate(R.layout.sample_image_cell_view, parent,false))
    }

    override fun getItemCount(): Int {
        val count = model.getFileCount()
        Log.d("FilesRecyclerAdapter", "File count $count")
        return count
    }

    override fun onBindViewHolder(holder: ImageFileViewHolder, position: Int) {
        val f : File? = model.getImageFileForPosition(position)
        if(f != null) {
            holder.file = f
            holder.model = model
            holder.textView.text = f.name
            Picasso.with(holder.imageView.context)
                .load(f)
                .into(holder.imageView)
            holder.itemView.setOnClickListener {
                Log.v("FilesRecyclerAdapter", "Clicked!")
                (it.context as AppCompatActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.container, DetailFragment.newInstance(f.toURI().toString()))
                    .addToBackStack("detail")
                    .commit()
            }
        }
    }
}