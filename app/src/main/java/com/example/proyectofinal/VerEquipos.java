package com.example.proyectofinal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectofinal.Adapter.EquipoAdapter;
import com.example.proyectofinal.Model.Equipo;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class VerEquipos extends Fragment {

    NavController navController;
    RecyclerView recyclerView;
    EquipoAdapter equipoAdapter;
    FirebaseFirestore firestore;
    FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ver_equipos, container, false);
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String currentUserId = currentUser.getUid();

        firestore = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerViewVerEquipos);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Query query = firestore.collection("equipos").whereEqualTo("idAutor", currentUserId);

        FirestoreRecyclerOptions<Equipo> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Equipo>().setQuery(query, Equipo.class).build();
        equipoAdapter = new EquipoAdapter(firestoreRecyclerOptions, requireContext());
        recyclerView.setAdapter(equipoAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        equipoAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        equipoAdapter.stopListening();
    }
}
