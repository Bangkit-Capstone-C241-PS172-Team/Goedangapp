package com.example.goedangapp.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.goedangapp.R
import com.example.goedangapp.databinding.ActivityMainBinding
import com.example.goedangapp.ui.dashboard.DashboardFragment
import com.example.goedangapp.ui.sales.SalesFragment
import com.example.goedangapp.ui.inventory.StockFragment
import com.example.goedangapp.ui.input.StockInputFragment
import com.example.goedangapp.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(DashboardFragment())

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboard -> {
                    replaceFragment(DashboardFragment())
                    true
                }
                R.id.sales -> {
                    replaceFragment(SalesFragment())
                    true
                }
                R.id.stock -> {
                    replaceFragment(StockFragment())
                    true
                }
                R.id.profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        binding.stockInput.setOnClickListener{
            replaceFragment(StockInputFragment())

            // untuk menonaktifkan item lain saat stockInput aktif
            binding.bottomNavigationView.selectedItemId = R.id.fab
        }

    }

    // Untuk ganti fragment di main screen
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}