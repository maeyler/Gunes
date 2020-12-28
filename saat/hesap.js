"use strict"
/** 
 * Immutable class for latitude, longitude, Time Zone
 **/
class Location {
    constructor(lat=0, lon=0, zone) {
        this.lat = Number(lat)
        this.lon = Number(lon)
        if (!zone) zone = -new Date().getTimezoneOffset()/60
        this.zone = Number(zone)
    }
    toString() {
        let s = this.zone<0? '-' : '+'
        return this.lat+', '+ this.lon+', '+s+this.zone
    }
    static fromString(s) {
        let [lat, lon, zone] = s.split(/[ ,-]+/)
        return new Location(lat, lon, zone)
    }
}
const J2000 = 10958; //number of days between 1970 and 2000 
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
        let g = M.normal(357.529 + 0.98560028*d) //in degrees
        let q = M.normal(280.459 + 0.98564736*d) //365.242 days
        let L = M.normal(q + 1.915*M.sin(g) + 0.020*M.sin(2*g))
        let e = 23.439  // - 0.00000036* d
        let RA = M.normal(M.arctan2(M.cos(e)*M.sin(L), M.cos(L)))
        this.eqTime = 4*(q - RA)  //equation of time in minutes
        if (q>300 && RA<50) this.eqTime -= 4*360;
        this.declin = M.arcsin(M.sin(e)* M.sin(L)) //declination
    }
    toString() {
        return 'Day '+ this.num+' '+ this.str
            +', eqTime='+this.eqTime.toFixed(4)
            +', declin='+this.declin.toFixed(4)
    }
}
/** 
 * Singleton for noon and sunset at given Location and Day
 * All values are calculated in minutes
 **/
class Sunrise { //One global instance is enough
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
        this.sunset = this.timeOf(1); //sunset-noon difference
        let min2000 = (day.num + J2000)*24*60 
            + 12*60 - day.eqTime - 4*loc.lon
        this.minutesToStr = m => {
            let t = (min2000 + m)*60*1000;
            return new Date(t).toTimeString().substring(0, 5)
        }
    }
    toString() {
        let str = (this.loc? '\n'+'Location: '
            +this.loc+'\n'+this.toReport() : '')
        return this.day +str
    }
    toReport() {
        if (!this.loc || !this.day) return 'Not initialized'
        return this.day.str
            + this.day.eqTime.toFixed(1).padStart(7)
            + this.day.declin.toFixed(1).padStart(8)
            + this.minutesToStr(0).padStart(8) //noon
            + this.minutesToStr(this.sunset).padStart(8)
    }
}
const PI_180 = Math.PI/180 //for radian conversion
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
    static fraction(t) {
        let s = ''+Math.round(60*Math.abs(t));
        if (s.length == 1) s = '0'+s;
        return s;
    }
    static toSixties(t, sep) {
        let m = Math.trunc(t)
        let s = m +sep+ M.fraction(t-m);
        if (m==0 && t<0) s = '-'+s;
        return s;
    }
    static timeToSeconds = t=> M.toSixties(t, ':')
    static angleToMinutes = t=> M.toSixties(t, '°')+"'"
    static d2 = n => (n<10? '0'+n : ''+n)
}
