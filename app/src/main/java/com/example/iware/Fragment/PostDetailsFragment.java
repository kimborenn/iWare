package com.example.iware.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.iware.Adapter.PostAdapter;
import com.example.iware.MainActivity;
import com.example.iware.Model.Post;
import com.example.iware.PostActivity;
import com.example.iware.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class PostDetailsFragment extends Fragment
{
    String postid;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    ImageView deletePost;
    String postPublisher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_post_details, container, false);

        SharedPreferences preferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        postid = preferences.getString("postid", "none");

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerView.setAdapter(postAdapter);

        deletePost = view.findViewById(R.id.delete_post);

        readPosts();



        return view;
    }

    private void manageDeleteButton(final Post currentPost)
    {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);

        final String profileid = prefs.getString("profileid", "none");
        postPublisher = currentPost.getPublisher();

        if (profileid.equals(postPublisher))
        {
            deletePost.setVisibility(View.VISIBLE);
            deletePost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    progressDialog.setMessage("Deleting");
                    progressDialog.show();

                    String postid = currentPost.getPostid();

                    DatabaseReference reference;
                    reference = FirebaseDatabase.getInstance().getReference("Posts");
                    reference.child(postid).removeValue();

                    reference = FirebaseDatabase.getInstance().getReference("Comments");
                    reference.child(postid).removeValue();

                    reference = FirebaseDatabase.getInstance().getReference("Likes");
                    reference.child(postid).removeValue();

                    progressDialog.dismiss();

                    startActivity(new Intent(getContext(), MainActivity.class));

                }
            });
        }
        else
        {
            deletePost.setVisibility(View.GONE);
        }
    }

    private void readPosts()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                Post post = dataSnapshot.getValue(Post.class);
                if (post != null)
                {
                    postList.add(post);
                    postAdapter.notifyDataSetChanged();
                    manageDeleteButton(post);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
