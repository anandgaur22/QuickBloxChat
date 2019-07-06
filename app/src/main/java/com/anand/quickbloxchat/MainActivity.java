package com.anand.quickbloxchat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

import app.quick_chat.ui.activity.DialogsActivity;
import app.quick_chat.ui.dialog.ProgressDialogFragment;
import app.quick_chat.utils.ICallback;
import app.quick_chat.utils.chat.ChatHelper;

import static app.quick_chat.ui.activity.ChatActivity.startForResult;

public class MainActivity extends AppCompatActivity {

    ICallback iCallback;

    String chatId="94260487";

    Button btn_chat,btn_chat_all;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_chat=findViewById(R.id.btn_chat);
        btn_chat_all=findViewById(R.id.btn_chat_all);

        btn_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ProgressDialogFragment.show(getSupportFragmentManager());
                createDialog(MainActivity.this, Integer.parseInt(chatId));

            }
        });

        btn_chat_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DialogsActivity.start(MainActivity.this);

            }
        });


    }


    public void createDialog(Activity context, int chatId) {


        Log.e("chatId---", "" + chatId);
        QBUser user = new QBUser();
        user.setId(chatId);

        ArrayList<QBUser> qbUserArrayList = new ArrayList<QBUser>();
        qbUserArrayList.add(user);


        ChatHelper.getInstance().createDialogWithSelectedUsers(qbUserArrayList,
                new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog dialog, Bundle args) {

                        startForResult(MainActivity.this, 174, dialog, "", iCallback);
                        ProgressDialogFragment.hide(getSupportFragmentManager());
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        ProgressDialogFragment.hide(getSupportFragmentManager());
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        iCallback = new ICallback() {
            @Override
            public void onSuccess(String data) {

            }
        };
    }
}
