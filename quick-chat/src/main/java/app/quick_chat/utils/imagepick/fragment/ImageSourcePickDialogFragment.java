package app.quick_chat.utils.imagepick.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import app.quick_chat.R;
import app.quick_chat.utils.ImageUtils;


public class ImageSourcePickDialogFragment extends DialogFragment {

    private static final int POSITION_GALLERY = 0;
    private static final int POSITION_CAMERA = 1;

    private OnImageSourcePickedListener onImageSourcePickedListener;

    public static void show(FragmentManager fm, OnImageSourcePickedListener onImageSourcePickedListener) {
        ImageSourcePickDialogFragment fragment = new ImageSourcePickDialogFragment();
        fragment.setOnImageSourcePickedListener(onImageSourcePickedListener);
        fragment.show(fm, ImageSourcePickDialogFragment.class.getSimpleName());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dlg_choose_image_from);
        builder.setItems(R.array.dlg_image_pick, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case POSITION_GALLERY:
                        onImageSourcePickedListener.onImageSourcePicked(ImageSource.GALLERY);
                        break;
                   case POSITION_CAMERA:
                        onImageSourcePickedListener.onImageSourcePicked(ImageSource.CAMERA);
                        break;
                }
            }
        });

        return builder.create();
    }

    public void setOnImageSourcePickedListener(OnImageSourcePickedListener onImageSourcePickedListener) {
        this.onImageSourcePickedListener = onImageSourcePickedListener;
    }

    public interface OnImageSourcePickedListener {

        void onImageSourcePicked(ImageSource source);
    }

    public enum ImageSource {
        GALLERY,
        CAMERA
    }

    public static class LoggableActivityImageSourcePickedListener implements OnImageSourcePickedListener {

        private AppCompatActivity activity;
        private Fragment fragment;

        public LoggableActivityImageSourcePickedListener(AppCompatActivity activity) {
            this.activity = activity;
        }

        public LoggableActivityImageSourcePickedListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onImageSourcePicked(ImageSource source) {
            switch (source) {
                case GALLERY:
                    if (fragment != null) {
                        ImageUtils.startImagePicker(fragment);
                    } else {
                        ImageUtils.startImagePicker(activity);
                    }
                    break;
            case CAMERA:
                    if (fragment != null) {
                        ImageUtils.startCameraForResult(fragment);
                    } else {
                        ImageUtils.startCameraForResult(activity);
                    }
                    break;
            }
        }
    }
}