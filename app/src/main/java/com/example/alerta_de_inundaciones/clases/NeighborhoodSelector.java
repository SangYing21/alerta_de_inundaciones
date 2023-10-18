package com.example.alerta_de_inundaciones.clases;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;

public class NeighborhoodSelector {
    private FirebaseFirestore firestore;
    private Context context;
    private ArrayAdapter<String> adapter;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser currentUser;


    public NeighborhoodSelector(Context context) {
        this.context = context;
        firestore = FirebaseFirestore.getInstance();
    }

    public void selectNeighborhood() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();


        firestore.collection("Barrios").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<String> neighborhoods = new ArrayList<>();
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    neighborhoods.add(documentSnapshot.getId());
                }

                Log.d("log", "->" + neighborhoods);
                adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, neighborhoods);

                SuccessDialog dialogsucces = new SuccessDialog(context, "Seleccione su barrio", adapter, "¿Desea cerrar la sesión?", new Runnable() {
                    @Override
                    public void run() {
                        Log.d("WarningSuccess", "Button clicked!");
                    }
                });

                dialogsucces.setOnNeighborhoodSelectedListener(new SuccessDialog.OnNeighborhoodSelectedListener() {
                    @Override
                    public void onNeighborhoodSelected(String neighborhood) {
                        updateBarrio(neighborhood);
                        //  Toast.makeText(context, "inicio->" + neighborhood, Toast.LENGTH_SHORT).show();
                    }
                });

                dialogsucces.show();
            }
        });
    }

    public void updateBarrio(String barrio) {
        final DocumentReference userRef = firestore.collection("Usuarios").document(currentUser.getEmail());
        final DocumentReference barrioRef = firestore.collection("Barrios").document(barrio);

        firestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot userSnapshot = transaction.get(userRef);
                String oldBarrio = userSnapshot.getString("Barrio");
                DocumentSnapshot barrioSnapshot = transaction.get(barrioRef);
                long newResidents = barrioSnapshot.getLong("residentes");
 //               Log.d("UpdateBarrio", " newResidents ->" +  barrioSnapshot.getLong("residentes"));

                if (!barrio.equals(oldBarrio)) {
                    // Obtener la referencia del barrio anterior
                    final DocumentReference oldBarrioRef = firestore.collection("Barrios").document(oldBarrio);

                    // Obtener el número actual de residentes en el barrio anterior

                    DocumentSnapshot oldBarrioSnapshot = transaction.get(oldBarrioRef);
                    long oldResidents = oldBarrioSnapshot.getLong("residentes");
                 //   Log.d("UpdateBarrio", " oldResidents ->" + oldResidents);

                    // Disminuir el número de residentes en el barrio anterior
                    transaction.update(oldBarrioRef, "residentes", oldResidents - 1);

                    // Obtener el número actual de residentes en el nuevo barrio

                 //   long newResidents = barrioSnapshot.getLong("residentes");

                    // Aumentar el número de residentes en el nuevo barrio
                   transaction.update(barrioRef, "residentes", newResidents + 1);
                }



                // Actualizar el barrio del usuario
                transaction.update(userRef, "Barrio", barrio);

                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("UpdateBarrio", "Barrio updated successfully.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("UpdateBarrio", "Error updating barrio: " + e.getMessage());
            }
        });
    }


}


