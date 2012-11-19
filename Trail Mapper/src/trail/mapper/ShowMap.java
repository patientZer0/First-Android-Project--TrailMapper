package trail.mapper;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
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

	protected PowerManager.WakeLock wakeLock;
	
	private Drawable drawable;
	private TextView mLatLng;
	private MyLocationOverlay myLocationOverlay;
	private MapView mapView;
	private MyOverlays itemizedOverlay;
	private LocationManager locationManager;
	private Handler handler;
	private MapController mapController;
	
	private boolean recording;
	
	private List<Overlay> mapOverlays;
	private List<GeoPoint> geoPointsArray;
	private List<Location> locationPointsArray;

    private static final int UPDATE_LATLNG = 2;
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
  
        mLatLng = (TextView) findViewById(R.id.latlng);
        
        // Get the Map View and configure
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.setSatellite(true);
        mapController = mapView.getController();
        mapController.setZoom(20);

        mapOverlays = mapView.getOverlays();
        
        // Reference LocationManager object
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        // Handler for updating text fields on the UI like the lat/long and address
        handler = new Handler() {
        	public void handleMessage(Message msg) {
        		switch (msg.what) {
	        		case UPDATE_LATLNG:
	        			mLatLng.setText((String) msg.obj);
	        			break;
        		}
        	}
        };
        
        // Create an overlay that shows current location
        myLocationOverlay = new MyLocationOverlay(this, mapView);
        mapView.getOverlays().add(myLocationOverlay);
        
        drawable = this.getResources().getDrawable(R.drawable.ic_maps_indicator_current_position);
        itemizedOverlay = new MyOverlays(this, drawable, 30);
        
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
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		
		for (int i = 0; i < geoPointsArray.size(); i++) {
			
			//savedInstanceState.putIntegerArrayList(null, geoPointsArray.get(i));
		}
	}
	
	@Override
    protected void onStop() {
    	super.onStop();
    	locationManager.removeUpdates(listener);
    }
    
    private void setup() {
    	Location gpsLocation = null;
    	
    	locationManager.removeUpdates(listener);
    	myLocationOverlay.enableMyLocation();
    	myLocationOverlay.enableCompass();
    	
    	mLatLng.setText(R.string.unknown);
    	
    	// Request Update from GPS and Network Providers
    	gpsLocation = requestUpdatesFromProvider(
    			LocationManager.GPS_PROVIDER, R.string.not_support_gps);
    	
    	updateUILocation(gpsLocation);
    }
    
    
    /**
     * Method to register location updates with a desired location provider.  If the requested
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
    	// Updates UI with new location
    	Message.obtain(handler,
    			UPDATE_LATLNG,
    			location.getLatitude() + ", " + location.getLongitude()).sendToTarget();
    }
    
    private final LocationListener listener = new LocationListener() {
    	
    	public void onLocationChanged(Location location) {
    		int lat = (int) (location.getLatitude() * 1E6);
    		int lng = (int) (location.getLongitude() * 1E6);
    		GeoPoint point = new GeoPoint(lat, lng);
    	
    		mapController.animateTo(point);
    		
    		OverlayItem overlayItem = new OverlayItem(point, "", "");
    		
    		itemizedOverlay.addOverlay(overlayItem);
    		
    		//updateUILocation(location);	
    		if (itemizedOverlay.size() > 0) {
    			mapView.getOverlays().add(myLocationOverlay);
    			mapView.getOverlays().add(itemizedOverlay);
	    		if (recording) {			
	    			locationPointsArray.add(location);
	    			geoPointsArray.add(point);
	    		}
    		}
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
