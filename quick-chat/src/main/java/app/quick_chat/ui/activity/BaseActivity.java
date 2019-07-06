package app.quick_chat.ui.activity;

import android.os.Bundle;
import android.os.PersistableBundle;

import android.view.View;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;

import com.google.android.material.snackbar.Snackbar;

import app.quick_chat.R;
import app.quick_chat.utils.ErrorUtils;


public abstract class BaseActivity extends CoreBaseActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();

    protected ActionBar actionBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putInt("dummy_value", 0);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    protected abstract View getSnackbarAnchorView();

    protected Snackbar showErrorSnackbar(@StringRes int resId, Exception e,
                                         View.OnClickListener clickListener) {
        return ErrorUtils.showSnackbar(getSnackbarAnchorView(), resId, e,
                R.string.dlg_retry, clickListener);
    }

}