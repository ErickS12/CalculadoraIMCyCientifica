package com.fcc.calculadoraimcycientifica

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fcc.calculadoraimcycientifica.databinding.ActivityMainBinding
import androidx.fragment.app.commit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val esHorizontal = resources.configuration.orientation ==
                Configuration.ORIENTATION_LANDSCAPE
        val actual = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        android.util.Log.d("ROT", "horizontal=$esHorizontal actual=${actual?.javaClass?.simpleName}")

        val necesitaCientifica = esHorizontal && actual !is CalculadoraCientificaFragment
        val necesitaImc = !esHorizontal && actual !is CalculadoraImcFragment

        if (necesitaCientifica || necesitaImc) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(
                    R.id.fragmentContainerView,
                    if (esHorizontal) CalculadoraCientificaFragment() else CalculadoraImcFragment()
                )
            }
        }
    }
}