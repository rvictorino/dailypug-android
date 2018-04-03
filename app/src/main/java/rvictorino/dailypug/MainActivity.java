package rvictorino.dailypug;

import android.app.WallpaperManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import rvictorino.dailypug.helper.ImageCropHelper;
import rvictorino.dailypug.service.RefreshDailyPictureJobService;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, WallpaperChanger {

    /*
    * TODO
    * icon: pug face
    * better ui
    * add proper logging everywhere
    * */

    private Switch pugSwitch;

    private JobScheduler jobScheduler = null;
    private JobInfo jobInfo = null;
    private static int jobId = 0;

    private WallpaperManager wpManager;
    private Toast wallpaperSetToast;
    private Toast wallpaperResetToast;

    private static final int HALF_DAY = 43200000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // view-related elements
        pugSwitch = findViewById(R.id.switch_button);
        pugSwitch.setOnCheckedChangeListener(this);

        // initialize job scheduler
        jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobInfo = new JobInfo.Builder(jobId, new ComponentName(this, RefreshDailyPictureJobService.class))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPeriodic(HALF_DAY) // ~12h in ms
            .build();

        // initialize wallpaper manager
        wpManager = WallpaperManager.getInstance(getApplicationContext());


        // toasts messages feedback
        wallpaperSetToast = Toast.makeText(getApplicationContext(), R.string.toast_success, Toast.LENGTH_SHORT);
        wallpaperResetToast = Toast.makeText(getApplicationContext(), R.string.toast_failure, Toast.LENGTH_SHORT);
    }

    @Override
    public void onCheckedChanged(CompoundButton switchView, boolean isChecked) {

        if(isChecked) {
            if(hasImageBeenSavedToday()) {
                Bitmap image = getStoredImage();
                setBitmapWallpaper(image);
            } else {
                // async download of pug picture
                DownloadPugPictureTask dTask = new DownloadPugPictureTask(this);
                dTask.execute();
            }

            // schedule future executions of same task
            jobScheduler.schedule(jobInfo);
        } else {
            // stop current job if needed
            jobScheduler.cancel(jobId);
            try {
                // reset wallpaper to defaults
                wpManager.clear();
                wallpaperResetToast.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void setBitmapWallpaper(Bitmap bitmap) {

        saveImage(bitmap);

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if(windowManager == null) {
            return;
        }

        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int deviceWidth = metrics.widthPixels;

        Bitmap cropped = ImageCropHelper.centerCropWallpaper(bitmap,
                Math.min(wpManager.getDesiredMinimumWidth(), wpManager.getDesiredMinimumHeight()),
                deviceWidth);


        try {
            wpManager.setBitmap(cropped);
            wallpaperSetToast.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
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
