"use strict";
const KEY="999d50c94b878b4f0728577fa6a63b1c",
    MAX = 12,  //chars in location name
    NUM_LOC = 5, //number of random locations
    LAT_MIN = 10, LAT_MAX = 70, //random latitude limits
    BR = '<br>'
var //global values
    hm, //hour-minute string -- update every
    lat, lon, //current coordinates
    lastw,  //time of last wheather reponse
    other   //array of latitudes -- not used
function twoDigits(t) {
    if (t>9) return ""+t;
    return "0"+t;
}
//no option: do not round
//option: add minutes & round to closest minute
//option 'wd': add week day
function toHM(t, option) {
    let d = new Date()  //without option
    if ((d.getTime() - lastw) > 3600*1000)
        getWeather() //more than an hour passed
    if (option) {
        if (typeof(option) == "number") 
            t += 60*option  //seconds
        d = new Date(t*1000)  //msec
        if (d.getSeconds()>29)
          d = new Date((t+30)*1000) //round
    } 
    let h = d.getHours()
    let m = d.getMinutes()
    let s = twoDigits(h)+":"+twoDigits(m)
    if (option == 'wd') //week day
        s = d.toString().split(' ')[0]+' '+s
    return s
}
function setHourMin() {
    hm = toHM()  //current time
    //document.title = "Clock "+hm
}
function count() {
    let d = new Date();
    let sec = twoDigits(d.getSeconds());
    if (sec == "00") setHourMin();
    saat.innerText = hm+":"+sec;
    setTimeout(count, 1000);
}
function askLocation1() { //ask system -- accurate
    if (navigator.geolocation)
      navigator.geolocation.getCurrentPosition(getLocation1)
    else console.error('geolocation not found') 
}
function getLocation1(p) {
    lat = p.coords.latitude; lon = p.coords.longitude;
    reportLocation()
}
function askLocation2() { //ipinfo.io -- approximate
    const u = "https://ipinfo.io/json"
    fetch(u).then(r => r.json()).then(getLocation2)
}
function getLocation2(p) {
    console.log("ipinfo.io", p.city)
    let a = p.loc.split(',')
    lat = Number(a[0]); lon = Number(a[1]); 
    reportLocation()
}
function askLocation3() { //extreme-ip -- approximate
    const u = "https://extreme-ip-lookup.com/json/"
    fetch(u).then(r => r.json()).then(getLocation3)
}
function getLocation3(p) {
    console.log("extreme-ip", p.city)
    lat = Number(p.lat); lon = Number(p.lon); 
    reportLocation()
}
function reportLocation() {
    console.log(lat.toFixed(4), lon.toFixed(4))
    getWeather()
}
function askWeather(lat, lon, callback) {
    const url = "https://api.openweathermap.org/data/2.5/weather?"
    let u = url+"lat="+lat+"&lon="+lon+"&units=metric&APPID="+KEY
    //console.log(u); u = u.replace('/weather','/forecast')
    fetch(u).then(r => r.json()).then(callback)
}
function hideDialog() {
    diyalog.style.display='none'
}
function getWeather() {
    hideDialog()
    hava2.innerText = "getting weather"
    detay.innerText = ''
    namaz.innerText = ''
    askWeather(lat, lon, showWeather)
    askForecast()
}
function showWeather(data) {
    lastw = new Date().getTime()
    let w = data.weather[0], ss = data.sys
    let yy = dToLoc(data.name, ss.country)
    let hh = wToText(w.main, data.main.temp)
    document.title = hh; console.log(hm, hh)
    hava2.innerHTML = iconToHTML(w.icon)+' '+hh
    yer2.innerText = yy; console.log(hh, yy)
    let loc = "["+data.coord.lat.toFixed(2)
            +", "+data.coord.lon.toFixed(2)+"]"
    latlon.value = loc
    detay.innerText = hh+'\n'+yy+'\n'+loc
        +'\nWind  '+(3.6*data.wind.speed).toFixed(0)+' kph'
        +'\nPressure '+data.main.pressure.toFixed(0)
        +'\nHumidity '+data.main.humidity.toFixed(0)+'%'
    let s1 = ss.sunrise, s2 = ss.sunset, nn = (s1+s2)/2
    namaz.innerText = 'Güneş '+toHM(s1, -6)
        +'\nÖğle  '+toHM(nn, +5)+'\nAkşam '+toHM(s2, +7)
}
function askForecast() {
    const url = "https://api.openweathermap.org/data/2.5/forecast?"
    let u = url+"lat="+lat+"&lon="+lon+"&units=metric&APPID="+KEY
    //console.log(u)
    fetch(u).then(r => r.json()).then(showForecast)
}
function showForecast(data) {
    let s = ''
    for (let i=0; i<8; i++) { // every 3 hr
        if (i == 4) s += BR
        s += oneLine(data.list[i])
    }
    loc.innerText = ''; tahmin.innerHTML = s
}
function oneLine(d) {
    let w = d.weather[0]
    let s = wToText(w.main, d.main.temp)
        +BR+ iconToHTML(w.icon)
        +BR+ toHM(d.dt, 'wd')
    return "<div class=tahmin>"+s+"</div>"
}
function getOthers() {
    loc.innerText = ''; tahmin.innerText=''; other = []
    let S = new Set(), M = LAT_MAX - LAT_MIN
    while (S.size < NUM_LOC) { //LAT_MIN ≤ k < LAT_MAX
        let k = LAT_MIN + Math.floor(M*Math.random())
        S.add(k)  
    }
    for (let k of S) askWeather(k, lon, addOther)
}
function addOther(data) {
    const B = '<td>', E = '</td>'
    let w = data.weather[0], ss = data.sys
    let hh = B+data.coord.lat+E  //.toFixed(1)
        + B+dToLoc(data.name, ss.country)+E
        + B+iconToHTML(w.icon)+E
        + B+wToText(w.main, data.main.temp)+E
    other.push(hh)
    other.sort((x,y) => (x>y? -1 : 1))
    let s = '<tr><th>Latitude</th><th>City</th>'
        + '<th>Current</th><th>weather</th></tr>'
        + '<tr>'+other.join('</tr>\n<tr>')+'</tr>'
    loc.innerHTML = '<table>'+s+'</table>'
}
function iconToHTML(i, c) {
    const URL = "https://openweathermap.org/img/w/"
    //let s = (c ? ' class='+c : '')
    return '<img src='+URL+i+'.png>'  //'.png'+s+'>'
}
function dToLoc(name, country) {
    if (!name) return '??'
    if (name.length > MAX) 
        name = name.substring(0, MAX)
    if (!country) return name
    return name+', '+country
}
function wToText(w, t) {
    return w+" "+t.toFixed(0)+"°" //no need to convert
}
function convert(kelvin){
    return (kelvin - 273.15);
    //return celsius*1.8 + 32
}

