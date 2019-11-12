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
        childColumns = "stationId",
        onDelete = CASCADE, onUpdate = CASCADE), indices = {@Index(value = "stationId")})
public class PointOfCharge {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long stationId;

    private int voltage;

    private int kw;

    @ColumnInfo(name = "status_type_id")
    private int statusTypeId;

    public PointOfCharge() {
    }

    @Ignore
    public PointOfCharge( int voltage, int kw, int statusTypeId) {
        this.voltage = voltage;
        this.kw = kw;
        this.statusTypeId = statusTypeId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id){ this.id = id;}

    public long getStationId() {
        return stationId;
    }

    public void setStationId(long stationId){this.stationId = stationId;}

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
