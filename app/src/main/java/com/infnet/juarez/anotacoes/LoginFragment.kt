package com.infnet.juarez.anotacoes

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.*
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.infnet.juarez.anotacoes.modelo.Produto
import com.infnet.juarez.anotacoes.modelo.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoginFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private var mUser: FirebaseUser? = null
    private lateinit var loja : Loja

    var usuario = Usuario()

    private lateinit var txtEmail: TextView
    private lateinit var txtSenha: TextView
    private lateinit var txtUsuarioLogado: TextView
    private lateinit var btnCriar: Button
    private lateinit var btnLogin: Button
    private lateinit var btnFirebaseUi: Button
    private lateinit var btnPremium: Button

    private val sharedViewModel: DadosViewModel by activityViewModels()

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        updateUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = inflater.inflate(R.layout.fragment_login, container, false)

        if (mUser != null) {
            mUser = null
        }

        txtEmail = fragmentBinding.findViewById(R.id.txtEmail)
        txtSenha = fragmentBinding.findViewById(R.id.txtSenha)
        txtUsuarioLogado = fragmentBinding.findViewById(R.id.txtUsuarioLogado)
        btnLogin = fragmentBinding.findViewById(R.id.btnLogin)
        btnCriar = fragmentBinding.findViewById(R.id.btnCriar)
        btnFirebaseUi = fragmentBinding.findViewById(R.id.btnFirebaseUi)
        btnPremium = fragmentBinding.findViewById<Button>(R.id.btnPremium)

        txtUsuarioLogado.setText(usuario.email)

        btnLogin.setOnClickListener() {
            if (validaEmailSenha()) {
                Log.i("TRILHA-Login-", txtEmail.text.toString())
                mAuth
                    .signInWithEmailAndPassword(txtEmail.text.toString(), txtSenha.text.toString())
                    .addOnCompleteListener() {
                        if (it.isSuccessful) {
                            mUser = mAuth.currentUser
                            usuario.email = mUser!!.email

                            sharedViewModel.registraUsusario(usuario)

                            findNavController().navigate(R.id.action_loginFragment_to_anotacaoFragment)
                        } else {
                            Log.i("ERRO ENTRAR LOGIN", it.exception!!.message.toString())
                            Toast.makeText(
                                getActivity(),
                                "Não foi possível conectar a este usuário",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        updateUser()
                    }
            }
        }

        btnCriar.setOnClickListener() {
            if (validaEmailSenha()) {
                Log.i("TRILHA-Criar-", txtEmail.text.toString())
                mAuth
                    .createUserWithEmailAndPassword(
                        txtEmail.text.toString(),
                        txtSenha.text.toString()
                    )
                    .addOnCompleteListener() {
                        if (it.isSuccessful) {
                            mUser = mAuth.currentUser
                            usuario.email = mUser!!.email

                            sharedViewModel.registraUsusario(usuario)

                            findNavController().navigate(R.id.action_loginFragment_to_anotacaoFragment)
                        } else {
                            Log.i("ERRO CRIAÇÃO LOGIN", it.exception!!.message.toString())
                            Toast.makeText(
                                getActivity(),
                                "Não foi possível criar usuário",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        updateUser()
                    }
            }
        }

        btnFirebaseUi.setOnClickListener() {
            txtEmail.setText(null)
            txtSenha.setText(null)
            txtUsuarioLogado.setText(null)

            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build()
//                AuthUI.IdpConfig.FacebookBuilder().build()
            )

            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
            signInLauncher.launch(signInIntent)
        }

        val btnComprar = fragmentBinding.findViewById<Button>(R.id.btnPremium)
        btnComprar.setOnClickListener {
            loja = Loja(AppCompatActivity())

            val produto = loja.produtos.get(0)
            loja.efetuarCompra(produto)

        }

//        findNavController().navigate(R.id.action_loginFragment_to_pesquisaFragment)
        return fragmentBinding
    }

    fun updateUser() {
        if (mUser == null) {
            usuario.email = null
        } else {
            usuario.email = mUser!!.email
            sharedViewModel.registraUsusario(usuario)
        }
    }

    override fun onStart() {
        super.onStart()
        mUser = mAuth.currentUser
        updateUser()
    }

    override fun onStop() {
        super.onStop()
        mAuth.signOut()
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            mUser = FirebaseAuth.getInstance().currentUser
            if (mUser != null) {
                usuario.email = mUser!!.email
                sharedViewModel.registraUsusario(usuario)

                findNavController().navigate(R.id.action_loginFragment_to_anotacaoFragment)
            }
        }
    }

    private fun validaEmailSenha(): Boolean {
        if (txtEmail.length() == 0 || txtSenha.length() == 0) {
            Toast.makeText(
                getActivity(),
                "Email e Senha devem ser preenchidos corretamente",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }


}