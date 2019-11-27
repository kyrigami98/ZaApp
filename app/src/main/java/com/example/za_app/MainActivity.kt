package com.example.za_app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
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
import android.location.*
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.LatLng
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.modal.*
import kotlinx.android.synthetic.main.modal.view.*
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
    GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener {


    /**********Variables*****************************************************************************/
    private val MY_LOCATION_REQUEST_CODE = 1
    private var googleMap:GoogleMap?=null
    private var locationManager : LocationManager? = null

    var imageload = Uri.parse("")

    var Mylongtude = 2.315834
    var Mylatitude = 9.0578879005793

    var NomLieu =""
    var speciality =""
    var JourOuv =""
    var heureOuv =""
    var heureFerm =""

    var markerdrag = HashMap<Marker, Integer>()

    private val TAG = MainActivity::class.java.simpleName

    /*************************************************************************************************/

    fun snack(s: String) {
        val snack = Snackbar.make(this.toolbar,s,
            Snackbar.LENGTH_LONG)

        snack.setAction("Ok!", View.OnClickListener {

        })
        snack.show()
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        getcurentLocalisation()
        mainLoginBtn.hide()
        supprimer.hide()

        supprimer.setOnClickListener { view ->
            googleMap!!.clear()
            supprimer.hide()
            mainLoginBtn.hide()
            fab.show()
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

                googleMap!!.clear()
                getcurentLocalisation()

                if(getCountryInfo(Mylatitude,Mylongtude) == false) {

                    addMarkerMap(
                        Mylatitude, Mylongtude, "Maintenez le marqueur pour le déplacer",
                        "Cliquez pour éditer!", 7.0f, true, R.drawable.logo
                    )
                    supprimer.show()
                    fab.hide()
                    mainLoginBtn.show()
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

                    addMarkerMap(GetCameraCenter()!!.target.latitude,GetCameraCenter()!!.target.longitude,
                        "Maintenez le marqueur pour le déplacer", "Cliquez pour éditer!",
                        GetCameraCenter()!!.zoom,
                        true,R.drawable.logo)
                    supprimer.show()
                    fab.hide()
                    mainLoginBtn.show()
                }
                else{
                        snack("Ce service est indisponible dans ce pays!")
                         mainLoginBtn.hide()
                        fab.show()
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
            mDialogView.jourOuv.setText(JourOuv)
            mDialogView.heureDeb.setText(heureOuv)
            mDialogView.heureFerm.setText(heureFerm)


            /*************Pick image*****************************************************************************/

            mDialogView.imageView5.setImageURI(imageload)
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
                val handler = Handler()
                handler.postDelayed(Runnable {
                    mDialogView.imageView5.setImageURI(imageload)
                }, (15000))

            }
            /*************************************************************************************************/

            //login button click of custom layout
            mDialogView.dialogLoginBtn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
                //get text from EditTexts of custom layout
                NomLieu = mDialogView.dialogNameEt.text.toString()
                speciality = mDialogView.specialite.text.toString()
                JourOuv = mDialogView.jourOuv.text.toString()
                heureOuv = mDialogView.heureDeb.text.toString()
                heureFerm = mDialogView.heureFerm.text.toString()
                //set the input text in TextView

                googleMap!!.clear()

                addMarkerMap(GetCameraCenter()!!.target.latitude,GetCameraCenter()!!.target.longitude,
                    NomLieu,speciality,
                    GetCameraCenter()!!.zoom,true,
                    R.drawable.logo)

            }

            mDialogView.dialogCancelBtn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
            }

        }

    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000
        //Permission code
        private val PERMISSION_CODE = 1001
    }


    //handle result of picked image
    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            imageload = data?.data

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
                var coordinate =  LatLng(p0!!.position.latitude, p0.position.longitude)
                var location = CameraUpdateFactory.newLatLng(
                    coordinate)
                googleMap!!.animateCamera(location)
                p0.showInfoWindow()

                if(getCountryInfo(p0.position.latitude, p0.position.longitude) == false){
                    p0.setIcon(BitmapDescriptorFactory.fromBitmap(resizeImage(100,100,
                        R.drawable.logo)))
                    mainLoginBtn.isEnabled = true
                    mainLoginBtn.isClickable = true
                } else{
                    p0.setIcon(BitmapDescriptorFactory.fromBitmap(resizeImage(100,100,
                        android.R.drawable.ic_delete)))
                    snack("Le service est indisponible dans ce pays.")
                    mainLoginBtn.isEnabled = false
                    mainLoginBtn.isClickable = false
                }


            }
            override fun onMarkerDrag(p0: Marker?) {
                p0!!.hideInfoWindow()
            }
        })


        googleMap!!.setOnMarkerClickListener {

            mainLoginBtn.callOnClick()

            // Return false to indicate that we have not consumed the event and that we wish
            // for the default behavior to occur (which is for the camera to move such that the
            // marker is centered and for the marker's info window to open, if it has one).
            false
        }


        googleMap!!.setOnInfoWindowClickListener {
            // Return false to indicate that we have not consumed the event and that we wish
            // for the default behavior to occur (which is for the camera to move such that the
            // marker is centered and for the marker's info window to open, if it has one).
            false
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
                     ,image : Int){
        val PERTH = LatLng(latitude, longitude)
        val perth = googleMap!!.addMarker(
            MarkerOptions()
                .position(PERTH)
                .draggable(drag)
                .title(titre)
                .snippet(info)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeImage(100,100,image)))
        )
        perth.showInfoWindow()
        perth.tag = 1
        googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(PERTH, zoomLevel))

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
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
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
            R.id.nav_send -> {

            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }






}
