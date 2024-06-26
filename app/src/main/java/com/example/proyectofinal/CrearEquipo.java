package com.example.proyectofinal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.proyectofinal.Model.Equipo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class CrearEquipo extends Fragment {
    private static final int PICK_IMAGE = 1;
    private static final String STORAGE_PATH = "logoequip/";

    private Uri imageUri;
    private StorageReference storageReference;

    NavController navController;
    Button btnCrearEquipo;
    EditText etNombreCrearEquipo;
    EditText etUbiCrearEquipo;
    Button btnSeleccionarLogo;
    ImageView imageViewLogo;

    FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crear_equipo, container, false);

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        btnCrearEquipo = view.findViewById(R.id.btnCrear);
        etNombreCrearEquipo = view.findViewById(R.id.NombreCrearEquipo);
        etUbiCrearEquipo = view.findViewById(R.id.UbiCrearEquipo);
        btnSeleccionarLogo = view.findViewById(R.id.btnSeleccionarLogo);
        imageViewLogo = view.findViewById(R.id.imageViewLogo);

        if (db == null) {
            db = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            db.setFirestoreSettings(settings);
        }

        storageReference = FirebaseStorage.getInstance().getReference();

        btnCrearEquipo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombreEquipo = etNombreCrearEquipo.getText().toString().trim();
                String ubicacionEquipo = etUbiCrearEquipo.getText().toString().trim();

                if (!nombreEquipo.isEmpty() && !ubicacionEquipo.isEmpty()) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String idAutor = currentUser != null ? currentUser.getUid() : null;
                    String equipoId = db.collection("equipos").document().getId();
                    Equipo equipo = new Equipo(equipoId, nombreEquipo, ubicacionEquipo, idAutor);

                    if (imageUri != null) {
                        uploadImageToStorage(equipo);
                    } else {
                        saveEquipoToFirestore(equipo);
                    }
                } else {
                    Toast.makeText(requireContext(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSeleccionarLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        return view;
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null) {
                imageViewLogo.setImageURI(imageUri);
                imageViewLogo.setVisibility(View.VISIBLE);
            }
        }
    }

    private void uploadImageToStorage(final Equipo equipo) {
        if (imageUri != null) {
            StorageReference filePath = storageReference.child(STORAGE_PATH + UUID.randomUUID().toString());
            filePath.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                            equipo.setLogo(uri.toString());
                            saveEquipoToFirestore(equipo);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveEquipoToFirestore(Equipo equipo) {
        db.collection("equipos").document(equipo.getId()).set(equipo)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(requireContext(), "Equipo creado", Toast.LENGTH_SHORT).show();
                    navController.navigateUp(); // Regresar al fragmento anterior
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error al crear equipo", Toast.LENGTH_SHORT).show();
                });
    }


}
