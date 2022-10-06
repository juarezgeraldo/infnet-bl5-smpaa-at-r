package com.infnet.juarez.anotacoes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.infnet.juarez.anotacoes.modelo.Anotacao
import com.infnet.juarez.anotacoes.modelo.Usuario

class DadosViewModel : ViewModel() {

    private val _usuario = MutableLiveData<Usuario?>()
    val usuario: MutableLiveData<Usuario?> = _usuario

    fun registraUsusario(usuario: Usuario ){
        _usuario.value = usuario
    }
    fun recuperaUsusario(): Usuario? {
        return usuario.value
    }

    private val _anotacao = MutableLiveData<Anotacao?>()
    val anotacao: MutableLiveData<Anotacao?> = _anotacao

    fun registraAnotacao(anotacao: Anotacao ){
        _anotacao.value = anotacao
    }
    fun recuperaAnotacao(): Anotacao? {
        return anotacao.value
    }
}