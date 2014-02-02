package com.tistory.everysw.gimbalsample;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

public class GoogleMapActivity extends FragmentActivity 
							implements OnMyLocationButtonClickListener,
							GooglePlayServicesClient.ConnectionCallbacks,
							GooglePlayServicesClient.OnConnectionFailedListener,
							LocationListener, 
							OnMapClickListener, 
							OnMapLongClickListener,
							OnMarkerClickListener {

	public static final float DEFAULT_LOCATION_SEOUL_LATITUDE = 37.540705f;
	public static final float DEFAULT_LOCATION_SEOUL_LONGITUDE = 126.956764f;
	public static final float MAP_DEFAULT_LOCATION_ZOOM = 11f;
	public static final float MAP_CURRENT_LOCATION_ZOOM = 18f;
	public static final int MIN_POLYGON_POINTS = 3;
		
	private GoogleMap googleMap;
	private LocationClient locationClient;
	private LatLng currentLocation = new LatLng(DEFAULT_LOCATION_SEOUL_LATITUDE, DEFAULT_LOCATION_SEOUL_LONGITUDE);
	
	private ArrayList<LatLng> arrSelectedPoints = new ArrayList<LatLng>();
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		
		googleMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		
		googleMap.setMyLocationEnabled(true);
		googleMap.setOnMyLocationButtonClickListener(this);
		googleMap.setOnMapClickListener(this);
		googleMap.setOnMapLongClickListener(this);
		googleMap.setOnMarkerClickListener(this);		

		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, MAP_DEFAULT_LOCATION_ZOOM));
		
		//checkGPS();
		
	}
	
    @Override
    protected void onStart() {
        super.onStart();
        startLocationUpdate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdate();        
    }

    
    void startLocationUpdate() {
        if(isGoogleServiceAvailable()) {
            setUpLocationClientIfNeeded();
            locationClient.connect();
        }
    }
    
    void stopLocationUpdate() {
        if(isGoogleServiceAvailable()) {        
            if(locationClient.isConnected()) {
                locationClient.removeLocationUpdates(this);
            }
            
            locationClient.disconnect();
        }
    }

    private void setUpLocationClientIfNeeded() {
        if (locationClient == null) {
            locationClient = new LocationClient(
                    getApplicationContext(),
                    this,
                    this);
        }
    }
    
    @Override
    public void onConnected(Bundle arg0) {
        
        LocationRequest request = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(16)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        
        locationClient.requestLocationUpdates(request, this); 
        Location lastLocation = locationClient.getLastLocation();
        if(lastLocation != null) {
            currentLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        }
    }

    @Override
    public void onDisconnected() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
    }    
    

	@Override
	public boolean onMyLocationButtonClick() {   
	    
	    if(currentLocation != null) {
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, MAP_CURRENT_LOCATION_ZOOM));
		}
		
		return true;
	}

	public void checkGPS()
	{
		LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean isGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!isGPS)
		{
			Toast.makeText(GoogleMapActivity.this, "GPS 기능을 켜주세요", Toast.LENGTH_LONG).show();
			startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
		}
	}

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub
        
    }



    @Override
    public void onMapClick(LatLng arg0) {
        googleMap.clear();
        arrSelectedPoints.clear();        
    }

    @Override
    public void onMapLongClick(LatLng point) {
        googleMap.addMarker(new MarkerOptions().position(point));
        arrSelectedPoints.add(point);
    }

    @Override
    public boolean onMarkerClick(Marker arg0) {

        if(arrSelectedPoints.size() >= MIN_POLYGON_POINTS) {
            addPolygon();
        }
        
        return false;
    }
    
    private void addPolygon() {
        PolygonOptions options = new PolygonOptions();
        options.addAll(arrSelectedPoints);
        options.strokeWidth(getResources().getInteger(R.integer.map_stroke_width));
        options.strokeColor(getResources().getColor(R.color.map_polygon_stroke_color));
        options.fillColor(getResources().getColor(R.color.map_polygon_fill_color));
        googleMap.addPolygon(options);
    }
    
    private boolean isGoogleServiceAvailable() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), getResources().getString(R.string.app_name));
            }
            return false;
        }
    }
    
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
    

}
