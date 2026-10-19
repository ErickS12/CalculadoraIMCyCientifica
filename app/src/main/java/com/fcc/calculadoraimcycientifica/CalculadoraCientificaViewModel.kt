package com.fcc.calculadoraimcycientifica

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class CalculadoraCientificaViewModel(private val state: SavedStateHandle) : ViewModel() {
    //Creacion de variables
    val entrada: MutableLiveData<String> = state.getLiveData("entrada", "")
    val expresionPrevia: MutableLiveData<String> = state.getLiveData("expresionPrevia", "")
    val resultado: MutableLiveData<String> = state.getLiveData("resultado", "")
    val hayError: MutableLiveData<Boolean> = state.getLiveData("hayError", false)
    val mostrandoResultado: MutableLiveData<Boolean> = state.getLiveData("mostrandoResultado", false)

    val enGrados: MutableLiveData<Boolean> = state.getLiveData("enGrados", true)
    val ans: MutableLiveData<Double> = state.getLiveData("ans", 0.0)

    // AC: limpia todo, pero Ans NO se borra
    fun limpiarTodo() {
        entrada.value = ""
        expresionPrevia.value = ""
        resultado.value = ""
        hayError.value = false
        mostrandoResultado.value = false
    }
}