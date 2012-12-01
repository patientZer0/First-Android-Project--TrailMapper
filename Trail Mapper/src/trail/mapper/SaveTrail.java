package trail.mapper;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class SaveTrail extends Activity {

	Bundle extras;
	String myTrail;
	
	ArrayList<Integer> latArray;
	ArrayList<Integer> lngArray;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.save_trail);
		
		extras = getIntent().getExtras();
		
		//myTrail = extras.getString("trailname");
		//myTrail = "This is some text";
		
		latArray = new ArrayList<Integer>();
		lngArray = new ArrayList<Integer>();
		
		latArray = extras.getIntegerArrayList("latitude");
		lngArray = extras.getIntegerArrayList("longitude");
		
		for (int i = 0; i < latArray.size()-1; i++) {
		Toast.makeText(getApplicationContext(),
               "My Trail name is: " + myTrail + 
               " Some lat " + latArray.get(i) / 1E6 + " : " + "Some long " + lngArray.get(i) / 1E6, Toast.LENGTH_LONG)
                .show();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
}
