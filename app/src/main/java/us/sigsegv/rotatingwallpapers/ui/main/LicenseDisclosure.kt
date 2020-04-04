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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import us.sigsegv.rotatingwallpapers.R

/**
 * A simple [Fragment] subclass.
 * Use the [LicenseDisclosure.newInstance] factory method to
 * create an instance of this fragment.
 */
class LicenseDisclosure : Fragment() {
    private lateinit var viewModel: MainViewModel
    private lateinit var licenseContent: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_license_disclosure, container, false)
        licenseContent = view.findViewById(R.id.license_content);
        if(view.context != null) {
            viewModel.loadLicense(view.context, licenseContent)
        }
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment LicenseDisclosure.
         */
        @JvmStatic
        fun newInstance() =
            LicenseDisclosure().apply {
                arguments = Bundle().apply {}
            }
    }
}
