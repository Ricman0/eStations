package it.univaq.estations.Database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.google.android.gms.maps.model.LatLng;

import it.univaq.estations.model.PointOfCharge;
import it.univaq.estations.model.Station;

@androidx.room.Database(entities = { Station.class, PointOfCharge.class}, version = 1)
@TypeConverters(LatLngConverter.class)
public abstract class Database extends RoomDatabase {

    public abstract StationDao getStationDao();
    public abstract PointOfChargeDao getPointOfChargeDao() ;

    private static Database instance = null;

    //private Database(){}
    //protected Database(){}

    public static Database getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(
                    context,
                    Database.class,
                    "myRoomDatabase").build();
        }
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }
}
