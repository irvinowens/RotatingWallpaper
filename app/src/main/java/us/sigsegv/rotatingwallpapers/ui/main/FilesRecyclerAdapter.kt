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

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import us.sigsegv.rotatingwallpapers.R
import java.io.File

class FilesRecyclerAdapter(val model: MainViewModel) : RecyclerView.Adapter<ImageFileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageFileViewHolder {
        return ImageFileViewHolder(LayoutInflater
                .from(parent.context)
                .inflate(R.layout.sample_image_cell_view, parent,false))
    }

    override fun getItemCount(): Int {
        return model.getFileCount()
    }

    override fun onBindViewHolder(holder: ImageFileViewHolder, position: Int) {
        val f : File? = model.getImageFileForPosition(position)
        if(f != null) {
            holder.file = f
            holder.model = model
            holder.textView.text = f.name
            Picasso.get()
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