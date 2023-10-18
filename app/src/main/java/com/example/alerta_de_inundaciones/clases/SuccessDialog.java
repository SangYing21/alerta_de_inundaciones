package com.example.alerta_de_inundaciones.clases;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alerta_de_inundaciones.R;


public class SuccessDialog {

    private Context context;
    private String title;
    private ArrayAdapter<String> autocomplete_neigborhood;
    private String message;
    private Runnable onClickListener;

    public SuccessDialog(Context context, String title, ArrayAdapter<String> autocomplete_neigborhood, String message, Runnable onClickListener) {
        this.context = context;
        this.title = title;
        this.autocomplete_neigborhood = autocomplete_neigborhood;
        this.message = message;
        this.onClickListener = onClickListener;
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);

        View view = LayoutInflater.from(context).inflate(R.layout.layout_success, (ViewGroup) null);

        builder.setView(view);
        ((TextView) view.findViewById(R.id.textTitle)).setText(title);

        AutoCompleteTextView neighborhoodView = view.findViewById(R.id.autoComplete_neigborhood);
        neighborhoodView.setAdapter(autocomplete_neigborhood);
        // ((TextView) view.findViewById(R.id.textMessage)).setText(message);
        ((Button) view.findViewById(R.id.buttonAction)).setText("Aceptar");
        ((Button) view.findViewById(R.id.buttonCancel)).setText("Cancelar");
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_success);

        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                if (onClickListener != null) {
                    onClickListener.run();
                    String neighborhoodSelected = neighborhoodView.getText().toString();
                    onNeighborhoodSelected(neighborhoodSelected);
                }
            }
        });


        view.findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }

    public interface OnNeighborhoodSelectedListener {
        void onNeighborhoodSelected(String neighborhood);
    }

    private OnNeighborhoodSelectedListener onNeighborhoodSelectedListener;

    public void setOnNeighborhoodSelectedListener(OnNeighborhoodSelectedListener listener) {
        this.onNeighborhoodSelectedListener = listener;
    }

    private void onNeighborhoodSelected(String neighborhood) {
        if (onNeighborhoodSelectedListener != null) {
            onNeighborhoodSelectedListener.onNeighborhoodSelected(neighborhood);
        }
    }

}

