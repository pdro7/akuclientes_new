package com.akumaya.clientes24.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Document(indexName="clientes")
public class ClienteDoc {
    @Id private UUID id;
    @Field(type=FieldType.Date)    private OffsetDateTime horaRegistro;
    @Field(type=FieldType.Text)    private String nombreTutor;
    @Field(type=FieldType.Keyword) private String ciudad;
    @Field(type=FieldType.Keyword) private String departamento;
    @Field(type=FieldType.Text)    private String nombreHijo;
    @Field(type=FieldType.Integer) private Integer edadHijo;
    @Field(type=FieldType.Text)    private String comoNosConocio;
    @Field(type=FieldType.Boolean) private boolean aceptaNewsletter;
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
