package app.quick_chat.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;

import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import app.quick_chat.R;
import app.quick_chat.ui.activity.AttachmentImageActivity;
import app.quick_chat.utils.Consts;
import app.quick_chat.utils.ResourceUtils;
import app.quick_chat.utils.SharedPrefsHelper;
import app.quick_chat.utils.TimeUtils;
import app.quick_chat.utils.chat.ChatHelper;
import app.quick_chat.utils.qb.PaginationHistoryListener;
import app.quick_chat.utils.qb.QbUsersHolder;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class ChatAdapter extends BaseListAdapter<QBChatMessage> implements StickyListHeadersAdapter {

    private static final String TAG = ChatAdapter.class.getSimpleName();
    private final QBChatDialog chatDialog;
    private OnItemInfoExpandedListener onItemInfoExpandedListener;
    private PaginationHistoryListener paginationListener;
    private int previousGetCount = 0;
    private boolean hideTime = false;

    public ChatAdapter(Context context, QBChatDialog chatDialog, List<QBChatMessage> chatMessages) {
        super(context, chatMessages);
        this.chatDialog = chatDialog;
    }

    public void setOnItemInfoExpandedListener(OnItemInfoExpandedListener onItemInfoExpandedListener) {
        this.onItemInfoExpandedListener = onItemInfoExpandedListener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final QBChatMessage chatMessage = getItem(position);
//        if (convertView == null) {
        holder = new ViewHolder();
        boolean _isIncoming = isIncoming(chatMessage);
        if (_isIncoming)
            convertView = inflater.inflate(R.layout.list_item_chat_incoming_message, parent, false);
        else
            convertView = inflater.inflate(R.layout.list_item_chat_outgoing_message, parent, false);

        holder.messageBodyTextView = (TextView) convertView.findViewById(R.id.text_image_message);
        holder.messageAuthorTextView = (TextView) convertView.findViewById(R.id.text_message_author);
        holder.messageContainerLayout = convertView.findViewById(R.id.layout_chat_message_container);
        holder.messageBodyContainerLayout = (RelativeLayout) convertView.findViewById(R.id.layout_message_content_container);
        holder.messageInfoTextView = (TextView) convertView.findViewById(R.id.text_message_info);
        holder.attachmentImageView = convertView.findViewById(R.id.image_message_attachment);
        holder.attachmentProgressBar = (ProgressBar) convertView.findViewById(R.id.progress_message_attachment);
        holder.ivProfile = convertView.findViewById(R.id.iv_profile);
      //  holder.cardView = convertView.findViewById(R.id.card);

        convertView.setTag(holder);


        setIncomingOrOutgoingMessageAttributes(holder, chatMessage);
        setMessageBody(holder, chatMessage);
        setMessageInfo(chatMessage, holder,position);
        setMessageAuthor(holder, chatMessage);


        if (_isIncoming & !TextUtils.isEmpty(SharedPrefsHelper.USER_IMAGE_FRIEND)) {
            Glide.with(context).load(SharedPrefsHelper.USER_IMAGE_FRIEND).apply(RequestOptions.circleCropTransform()).into(holder.ivProfile);
        } else if (_isIncoming) {
            Glide.with(context).load(SharedPrefsHelper.USER_IMAGE_FRIEND).load(R.drawable.profile_round).into(holder.ivProfile);
        }


        if (!_isIncoming && !TextUtils.isEmpty(SharedPrefsHelper.USER_IMAGE)) {
            Glide.with(context).load(SharedPrefsHelper.USER_IMAGE).apply(RequestOptions.circleCropTransform().placeholder(R.drawable.profile_round)).into(holder.ivProfile);
        } else if (!_isIncoming) {
            Glide.with(context).load(SharedPrefsHelper.USER_IMAGE_FRIEND).load(R.drawable.profile_round).into(holder.ivProfile);
        }

        holder.messageContainerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasAttachments(chatMessage)) {
                    Collection<QBAttachment> attachments = chatMessage.getAttachments();
                    QBAttachment attachment = attachments.iterator().next();
                    AttachmentImageActivity.start(context, attachment.getUrl());
                }
            }
        });

        if (isIncoming(chatMessage) && !isRead(chatMessage)) {
            readMessage(chatMessage);
        }

        downloadMore(position);
        return convertView;
    }

    private void downloadMore(int position) {
        if (position == 0) {
            if (getCount() != previousGetCount) {
                paginationListener.downloadMore();
                previousGetCount = getCount();
            }
        }
    }

    public void setPaginationHistoryListener(PaginationHistoryListener paginationListener) {
        this.paginationListener = paginationListener;
    }

  /*  private void toggleItemInfo(ViewHolder holder, int position) {
        boolean isMessageInfoVisible = holder.messageInfoTextView.getVisibility() == View.VISIBLE;
        holder.messageInfoTextView.setVisibility(isMessageInfoVisible ? View.GONE : View.VISIBLE);

        if (onItemInfoExpandedListener != null) {
            onItemInfoExpandedListener.onItemInfoExpanded(position);
        }
    }*/



    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        Log.e("header ", "" + position);
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.view_chat_message_header, parent, false);
            holder.dateTextView = (TextView) convertView.findViewById(R.id.header_date_textview);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        QBChatMessage chatMessage = getItem(position);
        holder.dateTextView.setText(TimeUtils.getDate(chatMessage.getDateSent() * 1000));

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.dateTextView.getLayoutParams();
        if (position == 0) {
            lp.topMargin = ResourceUtils.getDimen(R.dimen.chat_date_header_top_margin);
        } else {
            lp.topMargin = 0;
        }
        holder.dateTextView.setLayoutParams(lp);
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        QBChatMessage chatMessage = getItem(position);
        return TimeUtils.getDateAsHeaderId(chatMessage.getDateSent() * 1000);
    }

    private void setMessageBody(final ViewHolder holder, QBChatMessage chatMessage) {
        if (hasAttachments(chatMessage)) {
            Collection<QBAttachment> attachments = chatMessage.getAttachments();
            QBAttachment attachment = attachments.iterator().next();

            holder.messageBodyTextView.setVisibility(View.GONE);
            holder.attachmentImageView.setVisibility(View.VISIBLE);
            holder.attachmentProgressBar.setVisibility(View.VISIBLE);

            RequestOptions options = new RequestOptions();
//            options.error(R.drawable.ic_error_white);
//            options.dontTransform().override(Consts.PREFERRED_IMAGE_SIZE_FULL, Consts.PREFERRED_IMAGE_SIZE_FULL);

            Glide.with(context)
                    .load(attachment.getUrl()).apply(options)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.attachmentImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            holder.attachmentProgressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.attachmentImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            holder.attachmentProgressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.attachmentImageView);
        } else {
            holder.messageBodyTextView.setText(chatMessage.getBody());
            holder.messageBodyTextView.setVisibility(View.VISIBLE);
            holder.attachmentImageView.setVisibility(View.GONE);
            holder.attachmentProgressBar.setVisibility(View.GONE);
        }
    }

    private void setMessageAuthor(ViewHolder holder, QBChatMessage chatMessage) {
        if (isIncoming(chatMessage)) {
            QBUser sender = QbUsersHolder.getInstance().getUserById(chatMessage.getSenderId());
            holder.messageAuthorTextView.setText(sender.getFullName());
            holder.messageAuthorTextView.setVisibility(View.GONE);

            if (hasAttachments(chatMessage)) {
                holder.messageAuthorTextView.setBackgroundResource(R.drawable.shape_rectangle_semi_transparent);
                holder.messageAuthorTextView.setTextColor(ResourceUtils.getColor(R.color.text_color_white));
            } else {
                holder.messageAuthorTextView.setBackgroundResource(0);
                holder.messageAuthorTextView.setTextColor(ResourceUtils.getColor(R.color.text_color_dark_grey));
            }
        } else {
            holder.messageAuthorTextView.setVisibility(View.GONE);
        }
    }

    private void setMessageInfo(QBChatMessage chatMessage, ViewHolder holder,int position) {
//        holder.messageInfoTextView.setText(TimeUtils.getTime(chatMessage.getDateSent() * 1000));
        hideDate(position, holder.messageInfoTextView);
    }


    private void hideDate(int position,TextView tvTime){
        QBChatMessage currentMess=getItem(position);
        if(position!=0){
            QBChatMessage previousMess=getItem(position-1);
           long currentTime=TimeUtils.getTimeAsHeaderId(currentMess.getDateSent()*1000);
           long previousTime=TimeUtils.getTimeAsHeaderId(previousMess.getDateSent()*1000);

            long currentDate=TimeUtils.getDateAsHeaderId(currentMess.getDateSent() * 1000);
            long previousDate=TimeUtils.getDateAsHeaderId(previousMess.getDateSent() * 1000);

           if(currentTime==previousTime){
               tvTime.setVisibility(View.GONE);
           }else {
               tvTime.setVisibility(View.VISIBLE);
           }

           if(currentDate!=previousDate){
               tvTime.setText(TimeUtils.getDate(currentMess.getDateSent() * 1000));
           }else {
               tvTime.setText(TimeUtils.getTime(currentMess.getDateSent() * 1000));
           }
           Log.e("DATE ",currentTime+"   "+previousTime);
       }else {
           tvTime.setText(TimeUtils.getDate(currentMess.getDateSent() * 1000));
       }
    }

    @SuppressLint("RtlHardcoded")
    private void setIncomingOrOutgoingMessageAttributes(ViewHolder holder, QBChatMessage chatMessage) {
        boolean isIncoming = isIncoming(chatMessage);
        int gravity = isIncoming ? Gravity.LEFT : Gravity.RIGHT;
        holder.messageContainerLayout.setGravity(gravity);
//        holder.messageInfoTextView.setGravity(gravity);

        int messageBodyContainerBgResource = isIncoming
                ? R.drawable.incoming_message_bg
                : R.drawable.aaa;
        if (hasAttachments(chatMessage)) {
            holder.messageBodyContainerLayout.setBackgroundResource(0);
            holder.messageBodyContainerLayout.setPadding(0, 0, 0, 0);
//            holder.attachmentImageView.setMaskResourceId(messageBodyContainerBgResource);
        } else {
//            holder.messageBodyContainerLayout.setBackgroundResource(messageBodyContainerBgResource);
        }

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.messageAuthorTextView.getLayoutParams();
        if (isIncoming && hasAttachments(chatMessage)) {
            lp.leftMargin = ResourceUtils.getDimen(R.dimen.chat_message_attachment_username_margin);
            lp.topMargin = ResourceUtils.getDimen(R.dimen.chat_message_attachment_username_margin);
        } else if (isIncoming) {
            lp.leftMargin = ResourceUtils.getDimen(R.dimen.chat_message_username_margin);
            lp.topMargin = 0;
        }
        holder.messageAuthorTextView.setLayoutParams(lp);

        int textColorResource = isIncoming
                ? R.color.text_color_black
                : R.color.text_color_white;
        holder.messageBodyTextView.setTextColor(ResourceUtils.getColor(textColorResource));
    }

    private boolean hasAttachments(QBChatMessage chatMessage) {
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        return attachments != null && !attachments.isEmpty();
    }

    private boolean isIncoming(QBChatMessage chatMessage) {
        QBUser currentUser = ChatHelper.getCurrentUser();
        return chatMessage.getSenderId() != null && !chatMessage.getSenderId().equals(currentUser.getId());
    }

    private boolean isRead(QBChatMessage chatMessage) {
        Integer currentUserId = ChatHelper.getCurrentUser().getId();
        return !CollectionsUtil.isEmpty(chatMessage.getReadIds()) && chatMessage.getReadIds().contains(currentUserId);
    }

    private void readMessage(QBChatMessage chatMessage) {
        try {
            chatDialog.readMessage(chatMessage);
        } catch (XMPPException | SmackException.NotConnectedException e) {
            Log.w(TAG, e);
        }
    }


    private static class HeaderViewHolder {
        public TextView dateTextView;
    }

    private static class ViewHolder {
        public TextView messageBodyTextView;
        public TextView messageAuthorTextView;
        public TextView messageInfoTextView;
        public LinearLayout messageContainerLayout;
        public RelativeLayout messageBodyContainerLayout;
        public ImageView attachmentImageView;
        public ProgressBar attachmentProgressBar;
        public CardView cardView;
        public ImageView ivProfile;
    }

    public interface OnItemInfoExpandedListener {
        void onItemInfoExpanded(int position);
    }
}
