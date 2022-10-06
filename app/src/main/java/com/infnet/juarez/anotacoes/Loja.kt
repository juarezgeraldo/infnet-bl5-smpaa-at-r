package com.infnet.juarez.anotacoes

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.infnet.juarez.anotacoes.modelo.Produto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Loja: BillingClientStateListener, SkuDetailsResponseListener, PurchasesUpdatedListener {

    private lateinit var context: AppCompatActivity
    val produtos: MutableList<Produto> = ArrayList<Produto>()
    private lateinit var clienteInApp: BillingClient

    constructor(context: AppCompatActivity) {

        this.context = context
        produtos.add(Produto("Versão Premium do aplicativo", "Com esta versão não serão apresentadas propagandas", null))
        //---------------------------------------------------------------------------
        //Passo 1 - Inicializar a comunicação com o Google Play
        clienteInApp = BillingClient
            .newBuilder(context)
            .enablePendingPurchases() //obrigatório
            .setListener(this) //obrigatório
            .build()
        clienteInApp.startConnection(this)
    }
//
//-------------------------------------------------------
//Calback do Passo 1
override fun onBillingSetupFinished(billingResult: BillingResult) {

    if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {

        Log.i("DR4", "Comunicação com o Google Play estabelecida")
        //-------------------------------------------------------------------
        //Passo 2 - Buscar os dados dos produtos
        val skuList = ArrayList<String>()
        for (produto in produtos) {
            skuList.add(produto.sku)
        }
        val params = SkuDetailsParams
            .newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)
            .build()
        clienteInApp.querySkuDetailsAsync(params, this)
    }
}

    override fun onBillingServiceDisconnected() {
        Log.i("DR4", "Houve uma desconexão com o Google Play")
    }

    //-------------------------------------------------------

    //-------------------------------------------------------------------------
    //Callback do Passo 2

    override fun onSkuDetailsResponse(billingResult: BillingResult, skuDetailsList: MutableList<SkuDetails>?) {
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK
            && skuDetailsList != null
            && skuDetailsList.size > 0
        ) {

            for (produto in produtos) {

                for (product in skuDetailsList) {

                    if (produto.sku.equals(product.sku)) {

                        produto.descicao = product.description
                        produto.preco = product.price
                        produto.skuDetails = product
                        Log.i("DR4", "Produto: ${produto.descicao} = ${produto.preco}")
                    }
                }
            }
            Log.i("DR4", "Dados dos produtos carregados")
        }
    }

    //------------------------------------------------------------------------
    //Passo 3 - Executar o fluxo de compra
    fun efetuarCompra(produto: Produto) {

        Log.i("DR4", "Efetuando uma compra")
        val params = BillingFlowParams
            .newBuilder()
            .setSkuDetails(produto.skuDetails!!)
            .build()
        clienteInApp.launchBillingFlow(context, params)
    }

    //-------------------------------------------------------------------------
    //Callback do Passo 3
    override fun onPurchasesUpdated(billingResult: BillingResult, purchaseList: MutableList<Purchase>?) {
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK
            && purchaseList != null
            && purchaseList.size > 0
        ) {

            Log.i("DR4", "Compra efetuada")
            for (purchase in purchaseList) {

                GlobalScope.launch(Dispatchers.IO) {

                    handlePurchase(purchase)
                }
            }
        } else if (billingResult?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i("DR4", "Usuário cancelou a compra")
        }
    }

    suspend fun handlePurchase(purchase : Purchase) {

        if(purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {

            Log.i("DR4", "Compra Confirmada")
            if(!purchase.isAcknowledged) {

                val params = AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                val result = withContext(Dispatchers.IO) {
                    clienteInApp.acknowledgePurchase(params)
                }
            }
        }
    }

    fun fecharLoja() {

        Log.i("DR4", "Fechando a Loja")
        clienteInApp.endConnection()
    }
}
