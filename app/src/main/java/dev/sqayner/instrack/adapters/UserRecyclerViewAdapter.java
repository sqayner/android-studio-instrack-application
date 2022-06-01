package dev.sqayner.instrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
    private OnClickListener onItemClickListener, onUnfollowButtonClickListener, onFollowButtonClickListener;
    private ActionButtonTypes actionButtonType;

    public UserRecyclerViewAdapter(List<Profile> profiles, Context context) {
        this.profiles = profiles;
        this.context = context;
    }

    public void setOnFollowButtonClickListener(OnClickListener onFollowButtonClickListener) {
        this.onFollowButtonClickListener = onFollowButtonClickListener;
    }

    public void setActionButtonType(ActionButtonTypes actionButtonType) {
        this.actionButtonType = actionButtonType;
    }

    public void setOnUnfollowButtonClickListener(OnClickListener onUnfollowButtonClickListener) {
        this.onUnfollowButtonClickListener = onUnfollowButtonClickListener;
    }

    public void setOnItemClickListener(OnClickListener onItemClickListener) {
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
        holder.setData(user, position);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    public void deleteProfile(Profile profile) {
        profiles.removeIf(p -> profile.getUsername().equals(p.getUsername()));
        profiles.remove(profile);
    }

    public enum ActionButtonTypes {
        FOLLOW,
        UNFOLLOW,
        CANCEL
    }

    public interface OnClickListener {
        void onClick(Profile user, int position);
    }

    public class VH extends RecyclerView.ViewHolder {
        private View viewBody;
        private TextView displayNameTv, usernameTv;
        private ImageView ivPhoto;
        private Button actionBtn;

        public VH(@NonNull View itemView) {
            super(itemView);
            viewBody = itemView.findViewById(R.id.user_ll_body);
            displayNameTv = itemView.findViewById(R.id.user_tv_displayname);
            usernameTv = itemView.findViewById(R.id.user_tv_username);
            ivPhoto = itemView.findViewById(R.id.user_iv_photo);
            actionBtn = itemView.findViewById(R.id.user_btn_action);
        }

        public void setData(Profile user, int position) {
            displayNameTv.setText(user.getFull_name());
            if (user.getFull_name().equals(""))
                displayNameTv.setVisibility(View.GONE);
            usernameTv.setText(user.getUsername());
            Picasso.get().load(user.getProfile_pic_url()).into(ivPhoto);

            viewBody.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null)
                        onItemClickListener.onClick(user, position);
                }
            });

            if (actionButtonType == ActionButtonTypes.UNFOLLOW)
                actionBtn.setText(R.string.item_user_unfollow_btn_text);
            else if (actionButtonType == ActionButtonTypes.FOLLOW)
                actionBtn.setText(R.string.item_user_follow_btn_text);
            else if (actionButtonType == ActionButtonTypes.CANCEL)
                actionBtn.setText(R.string.item_user_follow_btn_text);

            actionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actionButtonType == ActionButtonTypes.UNFOLLOW) {
                        if (onUnfollowButtonClickListener != null)
                            onUnfollowButtonClickListener.onClick(user, position);
                    } else if (actionButtonType == ActionButtonTypes.FOLLOW) {
                        if (onFollowButtonClickListener != null)
                            onFollowButtonClickListener.onClick(user, position);
                    } else if (actionButtonType == ActionButtonTypes.CANCEL) {
                        if (onFollowButtonClickListener != null)
                            onFollowButtonClickListener.onClick(user, position);
                    }
                }
            });


        }
    }
}
