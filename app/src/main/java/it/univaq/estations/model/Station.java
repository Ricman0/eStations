package it.univaq.estations.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import it.univaq.estations.database.LatLngConverter;

@Entity(tableName = "stations")
public class Station {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "name")
    private String title;

    @ColumnInfo(name = "usage_cost")
    private String usageCost;

    private String address;

    private String town;

    @ColumnInfo(name = "state_or_province")
    private String stateOrProvince;

    @TypeConverters(LatLngConverter.class)
    private LatLng position;

    private String url;

    @ColumnInfo(name = "number_of_points_of_charge")
    private int numberOfPointsOfCharge;

    @Ignore
    private ArrayList<PointOfCharge> pointsOfCharge;

    @ColumnInfo(name = "url_image")
    private String stationImageUrl;

    @ColumnInfo(name = "distance_from_user")
    private double distanceFromUser;

    @Ignore // ignore this constructor because it si necessary do this.pointsOfCharge = new ArrayList<PointOfCharge>(); and setDistanceFromUser(0);
    public Station(){}


    public Station(String title, String usageCost,String address, String town, String stateOrProvince,
                   LatLng position, String url, int numberOfPointsOfCharge, String stationImageUrl) {
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
        setDistanceFromUser(0);
    }

    // getter and setter


    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    /**
     * Function to return the usage cost when it is provided.
     *
     * @return Object the usage cost
     */
    public Object usageCost(){

        String cost = usageCost.substring(0,4);
        cost = cost.replaceFirst(",", ".");
        try {
            Float floatCost = Float.parseFloat(cost);
            return floatCost;

        } catch (NumberFormatException e) {
            return usageCost;
        }
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


    public String getStationImageUrl() {

        return stationImageUrl;
    }

    public void setStationImageUrl(String stationImageUrl) {
        this.stationImageUrl = stationImageUrl;
    }

    public double getDistanceFromUser(){
        return distanceFromUser;
    }

    public void setDistanceFromUser(double distanceFromUser) {
        this.distanceFromUser = distanceFromUser;
    }


    /**
     * Function to add a point of change in the ArrayList pointsOfCharge
     *
     * @param pointOfCharge PointOfCharge The point of charge to add to the points of charge ArrayList
     * @author Claudia Di Marco & Riccardo Mantini
     */
    public void addPointOfCharge(PointOfCharge pointOfCharge){
        this.pointsOfCharge.add(pointOfCharge);
    }

    /**
     * Function to add a list of points of change in the ArrayList pointsOfCharge
     *
     * @param pointOfChargeList List<PointOfCharge> List of points of charge to add
     * @author Claudia Di Marco & Riccardo Mantini
     */
    public void addPointOfChargeList(List<PointOfCharge> pointOfChargeList){
        System.out.println("quanti poc?? " + pointOfChargeList.size());
        this.pointsOfCharge.addAll(pointOfChargeList);
    }

    /**
     * Function to know if one charging point of the station is free
     *
     * @return true if one point of charge is free, false otherwise
     * @author Claudia Di Marco & Riccardo Mantini
     */
    public Boolean isFree(){
        boolean isFree = false;
        Iterator<PointOfCharge> it =  this.pointsOfCharge.iterator();
        while (it.hasNext()){
            PointOfCharge p = it.next();
            if(p.getStatusTypeId() == 50) isFree=true;
        }
        return isFree;
    }

    /**
     * Function to calculate the distance between the user and the station, then it sets this
     * distance in the distanceFromUser attribute.
     *
     * @param currentPosition LatLng the user current position
     * @author Claudia Di Marco & Riccardo Mantini
     */
    public void calcAndSetDistanceFromUser(LatLng currentPosition){
        float[] dist = new float[1];
        android.location.Location.distanceBetween(currentPosition.latitude, currentPosition.longitude,
                this.position.latitude, this.position.longitude, dist);
        setDistanceFromUser(((int) dist[0] / 100) / 10.0);
    }


}
