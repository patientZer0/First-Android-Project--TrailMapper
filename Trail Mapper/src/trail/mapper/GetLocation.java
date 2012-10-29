package trail.mapper;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class GetLocation extends Activity {

	private TextView mLatLng;
	private TextView mAddress;
	private LocationManager mLocationManager;
	private Handler mHandler;
	private boolean mGeocoderAvailable;
	
	// UI handler codes.
    private static final int UPDATE_ADDRESS = 1;
    private static final int UPDATE_LATLNG = 2;

    private static final int TEN_SECONDS = 10000;
    private static final int TEN_METERS = 10;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);
        
        Intent intent = getIntent();
        
     // Restore app state after rotation
        mLatLng = (TextView) findViewById(R.id.latlng);
        mAddress = (TextView) findViewById(R.id.address);
        
        mGeocoderAvailable =
        		Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent();
        		
        // Handler for updating text fields on the UI like the lat/long and address
        mHandler = new Handler() {
        	public void handleMessage(Message msg) {
        		switch (msg.what) {
	        		case UPDATE_ADDRESS:
	        			mAddress.setText((String) msg.obj);
	        			break;
	        		case UPDATE_LATLNG:
	        			mLatLng.setText((String) msg.obj);
	        			break;
        		}
        	}
        };
        
        //get a reference to the LocationManager object.
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    protected void onStart(){
    	super.onStart();
    }
    
    protected void onResume() {
    	super.onResume();
    	setup();
    }
    
    // Stop receiving location updates whenever Activity becomes invisible.
    @Override
    protected void onStop() {
    	super.onStop();
    	mLocationManager.removeUpdates(listener);
    }
    
    private void setup() {
    	Location gpsLocation = null;
    	Location networkLocation = null;
    	mLocationManager.removeUpdates(listener);
    	mLatLng.setText(R.string.unknown);
    	mAddress.setText(R.string.unknown);
    	
    	// Request updates from GPS and Network Providers
    	gpsLocation = requestUpdatesFromProvider(
    			LocationManager.GPS_PROVIDER, R.string.not_support_gps);
    	networkLocation = requestUpdatesFromProvider(
    			LocationManager.NETWORK_PROVIDER, R.string.not_support_network);
    	
    	// Compare locations and return more accurate location
    	if (gpsLocation != null && networkLocation != null) {
    		updateUILocation(getBetterLocation(gpsLocation, networkLocation));
    	} else if (gpsLocation != null) {
    		updateUILocation(gpsLocation);
    	} else if (networkLocation != null) {
    		updateUILocation(networkLocation);
    	}
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
    	if (mLocationManager.isProviderEnabled(provider)) {
    		mLocationManager.requestLocationUpdates(provider, TEN_SECONDS, TEN_METERS, listener);
    		location = mLocationManager.getLastKnownLocation(provider);
    	} else {
    		Toast.makeText(this, errorResId, Toast.LENGTH_LONG).show();
    	}
    	return location;
    }
    
    private void doReverseGeocoding(Location location) {
    	(new ReverseGeocodingTask(this)).execute(new Location[] {location});
    }
    
    private void updateUILocation(Location location) {
    	// Updates UI with new location
    	Message.obtain(mHandler,
    			UPDATE_LATLNG,
    			location.getLatitude() + ", " + location.getLongitude()).sendToTarget();
    	
    	// Bypass reverse-geocoding only if the Geocoder service is available on the device
    	if (mGeocoderAvailable) doReverseGeocoding(location);
    }
    
    private final LocationListener listener = new LocationListener() {
    	
    	public void onLocationChanged(Location location) {
    		// A new location update is received. Do something useful with it. Update the UI with
    		// the location update.
    		updateUILocation(location);
    	}
    	
    	public void onProviderDisabled(String provider) {
    		
    	}
    	
    	public void onProviderEnabled(String provider) {
    		
    	}
    	
    	public void onStatusChanged(String provider, int status, Bundle extras) {
    		
    	}
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
    
    // AsyncTask encapsulating the reverse-geocoding API
    private class ReverseGeocodingTask extends AsyncTask<Location, Void, Void> {
    	Context mContext;
    	
    	public ReverseGeocodingTask(Context context) {
    		super();
    		mContext = context;
    	}
    	
    	@Override
    	protected Void doInBackground(Location... params) {
    		Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
    		
    	Location loc = params[0];
    		List<Address> addresses = null;
    		try {
    			addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
    		} catch (IOException e) {
    			e.printStackTrace();
    			// Update address field with the exception.
    			Message.obtain(mHandler, UPDATE_ADDRESS, e.toString()).sendToTarget();
    		}
    		if (addresses != null && addresses.size() > 0) {
    			Address address = addresses.get(0);
    			// Format the first line of address (if available), city, and country name
    			String addressText = String.format("%s, %s, %s",
    					address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
    					address.getLocality(),
    					address.getCountryName());
    			// Update address field on UI
    			Message.obtain(mHandler, UPDATE_ADDRESS, addressText).sendToTarget();
    		}
    		return null;
    	}
    }
}
