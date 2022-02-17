package dev.sqayner.instrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.instagram4j.instagram4j.models.user.Profile;
import com.squareup.picasso.Picasso;

import java.util.List;

import dev.sqayner.instrack.R;

public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.VH> {

    private List<Profile> profiles;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public UserRecyclerViewAdapter(List<Profile> profiles, Context context) {
        this.profiles = profiles;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public UserRecyclerViewAdapter.VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserRecyclerViewAdapter.VH holder, int position) {
        Profile user = profiles.get(position);
        holder.setData(user);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    public interface OnItemClickListener {
        void onClick(Profile user);
    }

    public class VH extends RecyclerView.ViewHolder {
        private View viewBody;
        private TextView displayNameTv, usernameTv;
        private ImageView ivPhoto;

        public VH(@NonNull View itemView) {
            super(itemView);
            viewBody = itemView.findViewById(R.id.user_ll_body);
            displayNameTv = itemView.findViewById(R.id.user_tv_displayname);
            usernameTv = itemView.findViewById(R.id.user_tv_username);
            ivPhoto = itemView.findViewById(R.id.user_iv_photo);
        }

        public void setData(Profile user) {
            displayNameTv.setText(user.getFull_name());
            if (user.getFull_name().equals(""))
                displayNameTv.setVisibility(View.GONE);
            usernameTv.setText(user.getUsername());
            Picasso.get().load(user.getProfile_pic_url()).into(ivPhoto);

            viewBody.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null)
                        onItemClickListener.onClick(user);
                }
            });
        }
    }
}
