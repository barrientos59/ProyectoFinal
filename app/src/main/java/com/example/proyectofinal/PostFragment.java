package com.example.proyectofinal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PostFragment extends Fragment {
    private NavController navController;
    private AppViewModel appViewModel;

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        view.findViewById(R.id.gotoNewPostFragmentButton).setOnClickListener(v -> navController.navigate(R.id.newPostFragment));

        // RecyclerView
        RecyclerView postsRecyclerView = view.findViewById(R.id.postsRecyclerView);

        Query query = FirebaseFirestore.getInstance().collection("posts").orderBy("timeStamp", Query.Direction.DESCENDING).limit(50);

        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .setLifecycleOwner(this)
                .build();

        postsRecyclerView.setAdapter(new PostsAdapter(options));

        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
    }

    class PostsAdapter extends FirestoreRecyclerAdapter<Post, PostsAdapter.PostViewHolder> {
        public PostsAdapter(@NonNull FirestoreRecyclerOptions<Post> options) {
            super(options);
        }

        @NonNull
        @Override
        public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_post, parent, false));
        }

        @Override
        protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull final Post post) {
            if (post.authorPhotoUrl == null) {
                holder.authorPhotoImageView.setImageResource(R.drawable.user);
            } else {
                Glide.with(getContext()).load(post.authorPhotoUrl).circleCrop().into(holder.authorPhotoImageView);
            }
            holder.authorTextView.setText(post.author);
            holder.contentTextView.setText(post.content);

            // Gestión de likes
            final String postKey = getSnapshots().getSnapshot(position).getId();
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (post.likes.containsKey(uid)) {
                holder.likeImageView.setImageResource(R.drawable.like_on);
            } else {
                holder.likeImageView.setImageResource(R.drawable.like_off);
            }
            holder.numLikesTextView.setText(String.valueOf(post.likes.size()));
            holder.likeImageView.setOnClickListener(view -> FirebaseFirestore.getInstance().collection("posts")
                    .document(postKey)
                    .update("likes." + uid, post.likes.containsKey(uid) ? FieldValue.delete() : true));

            // Fecha
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(post.timeStamp);
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            String fecha = format.format(calendar.getTime());
            holder.dateTextView.setText(fecha);

            // Miniatura de media
            if (post.mediaUrl != null) {
                holder.mediaImageView.setVisibility(View.VISIBLE);
                if ("audio".equals(post.mediaType)) {
                    Glide.with(requireView()).load(R.drawable.audio).centerCrop().into(holder.mediaImageView);
                } else {
                    Glide.with(requireView()).load(post.mediaUrl).centerCrop().into(holder.mediaImageView);
                }
                holder.mediaImageView.setOnClickListener(view -> {
                    appViewModel.postSeleccionado.setValue(post);
                    navController.navigate(R.id.mediaFragment);
                });
            } else {
                holder.mediaImageView.setVisibility(View.GONE);
            }

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (post.idAuthor.equals(currentUserId)) {
                holder.deletePostImageView.setVisibility(View.VISIBLE);
            } else {
                holder.deletePostImageView.setVisibility(View.GONE);
            }

            holder.deletePostImageView.setOnClickListener(view -> FirebaseFirestore.getInstance().collection("posts")
                    .document(postKey)
                    .delete()
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error deleting post", Toast.LENGTH_SHORT).show()));
        }

        class PostViewHolder extends RecyclerView.ViewHolder {
            ImageView authorPhotoImageView, likeImageView, mediaImageView, deletePostImageView;
            TextView authorTextView, contentTextView, numLikesTextView, dateTextView;

            PostViewHolder(@NonNull View itemView) {
                super(itemView);
                deletePostImageView = itemView.findViewById(R.id.deletePostImageView);
                authorPhotoImageView = itemView.findViewById(R.id.authorPhotoImageView);
                likeImageView = itemView.findViewById(R.id.likeImageView);
                mediaImageView = itemView.findViewById(R.id.mediaImage);
                authorTextView = itemView.findViewById(R.id.authorTextView);
                contentTextView = itemView.findViewById(R.id.contentTextView);
                numLikesTextView = itemView.findViewById(R.id.numLikesTextView);
                dateTextView = itemView.findViewById(R.id.dateTextView);
            }
        }
    }
}
