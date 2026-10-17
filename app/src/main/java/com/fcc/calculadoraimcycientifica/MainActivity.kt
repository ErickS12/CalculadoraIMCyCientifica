package com.fcc.calculadoraimcycientifica

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fcc.calculadoraimcycientifica.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        if (currentFragment !is CalculadoraImcFragment) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, CalculadoraImcFragment())
                .commit()
        }
    }
}