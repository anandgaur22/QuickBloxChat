package app.quick_chat.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;

import java.util.List;

import app.quick_chat.R;
import app.quick_chat.utils.ResourceUtils;
import app.quick_chat.utils.UiUtils;
import app.quick_chat.utils.qb.QbDialogUtils;


public class DialogsAdapter extends BaseSelectableListAdapter<QBChatDialog> {

    private static final String EMPTY_STRING = "";

    public DialogsAdapter(Context context, List<QBChatDialog> dialogs) {
        super(context, dialogs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        QBChatDialog dialog = getItem(position);

        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_dialog, parent, false);

            holder = new ViewHolder();
            holder.rootLayout = (ViewGroup) convertView.findViewById(R.id.root);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.text_dialog_name);
            holder.lastMessageTextView = (TextView) convertView.findViewById(R.id.text_dialog_last_message);
            holder.dialogImageView = (ImageView) convertView.findViewById(R.id.image_dialog_icon);
            holder.unreadCounterTextView = (TextView) convertView.findViewById(R.id.text_dialog_unread_count);
            holder.relative = convertView.findViewById(R.id.relative);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String dialogImage = QbDialogUtils.getDialogImage(dialog);
        //String dialogImage1=QbDialogUtils.getCustomData(dialog);

        RequestOptions options = new RequestOptions().override(200, 200);

        // Glide.with(context).load(dialogImage1).apply(options.optionalCircleCrop()).into( holder.dialogImageView);
        Log.d("dialogImage1==", dialogImage);
        if (!TextUtils.isEmpty(dialogImage))
            Glide.with(context).load(dialogImage).apply(options.optionalCircleCrop()).into(holder.dialogImageView);
        else if (dialog.getType().equals(QBDialogType.GROUP)) {
            holder.dialogImageView.setBackgroundDrawable(UiUtils.getGreyCircleDrawable());
            holder.dialogImageView.setImageResource(R.drawable.ic_chat_group);
        } else {
            holder.dialogImageView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(position));
            holder.dialogImageView.setImageDrawable(null);
        }

        Log.e("DIALOG CUSTOM DATA ", QbDialogUtils.getCustomData(dialog));

        holder.nameTextView.setText(QbDialogUtils.getDialogName(dialog));
        holder.lastMessageTextView.setText(prepareTextLastMessage(dialog));

        int unreadMessagesCount = getUnreadMsgCount(dialog);
        if (unreadMessagesCount == 0) {
            holder.unreadCounterTextView.setVisibility(View.GONE);
            holder.relative.setVisibility(View.GONE);
        } else {
            holder.relative.setVisibility(View.VISIBLE);
            holder.unreadCounterTextView.setVisibility(View.VISIBLE);
            holder.unreadCounterTextView.setText(String.valueOf(unreadMessagesCount > 99 ? 99 : unreadMessagesCount));
        }

        holder.rootLayout.setBackgroundColor(isItemSelected(position) ? ResourceUtils.getColor(R.color.selected_list_item_color) :
                ResourceUtils.getColor(android.R.color.transparent));

        return convertView;
    }

    private int getUnreadMsgCount(QBChatDialog chatDialog) {
        Integer unreadMessageCount = chatDialog.getUnreadMessageCount();
        if (unreadMessageCount == null) {
            return 0;
        } else {
            return unreadMessageCount;
        }
    }

    private boolean isLastMessageAttachment(QBChatDialog dialog) {
        String lastMessage = dialog.getLastMessage();
        Integer lastMessageSenderId = dialog.getLastMessageUserId();
        return TextUtils.isEmpty(lastMessage) && lastMessageSenderId != null;
    }

    private String prepareTextLastMessage(QBChatDialog chatDialog) {
        if (isLastMessageAttachment(chatDialog)) {
            return context.getString(R.string.chat_attachment);
        } else {
            return chatDialog.getLastMessage();
        }
    }

    private static class ViewHolder {
        ViewGroup rootLayout;
        ImageView dialogImageView;
        TextView nameTextView;
        TextView lastMessageTextView;
        TextView unreadCounterTextView;
        RelativeLayout relative;
    }
}
