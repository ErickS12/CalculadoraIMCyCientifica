package com.fcc.calculadoraimcycientifica

import com.fcc.calculadoraimcycientifica.calculo.CategoriaImc
import com.fcc.calculadoraimcycientifica.calculo.ImcCalculadora
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImcCalculadoraTest {

    @Test fun pesoNormal() {
        val r = ImcCalculadora.calcular(70.0, 175.0, null)
        assertEquals(22.9, r.imc, 1e-9)
        assertEquals(CategoriaImc.NORMAL, r.categoria)
    }

    @Test fun bajoPeso() {
        val r = ImcCalculadora.calcular(50.0, 175.0, null)
        assertEquals(CategoriaImc.BAJO_PESO, r.categoria)
    }

    @Test fun sobrepeso() {
        val r = ImcCalculadora.calcular(80.0, 175.0, null)
        assertEquals(CategoriaImc.SOBREPESO, r.categoria)
    }

    @Test fun obesidad() {
        val r = ImcCalculadora.calcular(100.0, 175.0, null)
        assertEquals(CategoriaImc.OBESIDAD, r.categoria)
    }

    @Test fun sinCinturaNoHayRce() {
        assertNull(ImcCalculadora.calcular(70.0, 175.0, null).rce)
    }

    @Test fun rceFavorable() {
        val r = ImcCalculadora.calcular(70.0, 175.0, 80.0)
        assertEquals(0.457, r.rce!!, 0.001)
        assertTrue(r.rceFavorable)
    }

    @Test fun rceElevado() {
        val r = ImcCalculadora.calcular(70.0, 175.0, 100.0)
        assertFalse(r.rceFavorable)
    }
}