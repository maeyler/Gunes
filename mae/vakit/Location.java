package mae.vakit;

import java.util.TimeZone;

public class Location {

    final static TimeZone TR = TimeZone.getTimeZone("Turkey");
    final static Location[] DEFAULTS 
    = { new Location("Istanbul", 41, 29, TR), new Location("Ankara", 39.9f, 32.9f, TR),
        new Location("Mekke", 21.4f, 39.8f), new Location("Ekvator", 0, 0) };
    final static Location DEFAULT = DEFAULTS[0];

    String name; float latitude, longitude;  TimeZone zone;
    
    public Location(String s, float x, float y) { this(s, x, y, null); }
    public Location(String s, float x, float y, TimeZone z) { 
        name = s; latitude = x; longitude = y;  
        if (z==null ||  z.getID().equals("GMT")) {
            int h = Math.round(y/15);
            String sign = (y<0? "-" : "+");
            z = TimeZone.getTimeZone("GMT"+sign+h);
        }
        zone = z;
    }
    boolean filter() {
        return (Math.abs(latitude) < 55); 
    }
    public float hourOffset() { 
        return zone.getRawOffset()/3600f/1000; 
    }
    public boolean equals(Object x) {
        if (!(x instanceof Location)) return false;
        else return name.equals(((Location)x).name); 
    }
    public int hashCode() { return name.hashCode(); }
    public String toString() { return name; }
    
    public static Location toLocation(String t) {
        String[] a = t.split(" ");
        try { // protection against errors in data
            float x = Float.parseFloat(a[1]);
            float y = Float.parseFloat(a[2]);
            TimeZone z = a.length<4? null : TimeZone.getTimeZone(a[3]);
            Location loc = new Location(a[0], x, y, z);
            if (!loc.filter()) {
                String s = "Location not accepted: "+x+", "+y;
                throw new RuntimeException(s);
            }
            return loc;
        } catch (RuntimeException x) {
            System.out.println(x);
            return null;
        }
    }
}
