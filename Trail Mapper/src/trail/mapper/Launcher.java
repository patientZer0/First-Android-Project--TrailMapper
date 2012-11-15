package trail.mapper;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

public class Launcher extends FragmentActivity {

	private Button showMap;

	@SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Receive location updates
        showMap = (Button) findViewById(R.id.mapper);      
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	// Check if the GPS setting is currently enabled on the device.
    	LocationManager locationManager =
    			(LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    	
    	if (!gpsEnabled) {
    		// Alert dialog to enable GPS
    		new EnableGpsDialogFragment().show(getSupportFragmentManager(), "enableGpsDialog");
    	}
    }
    
    // Method to launch Settings
    private void enableLocationSettings() {
    	Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    	startActivity(settingsIntent);
    }
    
    public void showMap(View view) {
    	Intent showMapIntent = new Intent(this, ShowMap.class);
    	startActivity(showMapIntent);
    }
    
    /**
     * Dialog to prompt users to enable GPS on the device
     */
    private class EnableGpsDialogFragment extends DialogFragment {
    	
    	@Override
    	public Dialog onCreateDialog(Bundle savedInstanceState) {
    		return new AlertDialog.Builder(getActivity())
    				.setTitle(R.string.enable_gps)
    				.setMessage(R.string.enable_gps_dialog)
    				.setPositiveButton(R.string.enable_gps, new DialogInterface.OnClickListener() {						
						public void onClick(DialogInterface dialog, int which) {
							enableLocationSettings();							
						}
					})
					.create();
    	}
    }
}
