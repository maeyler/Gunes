package mae.vakit;

public class Method { 
    //http://praytimes.org/calculation/#Fajr_and_Isha
    //http://www.tavaf.com/Vakit%20Hesaplama.htm#Temkin
    final static float[] T = { 0, 0, 0, 0, 0, 0};
    final static float[] D = {-2,-6, 7, 4, 8, 2};
    final static Method[] DEFAULTS 
    = { new Method("Diyanet", 18, 17, D), new Method("Temkinsiz", 18, 17) }; 
    final static Method DEFAULT = DEFAULTS[0];
    
    final String name; 
    final float imsakAcisi, yatsiAcisi;
    final float[] temkin; 
    public Method(String s, float i, float y) { this(s, i, y, T); }
    public Method(String s, float i, float y, float[] t) { 
        name = s; imsakAcisi = i; yatsiAcisi = y;  temkin = t;
    }
    public boolean equals(Object x) {
        if (!(x instanceof Method)) return false;
        else return name.equals(((Method)x).name); 
    }
    public int hashCode() { return name.hashCode(); }
    public String toString() { return name; }
    
    static float[] convert(String[] a) {
        float[] t = new float[6];
        for (int i=0; i<6; i++)
            t[i] = Float.parseFloat(a[i+3]);
        return t; //Arrays.copyOfRange(a, 3, 9)
    }
    public static Method toMethod(String s) {
        String[] a = s.split(" ");
        try { // protection against errors in data
            float i = Float.parseFloat(a[1]);
            float y = Float.parseFloat(a[2]);
            float[] t = (a.length==9? convert(a) : T);
            Method m = new Method(a[0], i, y, t);
            return m;
        } catch (RuntimeException x) {
            System.out.println(x);
            return null;
        }
    }
}
