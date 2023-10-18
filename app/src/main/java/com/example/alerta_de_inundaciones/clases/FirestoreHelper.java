package com.example.alerta_de_inundaciones.clases;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

public class FirestoreHelper implements FirestoreCallback {
    private FirebaseFirestore db;
    private String email;
    private String neighborhood;
    private Context context;
    private DocumentReference userRef;
    private ListenerRegistration listenerRegistration;

    public FirestoreHelper(Context context, String email) {
        this.db = FirebaseFirestore.getInstance();
        this.context = context;
        this.email = email;
        this.userRef = db.collection("Usuarios").document(email);
    }

    public void startListeningForNeighborhoodChanges() {
        listenerRegistration = userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(TAG, "Error al escuchar cambios en el documento", e);
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String newNeighborhood = documentSnapshot.getString("Barrio");
                    Log.d(TAG, "El nuevo barrio es " + newNeighborhood);
                    neighborhood = newNeighborhood;
                   // Toast.makeText(context, "Nuevo barrio -> " + newNeighborhood, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void stopListeningForNeighborhoodChanges() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    @Override
    public void onCallback(String neighborhood) {
        this.neighborhood = neighborhood;
        Toast.makeText(context, "Barrio -> " + neighborhood, Toast.LENGTH_SHORT).show();
    }

    public void getNeighborhood(FirestoreCallback callback) {
        getUserNeighborhood(email, callback);
    }

    private void getUserNeighborhood(String email, FirestoreCallback callback) {
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String neighborhood = documentSnapshot.getString("Barrio");
                    Log.d(TAG, "El barrio es " + neighborhood);
                    callback.onCallback(neighborhood);
                } else {
                    Log.d(TAG, "No se encontró el documento");
                    callback.onCallback(null);
                }
            }
        }).addOnFailureListener(e -> {
            Log.d(TAG, "Error al obtener el documento", e);
            callback.onCallback(null);
        });
    }

}
