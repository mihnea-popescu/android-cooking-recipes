package com.mihneapopescu.cookingrecipes.services.helpers;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitmapToUri {
    public static Uri process(Bitmap bitmap, Context context) {
        // Initialize a new file
        String fileName = "image"+System.currentTimeMillis()+".jpg";
        File file = new File(context.getFilesDir(), fileName);

        try{
            // Compress the bitmap and save in jpg format
            OutputStream stream;
            stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
            stream.flush();
            stream.close();
        }catch(IOException e){
            e.printStackTrace();
        }

        // Return the saved bitmap uri
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
    }
}

