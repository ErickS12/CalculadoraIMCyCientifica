package com.fcc.calculadoraimcycientifica

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.fcc.calculadoraimcycientifica.databinding.FragmentCalculadoraIMCBinding
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class CalculadoraImcFragment : Fragment() {

    private var _binding: FragmentCalculadoraIMCBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ImcViewModel

    private val atras = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            viewModel.editarDatos()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = requireActivity()
        val factory = SavedStateViewModelFactory(activity.application, activity)
        viewModel = ViewModelProvider(activity, factory)[ImcViewModel::class.java]
        //android.util.Log.d("VM", "ImcViewModel id=${System.identityHashCode(viewModel)}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalculadoraIMCBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, atras)

        restaurarFormulario()
        escucharCambios()
        configurarBotones()
        observarEstado()
    }

    private fun restaurarFormulario() {
        if (viewModel.esMetrico) {
            binding.toggleSistema.check(R.id.btnMetrico)
            binding.layoutAltura.visibility = View.VISIBLE
            binding.filaAlturaIngles.visibility = View.GONE
            binding.layoutPeso.suffixText = getString(R.string.unidad_kg)
        } else {
            binding.toggleSistema.check(R.id.btnIngles)
            binding.layoutAltura.visibility = View.GONE
            binding.filaAlturaIngles.visibility = View.VISIBLE
            binding.layoutPeso.suffixText = getString(R.string.unidad_lb)
        }

        when (viewModel.sexo) {
            Sexo.MUJER -> binding.toggleSexo.check(R.id.btnMujer)
            Sexo.HOMBRE -> binding.toggleSexo.check(R.id.btnHombre)
            null -> binding.toggleSexo.clearChecked()
        }

        binding.etEdad.setText(viewModel.edad)
        binding.etPeso.setText(viewModel.peso)
        binding.etAltura.setText(viewModel.altura)
        binding.etAlturaFt.setText(viewModel.alturaFt)
        binding.etAlturaIn.setText(viewModel.alturaIn)
        binding.etCintura.setText(viewModel.cintura)

        validarFormulario()
    }

    private fun escucharCambios() {
        binding.toggleSistema.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnMetrico -> {
                        viewModel.esMetrico = true
                        binding.layoutAltura.visibility = View.VISIBLE
                        binding.filaAlturaIngles.visibility = View.GONE
                        binding.layoutPeso.suffixText = getString(R.string.unidad_kg)
                    }
                    R.id.btnIngles -> {
                        viewModel.esMetrico = false
                        binding.layoutAltura.visibility = View.GONE
                        binding.filaAlturaIngles.visibility = View.VISIBLE
                        binding.layoutPeso.suffixText = getString(R.string.unidad_lb)
                    }
                }
                validarFormulario()
            }
        }

        binding.toggleSexo.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnMujer -> viewModel.sexo = Sexo.MUJER
                    R.id.btnHombre -> viewModel.sexo = Sexo.HOMBRE
                }
            } else if (binding.toggleSexo.checkedButtonId == View.NO_ID) {
                viewModel.sexo = null
            }
            validarFormulario()
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.edad = binding.etEdad.text?.toString() ?: ""
                viewModel.peso = binding.etPeso.text?.toString() ?: ""
                viewModel.altura = binding.etAltura.text?.toString() ?: ""
                viewModel.alturaFt = binding.etAlturaFt.text?.toString() ?: ""
                viewModel.alturaIn = binding.etAlturaIn.text?.toString() ?: ""
                viewModel.cintura = binding.etCintura.text?.toString() ?: ""
                validarFormulario()
            }
        }

        binding.etEdad.addTextChangedListener(textWatcher)
        binding.etPeso.addTextChangedListener(textWatcher)
        binding.etAltura.addTextChangedListener(textWatcher)
        binding.etAlturaFt.addTextChangedListener(textWatcher)
        binding.etAlturaIn.addTextChangedListener(textWatcher)
        binding.etCintura.addTextChangedListener(textWatcher)
    }

    private fun validarFormulario() {
        val edadValida = viewModel.edad.isNotBlank() && (viewModel.edad.toIntOrNull() ?: 0) > 0
        val pesoValido = viewModel.peso.isNotBlank() && (viewModel.peso.toDoubleOrNull() ?: 0.0) > 0.0
        val sexoValido = viewModel.sexo != null
        val alturaValida = if (viewModel.esMetrico) {
            viewModel.altura.isNotBlank() && (viewModel.altura.toDoubleOrNull() ?: 0.0) > 0.0
        } else {
            val ft = viewModel.alturaFt.toDoubleOrNull() ?: 0.0
            val inch = viewModel.alturaIn.toDoubleOrNull() ?: 0.0
            (ft * 12 + inch) > 0.0
        }

        binding.btnCalcular.isEnabled = edadValida && pesoValido && sexoValido && alturaValida
    }

    private fun configurarBotones() {
        binding.btnCalcular.setOnClickListener {
            ocultarTeclado()
            viewModel.calcular()
        }

        binding.btnLimpiar.setOnClickListener {
            limpiarCampos()
        }

        binding.resultado.btnEditarDatos.setOnClickListener {
            viewModel.editarDatos()
        }

        binding.resultado.btnNuevoCalculo.setOnClickListener {
            viewModel.nuevoCalculo()
            limpiarCampos()
        }
    }

    private fun limpiarCampos() {
        viewModel.limpiar()
        binding.toggleSexo.clearChecked()
        binding.etEdad.setText("")
        binding.etPeso.setText("")
        binding.etAltura.setText("")
        binding.etAlturaFt.setText("")
        binding.etAlturaIn.setText("")
        binding.etCintura.setText("")
        validarFormulario()
    }

    private fun observarEstado() {
        viewModel.mostrandoResultado.observe(viewLifecycleOwner) { mostrando ->
            if (mostrando) {
                calcularYMostrarResultado()
                binding.nestedScrollView.visibility = View.GONE
                binding.barraInferior.visibility = View.GONE
                binding.divisor.visibility = View.GONE
                binding.resultado.root.visibility = View.VISIBLE
                atras.isEnabled = true
            } else {
                binding.resultado.root.visibility = View.GONE
                binding.nestedScrollView.visibility = View.VISIBLE
                binding.barraInferior.visibility = View.VISIBLE
                binding.divisor.visibility = View.VISIBLE
                atras.isEnabled = false
            }
        }
    }

    private fun calcularYMostrarResultado() {
        val pesoKg: Double
        val alturaCm: Double

        if (viewModel.esMetrico) {
            pesoKg = viewModel.peso.toDoubleOrNull() ?: 0.0
            alturaCm = viewModel.altura.toDoubleOrNull() ?: 0.0
        } else {
            val pesoLb = viewModel.peso.toDoubleOrNull() ?: 0.0
            pesoKg = pesoLb * 0.45359237
            val ft = viewModel.alturaFt.toDoubleOrNull() ?: 0.0
            val inch = viewModel.alturaIn.toDoubleOrNull() ?: 0.0
            val alturaInches = ft * 12 + inch
            alturaCm = alturaInches * 2.54
        }

        val alturaMetros = alturaCm / 100.0
        val imc = if (alturaMetros > 0) pesoKg / (alturaMetros * alturaMetros) else 0.0

        binding.resultado.tvValorImc.text = String.format(Locale.getDefault(), "%.1f", imc)

        // Sintaxis matemática corregida sin guiones ni saltos de línea erróneos
        val (categoriaRes, percent) = when {
            imc < 18.5 -> {
                val pct = max(0f, min(0.14f, (imc / 18.5 * 0.14).toFloat()))
                Pair(R.string.rango_bajo, pct)
            }
            imc < 25.0 -> {
                val pct = (0.14 + (imc - 18.5) / (25.0 - 18.5) * 0.26).toFloat()
                Pair(R.string.rango_normal, pct)
            }
            imc < 30.0 -> {
                val pct = (0.40 + (imc - 25.0) / (30.0 - 25.0) * 0.20).toFloat()
                Pair(R.string.rango_sobrepeso, pct)
            }
            else -> {
                val pct = min(1.0f, (0.60 + (imc - 30.0) / (40.0 - 30.0) * 0.40).toFloat())
                Pair(R.string.rango_obesidad, pct)
            }
        }

        binding.resultado.tvCategoria.text = getString(categoriaRes)
        binding.resultado.guideMarcador.setGuidelinePercent(percent)

        // Asignación de texto directa para evitar errores de recursos (strings) faltantes
        val textoInterpretacion = when (categoriaRes) {
            R.string.rango_bajo -> "Tu peso es inferior al rango considerado normal para tu estatura."
            R.string.rango_normal -> "Tu peso está dentro del rango saludable para tu estatura."
            R.string.rango_sobrepeso -> "Tu peso es superior al rango saludable para tu estatura."
            else -> "Tu peso indica obesidad. Te sugerimos consultar a un especialista."
        }
        binding.resultado.tvInterpretacion.text = textoInterpretacion

        val sexoTexto = when (viewModel.sexo) {
            Sexo.MUJER -> getString(R.string.mujer)
            Sexo.HOMBRE -> getString(R.string.hombre)
            null -> "Sin especificar"
        }

        binding.resultado.tvDatoSexo.text = sexoTexto
        binding.resultado.tvDatoEdad.text = if (viewModel.edad.isNotBlank()) getString(R.string.valor_anios, viewModel.edad) else "Sin especificar"
        binding.resultado.tvDatoPeso.text = if (viewModel.esMetrico) getString(R.string.valor_kg, viewModel.peso) else viewModel.peso + " lb"
        binding.resultado.tvDatoAltura.text = if (viewModel.esMetrico) getString(R.string.valor_cm, viewModel.altura) else viewModel.alturaFt + " ft " + viewModel.alturaIn + " in"

        val cinturaCm = viewModel.cintura.toDoubleOrNull()
        if (cinturaCm != null && cinturaCm > 0.0 && alturaCm > 0.0) {
            binding.resultado.filaCintura.visibility = View.VISIBLE
            binding.resultado.cardCinturaEstatura.visibility = View.VISIBLE
            binding.resultado.tvDatoCintura.text = getString(R.string.valor_cm, viewModel.cintura)

            val rce = cinturaCm / alturaCm
            binding.resultado.tvValorRce.text = String.format(Locale.getDefault(), "%.2f", rce)

            // Asignación directa para evitar errores en rce_favorable y rce_elevado
            binding.resultado.tvMensajeRce.text = if (rce <= 0.5) "Por debajo de 0.5, un valor favorable." else "Por encima de 0.5, un valor elevado."
        } else {
            binding.resultado.filaCintura.visibility = View.GONE
            binding.resultado.cardCinturaEstatura.visibility = View.GONE
        }
    }

    private fun ocultarTeclado() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        binding.root.clearFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = CalculadoraImcFragment()
    }
}