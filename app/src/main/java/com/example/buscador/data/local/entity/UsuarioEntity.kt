package com.example.buscador.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.buscador.model.Usuario

/**
 * Entidad de persistencia que representa la tabla "usuarios" de Room.
 *
 * Se mantiene independiente del modelo de dominio [Usuario] para que la capa de
 * presentación no dependa de anotaciones de base de datos (MVVM estricto).
 *
 * Decisiones técnicas:
 * - Las columnas usan snake_case mediante @ColumnInfo para mantener un esquema SQL legible.
 * - El correo se declara como índice único: evita dos perfiles con el mismo correo.
 * - La foto de perfil se guarda como texto porque Room no soporta android.net.Uri
 *   de forma nativa; se persiste la representación de la URI (content://...).
 *
 * @property idUsuario Clave primaria autogenerada.
 * @property nombreCompleto Nombre y apellidos del usuario.
 * @property correoElectronico Correo institucional único.
 * @property biografia Descripción libre del usuario.
 * @property uriFotoPerfil URI de la foto elegida con el selector del sistema, o null si no tiene.
 */
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo_electronico"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_usuario")
    val idUsuario: Long = 0L,

    @ColumnInfo(name = "nombre_completo")
    val nombreCompleto: String,

    @ColumnInfo(name = "correo_electronico")
    val correoElectronico: String,

    @ColumnInfo(name = "biografia")
    val biografia: String,

    @ColumnInfo(name = "uri_foto_perfil")
    val uriFotoPerfil: String?
)

/**
 * Mapper entidad -> dominio.
 * Se usa en la capa data para entregar objetos limpios a los ViewModel.
 */
fun UsuarioEntity.aModeloDominio(): Usuario = Usuario(
    idUsuario = idUsuario,
    nombreCompleto = nombreCompleto,
    correoElectronico = correoElectronico,
    biografia = biografia,
    uriFotoPerfil = uriFotoPerfil
)

/**
 * Mapper dominio -> entidad.
 * Se usa antes de insertar o actualizar una fila en Room.
 */
fun Usuario.aEntidadRoom(): UsuarioEntity = UsuarioEntity(
    idUsuario = idUsuario,
    nombreCompleto = nombreCompleto,
    correoElectronico = correoElectronico,
    biografia = biografia,
    uriFotoPerfil = uriFotoPerfil
)
