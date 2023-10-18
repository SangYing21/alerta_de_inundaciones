package com.example.alerta_de_inundaciones.clases;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alerta_de_inundaciones.R;


public class WarningDialog {

    private Context context;
    private String title;
    private String message;
    private Runnable onClickListener;

    public WarningDialog(Context context, String title, String message, Runnable onClickListener) {
        this.context = context;
        this.title = title;
        this.message = message;
        this.onClickListener = onClickListener;
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);

        View view = LayoutInflater.from(context).inflate(R.layout.layout_warning, (ViewGroup) null);

        builder.setView(view);
        ((TextView) view.findViewById(R.id.textTitle)).setText(title);
        ((TextView) view.findViewById(R.id.textMessage)).setText(message);
        ((Button) view.findViewById(R.id.buttonAction)).setText("Ok");
        ((Button) view.findViewById(R.id.buttonCancel)).setText("Cancelar");
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_warning);

        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                if (onClickListener != null) {
                    onClickListener.run();
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

}

