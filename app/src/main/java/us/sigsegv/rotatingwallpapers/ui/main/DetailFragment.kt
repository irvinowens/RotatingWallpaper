package us.sigsegv.rotatingwallpapers.ui.main

import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.graphics.scale
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.sample_image_cell_view.view.*

import us.sigsegv.rotatingwallpapers.R
import java.io.File
import java.net.URI

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_FILE_URI = "file_uri"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [DetailFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [DetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailFragment : Fragment() {
    private var imageUri: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var image : ImageView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageUri = it.getString(ARG_FILE_URI)
            Log.v("DetailFragment", "$imageUri for image")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to [Activity.onResume] of the containing
     * Activity's lifecycle.
     */
    override fun onResume() {
        super.onResume()
        // let's just display image if possible
        image = activity?.findViewById(R.id.detailImageView)
        if(imageUri != null && image != null) {
            Log.v("DetailFragment", "Loading image")
            val wm = activity?.windowManager
            val display = wm?.defaultDisplay
            val size = Point()
            display?.getSize(size)
            val displayWidth = size.x
            val displayHeight = size.y
            Picasso.with(context?.applicationContext).load(imageUri!!)
                .resize(displayWidth, displayHeight).into(image)
            image?.setOnClickListener(View.OnClickListener {
                listener!!.onFragmentInteraction(Uri.parse(imageUri))
                showSnackbar("Changed wallpaper")
            })
        } else {
            Log.v("DetailFragment", "Image uri was null")
        }
    }

    private fun showSnackbar(text: String) {
        val view : View? = activity?.findViewById(R.id.detailImageView)
        if(view != null) {
            Snackbar.make(view, text, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
        image = activity?.findViewById(R.id.detailImageView)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment DetailFragment.
         * */
        @JvmStatic
        fun newInstance(param1: String) =
            DetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FILE_URI, param1)
                }
            }
    }
}
