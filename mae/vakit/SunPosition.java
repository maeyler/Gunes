package mae.vakit;

import java.util.Date;

//calculates the angular coordinates of the sun on a given day
//http://praytimes.org/calculation/
//http://aa.usno.navy.mil/faq/docs/SunApprox.php
//http://en.wikipedia.org/wiki/Julian_day#Calculation

public class SunPosition extends Reporter {

    final Timer date = new Timer();  
    int day;               //input: number of days since 01.01.2000
    float eqTime, declin;  //output: equation of time & declination of the Sun
    
    public void setDate(double d, boolean report) {
        day = (int)d; 
        date.setTime(Timer.fromJulian(day)); 
        float g = normalize(357.529 + 0.98560028* day); //in degrees
        float q = normalize(280.459 + 0.98564736* day); //period 365.242 days
        float L = normalize(q + 1.915* sin(g) + 0.020* sin(2*g));
        float e = 23.439f;  //(float)(23.439 - 0.00000036* day);
        float RA = normalize(arctan2(cos(e)*sin(L), cos(L)));  //in degrees
 
        eqTime = 4*(q - RA);             //equation of time in minutes
        if (q>300 & RA<50) eqTime -= 4*360;
        declin = arcsin(sin(e)* sin(L)); //declination of the Sun in degrees
        if (report) report();
    }
    public void setDate(Date d) { 
        setDate(Timer.toJulian(d.getTime()), true);
    }
    String header() { 
        return "Day            EqTime     Declin";
    }
    public void report() {
        System.out.printf("%s   %8s  %10s %n", date.ddMMyyyy(), 
            timeToSeconds(eqTime), angleToMinutes(declin));
    }
    String plotTitle() { return "Declination & Equation of time"; }
    int numVars() { return 2; }
    void putData(double[][] X, int i) {
            X[1][i] = declin; 
            X[2][i] = eqTime/2; 
    }
    public String toString() { 
        return date.ddMMyyyy() 
         +" "+ timeToSeconds(eqTime) +"  "+ angleToMinutes(declin);
    }
        
    public static void main(String[] args) {
        long d = 5478;  //Jan 1, 2015
        SunPosition p = new SunPosition();
        p.report(d, 14, d+366);  //p.plot(d);
    }
}
