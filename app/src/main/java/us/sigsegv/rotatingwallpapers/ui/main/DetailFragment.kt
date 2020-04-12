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
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import us.sigsegv.rotatingwallpapers.MainActivity
import us.sigsegv.rotatingwallpapers.R
import java.io.File


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_FILE_URI = "file_uri"

/**
 * Will display the image that is to be used for the wallpaper
 */
class DetailFragment : Fragment() {
    private lateinit var viewModel: MainViewModel
    private var imageUri: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var image : ImageView? = null
    private var imageSizeTextView : TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
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
        imageSizeTextView = activity?.findViewById<TextView>(R.id.imageSizeTextView)
        if(imageUri != null && image != null) {
            Log.v("DetailFragment", "Loading image")
            viewModel.loadScaledImage(context!!.applicationContext, image!!, Uri.parse(imageUri), imageSizeTextView)
            image?.setOnClickListener {
                val dialog = showDialogForSettingBackground(it, Uri.parse(imageUri))
                dialog.show()
            }
            image?.setOnLongClickListener {
                Log.d("ImageFileViewHolder", "Long clicked!")
                val dialog = showDialogChoice(it, Uri.parse(imageUri))
                dialog.show()
                true
            }
            val appContext = context?.applicationContext
            if(appContext != null) {
                val point = viewModel.getPortraitScreenSize(appContext)
                Toast.makeText(
                    context,
                    context?.getString(R.string.screen_size_text, point.y, point.x),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Log.v("DetailFragment", "Image uri was null")
        }
    }

    private fun showDialogForSettingBackground(view: View, file: Uri) : AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(view.context)
            .setTitle(R.string.set_image_to_background)
            .setMessage(R.string.set_image_to_background_message)
            .setNegativeButton(R.string.cancel_button_text
            ) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.ok_button_text) { dialog, _ ->
                listener!!.onFragmentInteraction(file)
                showSnackbar("Changed wallpaper")
                dialog.dismiss()
            }
        return builder.create()
    }

    private fun showDialogChoice(view: View, file: Uri) : AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(view.context)
            .setTitle(R.string.edit_image_title)
            .setMessage(R.string.edit_image_message)
            .setNegativeButton(R.string.cancel_button_text
            ) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.ok_button_text) { dialog, _ ->
                checkAndLaunchImageEditingApp(file)
                dialog.dismiss()
            }
        return builder.create()
    }

    private fun checkAndLaunchImageEditingApp(file: Uri) {
        val editIntent = Intent(Intent.ACTION_EDIT)
        val photoURI = FileProvider.getUriForFile(
            context!!,
            context!!.applicationContext.packageName + ".fileprovider",
            File(file.path!!)
        )
        editIntent.setDataAndType(photoURI, "image/*")
        editIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        val context = activity?.applicationContext
        if(context != null) {
            val activities: List<ResolveInfo> = context.packageManager.queryIntentActivities(
                editIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            val isIntentSafe: Boolean = activities.isNotEmpty()
            if(isIntentSafe) {
                this.startActivityForResult(editIntent, MainActivity.Constants.SAVE_EDITED_FILE)
            } else {
                val string = activity?.getString(R.string.no_application_available_to_edit)
                if(string == null) {
                    showSnackbar("No app to edit")
                } else {
                    showSnackbar(string)
                }
            }
        } else {
            val string = activity?.getString(R.string.no_application_available_to_edit)
            if(string == null) {
                showSnackbar("No app to edit")
            } else {
                showSnackbar(string)
            }
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
