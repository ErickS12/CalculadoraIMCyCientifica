package com.fcc.calculadoraimcycientifica

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

enum class Sexo { MUJER, HOMBRE }

class ImcViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val SEXO_KEY = "sexo_key"
    private val EDAD_KEY = "edad_key"
    private val PESO_KEY = "peso_key"
    private val ALTURA_KEY = "altura_key"
    private val ALTURA_FT_KEY = "altura_ft_key"
    private val ALTURA_IN_KEY = "altura_in_key"
    private val CINTURA_KEY = "cintura_key"
    private val SISTEMA_KEY = "sistema_key"
    private val RESULTADO_KEY = "resultado_key"

    var sexo: Sexo?
        get() = savedStateHandle.get(SEXO_KEY)
        set(value) = savedStateHandle.set(SEXO_KEY, value)

    var edad: String
        get() = savedStateHandle.get(EDAD_KEY) ?: ""
        set(value) = savedStateHandle.set(EDAD_KEY, value)

    var peso: String
        get() = savedStateHandle.get(PESO_KEY) ?: ""
        set(value) = savedStateHandle.set(PESO_KEY, value)

    var altura: String
        get() = savedStateHandle.get(ALTURA_KEY) ?: ""
        set(value) = savedStateHandle.set(ALTURA_KEY, value)

    var alturaFt: String
        get() = savedStateHandle.get(ALTURA_FT_KEY) ?: ""
        set(value) = savedStateHandle.set(ALTURA_FT_KEY, value)

    var alturaIn: String
        get() = savedStateHandle.get(ALTURA_IN_KEY) ?: ""
        set(value) = savedStateHandle.set(ALTURA_IN_KEY, value)

    var cintura: String
        get() = savedStateHandle.get(CINTURA_KEY) ?: ""
        set(value) = savedStateHandle.set(CINTURA_KEY, value)

    var esMetrico: Boolean
        get() = savedStateHandle.get(SISTEMA_KEY) ?: true
        set(value) = savedStateHandle.set(SISTEMA_KEY, value)

    private var _mostrandoResultado: MutableLiveData<Boolean> =
        savedStateHandle.getLiveData(RESULTADO_KEY, false)

    val mostrandoResultado: LiveData<Boolean> get() = _mostrandoResultado

    fun calcular() { _mostrandoResultado.value = true }
    fun editarDatos() { _mostrandoResultado.value = false }

    fun limpiar() {
        savedStateHandle.remove<Sexo>(SEXO_KEY)
        savedStateHandle.remove<String>(EDAD_KEY)
        savedStateHandle.remove<String>(PESO_KEY)
        savedStateHandle.remove<String>(ALTURA_KEY)
        savedStateHandle.remove<String>(ALTURA_FT_KEY)
        savedStateHandle.remove<String>(ALTURA_IN_KEY)
        savedStateHandle.remove<String>(CINTURA_KEY)
        savedStateHandle.remove<Boolean>(SISTEMA_KEY)
    }

    fun nuevoCalculo() {
        limpiar()
        _mostrandoResultado.value = false
    }
}