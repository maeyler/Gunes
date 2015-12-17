package mae.vakit;

import java.io.*;
import java.util.Set;
import java.util.LinkedHashSet;

public class Reader {

    String[] dayA = { "Bugün", "21/03/2015", "21/06/2015", "22/09/2015", "21/12/2015" };
    Location[] locA = Location.DEFAULTS.clone();  
    Method[] metA = Method.DEFAULTS.clone();
    
    public String[] days() { return dayA; }
    public Location[] locations() { return locA; }
    public Method[] methods() { return metA; }
    public Reader(String fn) {
        try {
            readFile(new FileReader(fn));
        } catch (IOException x) {
            System.out.println(x); 
        }
    }
    public Reader(InputStream is) {
        try {
            readFile(new InputStreamReader(is));
        } catch (IOException x) {
            System.out.println(x); 
        }
    }
    void readDay(BufferedReader in) throws IOException {
            Set<String> a = new LinkedHashSet<String>();
            a.add("Bugün");  // first item is today
            String s = in.readLine(); // skip first line
            while (true) {
                s = in.readLine();
                if (s == null || s.length() == 0) break;
                a.add(s); 
            }
            if (a.size() == 1) return;
            dayA = new String[a.size()];
            a.toArray(dayA); 
    }
    void readLoc(BufferedReader in) throws IOException {
            Set<Location> a = new LinkedHashSet<Location>();
            String s = in.readLine(); // skip first line
            while (true) {
                s = in.readLine();
                if (s == null || s.length() == 0) break;
                Location d = Location.toLocation(s);
                if (d != null) a.add(d); 
            }
            if (a.size() == 0) return;
            locA = new Location[a.size()];
            a.toArray(locA); 
    }
    void readMet(BufferedReader in) throws IOException {
            Set<Method> a = new LinkedHashSet<Method>();
            String s = in.readLine(); // skip first line
            while (true) {
                s = in.readLine();
                if (s == null || s.length() == 0) break;
                Method m = Method.toMethod(s);
                if (m != null) a.add(m); 
            }
            if (a.size() == 0) return;
            metA = new Method[a.size()];
            a.toArray(metA); 
    }
    void readFile(java.io.Reader r) throws IOException {
            BufferedReader in = new BufferedReader(r);
            readDay(in); readLoc(in); readMet(in);
            in.close();
    }
    public static void main(String[] args) {
        Reader rdr = new Reader("Vakit.txt");
        //if (rdr.dayA == null) return;
        for (String s : rdr.days()) 
            System.out.println(s);
        for (Location a : rdr.locations()) 
            System.out.println(a+" "+a.zone.getID());
        for (Method m : rdr.methods()) 
            System.out.println(m);
    }
}
