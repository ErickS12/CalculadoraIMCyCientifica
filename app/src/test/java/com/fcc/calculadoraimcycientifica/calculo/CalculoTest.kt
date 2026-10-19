package com.fcc.calculadoraimcycientifica.calculo

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculoTest {

    private fun valor(expr: String, ans: Double = 0.0): Double {
        val r = evaluar(expr, enGrados = true, ans = ans)
        return (r as ResultadoCalculo.Exito).valor
    }

    private fun error(expr: String): TipoError {
        val r = evaluar(expr, enGrados = true, ans = 0.0)
        return (r as ResultadoCalculo.Error).tipo
    }

    @Test fun precedencia() = assertEquals(14.0, valor("2+3×4"), 1e-9)
    @Test fun parentesis() = assertEquals(20.0, valor("(2+3)×4"), 1e-9)
    @Test fun restaIzquierda() = assertEquals(5.0, valor("10−3−2"), 1e-9)
    @Test fun divisionIzquierda() = assertEquals(2.0, valor("8÷2÷2"), 1e-9)
    @Test fun signoNegativo() = assertEquals(-6.0, valor("2×−3"), 1e-9)
    @Test fun parentesisSinCerrar() = assertEquals(5.0, valor("(2+3"), 1e-9)
    @Test fun usaAns() = assertEquals(14.0, valor("Ans×2", ans = 7.0), 1e-9)
    @Test fun ansNegativo() = assertEquals(-3.0, valor("Ans+4", ans = -7.0), 1e-9)

    @Test fun divisionEntreCero() = assertEquals(TipoError.INDEFINIDO, error("1÷0"))
    @Test fun operadorSuelto() = assertEquals(TipoError.SINTAXIS, error("5+"))
}