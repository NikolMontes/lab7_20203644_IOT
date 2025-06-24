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
import com.example.telemoney.beans.Ingreso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class IngresoAdapter extends RecyclerView.Adapter<IngresoAdapter.IngresoViewHolder> {
    public interface OnIngresoActionListener {
        void onEditar(Ingreso ingreso);
        void onEliminar(Ingreso ingreso);
    }
    public interface OnEditarIngresoListener {
        void onEditar(Ingreso ingreso);
    }

    public interface OnEliminarIngresoListener {
        void onEliminar(Ingreso ingreso);
    }


    private List<Ingreso> listaIngresos;
//    private OnIngresoActionListener listener;
//
//    public IngresoAdapter(List<Ingreso> listaIngresos, OnIngresoActionListener listener) {
//        this.listaIngresos = listaIngresos;
//        this.listener = listener;
//    }
    private IngresoAdapter.OnEditarIngresoListener editarListener;
    private IngresoAdapter.OnEliminarIngresoListener eliminarListener;

    public IngresoAdapter(List<Ingreso> lista, IngresoAdapter.OnEditarIngresoListener editar, IngresoAdapter.OnEliminarIngresoListener eliminar) {
        this.listaIngresos = lista;
        this.editarListener = editar;
        this.eliminarListener = eliminar;
    }

    @NonNull
    @Override
    public IngresoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingreso, parent, false);
        return new IngresoViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull IngresoViewHolder holder, int position) {
        Ingreso ingreso = listaIngresos.get(position);
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());


        holder.tvTitulo.setText(ingreso.getTitulo());
        holder.tvDescripcion.setText(ingreso.getDescripcion().isEmpty() ? "(Sin descripción)" : ingreso.getDescripcion());
        holder.tvFecha.setText(formatoFecha.format(ingreso.getFecha()));
        holder.tvMonto.setText(String.format("S/. %.2f", ingreso.getMonto()));

//        holder.btnEditar.setOnClickListener(v -> {
//            if (listener != null) listener.onEditar(ingreso);
//        });
//
//        holder.btnEliminar.setOnClickListener(v -> {
//            if (listener != null) listener.onEliminar(ingreso);
//        });

        holder.btnEditar.setOnClickListener(v -> editarListener.onEditar(ingreso));
        holder.btnEliminar.setOnClickListener(v -> eliminarListener.onEliminar(ingreso));
    }

    @Override
    public int getItemCount() {
        return listaIngresos.size();
    }
    static class IngresoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescripcion, tvFecha, tvMonto;
        ImageButton btnEditar, btnEliminar;
        ImageView ivIcono;

        public IngresoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tv_titulo_ingreso);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion_ingreso);
            tvFecha = itemView.findViewById(R.id.tv_fecha_ingreso);
            tvMonto = itemView.findViewById(R.id.tv_monto_ingreso);
            btnEditar = itemView.findViewById(R.id.btn_editar_ingreso);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar_ingreso);
            ivIcono = itemView.findViewById(R.id.iv_icono_ingreso);
        }
    }

}
