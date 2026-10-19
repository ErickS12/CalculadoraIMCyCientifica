package com.fcc.calculadoraimcycientifica.calculo

import com.fcc.calculadoraimcycientifica.calculo.aplicarTecla
import org.junit.Assert.assertEquals
import org.junit.Test

class EntradaTecladoTest {

    // Aplica las teclas una por una, como si las presionaras
    private fun teclear(vararg teclas: String): String =
        teclas.fold("") { acc, t -> aplicarTecla(acc, t) }

    @Test fun operadorReemplazaAlAnterior() = assertEquals("5×3", teclear("5", "+", "×", "3"))
    @Test fun operadorAlInicioSeIgnora() = assertEquals("5", teclear("×", "5"))
    @Test fun menosComoSigno() = assertEquals("5×−3", teclear("5", "×", "−", "3"))
    @Test fun nuncaDosMenos() = assertEquals("5−", teclear("5", "−", "−"))
    @Test fun unSoloPunto() = assertEquals("1.23", teclear("1", ".", "2", ".", "3"))
    @Test fun puntoInicial() = assertEquals("0.5", teclear(".", "5"))
    @Test fun puntoTrasOperador() = assertEquals("3+0.", teclear("3", "+", "."))
    @Test fun cerosIzquierda() = assertEquals("7", teclear("0", "0", "7"))
    @Test fun decimalConCero() = assertEquals("0.05", teclear("0", ".", "0", "5"))
    @Test fun cierreSinAbrir() = assertEquals("5", teclear(")", "5"))
    @Test fun cierreDoble() = assertEquals("(5)", teclear("(", "5", ")", ")"))
    @Test fun parentesisVacio() = assertEquals("(", teclear("(", ")"))
    @Test fun multiplicacionPi() = assertEquals("2×π", teclear("2", "π"))
    @Test fun multiplicacionPiDespues() = assertEquals("π×2", teclear("π", "2"))
    @Test fun multiplicacionFuncion() = assertEquals("2×sin(", teclear("2", "sin("))
    @Test fun multiplicacionDiezElevado() = assertEquals("5×10^", teclear("5", "10^"))
    @Test fun multiplicacionInverso() = assertEquals("5×1÷(", teclear("5", "1÷("))
    @Test fun cuadradoYDigito() = assertEquals("5²×3", teclear("5", "²", "3"))
    @Test fun potencia() = assertEquals("5^3", teclear("5", "^", "3"))
    @Test fun borrarFuncion() = assertEquals("", teclear("sin(", "BORRAR"))
    @Test fun borrarMod() = assertEquals("5", teclear("5", " mod ", "BORRAR"))
    @Test fun borrarDigito() = assertEquals("12", teclear("1", "2", "3", "BORRAR"))
    @Test fun longitudMaxima() {
        val r = teclear(*Array(70) { "7" })
        assertEquals(60, r.length)
    }
}