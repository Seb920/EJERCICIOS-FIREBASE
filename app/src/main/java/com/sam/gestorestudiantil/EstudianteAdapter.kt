package com.sam.gestorestudiantil

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EstudianteAdapter(
    private val lista: MutableList<Estudiante>,
    private val onEditar: (Estudiante) -> Unit,
    private val onEliminar: (Estudiante) -> Unit
) : RecyclerView.Adapter<EstudianteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView  = view.findViewById(R.id.tvNombre)
        val tvCarrera: TextView = view.findViewById(R.id.tvCarrera)
        val tvCurso: TextView   = view.findViewById(R.id.tvCurso)
        val btnEditar: Button   = view.findViewById(R.id.btnEditar)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_estudiante, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val e = lista[position]
        holder.tvNombre.text  = "👤 ${e.nombre}"
        holder.tvCarrera.text = "🎓 ${e.carrera}"
        holder.tvCurso.text   = "📚 ${e.curso}"
        holder.btnEditar.setOnClickListener   { onEditar(e) }
        holder.btnEliminar.setOnClickListener { onEliminar(e) }
    }

    override fun getItemCount() = lista.size

    fun actualizar(nuevaLista: List<Estudiante>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}