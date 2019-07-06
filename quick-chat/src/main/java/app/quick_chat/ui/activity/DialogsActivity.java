package app.quick_chat.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.view.ActionMode;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.messages.services.SubscribeService;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import app.quick_chat.R;
import app.quick_chat.gcm.GooglePlayServicesHelper;
import app.quick_chat.managers.DialogsManager;
import app.quick_chat.ui.adapter.DialogsAdapter;
import app.quick_chat.ui.dialog.ProgressDialogFragment;
import app.quick_chat.utils.SharedPreferenceConstants;
import app.quick_chat.utils.SharedPrefsHelper;
import app.quick_chat.utils.chat.ChatHelper;
import app.quick_chat.utils.constant.GcmConsts;
import app.quick_chat.utils.qb.QbChatDialogMessageListenerImp;
import app.quick_chat.utils.qb.QbDialogHolder;
import app.quick_chat.utils.qb.QbDialogUtils;
import app.quick_chat.utils.qb.callback.QbEntityCallbackImpl;


public class DialogsActivity extends BaseActivity
        implements DialogsManager.ManagingDialogsCallbacks {
    private static final String TAG = DialogsActivity.class.getSimpleName();
    private static final int REQUEST_SELECT_PEOPLE = 174;
    private static final int REQUEST_DIALOG_ID_FOR_UPDATE = 165;
    public static List<QBChatDialog> dummydialog = new ArrayList<>();
    public static DialogsAdapter dialogsAdapter;
    EditText searchmessages;
    RelativeLayout iv_back;
    CardView CardViewsearchmessages;
    SharedPreferences sharedPreferences;
    LinearLayout emptyHintLayout;
    private ProgressBar progressBar;
    private FloatingActionButton fab;
    private ActionMode currentActionMode;
    private SwipyRefreshLayout setOnRefreshListener;
    private QBRequestGetBuilder requestBuilder;
    private Menu menu;
    private int skipRecords = 0;
    private boolean isProcessingResultInProgress;
    private BroadcastReceiver pushBroadcastReceiver;
    private GooglePlayServicesHelper googlePlayServicesHelper;
    private QBChatDialogMessageListener allDialogsMessagesListener;
    private SystemMessagesListener systemMessagesListener;
    private QBSystemMessagesManager systemMessagesManager;
    private QBIncomingMessagesManager incomingMessagesManager;
    private DialogsManager dialogsManager;
    private QBUser currentUser;
    ListView dialogsListView;

    public static void start(Context context) {
        Intent intent = new Intent(context, DialogsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs);
//        progressBar.setVisibility(View.GONE);

        //getSupportActionBar().hide();
        googlePlayServicesHelper = new GooglePlayServicesHelper();
        pushBroadcastReceiver = new PushBroadcastReceiver();
        allDialogsMessagesListener = new AllDialogsMessageListener();
        systemMessagesListener = new SystemMessagesListener();
        dialogsManager = new DialogsManager();
        currentUser = ChatHelper.getCurrentUser();
        initUi();
//        setActionBarTitle(getString(R.string.dialogs_logged_in_as, currentUser.getFullName()));

        registerQbChatListeners();
        if (QbDialogHolder.getInstance().getDialogs().size() > 0) {
            loadDialogsFromQb(true, true);
        } else {
            loadDialogsFromQb(false, true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        googlePlayServicesHelper.checkPlayServicesAvailable(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver,
                new IntentFilter(GcmConsts.ACTION_NEW_GCM_EVENT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterQbChatListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_dialogs, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isProcessingResultInProgress) {
            return super.onOptionsItemSelected(item);
        }

        int i = item.getItemId();
        if (i == R.id.menu_dialogs_action_logout) {
            userLogout();
            item.setEnabled(false);
            invalidateOptionsMenu();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            isProcessingResultInProgress = true;
            if (requestCode == REQUEST_SELECT_PEOPLE) {
                ArrayList<QBUser> selectedUsers = (ArrayList<QBUser>) data
                        .getSerializableExtra(SelectUsersActivity.EXTRA_QB_USERS);
                if (isPrivateDialogExist(selectedUsers)) {
                    selectedUsers.remove(ChatHelper.getCurrentUser());
                    QBChatDialog existingPrivateDialog = QbDialogHolder.getInstance().getPrivateDialogWithUser(selectedUsers.get(0));
                    isProcessingResultInProgress = false;
                    String dialogImage = QbDialogUtils.getDialogImage(existingPrivateDialog);

                    ChatActivity.startForResult(DialogsActivity.this, REQUEST_DIALOG_ID_FOR_UPDATE, existingPrivateDialog, dialogImage, null);
                    finish();
                } else {
                    ProgressDialogFragment.show(getSupportFragmentManager(), R.string.create_chat);
                    createDialog(selectedUsers);
                }
            } else if (requestCode == REQUEST_DIALOG_ID_FOR_UPDATE) {
                if (data != null) {
                    String dialogId = data.getStringExtra(ChatActivity.EXTRA_DIALOG_ID);
                    loadUpdatedDialog(dialogId);
                } else {
                    isProcessingResultInProgress = false;
                    updateDialogsList();
                }
            }
        } else {
            updateDialogsAdapter();
        }
    }

    private boolean isPrivateDialogExist(ArrayList<QBUser> allSelectedUsers) {
        ArrayList<QBUser> selectedUsers = new ArrayList<>();
        selectedUsers.addAll(allSelectedUsers);
        selectedUsers.remove(ChatHelper.getCurrentUser());
        return selectedUsers.size() == 1 && QbDialogHolder.getInstance().hasPrivateDialogWithUser(selectedUsers.get(0));
    }

    private void loadUpdatedDialog(String dialogId) {
        ChatHelper.getInstance().getDialogById(dialogId, new QbEntityCallbackImpl<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog result, Bundle bundle) {
                isProcessingResultInProgress = false;
                QbDialogHolder.getInstance().addDialog(result);
                updateDialogsAdapter();
            }

            @Override
            public void onError(QBResponseException e) {
                isProcessingResultInProgress = false;
            }
        });
    }

    @Override
    protected View getSnackbarAnchorView() {
        return findViewById(R.id.layout_root);
    }

    @Override
    public ActionMode startSupportActionMode(ActionMode.Callback callback) {
        currentActionMode = super.startSupportActionMode(callback);
        return currentActionMode;
    }

    private void userLogout() {
        ChatHelper.getInstance().destroy();
        SubscribeService.unSubscribeFromPushes(DialogsActivity.this);
        SharedPrefsHelper.getInstance().removeQbUser();
        LoginChatActivity.start(DialogsActivity.this);
        QbDialogHolder.getInstance().clear();
        ProgressDialogFragment.hide(getSupportFragmentManager());
        finish();
    }

    private void updateDialogsList() {
        requestBuilder.setSkip(skipRecords = 0);
        loadDialogsFromQb(true, true);
    }

    /*
     *
     * click on floating action button
     * */
    public void onStartNewChatClick(View view) {
        // SelectUsersActivity.startForResult(this, REQUEST_SELECT_PEOPLE);
    }

    private void initUi() {
        emptyHintLayout = _findViewById(R.id.layout_chat_empty);
         dialogsListView = _findViewById(R.id.list_dialogs_chats);
        progressBar = _findViewById(R.id.progress_dialogs);
        progressBar.setVisibility(View.VISIBLE);

        fab = _findViewById(R.id.fab_dialogs_new_chat);
        searchmessages = _findViewById(R.id.searchmessages);
        iv_back = _findViewById(R.id.host_back_btn);
        CardViewsearchmessages = _findViewById(R.id.CardViewsearchmessages);
        sharedPreferences = getSharedPreferences(SharedPreferenceConstants.PREF, MODE_PRIVATE);

        setOnRefreshListener = _findViewById(R.id.swipy_refresh_layout);
        dialogsAdapter = new DialogsAdapter(this, new ArrayList<>(QbDialogHolder.getInstance().getDialogs().values()));

        TextView listHeader = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.include_list_hint_header, dialogsListView, false);
        listHeader.setText(R.string.dialogs_list_hint);
        // dialogsListView.setEmptyView(emptyHintLayout);
        dialogsListView.addHeaderView(listHeader, null, false);
//
        dialogsListView.removeHeaderView(listHeader);

        dialogsListView.setAdapter(dialogsAdapter);

        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
                String dialogImage = QbDialogUtils.getDialogImage(selectedDialog);

                if (currentActionMode == null) {
                    sharedPreferences.edit().putString(SharedPreferenceConstants.userFriendsName, selectedDialog.getName()).apply();
                    sharedPreferences.edit().putString(SharedPreferenceConstants.otherprofilepic, dialogImage).apply();
                    Log.d("img==", dialogImage);
                    ChatActivity.startForResult(DialogsActivity.this, REQUEST_DIALOG_ID_FOR_UPDATE, selectedDialog, "", null);
                } else {
                    dialogsAdapter.toggleSelection(selectedDialog);
                }
            }
        });
        dialogsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
                startSupportActionMode(new DeleteActionModeCallback());
                dialogsAdapter.selectItem(selectedDialog);
                return true;
            }
        });
        requestBuilder = new QBRequestGetBuilder();

        setOnRefreshListener.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                requestBuilder.setSkip(skipRecords += ChatHelper.DIALOG_ITEMS_PER_PAGE);
                loadDialogsFromQb(true, false);
            }
        });
        doSearch();
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void doSearch() {

        searchmessages.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
               /* if (s.toString().length()>2)
                dialogsAdapter.getFilter().filter(s.toString());
*/
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = searchmessages.getText().toString().toLowerCase(Locale.getDefault());
                filter(text);
            }
        });
    }

    private void registerQbChatListeners() {
        incomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
        systemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();

        if (incomingMessagesManager != null) {
            incomingMessagesManager.addDialogMessageListener(allDialogsMessagesListener != null
                    ? allDialogsMessagesListener : new AllDialogsMessageListener());
        }

        if (systemMessagesManager != null) {
            systemMessagesManager.addSystemMessageListener(systemMessagesListener != null
                    ? systemMessagesListener : new SystemMessagesListener());
        }

        dialogsManager.addManagingDialogsCallbackListener(this);
    }

    private void unregisterQbChatListeners() {
        if (incomingMessagesManager != null) {
            incomingMessagesManager.removeDialogMessageListrener(allDialogsMessagesListener);
        }

        if (systemMessagesManager != null) {
            systemMessagesManager.removeSystemMessageListener(systemMessagesListener);
        }

        dialogsManager.removeManagingDialogsCallbackListener(this);
    }

    private void createDialog(final ArrayList<QBUser> selectedUsers) {
        ChatHelper.getInstance().createDialogWithSelectedUsers(selectedUsers,
                new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog dialog, Bundle args) {
                        isProcessingResultInProgress = false;
                        dialogsManager.sendSystemMessageAboutCreatingDialog(systemMessagesManager, dialog);
                        ChatActivity.startForResult(DialogsActivity.this, REQUEST_DIALOG_ID_FOR_UPDATE, dialog, "", null);
                        ProgressDialogFragment.hide(getSupportFragmentManager());
                        finish();  //   by me
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        isProcessingResultInProgress = false;
                        ProgressDialogFragment.hide(getSupportFragmentManager());
                        showErrorSnackbar(R.string.dialogs_creation_error, null, null);
                    }
                }
        );
    }

    private void loadDialogsFromQb(final boolean silentUpdate, final boolean clearDialogHolder) {
        isProcessingResultInProgress = true;
        if (!silentUpdate) {
            progressBar.setVisibility(View.VISIBLE);
        }
        ChatHelper.getInstance().getDialogs(requestBuilder, new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> dialogs, Bundle bundle) {
                isProcessingResultInProgress = false;
                progressBar.setVisibility(View.GONE);
                setOnRefreshListener.setRefreshing(false);
                if (clearDialogHolder) {
                    QbDialogHolder.getInstance().clear();
                }
                QbDialogHolder.getInstance().addDialogs(dialogs);
                dummydialog.addAll(dialogs);
                updateDialogsAdapter();
            }

            @Override
            public void onError(QBResponseException e) {
                isProcessingResultInProgress = false;
                progressBar.setVisibility(View.GONE);
                setOnRefreshListener.setRefreshing(false);
                Toast.makeText(DialogsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDialogsAdapter() {
        dialogsAdapter.updateList(new ArrayList<>(QbDialogHolder.getInstance().getDialogs().values()));
        if (QbDialogHolder.getInstance().getDialogs().values().size() > 0) {
            CardViewsearchmessages.setVisibility(View.VISIBLE);
            emptyHintLayout.setVisibility(View.GONE);
            dialogsListView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);


        } else {
            emptyHintLayout.setVisibility(View.VISIBLE);
            dialogsListView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);

        }
    }

    @Override
    public void onDialogCreated(QBChatDialog chatDialog) {
        progressBar.setVisibility(View.VISIBLE);

        updateDialogsAdapter();
    }

    @Override
    public void onDialogUpdated(String chatDialog) {
        progressBar.setVisibility(View.VISIBLE);

        updateDialogsAdapter();
    }

    @Override
    public void onNewDialogLoaded(QBChatDialog chatDialog) {
        progressBar.setVisibility(View.VISIBLE);

        updateDialogsAdapter();
    }

    private void filter(String filter) {
        filter = filter.toLowerCase(Locale.getDefault());
        QbDialogHolder.getInstance().clear();
        if (filter.length() == 0) {
            QbDialogHolder.getInstance().addDialogs(dummydialog);
        } else {

            final List<QBChatDialog> list = dummydialog;
            int count = list.size();
            QBChatDialog filterableString;

            for (QBChatDialog wp : dummydialog) {
                if (wp.getName().toLowerCase(Locale.getDefault())
                        .contains(filter)) {
                    QbDialogHolder.getInstance().addDialogs(Collections.singletonList(wp));

                    //  friendsListBeanList .add(wp);
                }
            }
           /* for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                if (filterableString.getName().toLowerCase().contains(filter)) {
                    QbDialogHolder.getInstance().addDialogs(dummydialog);

                }
            }*/

        }
        updateDialogsAdapter();
        /*RequestAdapter chatAdapter = new RequestAdapter();
        recyclerView1.setAdapter(chatAdapter);*/
    }

    private class DeleteActionModeCallback implements ActionMode.Callback {

        public DeleteActionModeCallback() {
            fab.hide();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.action_mode_dialogs, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int i = item.getItemId();
            if (i == R.id.menu_dialogs_action_delete) {
                deleteSelectedDialogs();
                if (currentActionMode != null) {
                    currentActionMode.finish();
                }
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            currentActionMode = null;
            dialogsAdapter.clearSelection();
            fab.show();
        }

        private void deleteSelectedDialogs() {
            final Collection<QBChatDialog> selectedDialogs = dialogsAdapter.getSelectedItems();
            ChatHelper.getInstance().deleteDialogs(selectedDialogs, new QBEntityCallback<ArrayList<String>>() {
                @Override
                public void onSuccess(ArrayList<String> dialogsIds, Bundle bundle) {
                    QbDialogHolder.getInstance().deleteDialogs(dialogsIds);
                    updateDialogsAdapter();
                }

                @Override
                public void onError(QBResponseException e) {
                    showErrorSnackbar(R.string.dialogs_deletion_error, e,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteSelectedDialogs();
                                }
                            });
                }
            });
        }
    }

    private class PushBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(GcmConsts.EXTRA_GCM_MESSAGE);
            Log.e(TAG, "Received broadcast " + intent.getAction() + " with data: " + message);
            requestBuilder.setSkip(skipRecords = 0);
            loadDialogsFromQb(true, true);
        }
    }

    private class SystemMessagesListener implements QBSystemMessageListener {
        @Override
        public void processMessage(final QBChatMessage qbChatMessage) {
            dialogsManager.onSystemMessageReceived(qbChatMessage);
        }

        @Override
        public void processError(QBChatException e, QBChatMessage qbChatMessage) {
        }
    }

    private class AllDialogsMessageListener extends QbChatDialogMessageListenerImp {
        @Override
        public void processMessage(final String dialogId, final QBChatMessage qbChatMessage, Integer senderId) {
            if (!senderId.equals(ChatHelper.getCurrentUser().getId())) {
                dialogsManager.onGlobalMessageReceived(dialogId, qbChatMessage);
            }
        }
    }
}
