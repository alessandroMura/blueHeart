package com.example.blueheart;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtilities {

    public static String savetxt(String filename, ArrayList data)  {
        String filepath = null;
        try {

            File root = new File(Environment.getExternalStorageDirectory(), "EDA");
            if (!root.exists()) {
                root.mkdirs();
            }
            File edaFile = new File(root, filename);
            filepath = edaFile.getAbsolutePath();

            Log.i("DB", "Percorso file " + String.valueOf(filepath));
            // Se il file non esiste lo crea altrimenti lo sovrascrive
            if (!edaFile.exists())
                edaFile.createNewFile();
            edaFile.delete();
            FileOutputStream fos = new FileOutputStream(edaFile);
            fos.write(String.valueOf(data).getBytes());
            fos.close();
            Log.i("DB", "File " + filename + " salvato");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("DB", "File " + filename + " non trovato");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("DB", "Errore nel salvataggio ");
        }

        return filepath;
    }

    public static ArrayList readEdrFromFile(String filepath){
        ArrayList edrValues = null;
        String line;

        try {
            FileInputStream fileInputStream = new FileInputStream (new File(filepath));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ( (line = bufferedReader.readLine()) != null )
                stringBuilder.append(line);

            fileInputStream.close();
            line = stringBuilder.toString();
            bufferedReader.close();

            edrValues = getEdrValues(line);
        }
        catch(FileNotFoundException ex) {
            Log.d("DB", ex.getMessage());
        }
        catch(IOException ex) {
            Log.d("DB", ex.getMessage());
        }

        return edrValues;
    }

    public static ArrayList getEdrValues(String s){
        List<String> edrValues = Arrays.asList(s.substring(1, s.length() - 1).split(","));
        ArrayList edrValuesd = new ArrayList();
        for(String edrValue : edrValues){
            edrValuesd.add(Double.parseDouble(edrValue));
        }
        return edrValuesd;
    }



}

