package mae.vakit;

import java.util.TimeZone;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Timer {
    
    final Calendar cal = new GregorianCalendar();
    
    public Timer() { }
    public Timer(TimeZone z) { setTimeZone(z); }
    public String ddMMyyyy() {
        int d = cal.get(Calendar.DAY_OF_MONTH);
        int m = cal.get(Calendar.MONTH)+1;
        int y = cal.get(Calendar.YEAR);
        return d2(d)+"/"+d2(m)+"/"+d2(y);
    }
    public String HHmm() {
        int h = cal.get(Calendar.HOUR_OF_DAY);
        int m = cal.get(Calendar.MINUTE);
        return d2(h)+":"+d2(m);
    }
    public String HHmmss() {
        int s = cal.get(Calendar.SECOND);
        return HHmm()+":"+d2(s);
    }
    public void setTime(long t) {
        cal.setTimeInMillis(t);
    }
    public void setTimeZone(TimeZone z) {
        cal.setTimeZone(z);
    }
    public double exact12am() {
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return (cal.getTimeInMillis()/60.0/1000);
    }
    public String toString() { 
        return ddMMyyyy() +" "+ HHmm();
    }

    final static int J2000 = 2451545 - 2440587;  // D2000-D1970
    public static double toJulian(long t) {
        return t/86400.0/1000 - J2000;
    }
    public static long fromJulian(double x) {
        return (long)((x + J2000)*86400*1000);
    }
    static String d2(int n) {
        return (n<10? "0"+n : ""+n);
    }
    public static void main(String[] args) {
        Timer T = new Timer();
        System.out.println(T.ddMMyyyy());
        System.out.println(T.HHmmss());
        System.out.println(T.exact12am());
    }
}
