package it.univaq.estations.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pointofcharges")
public class PointOfCharge {

    @PrimaryKey
    private long id;

    @ColumnInfo(name = "voltage")
    private int voltage;

    @ColumnInfo(name = "kw")
    private int kw;

    @ColumnInfo(name = "statusTypeId")
    private int statusTypeId;

    public PointOfCharge() {
    }

    public PointOfCharge(long id, int voltage, int kw, int statusTypeId) {
        this.id = id;
        this.voltage = voltage;
        this.kw = kw;
        this.statusTypeId = statusTypeId;
    }

    public PointOfCharge(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
