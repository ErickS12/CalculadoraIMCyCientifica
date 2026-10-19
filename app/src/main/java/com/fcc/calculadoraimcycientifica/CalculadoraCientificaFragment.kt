package com.fcc.calculadoraimcycientifica

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.fcc.calculadoraimcycientifica.databinding.FragmentCalculadoraCientificaBinding
import com.fcc.calculadoraimcycientifica.calculo.*
class CalculadoraCientificaFragment : Fragment() {

    private var _binding: FragmentCalculadoraCientificaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalculadoraCientificaViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalculadoraCientificaBinding.inflate(inflater, container, false)
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
        configurarTeclas()

        viewModel.entrada.observe(viewLifecycleOwner) { actualizarPantalla() }
        viewModel.resultado.observe(viewLifecycleOwner) { actualizarPantalla() }
        viewModel.expresionPrevia.observe(viewLifecycleOwner) { actualizarPantalla() }
        viewModel.mostrandoResultado.observe(viewLifecycleOwner) { actualizarPantalla() }
        viewModel.hayError.observe(viewLifecycleOwner) { actualizarPantalla() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun actualizarPantalla() {
        val mostrando = viewModel.mostrandoResultado.value == true
        val error = viewModel.hayError.value == true

        binding.tvExpresion.text = viewModel.expresionPrevia.value.orEmpty()
        binding.tvResultado.text = when {
            error || mostrando -> viewModel.resultado.value.orEmpty()
            else -> viewModel.entrada.value.orEmpty().ifEmpty { "0" }
        }
    }

    private fun configurarTeclas() {
        val teclas = listOf(
            binding.btn0 to "0", binding.btn1 to "1", binding.btn2 to "2",
            binding.btn3 to "3", binding.btn4 to "4", binding.btn5 to "5",
            binding.btn6 to "6", binding.btn7 to "7", binding.btn8 to "8",
            binding.btn9 to "9", binding.btnPunto to ".",

            binding.btnSumar to "+", binding.btnRestar to "−",
            binding.btnMultiplicar to "×", binding.btnDividir to "÷",
            binding.btnPotencia to "^", binding.btnMod to " mod ",

            binding.btnCuadrado to "²", binding.btnCubo to "³",
            binding.btnFactorial to "!", binding.btnPorcentaje to "%",

            binding.btnParAbre to "(", binding.btnParCierra to ")",

            binding.btnSin to "sin(", binding.btnCos to "cos(", binding.btnTan to "tan(",
            binding.btnAsin to "asin(", binding.btnAcos to "acos(", binding.btnAtan to "atan(",
            binding.btnLn to "ln(", binding.btnLog to "log(",

            binding.btnRaiz to "√(", binding.btnRaizCubica to "∛(",
            binding.btnAbsoluto to "abs(", binding.btnInverso to "1÷(",
            binding.btnExp to "e^", binding.btnDiezElevado to "10^",

            binding.btnPi to "π", binding.btnE to "e", binding.btnAns to "Ans",

            binding.btnAC to TECLA_AC, binding.btnBorrar to TECLA_BORRAR,
            binding.btnIgual to TECLA_IGUAL, binding.btnSigno to TECLA_SIGNO,
            binding.btn2nd to TECLA_2ND
        )

        teclas.forEach { (boton, token) ->
            boton.setOnClickListener { alPresionarTecla(token) }
        }
    }

    private fun alPresionarTecla(token: String) {
        // Después de un error, cualquier tecla limpia la pantalla
        if (viewModel.hayError.value == true) {
            viewModel.limpiarTodo()
            if (token == TECLA_AC || token == TECLA_BORRAR) return
        }

        // Después de "=" se puede continuar con el resultado
        if (viewModel.mostrandoResultado.value == true) {
            when (token) {
                TECLA_AC -> { viewModel.limpiarTodo(); return }
                TECLA_BORRAR, TECLA_IGUAL, TECLA_SIGNO, TECLA_2ND -> return
            }
            // Operador o sufijo: sigue con Ans. Cualquier otra tecla: cuenta nueva.
            val nueva = if (token in BINARIOS || token in SUFIJOS) {
                aplicarTecla("Ans", token)
            } else {
                aplicarTecla("", token)
            }
            viewModel.mostrandoResultado.value = false
            viewModel.resultado.value = ""
            viewModel.expresionPrevia.value = ""
            viewModel.entrada.value = nueva
            return
        }

        when (token) {
            TECLA_AC -> viewModel.limpiarTodo()
            TECLA_IGUAL -> alIgual()
            TECLA_SIGNO, TECLA_2ND -> { /* etapas posteriores */ }
            else -> {
                val actual = viewModel.entrada.value.orEmpty()
                viewModel.entrada.value = aplicarTecla(actual, token)
            }
        }
    }

    private fun alIgual() {
        val entrada = viewModel.entrada.value.orEmpty()
        if (entrada.isEmpty()) return

        val enGrados = viewModel.enGrados.value ?: true
        val ans = viewModel.ans.value ?: 0.0

        when (val r = evaluar(entrada, enGrados, ans)) {
            is ResultadoCalculo.Exito -> {
                viewModel.expresionPrevia.value = cerrarParentesis(entrada)
                viewModel.resultado.value = r.valor.toString() // provisional: Etapa 3
                viewModel.ans.value = r.valor
                viewModel.mostrandoResultado.value = true
            }
            is ResultadoCalculo.Error -> {
                viewModel.expresionPrevia.value = cerrarParentesis(entrada)
                viewModel.resultado.value = when (r.tipo) {
                    TipoError.INDEFINIDO -> "Indefinido"
                    TipoError.SINTAXIS -> "Expresión inválida"
                    TipoError.DESBORDAMIENTO -> "Desbordamiento"
                }
                viewModel.hayError.value = true
            }
        }
    }
}
