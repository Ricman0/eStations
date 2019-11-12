package it.univaq.estations.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.OnConflictStrategy;

import java.util.List;

import it.univaq.estations.model.PointOfCharge;

@Dao
public interface PointOfChargeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long save(PointOfCharge poc);

    @Delete
    public void delete(PointOfCharge poc);

    @Update
    public void update(PointOfCharge poc);

    @Query("SELECT * FROM pointsofcharge")
    public List<PointOfCharge> getAllPointsOfCharge();

    @Query("SELECT * FROM pointsofcharge WHERE id=:id")
    public PointOfCharge getById(long id);

    @Query("SELECT * FROM pointsofcharge WHERE stationId=:stationId")
    public List<PointOfCharge> getAllStationPointsOfCharge(long stationId);
}
