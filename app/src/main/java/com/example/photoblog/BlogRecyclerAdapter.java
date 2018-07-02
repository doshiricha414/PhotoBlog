package com.example.photoblog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {


    public List<BlogPost> blog_list;
    public Context context;
    public ImageView blogImageView;
    public CircleImageView circleImageView;
    public TextView usernameField,dateField;
    private FirebaseFirestore firebaseFirestore;

    public BlogRecyclerAdapter(List<BlogPost> blog_list){
        this.blog_list = blog_list;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent,false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        String desc_data = blog_list.get(position).getDescription();
        String downloadUrl = blog_list.get(position).getImage_url();
        String user_id = blog_list.get(position).getUser_id();

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    String username = task.getResult().getString("Name");
                    String userImage = task.getResult().getString("image");
                    holder.setUserData(username,userImage);

                }
            }
        });

        /*Date milliseconds = blog_list.get(position).getTimestamp();
        //Date date = new Date(milliseconds);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String dateString = simpleDateFormat.format(milliseconds).toString();*/
        holder.setDescText(desc_data);
        holder.setBlogImage(downloadUrl);
        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        //holder.setPostDate(dateString);
    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView descView;
        private View mView;
        private TextView blogLikeCount;
        private ImageView blogLikeBtn;
        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            blogLikeBtn = mView.findViewById(R.id.blog_like);

        }
        public void setDescText(String descText){
            descView = mView.findViewById(R.id.blog_description);
            descView.setText(descText);
        }
        public void setBlogImage(String downloadUrl){
            blogImageView = mView.findViewById(R.id.blog_post_image);
            Glide.with(context).load(downloadUrl).into(blogImageView);
        }
        public void setUserData(String username,String image){
            usernameField = mView.findViewById(R.id.blog_username);
            circleImageView = mView.findViewById(R.id.blog_user_image);
            usernameField.setText(username);
            Glide.with(context).load(image).into(circleImageView);
        }
        /*public void setPostDate(String date){
            dateField = mView.findViewById(R.id.blog_user_date);
            dateField.setText(date);
        }*/


    }
}
