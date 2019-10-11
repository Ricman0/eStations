package it.univaq.estations.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

@Entity(tableName = "stations")
public class Station {

    @PrimaryKey
    private String id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "town")
    private String town;

    @ColumnInfo(name = "stateOrProvince")
    private String stateOrProvince;

    @ColumnInfo(name = "position")
    private LatLng position;

    @ColumnInfo(name = "url")
    private String url;

    @ColumnInfo(name = "numberOfConnections")
    private int numberOfConnections;

    @Ignore
    private ArrayList<PointOfCharge> pointOfCharges;

    public Station(){}

    public Station(String id, String title, String address, String town, String stateOrProvince, LatLng position, String url, int numberOfConnections) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.town = town;
        this.stateOrProvince = stateOrProvince;
        this.position = position;
        this.url = url;
        this.numberOfConnections = numberOfConnections;
        this.pointOfCharges = new ArrayList<PointOfCharge>();
    }

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

    public void setNumberOfConnections(int numberOfConnections) {
        this.numberOfConnections = numberOfConnections;
    }

    public void addPointOfCharge(PointOfCharge pointOfCharge){
        this.pointOfCharges.add(pointOfCharge);
    }
}
