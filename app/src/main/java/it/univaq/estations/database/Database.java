package it.univaq.estations.database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import it.univaq.estations.model.PointOfCharge;
import it.univaq.estations.model.Station;

@androidx.room.Database(entities = { Station.class, PointOfCharge.class}, version = 1)
@TypeConverters({LatLngConverter.class})
public abstract class Database extends RoomDatabase {

    public abstract StationDao getStationDao();
    public abstract PointOfChargeDao getPointOfChargeDao() ;

    private static Database instance = null;

    public static Database getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(
                    context,
                    Database.class,
                    "EstationsRoomDatabase").build();
        }
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }
}
