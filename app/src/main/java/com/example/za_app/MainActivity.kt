package com.example.za_app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.*
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.LatLng
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.gms.maps.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.lieu_profil.*
import kotlinx.android.synthetic.main.lieu_profil.view.*
import kotlinx.android.synthetic.main.modal.*
import kotlinx.android.synthetic.main.modal.view.*
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.android.synthetic.main.search.view.*
import kotlinx.android.synthetic.main.user_profil.view.*
import java.nio.ByteBuffer
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
    GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener {


    /**********Variables*****************************************************************************/
    private val MY_LOCATION_REQUEST_CODE = 1
    private var googleMap:GoogleMap?=null
    private var locationManager : LocationManager? = null

    val generaltag = ""
    var supress =""
    private var currentMarker: Marker? = null

    private var context: Context? = null
    lateinit var imagePath: String
    var imagesList: MutableList<Uri> = arrayListOf()
    var listLieux: ArrayList<lieu> = arrayListOf()

    var Mylongtude = 2.315834
    var Mylatitude = 9.0578879005793

    var NomLieu =""
    var speciality =""
    var JourOuv =""
    var heureOuv =""
    var heureFerm =""
    var budget = ""

    var pass =""
    var nom=""
    var email=""
    var telephone =""

    var markerdrag = HashMap<Marker, Integer>()

    /****************Databases**************************************************************/
    private val TAG = MainActivity::class.java.simpleName
    private var mStorageRef: StorageReference? = null

    private val db = FirebaseFirestore.getInstance()

    private val stokage = FirebaseStorage.getInstance()
    private val storageRef = stokage.reference

    val database = FirebaseDatabase.getInstance()
    val myref = database.getReference()
    /*************************************************************************************************/


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        getcurentLocalisation()
        mainLoginBtn.hide()
        supprimer.hide()
        valider.hide()
        getAndlaodPoints()
        updatePoints()

        mStorageRef = FirebaseStorage.getInstance().getReference();

        valider.setOnClickListener { view ->
            if(!getCountryInfo(Mylatitude,Mylongtude)) {
                if (NomLieu.isNotEmpty() and speciality.isNotEmpty()) {
                    if (budget.isNotEmpty()) {
                    if (JourOuv.isNotEmpty()) {
                        if (heureOuv.isNotEmpty() and heureFerm.isNotEmpty()) {

                            if (supress.isNotEmpty()){

                                db.collection("lieux").document(supress)
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!")
                                        //on supprime les images du lieu
                                        getAndlaodPoints()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(TAG, "Error deleting document", e)
                                    }

                                supress=""
                            }

                            val lieuTab: lieu =
                                lieu(NomLieu, speciality,budget, JourOuv, heureOuv, heureFerm, Mylongtude, Mylatitude,email)
                            db.collection("lieux")
                                .add(lieuTab)
                            var imagesname = NomLieu.trim() + Mylatitude + Mylongtude;
                            sendImageDatabase(imagesname.trim())

                            snack("Ajouter avec succès!")
                            googleMap!!.clear()
                            supprimer.hide()
                            mainLoginBtn.hide()
                            fab.show()
                            valider.hide()
                            NomLieu = ""
                            speciality = ""
                            budget = ""
                            JourOuv = ""
                            heureOuv = ""
                            heureFerm = ""
                            supress=""
                            imagesList.clear()
                            googleMap!!.uiSettings.setScrollGesturesEnabled(true);

                        } else {
                            snack("Veillez choisir une heure de d'ouverture et de fermeture!")
                        }
                    } else {
                        snack("Veillez choisir les jours d'ouvertures!")
                    }
                    } else {
                        snack("Veillez renseigner un budget minimum!")
                    }
                } else {
                    snack("Veillez saisir le nom du lieu et les specialités!")
                }
            }else{
                snack("Le service est indisponible dans ce pays!")
            }

        }

        supprimer.setOnClickListener { view ->
            googleMap!!.clear()
            supprimer.hide()
            mainLoginBtn.hide()
            fab.show()
            valider.hide()
            googleMap!!.uiSettings.setScrollGesturesEnabled(true);
            getAndlaodPoints()
            supress=""

        }

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->

          val builder = AlertDialog.Builder(this)
            //set title for alert dialog
            builder.setTitle("Ajouter un lieu")
            //set message for alert dialog
            builder.setMessage("Voulez vous utilisez votre position actuelle ou un marqueur" +
                    " pour definir un nouveau lieu")
            builder.setIcon(R.drawable.logo)

            //performing positive action
            builder.setPositiveButton("Utiliser ma Position actuelle"){dialogInterface, which ->

                if(getCountryInfo(Mylatitude,Mylongtude) == false) {

                    googleMap!!.clear()
                    getcurentLocalisation()

                    addMarkerMap(
                        Mylatitude, Mylongtude, "Maintenez le marqueur pour le déplacer",
                        "Cliquez sur le crayon pour éditer!", 7.0f, true, R.drawable.logo,
                        "editable"
                    )

                    googleMap!!.uiSettings.setScrollGesturesEnabled(false);

                    supprimer.show()
                    fab.hide()
                    mainLoginBtn.show()
                    valider.show()

                }else{

                    snack("Le service est indisponible dans ce pays!")
                    mainLoginBtn.hide()
                    fab.show()
                }


            }
            //performing cancel action
            builder.setNeutralButton("Annuler"){dialogInterface , which ->
                fab.show()
                mainLoginBtn.hide()
            }
            //performing negative action
            builder.setNegativeButton("Utiliser un marqueur"){dialogInterface, which ->

                googleMap!!.clear()

                if(getCountryInfo(GetCameraCenter()!!.target.latitude,GetCameraCenter()!!.target.longitude) == false){

                    Mylatitude = GetCameraCenter()!!.target.latitude
                    Mylongtude = GetCameraCenter()!!.target.longitude

                    addMarkerMap(GetCameraCenter()!!.target.latitude,GetCameraCenter()!!.target.longitude,
                        "Maintenez le marqueur pour le déplacer", "Cliquez sur le crayon pour éditer!",
                        GetCameraCenter()!!.zoom,
                        true,R.drawable.logo,"editable")

                    googleMap!!.uiSettings.setScrollGesturesEnabled(false);
                    supprimer.show()
                    fab.hide()
                    mainLoginBtn.show()
                    valider.show()
                }
                else{
                        snack("Ce service est indisponible dans ce pays!")
                         mainLoginBtn.hide()
                        fab.show()
                    valider.hide()
                }

            }

            // Create the AlertDialog
            val alertDialog: AlertDialog = builder.create()
            // Set other dialog properties
            alertDialog.setCancelable(false)
            alertDialog.show()
        }


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

         dialogModal()


    }


    fun dialogModal(){
        //button click to show dialog
        mainLoginBtn.setOnClickListener {
            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.modal, null)
            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Modifier le marqueur")
            //show dialog
            val  mAlertDialog = mBuilder.show()

            mDialogView.dialogNameEt.setText(NomLieu)
            mDialogView.specialite.setText(speciality)
            mDialogView.budget.setText(budget)
            mDialogView.jourOuv.setText(JourOuv)
            mDialogView.heureDeb.setText(heureOuv)
            mDialogView.heureFerm.setText(heureFerm)

            if(imagesList.isNotEmpty()){
                mDialogView.imageView3.isVisible = true
            }else{
                mDialogView.imageView3.isVisible = false
            }

            mDialogView.jourOuv.setOnClickListener {

                val items = arrayOf("Lundi", "Mardi", "Mercredi", "Jeudi","Vendredi","Samedi","Dimanche")
                val selectedList = ArrayList<Int>()
                val builder = AlertDialog.Builder(this)

                builder.setTitle("Selectionnez les jours d'ouvertures:")
                builder.setMultiChoiceItems(items, null
                ) { dialog, which, isChecked ->
                    if (isChecked) {
                        selectedList.add(which)
                    } else if (selectedList.contains(which)) {
                        selectedList.remove(Integer.valueOf(which))
                    }
                }

                builder.setPositiveButton("Ajouter") { dialogInterface, i ->
                    val selectedStrings = ArrayList<String>()
                    JourOuv = ""
                    for (j in selectedList.indices) {
                        JourOuv = JourOuv+items[selectedList[j]]+"; "
                    }
                    mDialogView.jourOuv.setText(JourOuv)

                }

                builder.show()
            }


            /*************Pick image*****************************************************************************/

            mDialogView.addpic.setOnClickListener {
                //check runtime permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_DENIED){
                        //permission denied
                        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        //show popup to request runtime permission
                        requestPermissions(permissions, PERMISSION_CODE)
                    }
                    else{
                        //permission already granted
                        pickImageFromGallery()
                    }
                }
                else{
                    //system OS is < Marshmallow
                    pickImageFromGallery()
                }

                mDialogView.imageView3.isVisible = true

            }
            /*************************************************************************************************/
            mDialogView.heureDeb.setOnClickListener{
                val c = Calendar.getInstance()
                val hour = c.get(Calendar.HOUR)
                val minute = c.get(Calendar.MINUTE)

                val tpd = TimePickerDialog(this,TimePickerDialog.OnTimeSetListener(function = { view, h, m ->

                    val heure = if (h < 10) "0" + h else h
                    val minute = if (m < 10) "0" + m else m
                    mDialogView.heureDeb.setText(heure.toString() + ":" + minute.toString())

                }),hour,minute,false)
                tpd.setTitle("Heure d'ouverture");
                tpd.show()
            }


            mDialogView.heureFerm.setOnClickListener{
                val c = Calendar.getInstance()
                val hour = c.get(Calendar.HOUR)
                val minute = c.get(Calendar.MINUTE)

                val tpd = TimePickerDialog(this,TimePickerDialog.OnTimeSetListener(function = { view, h, m ->

                    val heure = if (h < 10) "0" + h else h
                    val minute = if (m < 10) "0" + m else m
                    mDialogView.heureFerm.setText(heure.toString() + ":" + minute.toString())

                }),hour,minute,false)
                tpd.setTitle("Heure de Fermerture");
                tpd.show()
            }

            //login button click of custom layout
            mDialogView.dialogLoginBtn.setOnClickListener {
                //dismiss dialog
                //get text from EditTexts of custom layout
                if(mDialogView.dialogNameEt.text.isNotEmpty() and mDialogView.specialite.text.isNotEmpty() and
                    mDialogView.jourOuv.text.isNotEmpty() and mDialogView.heureDeb.text.isNotEmpty() and
                    mDialogView.heureFerm.text.isNotEmpty() and mDialogView.budget.text.isNotEmpty() ){

                    NomLieu = mDialogView.dialogNameEt.text.toString()
                    speciality = mDialogView.specialite.text.toString()
                    budget = mDialogView.budget.text.toString()
                    JourOuv = mDialogView.jourOuv.text.toString()
                    heureOuv = mDialogView.heureDeb.text.toString()
                    heureFerm = mDialogView.heureFerm.text.toString()
                    //set the input text in TextView

                    mAlertDialog.dismiss()
                    googleMap!!.clear()

                    addMarkerMap(GetCameraCenter()!!.target.latitude,GetCameraCenter()!!.target.longitude,
                        NomLieu,speciality,
                        GetCameraCenter()!!.zoom,true,
                        R.drawable.logo,generaltag)

                }else{
                    Toast.makeText(this, "Remplissez tous les champs svp!", Toast.LENGTH_SHORT).show()
                }


            }

            mDialogView.dialogCancelBtn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
            }

        }

    }


    fun snack(s: String) {
        val snack = Snackbar.make(this.toolbar,s,
            Snackbar.LENGTH_LONG)

        snack.setAction("Ok!", View.OnClickListener {

        })
        snack.show()
    }

    fun getAndlaodPoints() {

        db.collection("lieux")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Log.d(TAG, "${document.data["nom"]} => ${document.data}")
                    addMarkerMap(
                        document.data["latitude"] as Double, document.data["longitude"] as Double,
                        document.data["nom"] as String,
                        document.data["specialite"].toString().plus(" ")
                            .plus("(Voir details)"),
                        7.0f, false, R.drawable.logo,document.id
                    )

                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

    }


    fun updatePoints() {

        db.collection("lieux")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "listen:error", e)
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        addMarkerMap(
                            dc.document.data["latitude"] as Double, dc.document.data["longitude"] as Double,
                            dc.document.data["nom"] as String,
                            dc.document.data["specialite"].toString().plus(" ")
                                .plus("(Voir details)"), 7.0f, false, R.drawable.logo,
                            dc.document.id
                        )
                    }
                }
                getAndlaodPoints()
            }

    }


    fun sendImageDatabase(imagesname: String) {
        if(imagesList.isNotEmpty()){
            var i = 0
            imagesList.forEach(){
                var file = it
                var sentence = imagesname.replace(" ", "")
                val riversRef = storageRef.child("imagesLieux/${sentence.trim()}/${i}")
                var uploadTask = riversRef.putFile(file)
                i++
                uploadTask.addOnFailureListener {
                    snack("Echec d'ajout des images !")
                }.addOnSuccessListener {

                }
            }
        }

    }

    private fun pickImageFromGallery() {

        if (Build.VERSION.SDK_INT < 19) {
            var intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Choisissez des images")
                , PICK_IMAGE_MULTIPLE
            )
        } else {
            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_MULTIPLE);
        }
    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000
        //Permission code
        private val PERMISSION_CODE = 1001
        private var PICK_IMAGE_MULTIPLE = 1
    }


    //handle result of picked image
    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        // When an Image is picked
        if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == Activity.RESULT_OK
            && null != data
        ) {
            if (data.getClipData() != null) {
                var count = data.clipData!!.itemCount
                for (i in 0..count - 1) {
                    var imageUri: Uri = data.clipData!!.getItemAt(i).uri
                    imageUri.let { imagesList.add(it) }
                    Toast.makeText(this, "Images chargées !", Toast.LENGTH_SHORT).show()
                }
            } else if (data.getData() != null) {
                var imagePath: String? = data.data!!.path
                Log.e("imagePath", imagePath);
            }

        }


    }

    fun getcurentLocalisation(){
    // Create persistent LocationManager reference
        var locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        try {
            // Request location updates
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f,
                locationListener)

        } catch (ex: SecurityException) {
            Log.d("myTag", "Security Exception, no location available")
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
           // thetext.setText("" + location.longitude + ":" + location.latitude);

           Mylongtude = location.longitude
           Mylatitude = location.latitude

        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    /*********resize Image*********************************************************************/

    fun resizeImage(height: Int,width : Int,resources: Int): Bitmap? {
        val bitmapdraw = getResources().getDrawable(resources)
        var b = bitmapdraw.toBitmap()
        val smallMarker = Bitmap.createScaledBitmap(b, width, height, false)
        return smallMarker
    }


    /*********Permission Maps location*********************************************************************/

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == IMAGE_PICK_CODE) {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
                    //permission from popup denied
                    snack("Oups! Permission refusé. Impossible de poursuivre l'action.")
                }
        }

            if (requestCode == MY_LOCATION_REQUEST_CODE) {
                if (permissions.size == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    googleMap!!.isMyLocationEnabled = true
                } else {
                    snack("Oups! Permission refusé. Impossible de poursuivre l'action.")
                }
            }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    /*********Google Maps*********************************************************************/

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG).show()

    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    fun InitMap() {
        val PERTH = LatLng(Mylatitude, Mylongtude)
        googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(PERTH, 7.0f))
    }

    @SuppressLint("WrongConstant")
    fun getCountryInfo(latitude: Double, longitude: Double): Boolean {
        try {
            var geo = Geocoder(this.applicationContext, Locale.getDefault())
            var addresses: List<Address> = emptyList()
            var AcceptCountry: List<String> = emptyList()
            AcceptCountry = listOf("Bénin")
            addresses = geo.getFromLocation(latitude, longitude, 1)

            if (addresses.isEmpty()) {
                snack("Oups! Le service est indisponible à cet endroit.")
                return false
            } else {
                return !(addresses.get(0).countryName).equals( AcceptCountry.get(0))
            }

        } catch (e: Exception) {
            snack("Euh! Le service est indisponible à cet endroit.")
            return false
        }

    }

    fun GetCameraCenter(): CameraPosition? {
        var camera = googleMap!!.cameraPosition
        return camera
    }

    override fun onMapReady(p0: GoogleMap?) {
        googleMap=p0

        googleMap!!.isBuildingsEnabled = true

        if (Build.VERSION.SDK_INT >= 23) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                googleMap!!.isMyLocationEnabled = true
            } else {
                // Request permission.
                ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_LOCATION_REQUEST_CODE)
            }
        }

        googleMap!!.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {

            override fun onMarkerDragStart(p0: Marker?) {
                p0!!.hideInfoWindow()
            }
            override fun onMarkerDragEnd(p0: Marker?) {

                Mylatitude = p0!!.position.latitude
                Mylongtude = p0!!.position.longitude

                var coordinate =  LatLng(p0!!.position.latitude, p0.position.longitude)
                var location = CameraUpdateFactory.newLatLng(
                    coordinate)
                googleMap!!.animateCamera(location)
                p0.showInfoWindow()

                if(getCountryInfo(p0.position.latitude, p0.position.longitude) == false){
                    p0.setIcon(BitmapDescriptorFactory.fromBitmap(resizeImage(70,70,
                        R.drawable.logo)))
                    mainLoginBtn.isEnabled = true
                    mainLoginBtn.isClickable = true
                    valider.show()
                } else{
                    p0.setIcon(BitmapDescriptorFactory.fromBitmap(resizeImage(70,70,
                        android.R.drawable.ic_delete)))
                    snack("Le service est indisponible dans ce pays.")
                    mainLoginBtn.isEnabled = false
                    mainLoginBtn.isClickable = false
                    valider.hide()
                }


            }
            override fun onMarkerDrag(p0: Marker?) {
                p0!!.hideInfoWindow()
            }
        })


        googleMap!!.setOnMarkerClickListener (object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker): Boolean {
                currentMarker = marker
                marker.zIndex += 1.0f


                return false
            }

        })


        googleMap!!.setOnInfoWindowClickListener {

            if(it.tag == "editable"){
                mainLoginBtn.callOnClick()
            }else{

            val builder = AlertDialog.Builder(this@MainActivity)
            val inflater = layoutInflater
            builder.setTitle(currentMarker!!.title)
            val dialogLayout = inflater.inflate(R.layout.lieu_profil, null)
            builder.setView(dialogLayout)
            builder.setPositiveButton("Fermer") { dialogInterface, i ->
            }

              val mAlertDialog2 = builder.show()

            db.collection("lieux").document(currentMarker!!.tag.toString())
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {

                        dialogLayout.budgetMin.text = document.data!!["budget"].toString().plus(" F CFA")
                        dialogLayout.special.text = document.data!!["specialite"].toString()
                        dialogLayout.ouvertureDay.text = document.data!!["jourOuv"].toString()
                        dialogLayout.ouverturehour.text = document.data!!["heureOuv"].toString()
                        dialogLayout.fermertureHour.text = document.data!!["heureFerm"].toString()

                        var lieu = currentMarker!!.title.toString().replace(" ", "").trim();

                        var path = "${lieu}${currentMarker!!.position.latitude}${currentMarker!!.position.longitude}"

                        var number = 0

                        Picasso.with(this@MainActivity)
                            .load(
                                "https://firebasestorage.googleapis.com/v0/b/zaapp-4771f.appspot.com/o/" +
                                        "imagesLieux%2F" + path + "%2F" + number
                                        + "?alt=media&token=dd682537-8e23-4150-99e9-7b12e3ec9d14"
                            )
                            .placeholder(android.R.drawable.ic_menu_camera)
                            .into(dialogLayout.img)

                        dialogLayout.suivant.setOnClickListener { view ->
                            if (number < 2) {
                                number++
                                Picasso.with(this@MainActivity)
                                    .load(
                                        "https://firebasestorage.googleapis.com/v0/b/zaapp-4771f.appspot.com/o/" +
                                                "imagesLieux%2F" + path + "%2F" + number
                                                + "?alt=media&token=dd682537-8e23-4150-99e9-7b12e3ec9d14"
                                    )
                                    .placeholder(android.R.drawable.ic_menu_camera)
                                    .into(dialogLayout.img)

                            } else {
                                number = 0
                                Picasso.with(this@MainActivity)
                                    .load(
                                        "https://firebasestorage.googleapis.com/v0/b/zaapp-4771f.appspot.com/o/" +
                                                "imagesLieux%2F" + path + "%2F" + number
                                                + "?alt=media&token=dd682537-8e23-4150-99e9-7b12e3ec9d14"
                                    )
                                    .placeholder(android.R.drawable.ic_menu_camera)
                                    .into(dialogLayout.img)
                            }

                        }
                        dialogLayout.precedent.setOnClickListener { view ->
                            if (number > 0) {
                                number--
                                Picasso.with(this@MainActivity)
                                    .load(
                                        "https://firebasestorage.googleapis.com/v0/b/zaapp-4771f.appspot.com/o/" +
                                                "imagesLieux%2F" + path + "%2F" + number
                                                + "?alt=media&token=dd682537-8e23-4150-99e9-7b12e3ec9d14"
                                    )
                                    .placeholder(android.R.drawable.ic_menu_camera)
                                    .into(dialogLayout.img)

                            } else {
                                number = 2
                                Picasso.with(this@MainActivity)
                                    .load(
                                        "https://firebasestorage.googleapis.com/v0/b/zaapp-4771f.appspot.com/o/" +
                                                "imagesLieux%2F" + path + "%2F" + number
                                                + "?alt=media&token=dd682537-8e23-4150-99e9-7b12e3ec9d14"
                                    )
                                    .placeholder(android.R.drawable.ic_menu_camera)
                                    .into(dialogLayout.img)
                            }
                        }


                        if (email == document.data!!["createur"].toString() || email == "kyrigami@gmail.com") {
                            dialogLayout.gestion.isGone = false

                        dialogLayout.suppLieu.setOnClickListener { view ->

                            val builder2 = AlertDialog.Builder(this)
                            builder2.setTitle("Voulez-vous supprimer définitivement ce lieu: " + it.title + "?")
                            builder2.setMessage("Attention cette opération est irréversible.")

                            builder2.setPositiveButton(android.R.string.yes) { dialog, which ->

                                db.collection("lieux").document(document.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!")
                                        //on supprime les images du lieu

                                        for (i in 0..2) {
                                            val desertRef = storageRef.child("imagesLieux/"
                                                    + path + "/" + i)
                                            desertRef.delete().addOnSuccessListener {
                                            }.addOnFailureListener {
                                            }
                                        }
                                        snack("Lieu supprimer avec succès!")
                                        mAlertDialog2.dismiss()
                                        getAndlaodPoints()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(TAG, "Error deleting document", e)
                                        snack("Impossible de supprimer ce lieu!")
                                    }


                            }

                            builder2.setNegativeButton(android.R.string.no) { dialog, which ->
                                Toast.makeText(
                                    applicationContext,
                                    android.R.string.no, Toast.LENGTH_SHORT
                                ).show()
                            }

                            builder2.show()

                            }


                            dialogLayout.modifLieu.setOnClickListener { view ->

                            googleMap!!.clear()

                                NomLieu  =document.data!!["nom"].toString()
                                speciality = document.data!!["specialite"].toString()
                                JourOuv = document.data!!["jourOuv"].toString()
                                heureOuv = document.data!!["heureOuv"].toString()
                                heureFerm = document.data!!["heureFerm"].toString()
                                budget = document.data!!["budget"].toString()

                                Mylatitude = it.position.latitude
                                Mylongtude = it.position.longitude

                                mAlertDialog2.dismiss()
                                mainLoginBtn.performClick()

                            if(getCountryInfo(Mylatitude,Mylongtude) == false){

                                addMarkerMap(Mylatitude,Mylongtude,
                                    it.title, "Cliquez sur le crayon pour éditer!",
                                    GetCameraCenter()!!.zoom,
                                    true,R.drawable.logo,"editable")

                                googleMap!!.uiSettings.setScrollGesturesEnabled(false);
                                fab.hide()
                                mainLoginBtn.show()
                                valider.show()
                                supprimer.show()
                            }
                            else{
                                snack("Ce service est indisponible dans ce pays!")
                                mainLoginBtn.hide()
                                fab.show()
                                valider.hide()
                            }

                                var coordinate =  LatLng(it!!.position.latitude, it.position.longitude)
                                var location = CameraUpdateFactory.newLatLng(
                                    coordinate)
                                googleMap!!.animateCamera(location)

                                dialogModal()
                                supress = document.id

                            }


                        }

                        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }

            false

            }

        }

        googleMap!!.setMinZoomPreference(6.0f)
        googleMap!!.setMaxZoomPreference(20.0f)

        googleMap!!.setOnMapLoadedCallback(GoogleMap.OnMapLoadedCallback {
            // Create a LatLngBounds that includes Australia.
           InitMap()

        })

        /****Maps Json style call********************************************************************/
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = googleMap!!.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.style_json
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Can't find style. Error: ",e )
        }
        /**********************************************************************************************/


    }

    fun addMarkerMap(latitude: Double,longitude: Double, titre: String,info: String, zoomLevel: Float,drag: Boolean
                     ,image : Int, tag : String){
        val PERTH = LatLng(latitude, longitude)
        val perth = googleMap!!.addMarker(
            MarkerOptions()
                .position(PERTH)
                .draggable(drag)
                .title(titre)
                .snippet(info)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeImage(70,70,image)))
        )

        if(tag == "editable"){
            perth.showInfoWindow()
        }
        perth.tag = tag
        googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(PERTH, zoomLevel))

    }

    fun search(things : String): MutableList<lieu> {

        listLieux.clear()
        val LieuRef = db.collection("lieux")
        LieuRef.whereEqualTo("nom", things)
            .whereGreaterThanOrEqualTo("nom", things)
            //.whereEqualTo("specialite", things)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty){
                    LieuRef.whereEqualTo("specialite", things)
                        .whereGreaterThanOrEqualTo("specialite", things)
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                var lieuxData = lieu(
                                    document.data["nom"] as String,
                                    document.data["specialite"] as String,
                                    document.data["budget"] as String,
                                    document.data["jourOuv"] as String,
                                    document.data["heureOuv"] as String,
                                    document.data["heureFerm"] as String,
                                    document.data["longitude"] as Double,
                                    document.data["latitude"] as Double,
                                    document.data["createur"] as String
                                )
                                listLieux.add(lieuxData)
                            }

                        }
                        .addOnFailureListener { exception ->

                        }
                }else {
                    for (document in documents) {
                        var lieuxData = lieu(
                            document.data["nom"] as String,
                            document.data["specialite"] as String,
                            document.data["budget"] as String,
                            document.data["jourOuv"] as String,
                            document.data["heureOuv"] as String,
                            document.data["heureFerm"] as String,
                            document.data["longitude"] as Double,
                            document.data["latitude"] as Double,
                            document.data["createur"] as String
                        )
                        listLieux.add(lieuxData)
                    }
                }
            }
            .addOnFailureListener { exception ->

            }

        return listLieux

    }


    fun searchModal(){

        val builder3 = AlertDialog.Builder(this@MainActivity)
        val inflater = layoutInflater
        builder3.setTitle("")
        val dialogLayout3 = inflater.inflate(R.layout.search, null)
        builder3.setView(dialogLayout3)
        builder3.setPositiveButton("Fermer") { dialogInterface, i ->
        }
        val searching = builder3.show()

        val lv = dialogLayout3.findViewById<ListView>(R.id.recipe_list_view)
        lv.transcriptMode = ListView.TRANSCRIPT_MODE_NORMAL
        lv.isStackFromBottom = true
        val prodAdapter = CustomAdapter(this@MainActivity, listLieux)

        lv.adapter = prodAdapter

        lv.setOnItemClickListener { parent, view, position, id ->

            val item = parent.getItemAtPosition(position) as lieu
            var longitudeSelect = item.longitude
            var latitudeSelect = item.latitude

            var coordinate =  LatLng(latitudeSelect, longitudeSelect)
            var location = CameraUpdateFactory.newLatLngZoom(
                coordinate, 20f)

            searching.dismiss()
            googleMap!!.animateCamera(location)

        }

        dialogLayout3.searchbar.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {

                search(s.toString())
                prodAdapter.notifyDataSetChanged()

            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {


            }
        })


    }


/**********MENU AND OPTIONS********************************************************************************/

    override fun onBackPressed() {

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.

        val userLog: SharedPreferences = getSharedPreferences("Log", Context.MODE_PRIVATE)
        email = userLog.getString("lastEmail", null).toString()
        pass = userLog.getString("pass", null).toString()

        db.collection("users").document(email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    nom = documents.data!!["nomprenom"].toString()
                    telephone = documents.data!!["telephone"].toString()

                    this.nav_view.profil.nomProfil.text = documents.data!!["nomprenom"].toString()
                } else {
                    nom = "vide"
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

        this.nav_view.emailProfil.text = email

        menuInflater.inflate(R.menu.main, menu)

        profil.setOnClickListener { view ->
            val builder4 = AlertDialog.Builder(this@MainActivity)
            val inflater = layoutInflater
            builder4.setTitle("")
            val dialogLayout4 = inflater.inflate(R.layout.user_profil, null)
            builder4.setView(dialogLayout4)
            builder4.setPositiveButton("Fermer") { dialogInterface, i ->
            }
            builder4.show()

            dialogLayout4.et_name.setText(nom)
            dialogLayout4.et_email.setText(email)
            dialogLayout4.numero.setText(telephone)

            dialogLayout4.btn_register.setOnClickListener { view ->

                if (dialogLayout4.et_name.text.isNotEmpty() && dialogLayout4.et_email.text.isNotEmpty()
                    && dialogLayout4.numero.text.isNotEmpty()) {

                    if ((dialogLayout4.et_password.text.toString())
                            .equals(pass)
                    ) {
                        if(dialogLayout4.checkBox.isChecked){

                            try {
                                nom = dialogLayout4.et_name.text.toString()
                                telephone = dialogLayout4.numero.text.toString()
                                nav_view.nomProfil.text = nom
                                db.collection("users").document(email)
                                    .update("telephone",dialogLayout4.numero.text.toString())

                                db.collection("users").document(email)
                                    .update("nomprenom",dialogLayout4.et_name.text.toString())

                                Toast.makeText(this, "Modifié!", Toast.LENGTH_LONG).show()

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }else{
                            Toast.makeText(this, "Veillez acceptez les conditions d'utilisation!",
                                Toast.LENGTH_LONG).show()
                        }

                    }else{
                        Toast.makeText(this,
                            "Veillez renseigner votre mot de passe actuel pour valider la modification!",
                            Toast.LENGTH_LONG).show()
                    }

                }else{
                    Toast.makeText(this,
                        "Veillez remplir tous les champs indispensables!",
                        Toast.LENGTH_LONG).show()
                }

            }

        }


        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_day -> {
                true
            }

            R.id.action_night -> {
                true
            }

            R.id.action_search ->{
                searchModal()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_see -> {
                // Handle the camera action
            }
            R.id.nav_fav -> {

            }

            R.id.nav_share -> {

            }
            R.id.nav_logout -> {

                val builder = AlertDialog.Builder(this@MainActivity)

                // Set the alert dialog title
                builder.setTitle("Se deconnecter ?")

                // Display a message on alert dialog
                builder.setMessage("Êtes-vous sûr de vouloir vous deconnecter?")

                // Set a positive button and its click listener on alert dialog
                builder.setPositiveButton("Se deconnecter"){dialog, which ->
                    val sharedPref: SharedPreferences = getSharedPreferences("Log", Context.MODE_PRIVATE)
                    sharedPref.edit().clear().commit()
                    finishAffinity()
                }
                builder.setNegativeButton("Annuler"){dialog,which ->
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()

            }
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onResume() {
        super.onResume()
    }

}
