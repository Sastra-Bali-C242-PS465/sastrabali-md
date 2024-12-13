package com.example.sabi.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sabi.data.store.SessionPreferences
import com.example.sabi.databinding.FragmentProfileBinding
import com.example.sabi.R
import com.example.sabi.ui.activity.LoginActivity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var sessionPreferences: SessionPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        sessionPreferences = SessionPreferences(requireContext())

        lifecycleScope.launch {
            sessionPreferences.email.collect { email ->
                binding.emailText.text = email ?: "Email tidak tersedia"
            }
        }

        binding.logoutButton.setOnClickListener {
            showLogoutDialog()
        }

        return binding.root
    }

    private fun showLogoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnBack).setOnClickListener {
            lifecycleScope.launch {
                sessionPreferences.clearSession()

                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                dialog.dismiss()
            }
        }

        dialogView.findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}