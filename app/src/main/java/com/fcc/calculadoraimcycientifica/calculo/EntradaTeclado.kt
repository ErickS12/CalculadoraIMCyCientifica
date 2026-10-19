package com.fcc.calculadoraimcycientifica.calculo

const val TECLA_AC = "AC"
const val TECLA_BORRAR = "BORRAR"
const val TECLA_IGUAL = "IGUAL"
const val TECLA_SIGNO = "SIGNO"
const val TECLA_2ND = "2ND"

private const val MAX_LONGITUD = 60

private val DIGITOS = ('0'..'9').map { it.toString() }
val BINARIOS = listOf("+", "−", "×", "÷", "^", " mod ")
 val SUFIJOS = listOf("!", "%", "²", "³")
private val ABRE_OPERANDO = listOf(
    "(", "sin(", "cos(", "tan(", "asin(", "acos(", "atan(",
    "ln(", "log(", "√(", "∛(", "abs(", "1÷(",
    "e^", "10^", "π", "e", "Ans"
)

// Tokens que ⌫ borra completos. Los más largos van primero:
// "asin(" termina igual que "sin(", y debe revisarse antes.
private val TOKENS_ATOMICOS = listOf(
    "asin(", "acos(", "atan(", "sin(", "cos(", "tan(", "log(", "abs(",
    "ln(", "√(", "∛(", "1÷(", "10^", "e^", " mod ", "Ans"
)

fun aplicarTecla(entrada: String, tecla: String): String {
    if (tecla == TECLA_BORRAR) return borrar(entrada)

    val nueva = when (tecla) {
        in DIGITOS -> ponerDigito(entrada, tecla)
        "." -> ponerPunto(entrada)
        ")" -> ponerParentesisCierre(entrada)
        in BINARIOS -> ponerBinario(entrada, tecla)
        in SUFIJOS -> ponerSufijo(entrada, tecla)
        in ABRE_OPERANDO -> ponerApertura(entrada, tecla)
        else -> entrada // IGUAL, SIGNO, 2ND, AC se manejan en otro lado
    }

    // R8: máximo 60 caracteres
    return if (nueva.length > MAX_LONGITUD) entrada else nueva
}

// ---------- Ayudantes de clasificación ----------

// Cierra un operando pero NO es dígito ni punto: ) π e Ans ! % ² ³
private fun cierraSinDigito(e: String): Boolean {
    if (e.endsWith("Ans")) return true
    return e.isNotEmpty() && e.last() in ")πe!%²³"
}

// Fin de operando: lo anterior más dígito o punto
private fun esFinOperando(e: String): Boolean {
    if (cierraSinDigito(e)) return true
    return e.isNotEmpty() && e.last() in "0123456789."
}

private fun terminaEnBinario(e: String): Boolean {
    if (e.endsWith(" mod ")) return true
    // "e^" y "10^" son tokens que abren un operando, no operadores sueltos
    if (e.endsWith("e^") || e.endsWith("10^")) return false
    return e.isNotEmpty() && e.last() in "+−×÷^"
}

private fun numeroActual(e: String): String =
    e.takeLastWhile { it in '0'..'9' || it == '.' }

private fun quitarBinariosFinales(e: String): String {
    var r = e
    while (terminaEnBinario(r)) {
        r = if (r.endsWith(" mod ")) r.dropLast(5) else r.dropLast(1)
    }
    return r
}

// ---------- Reglas por tipo de tecla ----------

// R5 (ceros a la izquierda) y R7 (× automática después de cierres)
private fun ponerDigito(e: String, d: String): String {
    if (cierraSinDigito(e)) return "$e×$d"
    val actual = numeroActual(e)
    if (actual == "0") {
        return if (d == "0") e else e.dropLast(1) + d
    }
    return e + d
}

// R4: un solo punto por número; al inicio o tras operador escribe "0."
private fun ponerPunto(e: String): String {
    if (cierraSinDigito(e)) return "$e×0."
    val actual = numeroActual(e)
    return when {
        actual.contains('.') -> e
        actual.isNotEmpty() -> "$e."
        else -> "${e}0."
    }
}

// R6: ")" solo si hay paréntesis abiertos y lo anterior es un operando
private fun ponerParentesisCierre(e: String): String {
    val abiertos = e.count { it == '(' } - e.count { it == ')' }
    return if (abiertos > 0 && esFinOperando(e)) "$e)" else e
}

// R1, R2, R3: operadores binarios
private fun ponerBinario(e: String, t: String): String {
    // R1: con la entrada vacía solo se acepta "−"
    if (e.isEmpty()) return if (t == "−") "−" else e

    // R2: ya hay un operador al final
    if (terminaEnBinario(e)) {
        if (t == "−") {
            return when {
                e.endsWith("+") -> e.dropLast(1) + "−"
                e.endsWith("−") -> e          // nunca dos "−" seguidos
                else -> "$e−"                 // signo negativo tras × ÷ ^ mod
            }
        }
        // El nuevo operador reemplaza al anterior
        val sinOperadores = quitarBinariosFinales(e)
        return if (esFinOperando(sinOperadores)) "$sinOperadores$t" else e
    }

    // "−" también puede ser signo negativo (después de "(" o de una función)
    if (t == "−") return "$e$t"

    // R3: necesita un operando antes
    return if (esFinOperando(e)) "$e$t" else e
}

// R3: sufijos ! % ² ³ necesitan un operando antes
private fun ponerSufijo(e: String, t: String): String =
    if (esFinOperando(e)) "$e$t" else e

// R7: si abre un operando justo después de cerrar otro, inserta "×"
private fun ponerApertura(e: String, t: String): String =
    if (esFinOperando(e)) "$e×$t" else e + t

// R9: ⌫ borra un token completo o un carácter
private fun borrar(e: String): String {
    for (t in TOKENS_ATOMICOS) {
        if (e.endsWith(t)) return e.dropLast(t.length)
    }
    return e.dropLast(1)
}