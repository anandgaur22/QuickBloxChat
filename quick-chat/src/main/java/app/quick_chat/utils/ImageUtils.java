package app.quick_chat.utils;

import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.quick_chat.CoreApp;
import app.quick_chat.R;
import app.quick_chat.utils.constant.MimeType;

public class ImageUtils {

    public static final int GALLERY_REQUEST_CODE = 183;
    public static final int CAMERA_REQUEST_CODE = 212;

    private static final String CAMERA_FILE_NAME_PREFIX = "CAMERA_";

    private ImageUtils() {
    }

    public static String saveUriToFile(Uri uri) throws Exception {
        ParcelFileDescriptor parcelFileDescriptor = CoreApp.getInstance().getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        InputStream inputStream = new FileInputStream(fileDescriptor);
        BufferedInputStream bis = new BufferedInputStream(inputStream);

        File parentDir = StorageUtils.getAppExternalDataDirectoryFile();
        String fileName = String.valueOf(System.currentTimeMillis()) + ".jpg";
        File resultFile = new File(parentDir, fileName);

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(resultFile));

        byte[] buf = new byte[2048];
        int length;

        try {
            while ((length = bis.read(buf)) > 0) {
                bos.write(buf, 0, length);
            }
        } catch (Exception e) {
            throw new IOException("Can\'t save Storage API bitmap to a file!", e);
        } finally {
            parcelFileDescriptor.close();
            bis.close();
            bos.close();
        }

        return resultFile.getAbsolutePath();
    }

    public static void startImagePicker(AppCompatActivity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(MimeType.IMAGE_MIME);
        activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.dlg_choose_image_from)), GALLERY_REQUEST_CODE);
    }

    public static void startImagePicker(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(MimeType.IMAGE_MIME);
        fragment.startActivityForResult(Intent.createChooser(intent, fragment.getString(R.string.dlg_choose_image_from)), GALLERY_REQUEST_CODE);
    }

    public static void startCameraForResult(AppCompatActivity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) == null) {
            return;
        }

        File photoFile = getTemporaryCameraFile();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        activity.startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }



    public static File getTemporaryCameraFile() {
        File storageDir = StorageUtils.getAppExternalDataDirectoryFile();
        File file = new File(storageDir, getTemporaryCameraFileName());
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File getLastUsedCameraFile() {
        File dataDir = StorageUtils.getAppExternalDataDirectoryFile();
        File[] files = dataDir.listFiles();
        List<File> filteredFiles = new ArrayList<>();
        for (File file : files) {
            if (file.getName().startsWith(CAMERA_FILE_NAME_PREFIX)) {
                filteredFiles.add(file);
            }
        }

        Collections.sort(filteredFiles);
        if (!filteredFiles.isEmpty()) {
            return filteredFiles.get(filteredFiles.size() - 1);
        } else {
            return null;
        }
    }

    private static String getTemporaryCameraFileName() {
        return CAMERA_FILE_NAME_PREFIX + System.currentTimeMillis() + ".jpg";
    }

    public static void startCameraForResult(Fragment fragment) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(CoreApp.getInstance().getPackageManager()) == null) {
            return;
        }
        File photoFile = getTemporaryCameraFile();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(CoreApp.getInstance().getApplicationContext(), CoreApp.getInstance().getApplicationContext().getPackageName() + ".app.quick_chat.provider", photoFile));
        fragment.startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }
}
