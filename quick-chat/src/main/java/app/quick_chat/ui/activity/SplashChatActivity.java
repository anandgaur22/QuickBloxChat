package app.quick_chat.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

import app.quick_chat.App;
import app.quick_chat.R;
import app.quick_chat.ui.dialog.ProgressDialogFragment;
import app.quick_chat.utils.SharedPrefsHelper;
import app.quick_chat.utils.chat.ChatHelper;

import static app.quick_chat.utils.ResourceUtils.getString;


public class SplashChatActivity extends CoreSplashActivity {

    private static final String TAG = SplashChatActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkConfigsWithSnackebarError()){
            proceedToTheNextActivityWithDelay();
        }
    }

    @Override
    protected String getAppName() {
        return getString(R.string.splash_app_title);
    }

    @Override
    protected void proceedToTheNextActivity() {
        if (checkSignIn()) {
            restoreChatSession();
        } else {
            LoginChatActivity.start(this);
            finish();
        }
    }

    @Override
    protected boolean sampleConfigIsCorrect() {
        boolean result = super.sampleConfigIsCorrect();
        result = result && App.getSampleConfigs() != null;
        return result;
    }

    private void restoreChatSession(){
        if (ChatHelper.getInstance().isLogged()) {
            DialogsActivity.start(this);
            finish();
        } else {
            QBUser currentUser = getUserFromSession();
            loginToChat(currentUser);
        }
    }

  /*
  *
  * get user from sharedpreference is user is login
  *
  * */

    private QBUser getUserFromSession(){
        QBUser user = SharedPrefsHelper.getInstance().getQbUser();
        user.setId(QBSessionManager.getInstance().getSessionParameters().getUserId());

        Log.e("saved user name",user.getLogin()+"  "+user.getPassword());

        return user;
    }

    @Override
    protected boolean checkSignIn() {
        return SharedPrefsHelper.getInstance().hasQbUser();
    }

    private void loginToChat(final QBUser user) {
        ProgressDialogFragment.show(getSupportFragmentManager(), R.string.dlg_restoring_chat_session);

        ChatHelper.getInstance().loginToChat(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
                Log.e(TAG, "Chat login onSuccess()");

                ProgressDialogFragment.hide(getSupportFragmentManager());
                DialogsActivity.start(SplashChatActivity.this);
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                ProgressDialogFragment.hide(getSupportFragmentManager());
                Log.e(TAG, "Chat login onError(): " + e);
                showSnackbarError( findViewById(R.id.layout_root), R.string.error_recreate_session, e,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loginToChat(user);
                            }
                        });
            }
        });
    }
}