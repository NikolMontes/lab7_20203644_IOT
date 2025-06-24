package com.example.telemoney;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemoney.beans.Egreso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EgresoAdapter extends RecyclerView.Adapter<EgresoAdapter.EgresoViewHolder>{

    public interface OnEgresoActionListener {
        void onEditar(Egreso egreso);
        void onEliminar(Egreso egreso);
    }

    public interface OnEditarEgresoListener {
        void onEditar(Egreso egreso);
    }

    public interface OnEliminarEgresoListener {
        void onEliminar(Egreso egreso);
    }

    private List<Egreso> listaEgresos;
//    private OnEgresoActionListener listener;
//
//    public EgresoAdapter(List<Egreso> listaEgresos, OnEgresoActionListener listener) {
//        this.listaEgresos = listaEgresos;
//        this.listener = listener;
//    }
    private OnEditarEgresoListener editarListener;
    private OnEliminarEgresoListener eliminarListener;

    public EgresoAdapter(List<Egreso> lista, OnEditarEgresoListener editar, OnEliminarEgresoListener eliminar) {
        this.listaEgresos = lista;
        this.editarListener = editar;
        this.eliminarListener = eliminar;
    }

    @NonNull
    @Override
    public EgresoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_egreso, parent, false);
        return new EgresoViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull EgresoViewHolder holder, int position) {
        Egreso egreso = listaEgresos.get(position);
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());


        holder.tvTitulo.setText(egreso.getTitulo());
        holder.tvDescripcion.setText(egreso.getDescripcion().isEmpty() ? "(Sin descripción)" : egreso.getDescripcion());
        holder.tvFecha.setText(formatoFecha.format(egreso.getFecha()));
        holder.tvMonto.setText(String.format("S/. %.2f", egreso.getMonto()));

//        holder.btnEditar.setOnClickListener(v -> {
//            if (listener != null) listener.onEditar(egreso);
//        });
//
//        holder.btnEliminar.setOnClickListener(v -> {
//            if (listener != null) listener.onEliminar(egreso);
//        });
        holder.btnEditar.setOnClickListener(v -> editarListener.onEditar(egreso));
        holder.btnEliminar.setOnClickListener(v -> eliminarListener.onEliminar(egreso));

    }

    @Override
    public int getItemCount() {
        return listaEgresos.size();
    }

    static class EgresoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescripcion, tvFecha, tvMonto;
        ImageButton btnEditar, btnEliminar;
        ImageView ivIcono;

        public EgresoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tv_titulo_ingreso); // si usas el mismo ID
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion_ingreso);
            tvFecha = itemView.findViewById(R.id.tv_fecha_ingreso);
            tvMonto = itemView.findViewById(R.id.tv_monto_ingreso);
            btnEditar = itemView.findViewById(R.id.btn_editar_ingreso);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar_ingreso);
            ivIcono = itemView.findViewById(R.id.iv_icono_ingreso);
        }
    }

}
