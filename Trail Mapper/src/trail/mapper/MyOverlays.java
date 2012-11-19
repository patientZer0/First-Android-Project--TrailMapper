package trail.mapper;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class MyOverlays extends ItemizedOverlay<OverlayItem> {
	
	private Context context;
	private int textSize;
	
	private List<OverlayItem> overlays;
	private List<GeoPoint> geoPointsArray;
	
	public MyOverlays(Context context, Drawable defaultMarker, int textSize) {
		super(boundCenterBottom(defaultMarker));
		this.context = context;
		this.textSize = textSize;
		
		geoPointsArray = new ArrayList<GeoPoint>();
		overlays = new ArrayList<OverlayItem>();
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow){
		super.draw(canvas, mapView, shadow);

		if (shadow == false) {
			for (int i = 0; i < overlays.size(); i++) {
				OverlayItem item = overlays.get(i);
				GeoPoint gPoint = item.getPoint();
				
				geoPointsArray.add(gPoint);
				
				Point pxPoint = new Point();

				mapView.getProjection().toPixels(gPoint, pxPoint);

				if (i == 0) {
					Paint painter = new Paint();
					painter.setColor(Color.GREEN);
					painter.setStrokeWidth(4);

					canvas.drawCircle(pxPoint.x, pxPoint.y, 5, painter);

					Paint pText = new Paint();
					pText.setTextAlign(Paint.Align.CENTER);
					pText.setTextSize(textSize);
					pText.setARGB(150, 0, 0, 0);

					canvas.drawText(item.getTitle(), pxPoint.x, pxPoint.y+textSize, pText);
				} else {
					OverlayItem gpLast = overlays.get(i-1);
					GeoPoint gPointLast = gpLast.getPoint();
					Point pxPointLast = new Point();
					mapView.getProjection().toPixels(gPointLast, pxPointLast);

					Paint painter = new Paint();
					painter.setColor(Color.GREEN);
					painter.setStrokeWidth(4);

					//canvas.drawCircle(pxPoint.x, pxPoint.y, 5, blue);
					canvas.drawLine(pxPoint.x, pxPoint.y, pxPointLast.x, pxPointLast.y, painter);
				}
			}
		}
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return overlays.get(i);
	}
	
	@Override
	public int size() {
		return overlays.size();
	}
	
	public void addOverlay(OverlayItem overlay) {
		overlays.add(overlay);
		populate();
	}
	
	protected boolean onTap(int index) {
		OverlayItem item = overlays.get(index);
		Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("This will end the activity");
		builder.setCancelable(true);
		builder.setPositiveButton("Affirmative", new OkOnClickListener());
		builder.setNegativeButton("Negative", new CancelOnClickListener());
		AlertDialog dialog = builder.create();
		dialog.show();
		return true;
	};
	
	private final class CancelOnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int which) {
			Toast.makeText(context, "You clicked no", Toast.LENGTH_LONG).show();
		}
	}
	
	private final class OkOnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int which) {
			Toast.makeText(context, "You clicked yes", Toast.LENGTH_LONG).show();
		}
	}
}
