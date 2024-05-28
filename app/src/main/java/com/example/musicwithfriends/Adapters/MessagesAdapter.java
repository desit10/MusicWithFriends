package com.example.musicwithfriends.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicwithfriends.Models.Message;
import com.example.musicwithfriends.R;

import java.util.ArrayList;

@UnstableApi
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder>{

    Context context;
    ArrayList<Message> messages;
    String sender;
    public MessagesAdapter(Context context, ArrayList<Message> messages, String sender) {
        this.context = context;
        this.messages = messages;
        this.sender = sender;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);

        return new MessagesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Message message = messages.get(position);

        if(message.getSender().equals(sender)){
            holder.layoutAnotherMessage.setVisibility(View.GONE);
            holder.textViewTextMyMessage.setText(message.getText());
            holder.textViewTimeMyMessage.setText(message.getDepartureTime());
        } else {
            holder.layoutMyMessage.setVisibility(View.GONE);
            holder.textViewSenderMessage.setText(message.getSender());
            holder.textViewTextAnotherMessage.setText(message.getText());
            holder.textViewTimeAnotherMessage.setText(message.getDepartureTime());
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.setIsRecyclable(false);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.setIsRecyclable(false);
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout layoutAnotherMessage;
        LinearLayout layoutMyMessage;

        TextView textViewSenderMessage;
        TextView textViewTextAnotherMessage;
        TextView textViewTimeAnotherMessage;

        TextView textViewTextMyMessage;
        TextView textViewTimeMyMessage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            layoutAnotherMessage = itemView.findViewById(R.id.layoutAnotherMessage);
            layoutMyMessage = itemView.findViewById(R.id.layoutMyMessage);

            textViewSenderMessage = itemView.findViewById(R.id.textViewSenderMessage);
            textViewTextAnotherMessage = itemView.findViewById(R.id.textViewTextAnotherMessage);
            textViewTimeAnotherMessage = itemView.findViewById(R.id.textViewTimeAnotherMessage);

            textViewTextMyMessage = itemView.findViewById(R.id.textViewTextMyMessage);
            textViewTimeMyMessage = itemView.findViewById(R.id.textViewTimeMyMessage);
        }
    }
}
