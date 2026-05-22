package br.com.POO_CRUD.dao;

import br.com.POO_CRUD.config.DatabaseConnection;
import br.com.POO_CRUD.model.Cliente;

import java.sql.*;

public class ClienteDAO {

    public boolean cpfJaCadastrado(String cpf) {
        String sql = "SELECT id FROM cliente WHERE cpf = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar CPF: " + e.getMessage());
        }
        return false;
    }

    public void salvar(Cliente cliente) {
        String sql = "INSERT INTO cliente (nome, cpf, cep, rua, cidade) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getCpf());
            stmt.setString(3, cliente.getCep());
            stmt.setString(4, cliente.getRua());
            stmt.setString(5, cliente.getCidade());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                cliente.setId(rs.getInt(1));
            }
            System.out.println("Cliente cadastrado com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao salvar cliente: " + e.getMessage());
        }
    }

    public void listarClientesComConta() {
        String sql = "SELECT c.id, c.nome, c.cpf, c.cep, c.rua, c.cidade, cb.numero_conta, cb.saldo " +
                "FROM cliente c " +
                "INNER JOIN conta_bancaria cb ON c.id = cb.cliente_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n=== LISTA DE CLIENTES E CONTAS ===");
            boolean temRegistros = false;

            while (rs.next()) {
                temRegistros = true;
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                String cpf = rs.getString("cpf");
                String cep = rs.getString("cep");
                String rua = rs.getString("rua");
                String cidade = rs.getString("cidade");
                String numeroConta = rs.getString("numero_conta");
                java.math.BigDecimal saldo = rs.getBigDecimal("saldo");

                System.out.printf("ID: %d | Nome: %-15s | CPF: %s | CEP: %s | Rua: %s | Cidade: %s | Conta: %s | Saldo: R$ %.2f\n",
                        id, nome, cpf, cep, rua, cidade, numeroConta, saldo);
            }

            if (!temRegistros) {
                System.out.println("Nenhum cliente cadastrado.");
            }
            System.out.println("==================================");

        } catch (SQLException e) {
            System.err.println("Erro ao listar clientes: " + e.getMessage());
        }
    }

    public void atualizarNome(int id, String novoNome) {
        String sql = "UPDATE cliente SET nome = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novoNome);
            stmt.setInt(2, id);

            if (stmt.executeUpdate() > 0) {
                System.out.println("Nome atualizado com sucesso!");
            } else {
                System.out.println("Nenhum cliente encontrado com esse ID.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar cliente: " + e.getMessage());
        }
    }

    public void deletar(int id) {
        String sql = "DELETE FROM cliente WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            if (stmt.executeUpdate() > 0) {
                System.out.println("Cliente e conta excluidos com sucesso!");
            } else {
                System.out.println("Nenhum cliente encontrado com esse ID.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao deletar cliente: " + e.getMessage());
        }
    }
}
