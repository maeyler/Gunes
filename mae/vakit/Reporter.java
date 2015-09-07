package mae.vakit;

import mae.util.Plotter;

//Simple framework to print and plot daily data
//static methods are included
abstract class Reporter {

    abstract void setDate(double d, boolean report);
    abstract String header();
    abstract void report();
    abstract String plotTitle();
    abstract int numVars();
    abstract void putData(double[][] X, int i);
    public void report(long d1, int step, long d2) {
        System.out.println(header());
        for (long d=d1; d<d2 ; d+=step) {
            setDate(d, true); //report(); 
        }
    }
    public void report(long d) { report(d, 1, d+1); } //one day
    public void plot(long d1, int step, long d2) {
        final int N = (int)(d2 - d1)/step; 
        final double[][] X = new double[1+numVars()][N];
        for (int i=0; i<N ; i++) {
            setDate(d1 + i*step, false); 
            X[0][i] = i; putData(X, i);
        }
        initPlotter(); Plotter.p2(plotTitle(), X);
    }
    public void plot(long d) { plot(d, 7, d+7*53); } //one year: 53 weeks
    
    //all data is in degrees, cnverted to radians
    static float cos(double x) {
        return (float)Math.cos(Math.toRadians(x));
    }
    static float sin(double x) {
        return (float)Math.sin(Math.toRadians(x));
    }
    static float tan(double x) {
        return (float)Math.tan(Math.toRadians(x));
    }
    static float arccos(double x) {
        return (float)Math.toDegrees(Math.acos(x));
    }
    static float arcsin(double x) {
        return (float)Math.toDegrees(Math.asin(x));
    }
    static float arctan2(double x, double y) {
        return (float)Math.toDegrees(Math.atan2(x, y));
    }
    static float normalize(double x) {
        while (x >= 360) x -= 360;
        while (x < 0) x += 360;
        return (float)x;  // 0<=x<360
    }
    static String fraction(float t) {
        String s = ""+Math.round(60*Math.abs(t));
        if (s.length() == 1) s = "0"+s;
        return s;
    }
    static String toSixties(float t, String sep) {
        int m = (int)t; 
        String s = m +sep+ fraction(t-m);
        if (m==0 && t<0) s = "-"+s;
        return s;
    }
    static String timeToSeconds(float t) {
        return toSixties(t, ":");
    }
    static String angleToMinutes(float t) {
        return toSixties(t, "°")+"'";
    }
    static void initPlotter() {
        if (Plotter.P != null) return;
        final int RES = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
        final float RES_RATIO = RES/96f;  //default resolution is 96 dpi
        int w = (int)(900*RES_RATIO); int h = (int)(560*RES_RATIO);
        Plotter.init(""); Plotter.P.setSize(w, h);
    }
}
