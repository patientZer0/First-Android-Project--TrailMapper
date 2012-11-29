package trail.mapper;

import java.sql.Date;
import java.util.*;
import android.location.Location;

public class Output {

//returns elevation of first point
  public Double startingElevation(ArrayList<Location> l) {
    return l.get(0).getAltitude();
  }
	
//returns elevation of last point
  public Double endingElevation(ArrayList<Location> l) {
    return l.get(l.size()-1).getAltitude();
  }
	
//returns difference in starting and ending elevations
  public Double elevationChange(ArrayList<Location> l) {
    return startingElevation(l)-endingElevation(l);
  }
	
//returns lowest elevation
  public Double minElevation(ArrayList<Location> l) {
    Double min = l.get(0).getAltitude();
      for (int i=1; i<l.size(); i++) {
	if (l.get(i).getAltitude() < min ) {
	   min = l.get(i).getAltitude();
	   }
      }
    return min;
  }
	
	
//returns highest elevation
  public Double maxElevation(ArrayList<Location> l) {
    Double max = l.get(0).getAltitude();
      for (int i=1; i<l.size(); i++) {
	if (l.get(i).getAltitude() > max ) {
	   max = l.get(i).getAltitude();
	   }
      }
    return max;
  }
	
// returns total distance in km
  public Double totalDistance(ArrayList<Location> l) {
    Double dis = 0.0;
    for (int i=0; i<l.size()-1; i++) {
	dis = dis + haversine(l.get(i), l.get(i+1));
    }
    return dis;
  }
	
//distance functions in different units
  public Double distanceInMiles(ArrayList<Location> l) {
    return totalDistance(l) / 1.609344;
  }
	
  public Double distanceInMeters(ArrayList<Location> l) {
    return totalDistance(l) * 1000;
  }
	
  public Double distanceInFeet(ArrayList<Location> l) {
    return totalDistance(l) * 3280.84;
  }
	
// returns distance in km between 2 points using the haversine formula
  public Double haversine(Location loc1, Location loc2) {
    Double latitude1 = loc1.getLatitude();
    Double latitude2 = loc2.getLatitude();
    Double longitude1 = loc1.getLongitude();
    Double longitude2 = loc2.getLongitude();
    Double dlat = Math.toRadians(latitude1-latitude2);
    Double dlong = Math.toRadians(longitude1-longitude2);
    Double lat1 = Math.toRadians(latitude1);
    Double lat2 = Math.toRadians(latitude2);
				
    Double a = Math.pow(Math.sin(dlat/2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlong/2), 2);
    Double b = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    Double c = 6371 * b;
		
    return c;
  }
	
//returns speed in miles per hour
  public Double mph(ArrayList<Location> l, Date start, Date end) {
    long startTime = start.getTime();
    long endTime = end.getTime();
    long diff = endTime - startTime;
    long millisecondsInHour = 1000 * 60 * 60;
    Double numberOfHours = (double) diff / millisecondsInHour;
    Double speed = distanceInMiles(l) / numberOfHours;
		
    return speed;
  }
	
//speed in km per hour
  public Double kmph(ArrayList<Location> l, Date start, Date end) {
    return mph(l, start, end) * 1.609344;
  }
	
//returns outputs given unit choice
  public String output(ArrayList<Location> l, Date start, Date end, Boolean metric) {
    String distance, distanceSecondary, distanceData, speed, speedData, elevationData;
		
    if (metric) {
      distance = totalDistance(l).toString();
      distanceSecondary = distanceInMeters(l).toString();
      distanceData = "Distance Traveled: " + distance + " km (" + distanceSecondary + " m)";
      speed = kmph(l, start, end).toString();
      speedData = "Speed: " + speed + " km/hr";
    } else {
      distance = distanceInMiles(l).toString();
      distanceSecondary = distanceInFeet(l).toString();
      distanceData = "Distance Traveled: " + distance + " mi (" + distanceSecondary + " ft)";
      speed = mph(l, start, end).toString();
      speedData = "Speed: " + speed + "mi/hr";
    }
    elevationData = "Elevation:\nLowest: " + minElevation(l).toString() + " Highest: " + maxElevation(l).toString()
			+ "\nChange: " + elevationChange(l).toString();
    String op = elevationData + "\n\n" + distanceData + "\n\n" + speedData;
		
    return op;
  }
	
}
