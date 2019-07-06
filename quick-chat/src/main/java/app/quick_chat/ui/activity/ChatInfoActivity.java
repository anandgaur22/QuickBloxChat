package app.quick_chat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.users.model.QBUser;

import java.util.List;

import app.quick_chat.R;
import app.quick_chat.ui.adapter.UsersAdapter;
import app.quick_chat.utils.qb.QbUsersHolder;


public class ChatInfoActivity extends BaseActivity {
    private static final String EXTRA_DIALOG = "dialog";

    private ListView usersListView;
    private QBChatDialog qbDialog;

    public static void start(Context context, QBChatDialog qbDialog) {
        Intent intent = new Intent(context, ChatInfoActivity.class);
        intent.putExtra(EXTRA_DIALOG, qbDialog);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_chat);

        qbDialog = (QBChatDialog) getIntent().getSerializableExtra(EXTRA_DIALOG);
        usersListView = _findViewById(R.id.list_login_users);

        actionBar.setDisplayHomeAsUpEnabled(true);

        buildUserList();
    }

    @Override
    protected View getSnackbarAnchorView() {
        return usersListView;
    }

    private void buildUserList() {
        List<Integer> userIds = qbDialog.getOccupants();
        List<QBUser> users = QbUsersHolder.getInstance().getUsersByIds(userIds);

        UsersAdapter adapter = new UsersAdapter(this, users);
        usersListView.setAdapter(adapter);
    }
}
