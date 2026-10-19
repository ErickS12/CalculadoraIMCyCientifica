package com.fcc.calculadoraimcycientifica.calculo
import kotlin.math.roundToInt

enum class CategoriaImc { BAJO_PESO, NORMAL, SOBREPESO, OBESIDAD }

data class ResultadoImc(
    val imc: Double,               // redondeado a 1 decimal
    val categoria: CategoriaImc,
    val rce: Double?               // relación cintura-estatura; null si no hay cintura
) {
    val rceFavorable: Boolean get() = (rce ?: 1.0) < 0.5
}

object ImcCalculadora {

    fun calcular(pesoKg: Double, alturaCm: Double, cinturaCm: Double?): ResultadoImc {
        val alturaMetros = alturaCm / 100.0
        val imcExacto = if (alturaMetros > 0) pesoKg / (alturaMetros * alturaMetros) else 0.0
        val imc = (imcExacto * 10).roundToInt() / 10.0

        val categoria = when {
            imc < 18.5 -> CategoriaImc.BAJO_PESO
            imc < 25.0 -> CategoriaImc.NORMAL
            imc < 30.0 -> CategoriaImc.SOBREPESO
            else -> CategoriaImc.OBESIDAD
        }

        val rce = if (cinturaCm != null && cinturaCm > 0.0 && alturaCm > 0.0) {
            cinturaCm / alturaCm
        } else null

        return ResultadoImc(imc, categoria, rce)
    }
}
