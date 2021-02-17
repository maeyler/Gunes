"use strict"
const
    VERSION = 'V0.8',
    PI_180 = Math.PI/180, //for radian-degree conversion
    J2000 = 10958,  //number of days between 1970 and 2000 
    HEADER = "Day        Delta EqTime Declin  Noon   Sunset"
/** 
 * Immutable class for latitude, longitude, Time Zone
 **/
class Location {
    constructor(lat=0, lng=0, zone) {
        this.lat = Number(lat)
        this.lng = Number(lng)
     // if (!zone) zone = -new Date().getTimezoneOffset()/60
        this.zone = (zone == undefined)?
            Math.round(lng/15) : Number(zone)
    }
    toString() {
        let s = this.zone<0? '' : '+'
        return this.lat+', '+ this.lng+', '+s+this.zone
    }
}
Location.fromString = (s) => { // emulate static method
    let f = n => !n ? 0 :
        n.length<7 ? n : Number(n).toFixed(4)
    let [lat, lng, zone] = s.split(/[ ,]+/)
    return new Location(f(lat), f(lng), zone)
}

/** 
 * Immutable class for equation of time and declination at d
 **/
class Day {
    constructor(d) { //d is a Date or number of days
        if (d instanceof Date) d = d.valueOf()/86400/1000-J2000
        d = Math.trunc(d)  //calculate at midnight 0:00
        this.num = d  //integer number of days since 1/1/2000
        let date = new Date((d + J2000)*86400*1000)
        //this date shows the given day at midnight
        this.str = F.stringFrom(date)
        let {eqTime, declin} = Day.calculate(d)
        this.eqTime = eqTime; this.declin = declin
        this.delta = (eqTime - Day.calculate(d+1).eqTime)*60
    }
    toString() {
        return 'Day '+ this.num+': '+this.str
    }
}
Day.fromString = (s) => new Day(new Date(s))
Day.calculate = (d) => {
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
/** 
 * Singleton for noon and sunset at given Location and Day
 * All values are calculated in minutes
 **/
class SunData { //Singleton instance G is used
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
        //when does sun reach angle a above the horizon?
        this.timeOf = a => 4*M.arccos((M.sin(a) - C1)/C0)
        //inverse: G.altitude(G.timeOf(a)) == a for all a
        this.altitude = m => M.arcsin(C0*M.cos(m/4) + C1)
        let C0 = M.cos(loc.lat)*M.cos(day.declin)
        let C1 = M.sin(loc.lat)*M.sin(day.declin)
        this.noon = (12+loc.zone)*60 - 4*loc.lng - day.eqTime
        this.half = this.timeOf(-1); //sunset-noon difference
    }
    toString() {
        let s = this.day
        if (this.loc) s += '\nLoc '+F.summary()
        return s
    }
}
const Kabe = new Location(21.422500, 39.826137, 3)
/**
 * Utility class for Math -- static methods
 **/
const M = {
    //all data is in degrees, converted to radians
    cos: x => Math.cos(x * PI_180),
    sin: x => Math.sin(x * PI_180),
    tan: x => Math.tan(x * PI_180),
    arccos: x => Math.acos(x) / PI_180,
    arcsin: x => Math.asin(x) / PI_180,
    arctan2: (x, y) => Math.atan2(x, y) / PI_180,
    qiblah: (p) => {
        let d = p.lng - Kabe.lng
        let x = M.sin(p.lat)*M.cos(d)
        let y = M.cos(p.lat)*M.tan(Kabe.lat)
        return M.arctan2(M.sin(d), x-y).toFixed(2)
    },
    normal: (x) => {
        while (x >= 360) x -= 360;
        while (x < 0) x += 360;
        return x;  // 0<=x<360
    }
}
const d2 = n => (n<10? '0'+n : ''+n)
/**
 * Utility class for format -- static methods
 **/
const F = {
    stringFrom: (date) => date.getFullYear()
        +'-'+d2(date.getMonth()+1)
        +'-'+d2(date.getDate()),
    toHHMM: (m) => {
        m += 0.5 //trunc avoids 7:60
        let h = Math.trunc(m/60)
        let n = Math.trunc(m-60*h)
        return d2(h)+':'+d2(n)
    },
    summary: (str='') => str+'('+G.loc+') '
            + F.toHHMM(G.noon-G.half).padStart(6)
            + F.toHHMM(G.noon).padStart(6)
            + F.toHHMM(G.noon+G.half).padStart(6),
    vakit: () => '<b>Diyanet</b>'
            +'\nGüneş '+ F.toHHMM(G.noon-G.half-6)
            +'\nÖğle  '+ F.toHHMM(G.noon+6)
            +'\nAkşam '+ F.toHHMM(G.noon+G.half+7),
    report: () => G.day.str
            + G.day.delta.toFixed(0).padStart(5)+'"'
            + G.day.eqTime.toFixed(1).padStart(6)+"'"
            + G.day.declin.toFixed(1).padStart(6)+'°'
            + F.toHHMM(G.noon).padStart(7)
            + F.toHHMM(G.noon+G.half).padStart(7)
}
const G = new SunData()  //global object for current data
