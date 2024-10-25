package me.ewahv1.plugin.Models.MisionRecoleccion;

public class MisionRecoleccionMisionModel {
    public int id; // ID de la misión
    public String nombre; // Nombre de la misión
    public String logo; // Logo de la misión
    public String descripcion; // Descripción de la misión
    public MisionRecoleccionObjetivosModel objetivo; // Objetivo de la misión
    public MisionRecoleccionRecompensaModel recompensa; // Recompensa de la misión
    public int tier; // Tier de la misión
}