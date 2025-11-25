package com.example.lotteryeventsystem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class AdminPosterFragment extends Fragment {

    private RecyclerView posterRecycleView;
    private AdminPosterAdapter adapter;
    private List<String> posterUrls = new ArrayList<>();

    public AdminPosterFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_view_images, container, false);

        posterRecycleView = view.findViewById(R.id.postersRecyclerView);
        posterRecycleView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 rows

        adapter = new AdminPosterAdapter(posterUrls);
        posterRecycleView.setAdapter(adapter);

        loadPosters();

        ImageButton backArrow = view.findViewById(R.id.back_button);
        backArrow.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private void loadPosters() {
        FirebaseFirestore.getInstance()
                .collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    posterUrls.clear();

                    for (DocumentSnapshot doc: queryDocumentSnapshots) {
                        String url = doc.getString("posterURL");
                        if (url != null) {
                            posterUrls.add(url);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
