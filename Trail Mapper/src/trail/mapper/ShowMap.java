package trail.mapper;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class ShowMap extends MapActivity {

	private MyLocationOverlay myLocationOverlay;
	private MapView mapView;
	
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);
        
        // Get the Map View and enable zoom controls
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        // Create an overlay that shows current location
        myLocationOverlay = new FixedMyLocation(this, mapView);
        
        // Add overlay to Map View
        mapView.getOverlays().add(myLocationOverlay);
        mapView.postInvalidate();
        
        // Call to zoom to location
     	zoomToMyLocation();
    }
    
    protected void onResume() {
    	super.onResume();
    	myLocationOverlay.enableMyLocation();
    }
    
    protected void onPause() {
    	super.onPause();
    	myLocationOverlay.disableMyLocation();
    }

    /**
	 * This method zooms to the user's location with a zoom level of 10.
	 */
	private void zoomToMyLocation() {
		GeoPoint myLocationGeoPoint = myLocationOverlay.getMyLocation();
		if(myLocationGeoPoint != null) {
			mapView.getController().animateTo(myLocationGeoPoint);
			mapView.getController().setZoom(10);
		}
		else {
			Toast.makeText(this, "Cannot determine location", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
