package mae.vakit;

//calculates Sunrise, Midday, Sunset at a given location
//http://praytimes.org/calculation/#Calculating_Prayer_Times

public class Sunrise extends SunPosition {

    Location loc = Location.DEFAULT; 
    double noonM; //minutes since 1/1/1970
    float noonF, sunset, C0, C1;
    String str;
    
    public void setLocation(Location a) { 
        if (loc.equals(a)) return;
        loc = a; date.setTimeZone(a.zone);
        setDate(day, true);
    }
    public void setLocation(String s, float x, float y) { 
        setLocation(new Location(s, x, y));
    }
    void setTime(double m) {
        date.setTime((long)((noonM + m + 0.5)*60*1000));
    }
    public void setDate(double d, boolean report) {
        super.setDate(d, false);
        C0 = cos(loc.latitude)*cos(declin);
        C1 = sin(loc.latitude)*sin(declin);
        int zone = Math.round(loc.hourOffset()); 
        noonF = zone*60 - eqTime - 4*loc.longitude; //noon in minutes 
        noonM = ((day + Timer.J2000)*24*60 + (12-zone)*60 + noonF); 
        sunset = timeOf(1); //sunset-noon difference
        setTime(-sunset);  str = date.ddMMyyyy() +" "+ date.HHmm();
        setTime(0);  // noon
        if (report) report();
    }
    public void report() {
        System.out.print(date.ddMMyyyy());  
        System.out.printf(" %6.1f  %6.1f  %6s", eqTime, declin, date.HHmm()); 
        setTime(sunset);  //
        System.out.printf("  %6s %n", date.HHmm());
    }
    float timeOf(float a) { 
        //when sun reaches an angle a below the horizon -- in minutes
        return 4*arccos((-sin(a) - C1)/C0);
    }
    float altitude(float m) {
        return arcsin(C0*cos(m/4) + C1);
    }
    String header() { 
        return loc+"\nDay          EqT   Declin   Midday  Sunset";
    }
    String plotTitle() { return loc+" -- Sunrise, Midday & Sunset"; }
    int numVars() { return 3; }
    void putData(double[][] X, int i) {
            X[1][i] = noonF - sunset; 
            X[2][i] = noonF; 
            X[3][i] = noonF + sunset; 
    }
    public String toString() { return str; }
    
    public static void main(String[] args) {
        long d = 5478;  //Jan 1, 2015
        SunPosition p = new Sunrise();
        p.report(d, 14, d+366);  //p.plot(d);
    }
}
