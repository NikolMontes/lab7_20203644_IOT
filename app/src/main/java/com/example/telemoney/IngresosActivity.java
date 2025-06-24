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


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.telemoney.R;

import com.example.telemoney.beans.Ingreso;
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
import java.util.List;
import java.util.Locale;

public class IngresosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private IngresoAdapter adapter;
    private List<Ingreso> listaIngresos;
    private LinearLayout emptyStateContainer;
    private TextView tvTotalIngresos;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ingresos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recycler_view_ingresos);
        emptyStateContainer = findViewById(R.id.empty_state_container);
        tvTotalIngresos = findViewById(R.id.tv_total_ingresos);
        FloatingActionButton btnAgregar = findViewById(R.id.btn_agregar_ingreso);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        listaIngresos = new ArrayList<>();
        adapter = new IngresoAdapter(listaIngresos, this::mostrarDialogoEditarIngreso, this::eliminarIngreso);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        cargarIngresos();

        btnAgregar.setOnClickListener(v -> mostrarDialogoIngreso(null));

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

        bottomNav.setSelectedItemId(R.id.nav_ingresos);

    }

    private void cargarIngresos() {
        db.collection("usuarios").document(userId).collection("ingresos")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;

                    listaIngresos.clear();
                    double total = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Ingreso ingreso = doc.toObject(Ingreso.class);
                        ingreso.setId(doc.getId());
                        listaIngresos.add(ingreso);
                        total += ingreso.getMonto();
                    }

                    adapter.notifyDataSetChanged();
                    emptyStateContainer.setVisibility(listaIngresos.isEmpty() ? View.VISIBLE : View.GONE);
                    tvTotalIngresos.setText(String.format("Total de Ingresos: S/. %.2f", total));
                });
    }

    private void mostrarDialogoIngreso(@NonNull Ingreso ingresoExistente) {
        View view = LayoutInflater.from(this).inflate(R.layout.agregar_ingreso, null);
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

        if (ingresoExistente != null) {
            tvTituloDialog.setText("Editar Ingreso");
            etTitulo.setText(ingresoExistente.getTitulo());
            etTitulo.setEnabled(false); // solo monto y descripción
            etMonto.setText(String.valueOf(ingresoExistente.getMonto()));
            etDescripcion.setText(ingresoExistente.getDescripcion());
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etFecha.setText(formato.format(ingresoExistente.getFecha()));
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
            Ingreso ingreso = new Ingreso(titulo, monto, descripcion, fecha);

            if (ingresoExistente != null) {
                db.collection("usuarios").document(userId)
                        .collection("ingresos").document(ingresoExistente.getId())
                        .update("monto", monto, "descripcion", descripcion)
                        .addOnSuccessListener(aVoid -> dialog.dismiss());
            } else {
                db.collection("usuarios").document(userId)
                        .collection("ingresos").add(ingreso)
                        .addOnSuccessListener(docRef -> dialog.dismiss());
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarDialogoEditarIngreso(Ingreso ingreso) {
        mostrarDialogoIngreso(ingreso);
    }

    private void eliminarIngreso(Ingreso ingreso) {
        db.collection("usuarios").document(userId)
                .collection("ingresos").document(ingreso.getId())
                .delete()
                .addOnSuccessListener(unused -> Toast.makeText(this, "Ingreso eliminado", Toast.LENGTH_SHORT).show());
    }


}