package com.example.rgr_localchat.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rgr_localchat.R;
import com.example.rgr_localchat.model.ChatMessage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<ChatMessage> messages = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    // НОВИЙ МЕТОД: Очищення чату
    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ChatViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);

        holder.tvMessage.setText(msg.getText());
        holder.tvTime.setText(timeFormat.format(new Date(msg.getTimestamp())));

        if (msg.isSystem()) {
            holder.msgContainer.setGravity(Gravity.CENTER);
            holder.bubbleLayout.setBackgroundColor(Color.TRANSPARENT);
            holder.tvMessage.setTypeface(null, Typeface.ITALIC);
            holder.tvMessage.setTextColor(Color.GRAY);
            holder.tvSender.setVisibility(View.GONE);
            holder.tvTime.setVisibility(View.GONE);
        } else {
            holder.tvMessage.setTypeface(null, Typeface.NORMAL);
            holder.tvMessage.setTextColor(Color.BLACK);
            holder.tvTime.setVisibility(View.VISIBLE);

            GradientDrawable background = new GradientDrawable();
            background.setCornerRadius(16);

            if (msg.isSentByMe()) {
                holder.msgContainer.setGravity(Gravity.END);
                background.setColor(Color.parseColor("#BBDEFB"));
                holder.tvSender.setVisibility(View.GONE);
            } else {
                holder.msgContainer.setGravity(Gravity.START);
                background.setColor(Color.parseColor("#EEEEEE"));
                holder.tvSender.setVisibility(View.VISIBLE);
                holder.tvSender.setText(msg.getSenderName());
            }
            holder.bubbleLayout.setBackground(background);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout msgContainer, bubbleLayout;
        TextView tvMessage, tvSender, tvTime;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            msgContainer = itemView.findViewById(R.id.msgContainer);
            bubbleLayout = itemView.findViewById(R.id.bubbleLayout);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}