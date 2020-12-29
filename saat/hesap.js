"use strict"
const 
    PI_180 = Math.PI/180, //for radian-degree conversion
    J2000 = 10958,  //number of days between 1970 and 2000 
    HEADER = "Day        EqTime Delta Declin  Noon   Sunset"
/** 
 * Immutable class for latitude, longitude, Time Zone
 **/
class Location {
    constructor(lat, lon, zone) {
        this.lat = Number(lat) || 0
        this.lon = Number(lon) || 0
     // if (!zone) zone = -new Date().getTimezoneOffset()/60
        this.zone = Number(zone) || Math.round(lon/15)
    }
    toString() {
        let s = this.zone<0? '' : '+'
        return this.lat+', '+ this.lon+', '+s+this.zone
    }
    static fromString(s) {
        let [lat, lon, zone] = s.split(/[ ,]+/)
        return new Location(lat, lon, zone)
    }
}
/** 
 * Immutable class for equation of time and declination at d
 **/
class Day {
    constructor(d) { //d is a Date or number of days
        if (d instanceof Date) d = d.valueOf()/86400/1000-J2000
        d = Math.trunc(d)  //calculate at midnight 0:00
        this.num = d  //integer number of days since 1/1/2000
        //local date -- date.toJSON().split('T')[0] gives UTC
        let date = new Date((d + J2000)*86400*1000)
        //this date shows the given day at midnight
        this.str = date.getFullYear()+'-'
            +M.d2(date.getMonth()+1)+'-'+M.d2(date.getDate())
        let {eqTime, declin} = Day.calculate(d)
        this.eqTime = eqTime; this.declin = declin
        this.delta = (eqTime - Day.calculate(d+1).eqTime)*60
    }
    toString() {
        return 'Day '+ this.num
            +', eqTime='+this.eqTime.toFixed(2)+"'"
            +', declin='+this.declin.toFixed(2)+'°'
            // +', delta='+this.delta.toFixed(0)+'"'
    }
    static fromString(s) {
        return new Day(new Date(s))
    }
    static calculate(d) {
        let g = M.normal(357.529 + 0.98560028*d) //in degrees
        let q = M.normal(280.459 + 0.98564736*d) //365.242 days
        let L = M.normal(q + 1.915*M.sin(g) + 0.020*M.sin(2*g))
        let e = 23.439 - 0.00000036* d //tiny correction
        let RA = M.normal(M.arctan2(M.cos(e)*M.sin(L), M.cos(L)))
        let eqTime = 4*(q - RA)  //equation of time in minutes
        if (q>300 && RA<50) eqTime -= 4*360
        let declin = M.arcsin(M.sin(e)* M.sin(L)) //declination
        return {eqTime, declin}
    }
}
/** 
 * Singleton for noon and sunset at given Location and Day
 * All values are calculated in minutes
 **/
class SunData { //Singleton instance is used
    constructor() {} //is this really needed?
    setDay(d) { //d is a Day or number of days
        this.day = d instanceof Day? d : new Day(d) 
        this.calculate()
    }
    setLoc(loc) {
        this.loc = loc; this.calculate()
    }
    calculate() {
        if (!this.loc || !this.day) return
        let loc = this.loc, day = this.day 
        //when does sun reach an angle a below the horizon?
        this.timeOf = a => 4*M.arccos((-M.sin(a) - C1)/C0)
        //inverse: G.altitude(G.timeOf(a)) == -a for all a
        this.altitude = m => M.arcsin(C0*M.cos(m/4) + C1)
        let C0 = M.cos(loc.lat)*M.cos(day.declin)
        let C1 = M.sin(loc.lat)*M.sin(day.declin)
        this.noon = (12+loc.zone)*60 - 4*loc.lon - day.eqTime
        this.half = this.timeOf(1); //sunset-noon difference
    }
    toString() {
        let L = '\nLocation (', R =')\n'
        return this.day+L+this.loc+R+this.report
    }
    get report() {
        if (!this.loc || !this.day) return 'Not initialized'
        return this.day.str
            + this.day.eqTime.toFixed(1).padStart(6)+"'"
            + this.day.delta.toFixed(0).padStart(5)+'"'
            + this.day.declin.toFixed(1).padStart(6)+'°'
            + M.toHHMM(this.noon).padStart(7)
            + M.toHHMM(this.noon+this.half).padStart(7)
    }
}
/**
 * Utility class for Math and format static methods
 **/
class M {
    //all data is in degrees, converted to radians
    static cos = x => Math.cos(x * PI_180)
    static sin = x => Math.sin(x * PI_180)
    static tan = x => Math.tan(x * PI_180)
    static arccos = x => Math.acos(x) / PI_180
    static arcsin = x => Math.asin(x) / PI_180
    static arctan2 = (x, y) => Math.atan2(x, y) / PI_180
    static normal(x) {
        while (x >= 360) x -= 360;
        while (x < 0) x += 360;
        return x;  // 0<=x<360
    }
    static toHHMM(m) {
        m += 0.5 //trunc works like round
        let h = Math.trunc(m/60)
        let n = Math.trunc(m-60*h)
        return M.d2(h)+':'+M.d2(n)
    }
    static d2 = n => (n<10? '0'+n : ''+n)
}
