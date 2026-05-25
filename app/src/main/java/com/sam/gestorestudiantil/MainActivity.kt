package com.sam.gestorestudiantil

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var spinCarrera: Spinner
    private lateinit var spinCurso: Spinner
    private lateinit var btnGuardar: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EstudianteAdapter

    private val listaEstudiantes = mutableListOf<Estudiante>()
    private val dbRef by lazy {
        FirebaseDatabase.getInstance().getReference("Estudiantes")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        etNombre    = findViewById(R.id.etNombre)
        spinCarrera = findViewById(R.id.spinCarrera)
        spinCurso   = findViewById(R.id.spinCurso)
        btnGuardar  = findViewById(R.id.btnGuardar)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EstudianteAdapter(
            listaEstudiantes,
            onEditar   = { mostrarDialogoEditar(it) },
            onEliminar = { confirmarEliminar(it) }
        )
        recyclerView.adapter = adapter

        btnGuardar.setOnClickListener { crear() }
        leer()
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    private fun crear() {
        val nombre  = etNombre.text.toString().trim()
        val carrera = spinCarrera.selectedItem.toString()
        val curso   = spinCurso.selectedItem.toString()

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Escribe el nombre", Toast.LENGTH_SHORT).show()
            return
        }

        val id = dbRef.push().key ?: return
        val estudiante = Estudiante(id, nombre, carrera, curso)

        dbRef.child(id).setValue(estudiante)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Estudiante guardado", Toast.LENGTH_SHORT).show()
                etNombre.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "❌ Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ── READ ──────────────────────────────────────────────────────────────────
    private fun leer() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nueva = mutableListOf<Estudiante>()
                for (child in snapshot.children) {
                    child.getValue(Estudiante::class.java)?.let { nueva.add(it) }
                }
                adapter.actualizar(nueva)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity,
                    "Error al leer: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    private fun mostrarDialogoEditar(estudiante: Estudiante) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_editar, null)

        val etN  = view.findViewById<EditText>(R.id.etNombreDialog)
        val spC  = view.findViewById<Spinner>(R.id.spinCarreraDialog)
        val spCu = view.findViewById<Spinner>(R.id.spinCursoDialog)

        etN.setText(estudiante.nombre)

        // Pre-seleccionar carrera actual en el spinner
        val carreras = resources.getStringArray(R.array.carreras)
        spC.setSelection(carreras.indexOf(estudiante.carrera).coerceAtLeast(0))

        // Pre-seleccionar curso actual en el spinner
        val cursos = resources.getStringArray(R.array.cursos)
        spCu.setSelection(cursos.indexOf(estudiante.curso).coerceAtLeast(0))

        AlertDialog.Builder(this)
            .setTitle("Editar Estudiante")
            .setView(view)
            .setPositiveButton("Actualizar") { _, _ ->
                val nombre  = etN.text.toString().trim()
                val carrera = spC.selectedItem.toString()
                val curso   = spCu.selectedItem.toString()

                if (nombre.isEmpty()) {
                    Toast.makeText(this, "Escribe el nombre", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val cambios = mapOf<String, Any>(
                    "nombre"  to nombre,
                    "carrera" to carrera,
                    "curso"   to curso
                )

                dbRef.child(estudiante.id).updateChildren(cambios)
                    .addOnSuccessListener {
                        Toast.makeText(this, "✅ Actualizado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "❌ Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    private fun confirmarEliminar(estudiante: Estudiante) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("¿Eliminar a ${estudiante.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                dbRef.child(estudiante.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "🗑️ Eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "❌ Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }
}