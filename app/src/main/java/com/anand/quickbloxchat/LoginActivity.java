package com.anand.quickbloxchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

import app.quick_chat.ui.activity.DialogsActivity;
import app.quick_chat.utils.SharedPrefsHelper;
import app.quick_chat.utils.chat.ChatHelper;

public class LoginActivity extends AppCompatActivity {

    EditText username_et, password_et;
    Button signin_btn, signup_btn;
    String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username_et = findViewById(R.id.username_et);
        password_et = findViewById(R.id.password_et);
        signin_btn = findViewById(R.id.signin_btn);
        signup_btn = findViewById(R.id.signup_btn);

        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                username = username_et.getText().toString();
                password = password_et.getText().toString();

                ChatLogin(username);
            }
        });

    }

    public void ChatLogin(String username) {
        final QBUser user = new QBUser(username, "12345678");
        ChatHelper.getInstance().login(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
                SharedPrefsHelper.getInstance().saveQbUser(user);
                Log.d("id==", String.valueOf(user.getId()));
               // loginToChat(user, LoginActivity.this);

                Toast.makeText(getApplicationContext(), "login success.....", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onError(QBResponseException e) {
            }
        });
    }

    private void loginToChat(final QBUser user, final Context context) {

        ChatHelper.getInstance().loginToChat(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
                //This method is calling to subscribe push notification



            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("Chat login onError(): ", "" + e);

                Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
