package de.mide.pegsolitaire.model;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import de.mide.pegsolitaire.model.Board;
public class Utils {
    public static Board getBoardFromJson(Context context,String path){
        Board b = null;
        try{
            InputStream is = context.getAssets().open(path);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String jsonString = new String(buffer, "UTF-8");

            Gson gson = new Gson();
            Type type = new TypeToken<Board>(){}.getType();
            b = gson.fromJson(jsonString,type);
            b.setRC();

        } catch(IOException e){
            e.printStackTrace();
        }
        return b;
    }
}
