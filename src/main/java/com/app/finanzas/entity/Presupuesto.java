package com.app.finanzas.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "presupuesto")
public class Presupuesto {

    public enum Tipo { LIMITE, FIJO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String nombre;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "monto_estimado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoEstimado;

    @NotNull
    @Column(nullable = false, length = 10)
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private Tipo tipo = Tipo.LIMITE;

    @Size(max = 20)
    @Column(length = 20)
    private String color = "#C4985A";

    @Size(max = 50)
    @Column(length = 50)
    private String categoria;

    @Size(max = 200)
    @Column(length = 200)
    private String descripcion;

    @Min(1)
    @Max(31)
    @Column(name = "dia_vencimiento")
    private Integer diaVencimiento;

    @Column(nullable = false)
    private boolean activo = true;

    @OneToMany(mappedBy = "presupuesto", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PresupuestoPeriodo> periodos = new LinkedHashSet<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getMontoEstimado() { return montoEstimado; }
    public void setMontoEstimado(BigDecimal montoEstimado) { this.montoEstimado = montoEstimado; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getDiaVencimiento() { return diaVencimiento; }
    public void setDiaVencimiento(Integer diaVencimiento) { this.diaVencimiento = diaVencimiento; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Set<PresupuestoPeriodo> getPeriodos() { return periodos; }
    public void setPeriodos(Set<PresupuestoPeriodo> periodos) { this.periodos = periodos; }
}
