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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import us.sigsegv.rotatingwallpapers.R


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var recyclerAdapter: FilesRecyclerAdapter
    private var recyclerView: RecyclerView? = null
    private var fab : FloatingActionButton? = null
    private var progressBar: FrameLayout? = null
    private lateinit var requestFileIntent: Intent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * [.setRetainInstance] to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after [.onCreateView]
     * and before [.onViewStateRestored].
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.context = context
        recyclerAdapter = FilesRecyclerAdapter(viewModel)
        requestFileIntent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        recyclerView = activity?.findViewById(R.id.imagesRecycler)
        fab = activity?.findViewById(R.id.floatingActionButton)
        progressBar = activity?.findViewById(R.id.progressBar)
        viewModel.progressBar = progressBar
        if(fab != null) {
            fab?.setOnClickListener(View.OnClickListener {
                requestImage()
            })
        }
        if(recyclerView != null) {
            val llm = LinearLayoutManager(context)
            llm.orientation = LinearLayoutManager.VERTICAL
            recyclerView?.layoutManager = llm
            recyclerView?.adapter = recyclerAdapter
        }
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to [Activity.onResume] of the containing
     * Activity's lifecycle.
     */
    override fun onResume() {
        super.onResume()
        if(viewModel.fileList.isEmpty()) {
            viewModel.load(recyclerAdapter)
        }
    }

    private fun requestImage() {
        startActivityForResult(requestFileIntent, 716)
    }

    /*
     * When the Activity of the app that hosts files sets a result and calls
     * finish(), this method is invoked. The returned Intent contains the
     * content URI of a selected file. The result code indicates if the
     * selection worked or not.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // If the selection didn't work
        if (resultCode != Activity.RESULT_OK) {
            // Exit without doing anything else
            return
        }
        if (requestCode == 716) {
            progressBar?.visibility = View.VISIBLE
            viewModel.fetchImageAndSave(data, recyclerAdapter)
            Log.v("MainFragment", "Adding image")
        } else {
            // normal launch, ignoring
            Log.v("MainFragment", "Normal Launch Ignoring")
        }
    }
}
