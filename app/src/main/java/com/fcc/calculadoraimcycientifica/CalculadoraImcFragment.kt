package com.fcc.calculadoraimcycientifica

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.fcc.calculadoraimcycientifica.databinding.FragmentCalculadoraIMCBinding
import java.util.Locale
import kotlin.math.roundToInt

class CalculadoraImcFragment : Fragment() {

    private var _binding: FragmentCalculadoraIMCBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ImcViewModel by activityViewModels()

    private val atras = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            viewModel.editarDatos()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalculadoraIMCBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val zonasReservadas = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(
                zonasReservadas.left,
                zonasReservadas.top,
                zonasReservadas.right,
                zonasReservadas.bottom
            )
            insets
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, atras)

        restaurarFormulario()
        escucharCambios()
        configurarBotones()
        observarEstado()
    }

    private fun restaurarFormulario() {
        if (viewModel.esMetrico) {
            binding.toggleSistema.check(R.id.btnMetrico)
            binding.layoutAltura.isVisible = true
            binding.filaAlturaIngles.isVisible = false
            binding.layoutPeso.suffixText = getString(R.string.unidad_kg)
        } else {
            binding.toggleSistema.check(R.id.btnIngles)
            binding.layoutAltura.isVisible = false
            binding.filaAlturaIngles.isVisible = true
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
                        binding.layoutAltura.isVisible = true
                        binding.filaAlturaIngles.isVisible = false
                        binding.layoutPeso.suffixText = getString(R.string.unidad_kg)
                    }
                    R.id.btnIngles -> {
                        viewModel.esMetrico = false
                        binding.layoutAltura.isVisible = false
                        binding.filaAlturaIngles.isVisible = true
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

        val actualizarViewModel = {
            viewModel.edad = binding.etEdad.text?.toString() ?: ""
            viewModel.peso = binding.etPeso.text?.toString() ?: ""
            viewModel.altura = binding.etAltura.text?.toString() ?: ""
            viewModel.alturaFt = binding.etAlturaFt.text?.toString() ?: ""
            viewModel.alturaIn = binding.etAlturaIn.text?.toString() ?: ""
            viewModel.cintura = binding.etCintura.text?.toString() ?: ""
            validarFormulario()
        }

        binding.etEdad.doAfterTextChanged { actualizarViewModel() }
        binding.etPeso.doAfterTextChanged { actualizarViewModel() }
        binding.etAltura.doAfterTextChanged { actualizarViewModel() }
        binding.etAlturaFt.doAfterTextChanged { actualizarViewModel() }
        binding.etAlturaIn.doAfterTextChanged { actualizarViewModel() }
        binding.etCintura.doAfterTextChanged { actualizarViewModel() }
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
            binding.nestedScrollView.isVisible = !mostrando
            binding.barraInferior.isVisible = !mostrando
            binding.divisor.isVisible = !mostrando
            binding.resultado.root.isVisible = mostrando
            atras.isEnabled = mostrando

            if (mostrando) {
                calcularYMostrarResultado()
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
            alturaCm = (ft * 12 + inch) * 2.54
        }

        val alturaMetros = alturaCm / 100.0
        val imcExacto = if (alturaMetros > 0) pesoKg / (alturaMetros * alturaMetros) else 0.0
        val imc = (imcExacto * 10).roundToInt() / 10.0

        val r = binding.resultado
        r.tvValorImc.text = String.format(Locale.getDefault(), "%.1f", imc)

        val (categoriaRes, interpretacionRes) = when {
            imc < 18.5 -> R.string.categoria_bajo to R.string.interpretacion_bajo
            imc < 25.0 -> R.string.categoria_normal to R.string.interpretacion_normal
            imc < 30.0 -> R.string.categoria_sobrepeso to R.string.interpretacion_sobrepeso
            else -> R.string.categoria_obesidad to R.string.interpretacion_obesidad
        }
        r.tvCategoria.text = getString(categoriaRes)
        r.tvInterpretacion.text = getString(interpretacionRes)

        // La barra va de IMC 15 a 40
        r.guideMarcador.setGuidelinePercent(((imc - 15.0) / 25.0).toFloat().coerceIn(0f, 1f))

        r.tvDatoSexo.text = when (viewModel.sexo) {
            Sexo.MUJER -> getString(R.string.mujer)
            Sexo.HOMBRE -> getString(R.string.hombre)
            null -> getString(R.string.sin_dato)
        }
        r.tvDatoEdad.text = if (viewModel.edad.isNotBlank())
            getString(R.string.valor_anios, viewModel.edad)
        else getString(R.string.sin_dato)

        r.tvDatoPeso.text = getString(
            if (viewModel.esMetrico) R.string.valor_kg else R.string.valor_lb,
            viewModel.peso
        )
        r.tvDatoAltura.text = if (viewModel.esMetrico)
            getString(R.string.valor_cm, viewModel.altura)
        else
            getString(
                R.string.valor_ft_in,
                viewModel.alturaFt.ifBlank { "0" },
                viewModel.alturaIn.ifBlank { "0" }
            )

        val cinturaCm = viewModel.cintura.toDoubleOrNull()
        if (cinturaCm != null && cinturaCm > 0.0 && alturaCm > 0.0) {
            r.filaCintura.isVisible = true
            r.cardCinturaEstatura.isVisible = true
            r.tvDatoCintura.text = getString(R.string.valor_cm, viewModel.cintura)

            val rce = cinturaCm / alturaCm
            r.tvValorRce.text = String.format(Locale.getDefault(), "%.2f", rce)
            r.tvMensajeRce.text = getString(
                if (rce < 0.5) R.string.rce_favorable else R.string.rce_elevado
            )
        } else {
            r.filaCintura.isVisible = false
            r.cardCinturaEstatura.isVisible = false
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
}
