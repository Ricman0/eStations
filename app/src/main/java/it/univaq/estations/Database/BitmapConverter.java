package it.univaq.estations.Database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.room.TypeConverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class BitmapConverter {

    @TypeConverter
    public static Bitmap toBitmap(byte[] image) {
        Bitmap bitmapImage = null;
        if(image != null) {
            ByteArrayInputStream imageStream = new ByteArrayInputStream(image);
            bitmapImage = BitmapFactory.decodeStream(imageStream);
        }
        return bitmapImage;
    }

    @TypeConverter
    public static byte[] toByte(Bitmap image){
        // convert bitmap to byte
        byte imageInByte[] = null;
        if(image != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            imageInByte = stream.toByteArray();
        }
        return imageInByte;
    }
}
