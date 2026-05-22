package br.com.POO_CRUD.dao;

import br.com.POO_CRUD.config.DatabaseConnection;
import br.com.POO_CRUD.model.ContaBancaria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ContaBancariaDAO {

    public void salvar(ContaBancaria conta) {
        String sql = "INSERT INTO conta_bancaria (numero_conta, saldo, cliente_id) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, conta.getNumeroConta());
            stmt.setBigDecimal(2, conta.getSaldo());
            stmt.setInt(3, conta.getClienteId());

            stmt.executeUpdate();
            System.out.println("Conta bancaria criada e vinculada com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao criar conta bancaria: " + e.getMessage());
        }
    }
}
