package com.fcc.calculadoraimcycientifica.calculo

import org.mariuszgromada.math.mxparser.Expression
import org.mariuszgromada.math.mxparser.License
import org.mariuszgromada.math.mxparser.mXparser

enum class TipoError { INDEFINIDO, SINTAXIS, DESBORDAMIENTO }

sealed class ResultadoCalculo {
    data class Exito(val valor: Double) : ResultadoCalculo()
    data class Error(val tipo: TipoError) : ResultadoCalculo()
}

// Operador que queda "colgando": justo antes de ")" o al final del texto.
// Incluye "#" porque en la Etapa 7 "mod" se traducirá a "#"".
private val OPERADOR_COLGANTE = Regex("""[+\-*/^#]\s*(\)|$)""")

// Agrega los ")" que falten: "(2+3" pasa a "(2+3)"
fun cerrarParentesis(expresion: String): String {
    val faltan = expresion.count { it == '(' } - expresion.count { it == ')' }
    return if (faltan > 0) expresion + ")".repeat(faltan) else expresion
}

// Convierte tu texto a la sintaxis de mXparser.
// Aquí solo van las traducciones de la Etapa 2; las demás se agregan después.
private fun traducir(expresion: String, ans: Double): String {
    var t = cerrarParentesis(expresion)
    t = t.replace("−", "-")
        .replace("×", "*")
        .replace("÷", "/")
    // Ans se reemplaza por su valor (entre paréntesis por si es negativo)
    t = t.replace("Ans", "(" + java.math.BigDecimal.valueOf(ans).toPlainString() + ")")
    return t
}

fun evaluar(expresion: String, enGrados: Boolean, ans: Double): ResultadoCalculo {
    License.iConfirmNonCommercialUse("CalculadoraIMCyCientifica")

    // El modo de grados se usará desde la Etapa 6; lo dejo ya conectado
    if (enGrados) mXparser.setDegreesMode() else mXparser.setRadiansMode()

    if (expresion.isBlank()) return ResultadoCalculo.Error(TipoError.SINTAXIS)

    val traducida = traducir(expresion, ans)

    // Un operador sin operando después ("5+", "(5+)", "2×(3+)") es error de sintaxis
    if (OPERADOR_COLGANTE.containsMatchIn(traducida)) {
        return ResultadoCalculo.Error(TipoError.SINTAXIS)
    }

    val e = Expression(traducida)
    if (!e.checkSyntax()) return ResultadoCalculo.Error(TipoError.SINTAXIS)

    val valor = e.calculate()
    // Por ahora NaN e infinito se tratan como Indefinido; en las etapas 3 y 4
    // separamos Indefinido de Desbordamiento.
    return if (valor.isNaN() || valor.isInfinite()) {
        ResultadoCalculo.Error(TipoError.INDEFINIDO)
    } else {
        ResultadoCalculo.Exito(valor)
    }
}