package trail.mapper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

@SuppressLint("HandlerLeak")
public class ShowMap extends MapActivity {
	
	private Handler handler;
	private LocationManager locationManager;
	private MapController mapController;
	private MapView mapView;
	private MyLocationOverlay myLocationOverlay;
	private MyOverlays myOverlay;
	private Output outDistance;
	private TextView mDistance;
	private TextView mElevation;
	private TextView mLatLng;
	private TextView mTimer;
	
	private List<Overlay> mapOverlays;
	private List<GeoPoint> geoPointsArray;
	private ArrayList<Location> locationPointsArray;

    private static final int UPDATE_LATLNG = 1;
    private static final int UPDATE_DIST = 2;
    private static final int UPDATE_ELE = 3;
    private static final int UPDATE_SPEED = 4;
    private static final int TEN_SECONDS = 10000;
	private static final int TEN_METERS = 10;
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);
        
        geoPointsArray = new ArrayList<GeoPoint>();
        locationPointsArray = new ArrayList<Location>();
  
        outDistance = new Output();
        
        mLatLng = (TextView) findViewById(R.id.latlng);
        mDistance = (TextView) findViewById(R.id.distance);
        mElevation = (TextView) findViewById(R.id.elevation);
        mTimer = (TextView) findViewById(R.id.speed);
        
        // Get the Map View and configure
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.setSatellite(true);
        mapController = mapView.getController();
        mapController.setZoom(20);

        mapOverlays = mapView.getOverlays();
        
        // Reference LocationManager object
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        // Handler for updating text fields on the UI
        handler = new Handler() {
        	public void handleMessage(Message msg) {
        		switch (msg.what) {
	        		case UPDATE_LATLNG:
	        			mLatLng.setText((String) msg.obj);
	        			break;
	        		case UPDATE_DIST:
	        			mDistance.setText((String) msg.obj);
	        			break;
	        		case UPDATE_ELE:
	        			mElevation.setText((String) msg.obj);
	        			break;
	        		case UPDATE_SPEED:
	        			mTimer.setText((String) msg.obj);
	        			break;
        		}
        	}
        };
        
        // Create an overlay that shows current location
        myLocationOverlay = new FixedMyLocation(this, mapView);
        mapView.getOverlays().add(myLocationOverlay);
        
        myOverlay = new MyOverlays(this);
        
        mapOverlays.clear();
        mapOverlays.add(myLocationOverlay);
        mapView.invalidate();
    }
    
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
    protected void onResume() {
    	super.onResume();
    	setup();
    }
    
	@Override
    protected void onPause() {
    	super.onPause();
    	locationManager.removeUpdates(listener);
    	myLocationOverlay.disableMyLocation();
    	myLocationOverlay.disableCompass();
    }
	
	@Override
    protected void onStop() {
    	super.onStop();
    	locationManager.removeUpdates(listener);
    }
    
	// Initial app setup
    private void setup() {
    	Location gpsLocation = null;
    	Location networkLocation = null;
    	
    	locationManager.removeUpdates(listener);
    	
    	myLocationOverlay.enableMyLocation();
    	myLocationOverlay.enableCompass();
    	
    	// Set UI texts to unknown if no current information is available
    	mLatLng.setText(R.string.unknown);
    	mDistance.setText(R.string.unknown);
    	mElevation.setText(R.string.unknown);
    	mTimer.setText(R.string.unknown);
    	
    	// Request Update from Providers
       	gpsLocation = requestUpdatesFromProvider(LocationManager.GPS_PROVIDER, 
       			R.string.not_support_gps);
    	networkLocation = requestUpdatesFromProvider(LocationManager.NETWORK_PROVIDER, 
    			R.string.not_support_network);
    	
    	if (gpsLocation != null && networkLocation != null) {
    		getBetterLocation(gpsLocation, networkLocation);
    		if (getBetterLocation(gpsLocation, networkLocation) == gpsLocation) {
    			locationPointsArray.add(gpsLocation);
    			updateUILocation(gpsLocation);
    		} else if (getBetterLocation(gpsLocation, networkLocation) == networkLocation){
    			locationPointsArray.add(networkLocation);
    			updateUILocation(networkLocation);
    		}
    	} else if (gpsLocation != null) {
    		locationPointsArray.add(gpsLocation);
    		updateUILocation(gpsLocation);
    	} else if (networkLocation != null) {
    		locationPointsArray.add(networkLocation);
    		updateUILocation(networkLocation);
    	}
    }
    
    public void onClick(View v) {
		onCreateDialog().show();
	}
    
    /**
     * Method for when user presses the "pause" button. Creates a dialog to ask the user what they
     * would like to do. Either Save/Quit/or Resume. 
     */   
    protected Dialog onCreateDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
	    		builder.setTitle(R.string.question);
		    	builder.setPositiveButton(R.string.saver, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(getApplicationContext(),
                                "I'm sorry I can't do that yet.", Toast.LENGTH_SHORT)
                                .show();
					}
				});
		    	
		    	builder.setNeutralButton(R.string.resume, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(getApplicationContext(),
                                "Fun time resumed!", Toast.LENGTH_SHORT)
                                .show();
						dialog.dismiss();
					}
				});
		    	
		    	builder.setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(getApplicationContext(),
                                "Quitter!", Toast.LENGTH_SHORT)
                                .show();
						ShowMap.this.finish();
					}
				});

    	return builder.create();
    }
    
    /**
     * Method to register location updates with a desired location provider. If the requested
     * provider is not available on the device, the app displays a Toast with a message referenced
     * by a resource id.
     *
     * @param provider Name of the requested provider.
     * @param errorResId Resource id for the string message to be displayed if the provider does
     *                   not exist on the device.
     * @return A previously returned {@link android.location.Location} from the requested provider,
     *         if exists.
     */
    private Location requestUpdatesFromProvider(final String provider, final int errorResId) {
    	Location location = null;
    	if (locationManager.isProviderEnabled(provider)) {
    		locationManager.requestLocationUpdates(provider, TEN_SECONDS, TEN_METERS, listener);
    		location = locationManager.getLastKnownLocation(provider);
    	} else {
    		Toast.makeText(this, errorResId, Toast.LENGTH_LONG).show();
    	}
    	return location;
    }
    
    private void updateUILocation(Location location) {
    	
    	DecimalFormat locFormatter = new DecimalFormat("00.######");
    	DecimalFormat distFormatter = new DecimalFormat("0"); 
    	DecimalFormat speedFormatter = new DecimalFormat("0.##");
    	
    	double distDub = outDistance.distanceInFeet(locationPointsArray);
    	int distInt = (int)distDub;
    	
    	String elevation = Integer.toString((int)(location.getAltitude() * 3.28084));
    	String distance = distFormatter.format(distInt);
    	float speed = location.getSpeed() * (float)2.23694;
    	
    	// Updates UI with new location
    	Message.obtain(handler,
    			UPDATE_LATLNG,
    			locFormatter.format(location.getLatitude()) + ", " +
    			locFormatter.format(location.getLongitude())).sendToTarget();
    	
    	// Updates UI with total distance traveled
    	Message.obtain(handler,
    			UPDATE_DIST,
    			distance + " " + "ft").sendToTarget();
    	
    	// Updates UI with current elevation
    	Message.obtain(handler,
    			UPDATE_ELE,
    			elevation + " " + "ft").sendToTarget();
    	
    	// Updates UI with current speed
    	Message.obtain(handler,
    			UPDATE_SPEED,
    			speedFormatter.format(speed) + " " + "mph").sendToTarget();
    }
    
    private final LocationListener listener = new LocationListener() {
    	
    	public void onLocationChanged(Location location) {
    		int lat = (int) (location.getLatitude() * 1E6);
    		int lng = (int) (location.getLongitude() * 1E6);
    		GeoPoint point = new GeoPoint(lat, lng);
    		
    		locationPointsArray.add(location);

    		geoPointsArray.add(point);
    		
    		mapController.animateTo(point);
    		
    		OverlayItem overlayItem = new OverlayItem(point, "", "");
    		
    		myOverlay.addOverlay(overlayItem);
    		
    		// Create the path
    		if (myOverlay.size() > 0) {
    			mapView.getOverlays().add(myLocationOverlay);
    			mapView.getOverlays().add(myOverlay);
    		}
    		
    		// Update the UI with the new location information
    		updateUILocation(location);
    		
    		// This toast is for debug purposes only, comment out or delete when final app deploys
    		Toast.makeText(getApplicationContext(), "The Location Points Array is this big: " +
        			locationPointsArray.size(), Toast.LENGTH_SHORT).show();
    	}
    	
    	public void onProviderDisabled(String provider) {}
    	
    	public void onProviderEnabled(String provider) {}
    	
    	public void onStatusChanged(String provider, int status, Bundle extras) {}
    };
    
    /** Determines whether one Location reading is better than the current Location fix.
     * Code taken from
     * http://developer.android.com/guide/topics/location/obtaining-user-location.html
     *
     * @param newLocation  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new
     *        one
     * @return The better Location object based on recency and accuracy.
     */
    protected Location getBetterLocation(Location newLocation, Location currentBestLocation) {
    	if (currentBestLocation == null) {
    		// A new location is always better than no location
    		return newLocation;
    	}
    	
    	// Check whether the new location fix is newer or older
    	long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
    	boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
    	boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
    	boolean isNewer = timeDelta > 0;
    	
    	// If it's been more than two minutes since the current location, use the new location
    	// because the user has likely moved.
    	if (isSignificantlyNewer) {
    		return newLocation;
    	// If the new location is more than two minutes older, it must be worse
    	} else if (isSignificantlyOlder) {
    		return currentBestLocation;
    	}
    	
    	// Check whether the new location fix is more or less accurate
    	int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
    	boolean isLessAccurate = accuracyDelta > 0;
    	boolean isMoreAccurate = accuracyDelta < 0;
    	boolean isSignificantlyLessAccurate = accuracyDelta > 200;
    	
    	// check if the old and new location are from the same provider
    	boolean isFromSameProvider = isSameProvider(newLocation.getProvider(), 
    			currentBestLocation.getProvider());
    	
    	// Determine location quality using a combination of timeliness and accuracy
    	if (isMoreAccurate) {
    		return newLocation;
    	} else if (isNewer && !isLessAccurate) {
    		return newLocation;
    	} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
    		return newLocation;
    	}
    	return currentBestLocation;
    }
    
    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
    	if (provider1 == null) {
    		return provider2 == null;
    	}
    	return provider1.equals(provider2);
    }
}
