package it.univaq.estations.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

import it.univaq.estations.model.PointOfCharge;

@Dao
public interface PointOfChargeDao {

    @Insert
    public void save(PointOfCharge poc);

    @Delete
    public void delete(PointOfCharge poc);

    @Update
    public void update(PointOfCharge poc);

    @Query("SELECT * FROM pointofcharges")
    public List<PointOfCharge> getAllPointOfCharges();

    @Query("SELECT * FROM pointofcharges WHERE id=:id")
    public PointOfCharge getById(long id);

    @Query("SELECT * FROM pointofcharges WHERE station_id=:station_id")
    public List<PointOfCharge> getAllStationPointOfCharges(String station_id);
}
