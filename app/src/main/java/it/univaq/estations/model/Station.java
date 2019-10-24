package it.univaq.estations.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import it.univaq.estations.Database.LatLngConverter;
import it.univaq.estations.utility.LocationService;

@Entity(tableName = "stations")
public class Station {

    @PrimaryKey @NonNull
    private String id;

    @ColumnInfo(name = "name")
    private String title;

    @ColumnInfo(name = "usageCost")
    private String usageCost;

    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "town")
    private String town;

    @ColumnInfo(name = "stateOrProvince")
    private String stateOrProvince;

    @ColumnInfo(name = "position")
    @TypeConverters(LatLngConverter.class)
    private LatLng position;

    @ColumnInfo(name = "url")
    private String url;

    @ColumnInfo(name = "numberOfPointsOfCharge")
    private int numberOfPointsOfCharge;

    @Ignore
    private ArrayList<PointOfCharge> pointsOfCharge;

    @ColumnInfo(name = "urlImage")
    private String stationImageUrl;

    @ColumnInfo(name = "distanceFromUser")
    private double distanceFromUser;

    @Ignore
    public Station(){}


    public Station(String id, String title, String usageCost,String address, String town, String stateOrProvince,
                   LatLng position, String url, int numberOfPointsOfCharge, String stationImageUrl) {
        this.id = id;
        this.title = title;
        this.usageCost = usageCost;
        this.address = address;
        this.town = town;
        this.stateOrProvince = stateOrProvince;
        this.position = position;
        this.url = url;
        this.numberOfPointsOfCharge = numberOfPointsOfCharge;
        this.pointsOfCharge = new ArrayList<PointOfCharge>();
        this.stationImageUrl = stationImageUrl;
        calcDistanceFromUser();
    }

    @Ignore
    public Station(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return title;
    }

    public void setName(String name) {
        this.title = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsageCost() { return usageCost; }

    public Object usageCost(){

        String[] cost = usageCost.split(",");
        int intPart;
        int fractPart;
        float c = 0;

        System.out.println("vedo " + cost[0]);

        if (cost[0].length() == 1)
        {
            //String[] costFract = cost[1].split(" ");
            String costFract = cost[1].substring(0,1);
            System.out.println("vedo2 " + costFract);
            if(costFract.length() == 2)
            try {
                intPart = Integer.parseInt(cost[0]);
                fractPart = Integer.valueOf(costFract);
                c = intPart + ((float)fractPart/100);
            } catch(NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }
            return c;
        }
        else{
            // fare con il try ...
            String[] cost2 = usageCost.split(".");
            System.out.println("vedo4 " + cost2);
            if (cost2[0].length() == 1)
            {
                //String[] costFract = cost[1].split(" ");
                String costFract = cost2[1].substring(0,1);
                System.out.println("vedo3 " + costFract);
                if(costFract.length() == 2)
                    try {
                        intPart = Integer.parseInt(cost[0]);
                        fractPart = Integer.valueOf(costFract);
                        c = intPart + ((float)fractPart/100);
                    } catch(NumberFormatException nfe) {
                        System.out.println("Could not parse " + nfe);
                    }
                return c;
            }
        }
        return usageCost;

    }

    public void setUsageCost(String usageCost) {
        this.usageCost = usageCost;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getStateOrProvince() {
        return stateOrProvince;
    }

    public void setStateOrProvince(String stateOrProvince) {
        this.stateOrProvince = stateOrProvince;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getNumberOfPointsOfCharge() {
        return numberOfPointsOfCharge;
    }

    public void setNumberOfPointsOfCharge(int numberOfPointsOfCharge) {
        this.numberOfPointsOfCharge = numberOfPointsOfCharge;
    }

    public ArrayList<PointOfCharge> getPointsOfCharge() {
        return pointsOfCharge;
    }

    public void setPointsOfCharge(ArrayList<PointOfCharge> pointsOfCharge) {
        this.pointsOfCharge = pointsOfCharge;
    }

    public void addPointOfCharge(PointOfCharge pointOfCharge){
        this.pointsOfCharge.add(pointOfCharge);
    }

    public void addPointOfChargeList(List<PointOfCharge> pointOfChargeList){
        this.pointsOfCharge.addAll(pointOfChargeList);
    }

    public String getStationImageUrl() {

        return stationImageUrl;
    }

    public Boolean isFree(){
        boolean isFree = false;
        Iterator<PointOfCharge> it =  this.pointsOfCharge.iterator();
        while (it.hasNext()){
            PointOfCharge p = it.next();
            if(p.getStatusTypeId() == 50) isFree=true;
        }
        return isFree;
    }

    public double getDistanceFromUser(){
        return distanceFromUser;
    }

    public void setDistanceFromUser(double distanceFromUser) {
        this.distanceFromUser = distanceFromUser;
    }

    //TODO richiamare questa funzione ogni qual volta che l'utente cambia la sua posizione
    public void updateDistanceFromUser() {
        calcDistanceFromUser();
    }

    private void calcDistanceFromUser(){
        LatLng currentPosition = LocationService.getInstance().getCurrentLocation();
        float[] dist = new float[1];
        android.location.Location.distanceBetween(currentPosition.latitude, currentPosition.longitude,
                this.position.latitude, this.position.longitude, dist);
        this.distanceFromUser = (((int) dist[0] / 100) / 10.0);
    }

    public void setStationImageUrl(String stationImageUrl) {
        this.stationImageUrl = stationImageUrl;
    }
}
