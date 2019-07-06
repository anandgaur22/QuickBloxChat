package app.quick_chat.utils.imagepick;



import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import app.quick_chat.ui.activity.ChatActivity;
import app.quick_chat.utils.imagepick.fragment.ImagePickHelperFragment;
import app.quick_chat.utils.imagepick.fragment.ImageSourcePickDialogFragment;


public class ImagePickHelper {

    public void pickAnImage(ChatActivity fragment, int requestCode) {
        ImagePickHelperFragment imagePickHelperFragment = ImagePickHelperFragment.start(fragment, requestCode);
        showImageSourcePickerDialog(fragment.getSupportFragmentManager(), imagePickHelperFragment);
    }

    public void pickAnImage(FragmentActivity activity, int requestCode) {
        ImagePickHelperFragment imagePickHelperFragment = ImagePickHelperFragment.start(activity, requestCode);
        showImageSourcePickerDialog(activity.getSupportFragmentManager(), imagePickHelperFragment);
    }

    private void showImageSourcePickerDialog(FragmentManager fm, ImagePickHelperFragment fragment) {
        ImageSourcePickDialogFragment.show(fm,
                new ImageSourcePickDialogFragment.LoggableActivityImageSourcePickedListener(fragment));
    }
}
