package com.anand.quickbloxchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import app.quick_chat.utils.SharedPrefsHelper;
import app.quick_chat.utils.qb.QbUsersHolder;

public class SignUpActivity extends AppCompatActivity {

    EditText username_et,password_et,email_et;
    Button signup_btn;
    int quick_blox_id;
    String emailid,firstName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        username_et=findViewById(R.id.username_et);
        password_et=findViewById(R.id.password_et);
        email_et=findViewById(R.id.email_et);
        signup_btn=findViewById(R.id.signup_btn);

        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firstName=username_et.getText().toString();
                emailid=email_et.getText().toString();

                chatSignUp();

            }
        });

    }

    public void chatSignUp() {
        final QBUser user = new QBUser(emailid, "12345678");
        user.setEmail(emailid);
        user.setFullName(firstName);
        user.setPhone("+18904561812");
        StringifyArrayList<String> tags = new StringifyArrayList<String>();
        tags.add("car");
        tags.add("man");
        user.setTags(tags);
        user.setWebsite("www.mysite.com");
        QBUsers.signUp(user).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                SharedPrefsHelper.getInstance().saveQbUser(user);
                Log.d("data==", String.valueOf(user.getId()));
                quick_blox_id = user.getId();


                Toast.makeText(SignUpActivity.this, "Success....."+quick_blox_id, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);

            }

            @Override
            public void onError(QBResponseException e) {


            }
        });
    }


}
