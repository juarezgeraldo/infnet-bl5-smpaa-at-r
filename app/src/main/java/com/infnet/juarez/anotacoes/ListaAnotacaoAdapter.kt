package com.infnet.juarez.anotacoes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListaAnotacaoAdapter(private val listaArquivo: ArrayList<String>) :
    RecyclerView.Adapter<ListaAnotacaoAdapter.ViewHolder>() {

    //    var listaAnotacaos = ArrayList<Anotacao>()
//    set(value){
//        field = value
//        this.notifyDataSetChanged()
//    }
    lateinit var itemListner: RecyclerViewItemListner

    fun setRecyclerViewItemListner(listner: RecyclerViewItemListner) {
        itemListner = listner
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.lista_anotacao, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(listaArquivo[position], itemListner, position)
    }

    override fun getItemCount(): Int {
        return listaArquivo.size
    }

    //Classe interna = relação Tdo - Parte
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItem(arquivo: String, itemListner: RecyclerViewItemListner, position: Int) {

            val txtLstData = itemView.findViewById<TextView>(R.id.txtLstData)
            val txtLstTitulo = itemView.findViewById<TextView>(R.id.txtLstTitulo)
            val btnVisualizarArquivo = itemView.findViewById<ImageButton>(R.id.btnVisualizarArquivo)
            val btnExcluirarquivo = itemView.findViewById<ImageButton>(R.id.btnExcluirarquivo)

            val tituloArquivo = arquivo.substring(0, arquivo.indexOf("(", 0, false))
            val dataArquivo = arquivo.substring(arquivo.indexOf("(") + 1, arquivo.indexOf(")"), )

            txtLstData.setText(tituloArquivo)
            txtLstTitulo.setText(dataArquivo)

            btnVisualizarArquivo.setOnClickListener() {
                itemListner.recyclerViewBotaoVisualizarClicked(it, arquivo)
            }
            btnExcluirarquivo.setOnClickListener() {
                itemListner.recyclerViewBotaoExcluirClicked(it, arquivo)
            }

        }
    }
}
