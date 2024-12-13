package com.example.sabi.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sabi.R
import com.example.sabi.databinding.ActivityMainBinding
import com.example.sabi.ui.activity.camera.CameraActivity
import com.example.sabi.ui.fragment.HomeFragment
import com.example.sabi.ui.fragment.ProfileFragment
import com.example.sabi.ui.fragment.dictionary.DictionaryFragment
import com.example.sabi.ui.fragment.quiz.QuizFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadFragment(HomeFragment())

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.navigation_dictionary -> {
                    loadFragment(DictionaryFragment())
                    true
                }
                R.id.navigation_quiz -> {
                    loadFragment(QuizFragment())
                    true
                }
                R.id.navigation_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        binding.fabScan.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main, fragment)
            .commit()
    }
}
