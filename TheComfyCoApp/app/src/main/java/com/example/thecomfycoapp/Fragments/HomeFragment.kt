//package com.example.thecomfycoapp.Fragments
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.fragment.app.Fragment
//import com.example.thecomfycoapp.HomeActivity
//import com.example.thecomfycoapp.R
//
//class HomeFragment : Fragment() {
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? = inflater.inflate(R.layout.fragment_home, container, false)
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Hamburger icon
//        val ivMenu = view.findViewById<ImageView>(R.id.ivMenu)
//        ivMenu.setOnClickListener {
//            (requireActivity() as? HomeActivity)?.openDrawer()
//        }
//
//        // Welcome text
//        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
//        val fullName = requireActivity().intent.getStringExtra("name") ?: "User"
//        tvWelcome.text = "HI,\n$fullName!"
//    }
//}
package com.example.thecomfycoapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.thecomfycoapp.HomeActivity
import com.example.thecomfycoapp.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hamburger
        view.findViewById<ImageView>(R.id.ivMenu).setOnClickListener {
            (requireActivity() as? HomeActivity)?.openDrawer()
        }

        // Welcome text (optional)
        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        val fullName = requireActivity().intent.getStringExtra("name") ?: "USER"
        tvWelcome.text = "HI,\n$fullName!"
    }
}
