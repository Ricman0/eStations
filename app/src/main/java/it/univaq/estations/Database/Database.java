package it.univaq.estations.Database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import it.univaq.estations.model.PointOfCharge;
import it.univaq.estations.model.Station;

@androidx.room.Database(entities = { Station.class, PointOfCharge.class}, version = 0)
public abstract class Database extends RoomDatabase {
    public abstract StationDao getStationDao();

    private static Database instance = null;

    private Database(){}

    public static Database getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(
                    context,
                    Database.class,
                    "myRoomDatabase").build();
        }
        return instance;
    }

}
