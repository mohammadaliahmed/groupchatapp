package com.appsinventiv.corperstrip.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appsinventiv.corperstrip.Activities.ChatActivity;
import com.appsinventiv.corperstrip.Model.GroupModel;
import com.appsinventiv.corperstrip.R;
import com.appsinventiv.corperstrip.Utils.CommonUtils;
import com.appsinventiv.corperstrip.Utils.Constants;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by AliAh on 24/07/2018.
 */

public class GroupsListAdapter extends RecyclerView.Adapter<GroupsListAdapter.ViewHolder> {

    Context context;
    ArrayList<GroupModel> itemList;


    public GroupsListAdapter(Context context, ArrayList<GroupModel> itemList) {
        this.context = context;
        this.itemList = itemList;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_list_layout, parent, false);
        GroupsListAdapter.ViewHolder viewHolder = new GroupsListAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final GroupModel model = itemList.get(position);

        holder.groupName.setText(model.getName());
        holder.time.setText(CommonUtils.getFormattedDate(model.getTime()));
        if(model.getType().equals(Constants.MESSAGE_TYPE_IMAGE)){
            holder.message.setText(""+"\uD83D\uDCF7 Image");
        }else if(model.getType().equals(Constants.MESSAGE_TYPE_AUDIO)){
            holder.message.setText(""+"\uD83C\uDFB5 Audio");
        }
        else if(model.getType().equals(Constants.MESSAGE_TYPE_DOCUMENT)){
            holder.message.setText(""+"\uD83D\uDCC4 Document");
        }
        else if(model.getType().equals(Constants.MESSAGE_TYPE_TEXT)){
            holder.message.setText(model.getLastMessage());
        }




        holder.time.setText(CommonUtils.getFormattedDate(model.getTime()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, ChatActivity.class);
                i.putExtra("groupId", model.getId());
                i.putExtra("groupName", model.getName());

                context.startActivity(i);
            }
        });


        if(!model.getPicUrl().equals("")){
            Glide.with(context).load(model.getPicUrl()).into(holder.imageView);
        }else if(model.getPicUrl().equals("")){
            holder.imageView.setImageResource(R.drawable.ic_group);
        }

    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupName, message, time, count;
        CircleImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.groupName);
            message = itemView.findViewById(R.id.message);
            time = itemView.findViewById(R.id.time);
            count = itemView.findViewById(R.id.count);
            imageView = itemView.findViewById(R.id.imageView2);


        }
    }
}
