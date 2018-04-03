package rvictorino.dailypug.service;

import android.app.WallpaperManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import rvictorino.dailypug.ApplicationConstants;
import rvictorino.dailypug.DownloadPugPictureTask;
import rvictorino.dailypug.WallpaperChanger;
import rvictorino.dailypug.helper.ImageCropHelper;

public class RefreshDailyPictureJobService extends JobService implements WallpaperChanger {

    private DownloadPugPictureTask pictureDlTask;

    private WallpaperManager wpManager;

    private JobParameters jobParams;

    public RefreshDailyPictureJobService() {
        pictureDlTask = new DownloadPugPictureTask(this);
    }

    @Override
    public boolean onStartJob(final JobParameters params) {

        jobParams = params;

        if(hasImageBeenSavedToday()) {
            Bitmap image = getStoredImage();
            setBitmapWallpaper(image);
        } else {
            // async download of pug picture
            DownloadPugPictureTask dTask = new DownloadPugPictureTask(this);
            pictureDlTask.execute();
        }

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // stop task (which is in another thread) when job is finished
        if(pictureDlTask != null) {
            pictureDlTask.cancel(true);
        }
        return false;
    }


    @Override
    public void setBitmapWallpaper(Bitmap bitmap) {

        saveImage(bitmap);

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if(windowManager == null) {
            return;
        }

        // must be initialized once service is instantiated
        wpManager = WallpaperManager.getInstance(getApplicationContext());

        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int deviceWidth = metrics.widthPixels;

        Bitmap cropped = ImageCropHelper.centerCropWallpaper(bitmap,
                Math.min(wpManager.getDesiredMinimumWidth(), wpManager.getDesiredMinimumHeight()),
                deviceWidth);

        try {
            wpManager.setBitmap(cropped);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {
            jobFinished(jobParams, false);
        }
    }


    @Override
    public boolean saveImage(Bitmap bitmap) {
        FileOutputStream outputStream = null;

        boolean success;

        try {
            outputStream = openFileOutput(ApplicationConstants.FILE_NAME, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            success = true;
        } catch (IOException e) {
            // Error while creating file
            e.printStackTrace();
            success = false;
        } finally {
            if(outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    success = false;
                }
            }
        }

        return success;
    }

    @Override
    public Bitmap getStoredImage() {
        try {
            return BitmapFactory.decodeFile(getFilesDir().getPath() + "/" + ApplicationConstants.FILE_NAME);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean hasImageBeenSavedToday() {

        Date lastModified;
        Date now = new Date();

        File savedImage = new File(getFilesDir() + "/" + ApplicationConstants.FILE_NAME);
        if(!savedImage.exists()) {
            return false;
        }

        lastModified = new Date(savedImage.lastModified());

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(lastModified);
        cal2.setTime(now);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
