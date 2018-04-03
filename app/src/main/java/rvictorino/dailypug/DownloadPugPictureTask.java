package rvictorino.dailypug;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadPugPictureTask extends AsyncTask<Void, Void, Bitmap> {

    private URL targetURL = null;

    private WallpaperChanger wallpaperChanger;

    private static final String targetURLString = "https://dailypug.ovh/image.png";


    public DownloadPugPictureTask(WallpaperChanger wallpaperChanger) {

        this.wallpaperChanger = wallpaperChanger;

        try {
            targetURL = new URL(targetURLString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {

        Bitmap bitmap = null;


        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(targetURL)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        InputStream inputStream;

        if(response != null && response.body() != null) {
            inputStream = response.body().byteStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        }

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap image) {
        super.onPostExecute(image);
        if(image != null) {
            wallpaperChanger.setBitmapWallpaper(image);
        }
    }
}
