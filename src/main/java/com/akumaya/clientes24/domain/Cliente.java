package com.akumaya.clientes24.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="clientes")
public class Cliente {
    @Id @GeneratedValue private UUID id;

    @Column(name="hora_registro", nullable=false) private OffsetDateTime horaRegistro;
    @Column(name="nombre_tutor", length=200, nullable=false) private String nombreTutor;
    @Column(length=120) private String ciudad;
    @Column(length=120) private String departamento;
    @Column(name="nombre_hijo", length=200) private String nombreHijo;
    @Column(name="edad_hijo") private Integer edadHijo;
    @Column(name="como_nos_conocio", length=200) private String comoNosConocio;
    @Column(name="acepta_newsletter", nullable=false) private boolean aceptaNewsletter;

    // getters/setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OffsetDateTime getHoraRegistro() {
        return horaRegistro;
    }

    public void setHoraRegistro(OffsetDateTime horaRegistro) {
        this.horaRegistro = horaRegistro;
    }

    public String getNombreTutor() {
        return nombreTutor;
    }

    public void setNombreTutor(String nombreTutor) {
        this.nombreTutor = nombreTutor;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getNombreHijo() {
        return nombreHijo;
    }

    public void setNombreHijo(String nombreHijo) {
        this.nombreHijo = nombreHijo;
    }

    public Integer getEdadHijo() {
        return edadHijo;
    }

    public void setEdadHijo(Integer edadHijo) {
        this.edadHijo = edadHijo;
    }

    public String getComoNosConocio() {
        return comoNosConocio;
    }

    public void setComoNosConocio(String comoNosConocio) {
        this.comoNosConocio = comoNosConocio;
    }

    public boolean isAceptaNewsletter() {
        return aceptaNewsletter;
    }

    public void setAceptaNewsletter(boolean aceptaNewsletter) {
        this.aceptaNewsletter = aceptaNewsletter;
    }

}
