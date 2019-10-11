package it.univaq.estations.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import it.univaq.estations.model.Station;

@Dao
public interface StationDao {
    @Insert
    public void save(Station station);

    @Delete
    public void delete(Station station);

    @Update
    public void update(Station station);

    @Query("SELECT * FROM stations ORDER BY town ASC")
    public List<Station> getAllStations();

    @Query("SELECT * FROM stations WHERE id=:id")
    public Station getById(long id);
}
