package it.univaq.estations.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Station {

    private String id;

    private String title;

    private String address;

    private String town;

    private String stateOrProvince;

    private LatLng position;

    private String url;

    private int numberOfConnections;

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
