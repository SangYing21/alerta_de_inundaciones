package com.example.alerta_de_inundaciones;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.alerta_de_inundaciones.clases.FirestoreCallback;
import com.example.alerta_de_inundaciones.clases.FirestoreHelper;
import com.example.alerta_de_inundaciones.clases.NeighborhoodSelector;

import com.example.alerta_de_inundaciones.clases.WarningDialog;
import com.example.alerta_de_inundaciones.clases.WeatherAPI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class inicio extends AppCompatActivity implements FirestoreCallback {
    private static final String TAG = "mensaje";

    /*Objetos Firebase */

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser currentUser;
    CollectionReference neighborhoodRef, waterLevelRef;
    DocumentReference userneighborhoodRef;

    /* Clase dibujar ola */
    WaveView waveView;
    DecimalFormat df = new DecimalFormat("0.00");
    /*Elementos de la vista*/

    Button btnCerrarSesion;
    EditText val;
    TextView txt_user_name, txt_user_email, txtneighborhood, txttempeture, txtwindspeed, txtclima;
    ImageView image_user, imageViewClima;
    CardView cardNeighborhood;
    /* Variable*/
    String token, neighborhood, currentneighborhood, oldneneighborhood;
    ArrayAdapter<String> adapter;

    /*clases*/
    FirestoreHelper firestoreHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        waveView = findViewById(R.id.wave_view);
        txt_user_name = findViewById(R.id.txt_user_name);
        txt_user_email = findViewById(R.id.txt_user_email);
        txttempeture = findViewById(R.id.txttempeture);
        txtwindspeed = findViewById(R.id.txtwindspeed);
        txtclima = findViewById(R.id.txtclima);
        txtneighborhood = findViewById(R.id.txtneighborhood);
        image_user = findViewById(R.id.image_user);
        cardNeighborhood = findViewById(R.id.cardNeighborhood);
        imageViewClima = findViewById(R.id.imageViewClima);

        txt_user_name.setText(currentUser.getDisplayName());
        txt_user_email.setText(currentUser.getEmail());
        waveView.setWaveHeight(100f);
        // Instanciar un objeto de la clase FirestoreHelper
        firestoreHelper = new FirestoreHelper(this, currentUser.getEmail());

        // Comenzar a escuchar los cambios en el campo "Barrio" de Firestore
        firestoreHelper.startListeningForNeighborhoodChanges();

        neighborhoodRef = db.collection("Barrios");

        userneighborhoodRef = db.collection("Usuarios").document(currentUser.getEmail());
        dessuscribeTopic(oldneneighborhood);
        //  suscribeTopic();
        getNeighborhood();

        waterLevelRef = db.collection("Dispositivos");


        cardNeighborhood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NeighborhoodSelector neighborhoodSelector = new NeighborhoodSelector(inicio.this);
                neighborhoodSelector.selectNeighborhood();
            }
        });


        Glide.with(this)
                .load(currentUser.getPhotoUrl())
                .placeholder(R.drawable.logo) //imagen de marcador de posición
                .error(R.drawable.logo) //imagen de error si falla la carga
                .circleCrop() //recorte circular de la imagen
                .into(image_user);

        image_user.setOnClickListener(v -> {

            WarningDialog dialog = new WarningDialog(inicio.this, "Atención", "¿Desea cerrar la sesión?", new Runnable() {
                @Override
                public void run() {
                    logout();

                    Log.d("WarningDialog", "Button clicked!");
                }
            });
            dialog.show();
        });


        SharedPreferences preferences = getSharedPreferences("datos", Context.MODE_PRIVATE);
        token = preferences.getString("token", "vacío");


        WeatherAPI weatherAPI = new WeatherAPI(this, "545dea6e2b89b2702f245795ef1d61c8");
        weatherAPI.getWeather("Bluefields", new WeatherAPI.WeatherCallback() {
            @Override
            public void onSuccess(double temperature, int humidity, double windSpeed, String descripcion) {
                Log.d("weatherAPI", "Temperature->" + temperature);
                txtwindspeed.setText(String.valueOf(df.format((windSpeed * 3.6)) + " Km/h"));
                txttempeture.setText(String.valueOf(temperature) + " C°");

                // Toast.makeText(inicio.this, ""+descripcion, Toast.LENGTH_SHORT).show();

                String clima = descripcion;

                switch (clima) {
                    case "clear sky":
                        imageViewClima.setImageResource(R.drawable.clear_sky);
                        txtclima.setText("Mayormente soleado");
                        break;
                    case "few clouds":
                        txtclima.setText("Poco nublado");
                        imageViewClima.setImageResource(R.drawable.few_clouds);
                        break;
                    case "scattered clouds":
                        txtclima.setText("Mayormente nublado");
                        imageViewClima.setImageResource(R.drawable.scattered_clouds);
                        break;
                    case "broken clouds":
                        txtclima.setText("Formación de lluvia");
                        imageViewClima.setImageResource(R.drawable.broken_clouds);
                        break;
                    case "shower rain":
                        txtclima.setText("Chubascos");
                        imageViewClima.setImageResource(R.drawable.shower_rain);
                        break;
                    case "rain":
                        txtclima.setText("Lloviendo");
                        imageViewClima.setImageResource(R.drawable.rain);
                        break;
                    case "thunderstorm":
                        txtclima.setText("Tormenta eléctrica");
                        imageViewClima.setImageResource(R.drawable.thunderstorm);
                        break;
                    default:
                        txtclima.setText("Cielo despejado");
                        imageViewClima.setImageResource(R.drawable.clear_sky);
                        break;
                }

            }

            @Override
            public void onError(String message) {
                Toast.makeText(inicio.this, "" + message, Toast.LENGTH_SHORT).show();
                Log.d("weatherAPI", "error->" + message);
            }
        });


    }


    private void getWaterLevel(String currentneighborhood) {
        Query query = waterLevelRef.whereEqualTo("Barrio", currentneighborhood);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                    Double nivelAgua = documentSnapshot.getDouble("Nivel agua");
                    //  Toast.makeText(inicio.this, "nivelAgua->" + nivelAgua, Toast.LENGTH_SHORT).show();
                    waveView.setWaveHeight(Float.valueOf(String.valueOf(nivelAgua)));
                    // Haz algo con el nivel de agua
                } else {
                    Toast.makeText(inicio.this, "No encontrado", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        if (currentUser == null) {
            irMain();
        }

        firestoreHelper.getNeighborhood(this);
    }

    private void logout() {
        mAuth.signOut();
        irMain();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Stop listening for neighborhood changes
        firestoreHelper.stopListeningForNeighborhoodChanges();
    }

    private void irMain() {
        Intent main = new Intent(inicio.this, MainActivity.class);
        startActivity(main);
        finish();
    }

    private void dataUser(String token, String neighborhood) {


        Map<String, Object> user = new HashMap<>();
        user.put("Nombres y apellidos", currentUser.getDisplayName());
        user.put("Email", currentUser.getEmail());
        user.put("Foto", currentUser.getPhotoUrl());
        user.put("Token", token);
        user.put("Barrio", neighborhood);


        // Buscar documentos que tengan el mismo correo electrónico que el usuario actual
        db.collection("Usuarios")
                .whereEqualTo("Email", currentUser.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean documentExists = false;
                        // Si se encuentra un documento, actualizar los campos del usuario
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            documentExists = true;
                            document.getReference().update(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot updated with ID: " + document.getId());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error updating document", e);
                                        }
                                    });
                        }
                        // Si no se encontró un documento existente, crear uno nuevo
                        if (!documentExists) {
                            db.collection("Usuarios")
                                    .document(currentUser.getEmail()) // usar el correo electrónico como ID del documento
                                    .set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot added with ID: " + currentUser.getEmail());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error adding document", e);
                                        }
                                    });
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });

    }


    @Override
    public void onCallback(String neighborhood) {
        // Este método se llamará cada vez que el valor de "Barrio" en Firestore cambie
        // neighborhood es el nuevo valor de "Barrio"
        this.neighborhood = neighborhood;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remover el listener cuando ya no se necesita escuchar los cambios
        firestoreHelper.stopListeningForNeighborhoodChanges();
    }

    public void getNeighborhood() {
        userneighborhoodRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    currentneighborhood = documentSnapshot.getString("Barrio");
                    oldneneighborhood = currentneighborhood;
                    txtneighborhood.setText(currentneighborhood);

                    // Desuscribirse del tópico anterior
                    dessuscribeTopic(oldneneighborhood);

                    // Suscribirse al nuevo tópico
                    suscribeTopic(currentneighborhood);

                } else {
                    txtneighborhood.setText("No hay selección");
                }
            }
        });
        userneighborhoodRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    // error al escuchar cambios
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    String newneighborhood = snapshot.getString("Barrio");
                    if (!newneighborhood.equals(currentneighborhood)) {
                        // Desuscribirse del tópico anterior
                        dessuscribeTopic(currentneighborhood);

                        // Suscribirse al nuevo tópico
                        suscribeTopic(newneighborhood);

                        currentneighborhood = newneighborhood;
                    }
                    txtneighborhood.setText(currentneighborhood);
                    getWaterLevel(currentneighborhood);
                }
            }
        });
    }


    private void suscribeTopic(String currentneighborhood) {
        FirebaseMessaging.getInstance().subscribeToTopic(currentneighborhood)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(inicio.this, "Si", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Suscripción exitosa al tópico ->" + currentneighborhood);
                        } else {
                            //  Toast.makeText(inicio.this, "No", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error al suscribirse al tópico -> " + currentneighborhood + task.getException().getMessage());
                        }
                    }
                });

    }

    private void dessuscribeTopic(String oldneneighborhood) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(oldneneighborhood)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(inicio.this, "Desuscripción exitosa del tópico", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Desuscripción exitosa del tópico ->" + oldneneighborhood);
                        } else {
                            // Toast.makeText(inicio.this, "Error al desuscribirse", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error al desuscribirse del tópico: " + task.getException().getMessage());
                        }
                    }
                });


    }


}