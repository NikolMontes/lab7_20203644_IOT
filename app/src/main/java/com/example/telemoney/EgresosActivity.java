package com.example.telemoney;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.telemoney.R;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemoney.beans.Egreso;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.List;


public class EgresosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EgresoAdapter adapter;
    private List<Egreso> listaEgresos;
    private LinearLayout emptyStateContainer;
    private TextView tvTotalEgresos;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_egresos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerView = findViewById(R.id.recycler_view_egresos);
        emptyStateContainer = findViewById(R.id.empty_state_container);
        tvTotalEgresos = findViewById(R.id.tv_total_egresos);
        FloatingActionButton btnAgregar = findViewById(R.id.btn_agregar_egreso);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        listaEgresos = new ArrayList<>();
        adapter = new EgresoAdapter(listaEgresos, this::mostrarDialogoEditarEgreso, this::eliminarEgreso);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        cargarEgresos();

        btnAgregar.setOnClickListener(v -> mostrarDialogoEgreso(null));

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

        bottomNav.setSelectedItemId(R.id.nav_egresos);


    }

    private void cargarEgresos() {
        db.collection("usuarios").document(userId).collection("egresos")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;

                    listaEgresos.clear();
                    double total = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Egreso egreso = doc.toObject(Egreso.class);
                        egreso.setId(doc.getId());
                        listaEgresos.add(egreso);
                        total += egreso.getMonto();
                    }

                    adapter.notifyDataSetChanged();
                    emptyStateContainer.setVisibility(listaEgresos.isEmpty() ? View.VISIBLE : View.GONE);
                    tvTotalEgresos.setText(String.format("Total de Egresos: S/. %.2f", total));
                });
    }

    private void mostrarDialogoEgreso(@NonNull Egreso egresoExistente) {
        View view = LayoutInflater.from(this).inflate(R.layout.agregar_egreso, null);
        TextInputEditText etTitulo = view.findViewById(R.id.et_titulo);
        TextInputEditText etMonto = view.findViewById(R.id.et_monto);
        TextInputEditText etDescripcion = view.findViewById(R.id.et_descripcion);
        TextInputEditText etFecha = view.findViewById(R.id.et_fecha);
        TextView tvTituloDialog = view.findViewById(R.id.tv_titulo_dialog);
        Button btnGuardar = view.findViewById(R.id.btn_guardar);
        Button btnCancelar = view.findViewById(R.id.btn_cancelar);

        final Calendar calendario = Calendar.getInstance();
        etFecha.setOnClickListener(v -> {
            new DatePickerDialog(this, (view1, year, month, dayOfMonth) -> {
                String fechaFormateada = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                etFecha.setText(fechaFormateada);
            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show();
        });

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        if (egresoExistente != null) {
            tvTituloDialog.setText("Editar Egreso");
            etTitulo.setText(egresoExistente.getTitulo());
            etTitulo.setEnabled(false);
            etMonto.setText(String.valueOf(egresoExistente.getMonto()));
            etDescripcion.setText(egresoExistente.getDescripcion());
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etFecha.setText(formato.format(egresoExistente.getFecha()));
        }

        btnGuardar.setOnClickListener(v -> {
            String titulo = etTitulo.getText().toString().trim();
            String montoStr = etMonto.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();
            String fechaStr = etFecha.getText().toString().trim();
            Date fecha = null;
            try {
                fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaStr);
            } catch (Exception e) {
                Toast.makeText(this, "Fecha inválida", Toast.LENGTH_SHORT).show();
                return;
            }

            if (titulo.isEmpty() || montoStr.isEmpty() || fecha==null) {
                Toast.makeText(this, "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            double monto = Double.parseDouble(montoStr);
            Egreso egreso = new Egreso(titulo, monto, descripcion, fecha);

            if (egresoExistente != null) {
                db.collection("usuarios").document(userId)
                        .collection("egresos").document(egresoExistente.getId())
                        .update("monto", monto, "descripcion", descripcion)
                        .addOnSuccessListener(aVoid -> dialog.dismiss());
            } else {
                db.collection("usuarios").document(userId)
                        .collection("egresos").add(egreso)
                        .addOnSuccessListener(docRef -> dialog.dismiss());
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarDialogoEditarEgreso(Egreso egreso) {
        mostrarDialogoEgreso(egreso);
    }

    private void eliminarEgreso(Egreso egreso) {
        db.collection("usuarios").document(userId)
                .collection("egresos").document(egreso.getId())
                .delete()
                .addOnSuccessListener(unused -> Toast.makeText(this, "Egreso eliminado", Toast.LENGTH_SHORT).show());
    }


}