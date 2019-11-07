package it.univaq.estations.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "pointsofcharge", foreignKeys = @ForeignKey(entity = Station.class,
        parentColumns = "id",
        childColumns = "station_id",
        onDelete = CASCADE), indices = {@Index(value = "station_id")})
public class PointOfCharge {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private long id;

    @NonNull
    @ColumnInfo(name = "station_id")
    private String station_id;

    @ColumnInfo(name = "voltage")
    private int voltage;

    @ColumnInfo(name = "kw")
    private int kw;

    @ColumnInfo(name = "statusTypeId")
    private int statusTypeId;

    @Ignore
    public PointOfCharge() {
    }

    public PointOfCharge(long id, String station_id, int voltage, int kw, int statusTypeId) {
        this.id = id;
        this.station_id = station_id;
        this.voltage = voltage;
        this.kw = kw;
        this.statusTypeId = statusTypeId;
    }

    @Ignore
    public PointOfCharge(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStation_id() {
        return station_id;
    }

    public void setStation_id(String station_id) {
        this.station_id = station_id;
    }

    public int getVoltage() {
        return voltage;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public int getKw() {
        return kw;
    }

    public void setKw(int kw) {
        this.kw = kw;
    }

    public int getStatusTypeId() {
        return statusTypeId;
    }

    public void setStatusTypeId(int statusTypeId) {
        this.statusTypeId = statusTypeId;
    }
}
