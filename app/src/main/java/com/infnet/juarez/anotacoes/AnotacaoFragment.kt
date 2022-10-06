package com.infnet.juarez.anotacoes

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.google.android.gms.ads.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.infnet.juarez.anotacoes.modelo.Anotacao
import com.infnet.juarez.anotacoes.modelo.Foto
import com.infnet.juarez.anotacoes.modelo.Usuario
import java.io.*
import java.util.*


class AnotacaoFragment : Fragment(), RecyclerViewItemListner, LocationListener {
    private var usuario: Usuario = Usuario()
    private var anotacao: Anotacao = Anotacao()
    private var foto: Foto = Foto()
    private val sharedViewModel: DadosViewModel by activityViewModels()

    lateinit var mAdView: AdView

    private var isInclusao: Boolean = true

    val COARSE_REQUEST = 12345
    val FINE_REQUEST = 54321
    val CAMERA_PERMISSION_CODE = 100
    val CAMERA_REQUEST = 1888

    @SuppressLint("SimpleDateFormat")
    val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = inflater.inflate(R.layout.fragment_anotacao, container, false)

        mAdView = AdView(requireActivity())

        val txtUsuario = fragmentBinding.findViewById<TextView>(R.id.txtUsuario)
        val edtData = fragmentBinding.findViewById<EditText>(R.id.edtData)
        val edtLatitude = fragmentBinding.findViewById<EditText>(R.id.edtLatitude)
        val edtLongitude = fragmentBinding.findViewById<EditText>(R.id.edtLongitude)
        val edtTitulo = fragmentBinding.findViewById<EditText>(R.id.edtTitulo)
        val edtTexto = fragmentBinding.findViewById<EditText>(R.id.edtTexto)
        val imgFoto = fragmentBinding.findViewById<ImageView>(R.id.imgFoto)
        val btnFoto = fragmentBinding.findViewById<Button>(R.id.btnFoto)
        val btnSalvar = fragmentBinding.findViewById<Button>(R.id.btnSalvar)
        val fabAnotacaoLogout =
            fragmentBinding.findViewById<FloatingActionButton>(R.id.fabAnotacaoLogout)

        edtData.isEnabled = false
        edtLatitude.isEnabled = false
        edtLongitude.isEnabled = false

        anotacao.data = dateFormat.format(Date())
        edtData.setText(dateFormat.format(Date()))

        var location = getLocation("GPS")
        if (location == null) {
            location = getLocation("NET")
        }
        usuario = sharedViewModel.recuperaUsusario()!!

        txtUsuario.setText(usuario.email)

        btnSalvar.setOnClickListener {
            if (validaCamposAnotacao(
                    edtTitulo.text.toString(),
                    edtTexto.text.toString()
                )
            ) {
                anotacao.titulo = edtTitulo.text.toString()
                anotacao.texto = edtTexto.text.toString()
                if (isInclusao) {
                    atualizaAnotacao("incluir")
                } else {
                    this.atualizaAnotacao("alterar")
                }
                edtTexto.setText(null)
                edtTitulo.setText(null)
                edtData.setText(dateFormat.format(Date()))
                imgFoto.setImageBitmap(null)
                this.atualizaListaanotacaos()
            }
        }

        btnFoto.setOnClickListener() {
            if (this.getActivity()
                    ?.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
                this.requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            } else {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            }
        }

        fabAnotacaoLogout.setOnClickListener() {
            findNavController().navigate(R.id.action_anotacaoFragment_to_loginFragment)
        }

//        atualizaListaanotacaos()

        mAdView.adListener = object : AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Code to be executed when an ad request fails.
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }

        return fragmentBinding
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val edtLatitude = requireActivity().findViewById<EditText>(R.id.edtLatitude)
        val edtLongitude = requireActivity().findViewById<EditText>(R.id.edtLongitude)


        MobileAds.initialize(requireActivity()) {}
        val adView = AdView(requireActivity())
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        mAdView = requireActivity().findViewById<AdView>(R.id.adView)
        mAdView.loadAd(adRequest)

        edtLatitude.setText(anotacao.latitude)
        edtLongitude.setText(anotacao.longitude)

        atualizaListaanotacaos()
    }

    private fun getLocation(tipo: String): Location? {
        var location: Location? = null
        val locationManager =
            getActivity()?.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        val isServiceEnable =
            locationManager.isProviderEnabled(if (tipo == "NET") LocationManager.NETWORK_PROVIDER else LocationManager.GPS_PROVIDER)
        if (isServiceEnable) {
            if (checkSelfPermission(
                    this.requireActivity(),
                    if (tipo == "NET") android.Manifest.permission.ACCESS_COARSE_LOCATION else Manifest.permission.ACCESS_FINE_LOCATION
                ).equals(PackageManager.PERMISSION_GRANTED)
            ) {
                locationManager.requestLocationUpdates(
                    if (tipo == "NET") LocationManager.NETWORK_PROVIDER else LocationManager.GPS_PROVIDER,
                    2000L,
                    0f,
                    this
                )
                location =
                    locationManager.getLastKnownLocation(if (tipo == "NET") LocationManager.NETWORK_PROVIDER else LocationManager.GPS_PROVIDER)
                anotacao.longitude = location?.longitude.toString()
                anotacao.latitude = location?.latitude.toString()
            } else {
                requestPermissions(
                    arrayOf(if (tipo == "NET") android.Manifest.permission.ACCESS_COARSE_LOCATION else Manifest.permission.ACCESS_FINE_LOCATION),
                    if (tipo == "NET") COARSE_REQUEST else FINE_REQUEST
                )
            }
        }
        return location
    }

    override fun onLocationChanged(p0: Location) {
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == COARSE_REQUEST || requestCode == FINE_REQUEST) {
        }
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            val imagem = (data?.extras!!["data"] as Bitmap?)!!
            val imgFoto = requireActivity().findViewById<ImageView>(R.id.imgFoto)
            imgFoto.setImageBitmap(imagem)

            val bitmap = (imgFoto.getDrawable() as BitmapDrawable).getBitmap()
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            val image = stream.toByteArray()
            foto.foto = image
        }
    }

    private fun gravaRegistroFoto(foto: Foto) {
        val fileName = anotacao.titulo + "(" + anotacao.data + ")" + ".fig"
        val fileOutputStream =
            getActivity()?.openFileOutput(fileName, AppCompatActivity.MODE_APPEND)
        fileOutputStream?.write(foto.foto)
        fileOutputStream?.close()
    }


    private fun gravaRegistro(anotacao: Anotacao) {
        val fileName = anotacao.titulo + "(" + anotacao.data + ")" + ".txt"
        val builder = StringBuilder()
        builder.append("Data: ").append(anotacao.data).append("\n")
            .append("Longitude: ").append(anotacao.longitude).append("\n")
            .append("Latitude: ").append(anotacao.latitude).append("\n")
            .append("Titulo: ").append(anotacao.titulo).append("\n")
            .append("Texto: ").append(anotacao.texto).append("\n")
        val registroTxt = builder.toString()

        val masterKeyAlias: String =
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val file: java.io.File =
            java.io.File(requireActivity().filesDir, fileName)

        val fileOutput = EncryptedFile.Builder(
            file,
            requireActivity(),
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        )
            .build()

        val encryptedOut: FileOutputStream = fileOutput.openFileOutput()

        val pw = PrintWriter(encryptedOut)
        pw.write(registroTxt)
        pw.flush()
        encryptedOut.close()

//        val fileOutputStream =
//            getActivity()?.openFileOutput(fileName, AppCompatActivity.MODE_APPEND)
//        fileOutputStream.write(registroTxt.toByteArray())
//        fileOutputStream?.close()
    }

    private fun validaCamposAnotacao(titulo: String, texto: String): Boolean {
        var mensagem: String = ""
        if (titulo.isEmpty()) {
            mensagem = "Título do anotação deve ser informado"
        } else {
            if (texto.isEmpty()) {
                mensagem = "Texto da anotação deve ser informado"
            }
        }
        if (mensagem.isEmpty()) {
            return true
        } else {
            Toast.makeText(this.requireActivity(), mensagem, Toast.LENGTH_LONG).show()
            return false
        }
    }

    private fun atualizaListaanotacaos() {

        val minhaLista: ArrayList<String>

        minhaLista = ArrayList()

        for (file in requireActivity().getFilesDir().listFiles()) {
            if (file.name.endsWith("txt")) {
                minhaLista.add(file.name)
            }
        }

        val lstanotacaos =
            this.requireActivity().findViewById<RecyclerView>(R.id.lstanotacaos)
        lstanotacaos.layoutManager = LinearLayoutManager(this.requireActivity())
        val adapter = ListaAnotacaoAdapter(minhaLista)
        adapter.setRecyclerViewItemListner(this)
        lstanotacaos.adapter = adapter
    }

    private fun leArquivo(fileName: String): Anotacao {
        val masterKeyAlias: String =
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val file: java.io.File =
            java.io.File(requireActivity().filesDir, fileName)

        val arquivo = EncryptedFile.Builder(
            file,
            requireActivity(),
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        )
            .build()

        val arquivoCriptografado: FileInputStream = arquivo.openFileInput()

        val br = BufferedReader(InputStreamReader(arquivoCriptografado))

        anotacao = Anotacao()

        br.lines().forEach { t ->
            if (t.indexOf("Data: ", 0, false) >= 0) {
                anotacao.data = t.replace("Data: ", "", true)
            } else if (t.indexOf("Longitude: ", 0, false) >= 0) {
                anotacao.longitude = t.replace("Longitude: ", "", true)
            } else if (t.indexOf("Latitude: ", 0, false) >= 0) {
                anotacao.latitude = t.replace("Latitude: ", "", true)
            } else if (t.indexOf("Titulo: ", 0, false) >= 0) {
                anotacao.titulo = t.replace("Titulo: ", "", true)
            } else if (t.indexOf("Texto: ", 0, false) >= 0) {
                anotacao.texto = t.replace("Texto: ", "", true)
            }
        }
        arquivoCriptografado.close()
        return anotacao
    }

    private fun leArquivoFoto(fileName: String): Foto {

        val fileInputString =
            getActivity()?.openFileInput(fileName)
        foto.foto = fileInputString?.readBytes()
        fileInputString?.close()

        return foto
    }

    private fun excluiArquivo(fileName: String): Boolean {
        val nomeRaiz = fileName.substring(0, fileName.length - 4)
        requireActivity().getFileStreamPath(nomeRaiz + ".txt").delete()
        requireActivity().getFileStreamPath(nomeRaiz + ".fig").delete()
        return true
    }

    private fun atualizaAnotacao(operacao: String) {
        when (operacao) {
            "incluir" -> {
                gravaRegistro(anotacao)
                gravaRegistroFoto(foto)
                Toast.makeText(
                    this.requireActivity(),
                    "Inclusão realizada com sucesso.",
                    Toast.LENGTH_LONG
                ).show()
            }
            "alterar" -> {
                gravaRegistro(anotacao)
                Toast.makeText(
                    this.requireActivity(),
                    "Alteração realizada com sucesso.",
                    Toast.LENGTH_LONG
                ).show()
            }
            "excluir" -> {
                gravaRegistro(Anotacao())
                Toast.makeText(
                    this.requireActivity(),
                    "Exclusão realizada com sucesso.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        atualizaListaanotacaos()
    }

    override fun recyclerViewBotaoVisualizarClicked(view: View, nomeArquivo: String) {
        val edtData = this.requireActivity().findViewById<EditText>(R.id.edtData)
        val edtLongitude = this.requireActivity().findViewById<TextView>(R.id.edtLongitude)
        val edtLatitude = this.requireActivity().findViewById<TextView>(R.id.edtLatitude)
        val edtTitulo = this.requireActivity().findViewById<EditText>(R.id.edtTitulo)
        val edtTexto = this.requireActivity().findViewById<EditText>(R.id.edtTexto)
        val imgFoto = this.requireActivity().findViewById<ImageView>(R.id.imgFoto)

        val anotacao = leArquivo(nomeArquivo)
        val foto = leArquivoFoto(nomeArquivo.replace("txt","fig"))
        val bitmapImage = foto.foto?.let { BitmapFactory.decodeByteArray(foto.foto, 0, it.size) }

        edtData.setText(anotacao.data)
        edtLongitude.setText(anotacao.longitude)
        edtLatitude.setText(anotacao.latitude)
        edtTitulo.setText(anotacao.titulo)
        edtTexto.setText(anotacao.texto)
        imgFoto.setImageBitmap(bitmapImage)
    }

    override fun recyclerViewBotaoExcluirClicked(view: View, nomeArquivo: String): Boolean {

        if (excluiArquivo(nomeArquivo)) {
            atualizaListaanotacaos()
        }
        return true
    }

}