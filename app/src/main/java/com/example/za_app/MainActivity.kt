package com.example.za_app

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.view.MenuItem
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.content.res.Resources.NotFoundException
import android.location.Location
import android.os.Build
import android.util.Log
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
    GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener {


    /**********Variables*****************************************************************************/
    private val MY_LOCATION_REQUEST_CODE = 1
    private var googleMap:GoogleMap?=null

    private val TAG = MainActivity::class.java!!.getSimpleName()

    /*************************************************************************************************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
        /**********************************************************************************************/

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            if (requestCode == MY_LOCATION_REQUEST_CODE) {
                if (permissions.size == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    googleMap!!.setMyLocationEnabled(true);
                } else {
                    // Permission was denied. Display an error message.
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


    override fun onMapReady(p0: GoogleMap?) {
        googleMap=p0

        if (Build.VERSION.SDK_INT >= 23) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                googleMap!!.setMyLocationEnabled(true);
            } else {
                // Request permission.
                ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_LOCATION_REQUEST_CODE);
            }
        }

        googleMap!!.setMinZoomPreference(6.0f);
        googleMap!!.setMaxZoomPreference(20.0f);

        googleMap!!.setOnMapLoadedCallback(GoogleMap.OnMapLoadedCallback {
            // Create a LatLngBounds that includes Australia.
            val Benin = LatLngBounds(
                LatLng( -6.0930506171051775, 2.0521418699095193), LatLng(17.99600167732847, 2.0472767657738586)
            )
            //googleMap!!.setLatLngBoundsForCameraTarget(Benin)
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

        //Adding draggable markers to map

        val PERTH = LatLng(6.3653600, 2.4183300)
        val perth = googleMap!!.addMarker(
            MarkerOptions()
                .position(PERTH)
                .draggable(true)
                .title("Maman Aya")
                .snippet("Specialité: Yovo Doko")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.yovo_doko))

        )
        perth.showInfoWindow();

        //Adding markers to map

        val latLng=LatLng(6.3653600,2.4183300)
        val markerOptions:MarkerOptions=MarkerOptions().position(latLng).title("Benin")

        // moving camera and zoom map

        val zoomLevel = 15.0f //This goes up to 21

        googleMap.let {
            it!!.addMarker(markerOptions)
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
        }


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
            R.id.nav_home -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_tools -> {

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
