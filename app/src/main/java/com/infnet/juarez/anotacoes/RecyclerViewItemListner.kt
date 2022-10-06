package com.infnet.juarez.anotacoes

import android.view.View

interface RecyclerViewItemListner {
    fun recyclerViewBotaoVisualizarClicked(view: View, arquivo: String)
    fun recyclerViewBotaoExcluirClicked(view: View, arquivo: String): Boolean
}

