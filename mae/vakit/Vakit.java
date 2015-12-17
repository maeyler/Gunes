package mae.vakit;

//verilen bir gün ve yerdeki namaz vakitlerini seçilen metotla hesaplar
public class Vakit extends Sunrise {
    
    Method meth = Method.DEFAULT; // Diyanet 
    // gün ortasý ile namaz vakitleri arasýndaki dakika farký 
    float[] time = new float[6];
    int twelve;  // gün ortasý ile öðle 12 arasýndaki dakika farký  
    GUI gui; 
    
    public Vakit(GUI g) { gui = g; }
    public void setMethod(Method m) {
        meth = m; setDate(day, false);
    }
    public void setLocation(Location a) { 
        if (loc.equals(a)) return;
        super.setLocation(a); 
        if (gui != null) gui.doTitle();
    }
    public void setDate(double d, boolean report) {
        super.setDate(d, false); 
        float[] a = meth.temkin;
        float i = a[0] - timeOf(meth.imsakAcisi); //imsak
        time[0] = (i<0 && i>-720? i : -720); //geceyarýsýný aþmasýn
        time[1] = a[1] - sunset;   //güneþ
        time[2] = a[2];            //öðle
        time[3] = a[3] + sunset/2; //ikindi vakti hesaplanmadý
        time[4] = a[4] + sunset;   //akþam
        float y = a[5] + timeOf(meth.yatsiAcisi); //yatsý
        time[5] = (y>0 && y<720? y : 720);  //geceyarýsýný aþmasýn
        twelve = (int)Math.round(date.exact12am() - noonM);
        if (gui != null) gui.doColors(); 
        if (report) report();
    }
    String header() { 
        return loc+" için "+meth+" Namaz Vakitleri \n"
            + "Gun          gunes   ogle    aksam   yatsi";
    }
    public int sunset()   { return Math.round(sunset); }
    public int v_imsak()  { return Math.round(time[0]); }
    public int v_gunes()  { return Math.round(time[1]); }
    public int v_ogle()   { return Math.round(time[2]); }
    public int v_ikindi() { return Math.round(time[3]); }
    public int v_aksam()  { return Math.round(time[4]); }
    public int v_yatsi()  { return Math.round(time[5]); }
    void print(int t) {
        //System.out.printf("  %6s", t); 
        setTime(t); System.out.printf("  %6s", date.HHmm()); 
    }
    public void report() {
        System.out.print(date.ddMMyyyy()); 
        print(v_gunes()); print(v_ogle()); print(v_aksam()); print(v_yatsi()); 
        System.out.println(); 
    }
    String plotTitle() { return loc+" -- Ögle-Aksam-Yatsi"; }
    void putData(double[][] X, int i) {
            X[1][i] = noonF + time[2]; 
            X[2][i] = noonF + time[4]; 
            X[3][i] = noonF + time[5]; 
    }
    public void imsakiye(long d) { report(d, 1, d+29); }
    public void imsakiye2015() { imsakiye(5646); } //Jun 18, 2015 
    public String toString() { 
        return date.ddMMyyyy() +" "+ loc;
    }

    //public static Class getMethods() { return Method.class; }
    public static void main(String[] args) {
        long d = 5478;  //Jan 1, 2015
        new Vakit(null).report(d, 14, d+366); 
        //imsakiye2015(); 
    }
}
