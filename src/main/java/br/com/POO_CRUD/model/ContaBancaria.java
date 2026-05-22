package br.com.POO_CRUD.model;

import java.math.BigDecimal;

public class ContaBancaria {
    private Integer id;
    private String numeroConta;
    private BigDecimal saldo;
    private Integer clienteId;

    public ContaBancaria(String numeroConta, BigDecimal saldo, Integer clienteId) {
        this.numeroConta = numeroConta;
        this.saldo = saldo;
        this.clienteId = clienteId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNumeroConta() { return numeroConta; }
    public void setNumeroConta(String numeroConta) { this.numeroConta = numeroConta; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }
}