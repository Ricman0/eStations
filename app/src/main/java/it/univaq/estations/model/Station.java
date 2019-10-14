package it.univaq.estations.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import it.univaq.estations.Database.LatLngConverter;

@Entity(tableName = "stations")
public class Station {

    @PrimaryKey @NonNull
    private String id;

    @ColumnInfo(name = "name")
    private String title;

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
    private int numberOfConnections;

    @Ignore
    private ArrayList<PointOfCharge> pointsOfCharge;

    @Ignore
    public Station(){}

    public Station(String id, String title, String address, String town, String stateOrProvince, LatLng position, String url, int numberOfPointsOfCharge) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.town = town;
        this.stateOrProvince = stateOrProvince;
        this.position = position;
        this.url = url;
        this.numberOfConnections = numberOfPointsOfCharge;
        this.pointsOfCharge = new ArrayList<PointOfCharge>();
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

    public int getNumberOfConnections() {
        return numberOfConnections;
    }

    public void setNumberOfConnections(int numberOfPointsOfCharge) {
        this.numberOfConnections = numberOfPointsOfCharge;
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
}
