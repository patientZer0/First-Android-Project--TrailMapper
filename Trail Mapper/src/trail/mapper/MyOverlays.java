package trail.mapper;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MyOverlays extends Overlay {
	
	private Context context;
	
	private List<OverlayItem> overlays;
	
	public MyOverlays(Context context) {
		this.context = context;
		overlays = new ArrayList<OverlayItem>();
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow){
		super.draw(canvas, mapView, shadow);

		if (shadow == false) {
			for (int i = 0; i < overlays.size(); i++) {
				OverlayItem item = overlays.get(i);
				GeoPoint gPoint = item.getPoint();
				
				Point pxPoint = new Point();

				mapView.getProjection().toPixels(gPoint, pxPoint);

				if (i == 0) {
					Paint painter = new Paint();
					painter.setColor(Color.GREEN);
					painter.setStrokeWidth(4);

					canvas.drawCircle(pxPoint.x, pxPoint.y, 5, painter);
				} else {
					OverlayItem gpLast = overlays.get(i-1);
					GeoPoint gPointLast = gpLast.getPoint();
					Point pxPointLast = new Point();
					mapView.getProjection().toPixels(gPointLast, pxPointLast);

					Paint painter = new Paint();
					painter.setColor(Color.GREEN);
					painter.setStrokeWidth(4);
					
					canvas.drawLine(pxPoint.x, pxPoint.y, pxPointLast.x, pxPointLast.y, painter);
				}
			}
		}
	}
	
	protected OverlayItem createItem(int i) {
		return overlays.get(i);
	}
	
	public int size() {
		return overlays.size();
	}
	
	public void addOverlay(OverlayItem overlay) {
		overlays.add(overlay);
	}
}
