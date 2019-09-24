package us.sigsegv.rotatingwallpapers.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
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
    private lateinit var requestFileIntent: Intent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requestFileIntent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.context = context
        recyclerView = activity?.findViewById(R.id.imagesRecycler)
        fab = activity?.findViewById(R.id.floatingActionButton)
        if(fab != null) {
            fab?.setOnClickListener(View.OnClickListener {
                requestImage()
            })
        }
        recyclerAdapter = FilesRecyclerAdapter(viewModel)
        if(recyclerView != null) {
            val llm = LinearLayoutManager(context)
            llm.orientation = LinearLayoutManager.VERTICAL
            recyclerView?.layoutManager = llm
            recyclerView?.adapter = recyclerAdapter
        }
        viewModel.load(recyclerAdapter)
    }

    private fun requestImage() {
        startActivityForResult(requestFileIntent, 0)
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
        viewModel.fetchImageAndSave(data, recyclerAdapter)
    }
}
