package com.example.telemoney;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ResumenActivity extends AppCompatActivity {

    private TextView tvTotalIngresos, tvTotalEgresos, tvBalance, tvMesActual;
    private FirebaseFirestore db;
    private String userId;
    private Calendar calendario;
    private SimpleDateFormat formatoFirestore, formatoMesVisual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resumen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvTotalIngresos = findViewById(R.id.tv_total_ingresos_resumen);
        tvTotalEgresos = findViewById(R.id.tv_total_egresos_resumen);
        tvBalance = findViewById(R.id.tv_balance_total);
        tvMesActual = findViewById(R.id.tv_mes_actual);
        ImageButton btnMesAnterior = findViewById(R.id.btn_mes_anterior);
        ImageButton btnMesSiguiente = findViewById(R.id.btn_mes_siguiente);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser()==null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("ResumenActivity", "✅ Usuario autenticado: " + userId);

        // Formatos para mes y fecha
        calendario = Calendar.getInstance();
        formatoFirestore = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        formatoMesVisual = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));

        tvMesActual.setOnClickListener(v -> mostrarDialogoMes());


        btnMesAnterior.setOnClickListener(v -> {
            calendario.add(Calendar.MONTH, -1);
            actualizarResumen();
        });

        btnMesSiguiente.setOnClickListener(v -> {
            calendario.add(Calendar.MONTH, 1);
            actualizarResumen();
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_ingresos) {
                return true;

            } else if (itemId == R.id.nav_egresos) {
                startActivity(new Intent(this, EgresosActivity.class));
                finish();
                return true;

            } else if (itemId == R.id.nav_resumen) {
                startActivity(new Intent(this, ResumenActivity.class));
                finish();
                return true;

            } else if (itemId == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            }

            return false;
        });

        bottomNav.setSelectedItemId(R.id.nav_resumen);

        // Inicial
        actualizarResumen();

    }

    private void actualizarResumen() {
        String mesFiltro = new SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(calendario.getTime());
        tvMesActual.setText(formatoMesVisual.format(calendario.getTime()));

        double[] totalIngresos = {0.0};
        double[] totalEgresos = {0.0};

        // Obtener ingresos del mes
        db.collection("usuarios").document(userId)
                .collection("ingresos")
                .whereGreaterThanOrEqualTo("fecha", "01/" + mesFiltro)
                .whereLessThanOrEqualTo("fecha", "31/" + mesFiltro)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Double monto = doc.getDouble("monto");
                        if (monto != null) totalIngresos[0] += monto;
                    }
                    tvTotalIngresos.setText(String.format(Locale.getDefault(), "S/. %.2f", totalIngresos[0]));

                    // Obtener egresos después de ingresos
                    db.collection("usuarios").document(userId)
                            .collection("egresos")
                            .whereGreaterThanOrEqualTo("fecha", "01/" + mesFiltro)
                            .whereLessThanOrEqualTo("fecha", "31/" + mesFiltro)
                            .get()
                            .addOnSuccessListener(querySnapshots -> {
                                for (QueryDocumentSnapshot doc : querySnapshots) {
                                    Double monto = doc.getDouble("monto");
                                    if (monto != null) totalEgresos[0] += monto;
                                }
                                tvTotalEgresos.setText(String.format(Locale.getDefault(), "S/. %.2f", totalEgresos[0]));
                                double balance = totalIngresos[0] - totalEgresos[0];
                                tvBalance.setText(String.format(Locale.getDefault(), "%s S/. %.2f", (balance >= 0 ? "+" : "-"), Math.abs(balance)));
                            });
                });
    }

    private void mostrarDialogoMes() {
        int anio = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendario.set(Calendar.YEAR, year);
            calendario.set(Calendar.MONTH, month);
            actualizarResumen();
        }, anio, mes, 1); // El día no importa

        // Ocultar selección de día
        try {
            ((ViewGroup) ((ViewGroup) dialog.getDatePicker()).getChildAt(0)).getChildAt(2).setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog.show();
    }


}