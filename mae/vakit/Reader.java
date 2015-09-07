package mae.vakit;

import java.io.*;
import java.util.Set;
import java.util.LinkedHashSet;

public class Reader {

    String[] dayA;  Location[] locA;  Method[] metA;
    
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
        if (rdr.dayA == null) return;
        for (String s : rdr.dayA) 
            System.out.println(s);
        for (Location a : rdr.locA) 
            System.out.println(a+" "+a.zone.getID());
    }
}
