package com.appsinventiv.corperstrip.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.appsinventiv.corperstrip.Model.GroupModel;
import com.appsinventiv.corperstrip.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by AliAh on 25/07/2018.
 */

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {
    Context context;
    ArrayList<GroupModel> itemList;
    ArrayList<String> userGroupsList;
    JoinOrLeave joinOrLeave;

    public GroupsAdapter(Context context, ArrayList<GroupModel> itemList, ArrayList<String> userGroupsList, JoinOrLeave joinOrLeave) {
        this.context = context;
        this.itemList = itemList;
        this.userGroupsList = userGroupsList;
        this.joinOrLeave = joinOrLeave;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_item_layout, parent, false);
        GroupsAdapter.ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupModel model = itemList.get(position);
        holder.groupName.setText(model.getName());
        holder.message.setText("");

        boolean flag = false;
        for (int i = 0; i < userGroupsList.size(); i++) {
            if (model.getId().equals(userGroupsList.get(i))) {
                flag = true;
            }
        }
        if (flag) {
            holder.button.setText("Leave");
            holder.button.setTextColor(context.getResources().getColor(R.color.colorRed));
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    joinOrLeave.onLeave(model);
                }
            });


        } else {
            holder.button.setText("Join");
            holder.button.setTextColor(context.getResources().getColor(R.color.colorGreen));
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    joinOrLeave.onJoin(model);
                }
            });

        }

        if(!model.getPicUrl().equals("")){
            Glide.with(context).load(model.getPicUrl()).into(holder.imageView);
        }else if(model.getPicUrl().equals("")){
            holder.imageView.setImageResource(R.drawable.ic_group);
        }


    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupName, message;
        Button button;
        CircleImageView imageView;


        public ViewHolder(View itemView) {

            super(itemView);
            groupName = itemView.findViewById(R.id.groupName);
            message = itemView.findViewById(R.id.message);
            button = itemView.findViewById(R.id.join_leave);

            imageView = itemView.findViewById(R.id.imageView2);

        }
    }

    public interface JoinOrLeave {
        public void onJoin(GroupModel groupModel);

        public void onLeave(GroupModel groupModel);

    }
}
