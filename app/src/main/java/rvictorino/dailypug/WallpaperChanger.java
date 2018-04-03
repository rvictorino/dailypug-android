package rvictorino.dailypug;

import android.graphics.Bitmap;

import java.util.Date;

public interface WallpaperChanger {
    /**
     * Set wallpaper using given image
     * @param bitmap image to set as wallpapaer
     */
    void setBitmapWallpaper(Bitmap bitmap);

    boolean saveImage(Bitmap bitmap);

    Bitmap getStoredImage();

    boolean hasImageBeenSavedToday();
}
