package app.quick_chat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import app.quick_chat.App;
import app.quick_chat.R;
import app.quick_chat.ui.adapter.UsersAdapter;
import app.quick_chat.ui.dialog.ProgressDialogFragment;
import app.quick_chat.utils.ErrorUtils;
import app.quick_chat.utils.SharedPrefsHelper;
import app.quick_chat.utils.chat.ChatHelper;


public class LoginChatActivity extends CoreBaseActivity {

    private ListView userListView;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginChatActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_chat);
        userListView = _findViewById(R.id.list_login_users);
        TextView listHeader = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.include_list_hint_header, userListView, false);
        listHeader.setText(R.string.login_select_user_for_login);
        userListView.addHeaderView(listHeader, null, false);
        userListView.setOnItemClickListener(new OnUserLoginItemClickListener());
//        buildUsersList();
        QBUser user =new QBUser("Hello","12345678");
        login(user);
//        signUp(user);
    }
    private void buildUsersList() {
        List<String> tags = new ArrayList<>();
        tags.add(App.getSampleConfigs().getUsersTag());

        QBUser user =new QBUser("Hello","12345678");

        QBUsers.getUsersByTags(tags, null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                UsersAdapter adapter = new UsersAdapter(LoginChatActivity.this, result);
                userListView.setAdapter(adapter);
            }

            @Override
            public void onError(QBResponseException e) {
                ErrorUtils.showSnackbar(userListView, R.string.login_cant_obtain_users, e,
                        R.string.dlg_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                buildUsersList();
                            }
                        });
            }
        });
    }


    /*
    * sign up user and then login
    *
    * */
    private void signUp(final QBUser user){
        QBUsers.signUp(user).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                SharedPrefsHelper.getInstance().saveQbUser(user);
                DialogsActivity.start(LoginChatActivity.this);
                finish();

                ProgressDialogFragment.hide(getSupportFragmentManager());
                login(user);
            }

            @Override
            public void onError(QBResponseException e) {
                ErrorUtils.showSnackbar(userListView, R.string.login_chat_login_error, e,
                        R.string.dlg_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                signUp(user);
                            }
                        });
            }
        });
    }


    private void login(final QBUser user){
        ProgressDialogFragment.show(getSupportFragmentManager(), R.string.dlg_login);
        ChatHelper.getInstance().login(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
                SharedPrefsHelper.getInstance().saveQbUser(user);
                DialogsActivity.start(LoginChatActivity.this);
                finish();

                ProgressDialogFragment.hide(getSupportFragmentManager());
                Log.d("id==", String.valueOf(user.getId()));

            }

            @Override
            public void onError(QBResponseException e) {
                ProgressDialogFragment.hide(getSupportFragmentManager());
                ErrorUtils.showSnackbar(userListView, R.string.login_chat_login_error, e,
                        R.string.dlg_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                login(user);
                            }
                        });
            }
        });
    }

    private class OnUserLoginItemClickListener implements AdapterView.OnItemClickListener {

        public static final int LIST_HEADER_POSITION = 0;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == LIST_HEADER_POSITION) {
                return;
            }


            final QBUser user = (QBUser) parent.getItemAtPosition(position);
            // We use hardcoded password for all users for test purposes
            // Of course you shouldn't do that in your app
            user.setPassword(App.getSampleConfigs().getUsersPassword());

            login(user);
        }

    }
}
