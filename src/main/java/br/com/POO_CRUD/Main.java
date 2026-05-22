package br.com.POO_CRUD;

import br.com.POO_CRUD.dao.ClienteDAO;
import br.com.POO_CRUD.dao.ContaBancariaDAO;
import br.com.POO_CRUD.model.Cliente;
import br.com.POO_CRUD.model.ContaBancaria;
import br.com.POO_CRUD.service.AlphaVantageService;
import br.com.POO_CRUD.service.ViaCepService;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ClienteDAO clienteDAO = new ClienteDAO();
        AlphaVantageService apiService = new AlphaVantageService();
        ViaCepService viaCepService = new ViaCepService();
        int opcao = 0;

        while (opcao != 5) {
            System.out.println("\n=== BANCO CONSOLE ===");
            System.out.println("1. Cadastrar Novo Cliente");
            System.out.println("2. Listar Clientes");
            System.out.println("3. Atualizar Cliente");
            System.out.println("4. Deletar Cliente");
            System.out.println("5. Sair");
            System.out.print("Escolha uma opcao: ");

            Integer opcaoLida = lerInteiro(scanner);
            if (opcaoLida == null) {
                System.out.println("\nEntrada encerrada. Encerrando o sistema...");
                break;
            }

            opcao = opcaoLida;
            if (opcao < 1 || opcao > 5) {
                System.out.println("Por favor, digite um numero valido.");
                continue;
            }

            switch (opcao) {
                case 1:
                    System.out.println("\n=== NOVO CLIENTE ===");
                    System.out.print("Nome: ");
                    String nome = scanner.nextLine();

                    // Validacao do CPF
                    String cpf = "";
                    while (true) {
                        System.out.print("CPF (somente numeros ou com pontuacao): ");
                        String entradaCpf = scanner.nextLine();
                        cpf = entradaCpf.replaceAll("\\D", "");

                        if (cpf.length() == 11) {
                            if (clienteDAO.cpfJaCadastrado(cpf)) {
                                System.out.println("Erro: Este CPF ja esta vinculado a uma conta. Digite outro.");
                            } else {
                                break;
                            }
                        } else {
                            System.out.println("Erro: O CPF deve conter exatamente 11 numeros.");
                        }
                    }

                    // Validacao do CEP
                    String cep = "";
                    ViaCepService.Endereco endereco = null;
                    while (true) {
                        System.out.print("CEP (somente numeros ou com traco): ");
                        String entradaCep = scanner.nextLine();
                        cep = entradaCep.replaceAll("\\D", "");

                        if (cep.length() == 8) {
                            endereco = viaCepService.buscarEnderecoPorCep(cep);
                            if (endereco != null) {
                                System.out.println("Endereco encontrado: " + endereco.getRua() + " - " + endereco.getCidade());
                                break;
                            }

                            System.out.println("Erro: CEP nao encontrado no ViaCEP. Digite outro.");
                        } else {
                            System.out.println("Erro: O CEP deve conter exatamente 8 numeros.");
                        }
                    }

                    // Salva Cliente
                    Cliente novoCliente = new Cliente(nome, cpf, cep, endereco.getRua(), endereco.getCidade());
                    clienteDAO.salvar(novoCliente);

                    // Salva Conta
                    if (novoCliente.getId() != null) {
                        String numeroContaGerado = "100" + novoCliente.getId() + "-" + (int)(Math.random() * 10);
                        ContaBancaria novaConta = new ContaBancaria(
                                numeroContaGerado,
                                new java.math.BigDecimal("0.00"),
                                novoCliente.getId()
                        );

                        ContaBancariaDAO contaDAO = new ContaBancariaDAO();
                        contaDAO.salvar(novaConta);
                        System.out.println("Conta gerada: " + numeroContaGerado);
                        System.out.println("\n=== CADASTRO CONCLUIDO ===");
                        System.out.println("ID: " + novoCliente.getId());
                        System.out.println("Nome: " + novoCliente.getNome());
                        System.out.println("CPF: " + novoCliente.getCpf());
                        System.out.println("CEP: " + novoCliente.getCep());
                        System.out.println("Rua: " + novoCliente.getRua());
                        System.out.println("Cidade: " + novoCliente.getCidade());
                        System.out.println("Conta: " + numeroContaGerado);
                        System.out.println("Saldo inicial: R$ 0.00");

                        // Chamada da API
                        apiService.exibirTop4AcoesB3();
                    }
                    break;

                case 2:
                    System.out.println("\nBuscando registros no banco de dados...");
                    clienteDAO.listarClientesComConta();
                    break;

                case 3:
                    System.out.println("\n=== ATUALIZAR CLIENTE ===");
                    System.out.print("Digite o ID do cliente que deseja alterar: ");
                    Integer idAtualizar = lerInteiro(scanner);
                    if (idAtualizar == null) {
                        System.out.println("Entrada encerrada.");
                        break;
                    }

                    System.out.print("Digite o NOVO nome para este cliente: ");
                    String novoNome = scanner.nextLine();
                    clienteDAO.atualizarNome(idAtualizar, novoNome);
                    break;

                case 4:
                    System.out.println("\n=== DELETAR CLIENTE ===");
                    System.out.print("Digite o ID do cliente que deseja EXCLUIR: ");
                    Integer idDeletar = lerInteiro(scanner);
                    if (idDeletar == null) {
                        System.out.println("Entrada encerrada.");
                        break;
                    }

                    System.out.print("ATENCAO: Isso excluira o cliente e a conta permanentemente. Confirma? (S/N): ");
                    String confirma = scanner.nextLine();

                    if (confirma.equalsIgnoreCase("S")) {
                        clienteDAO.deletar(idDeletar);
                    } else {
                        System.out.println("Operacao cancelada.");
                    }
                    break;

                case 5:
                    System.out.println("Encerrando o sistema...");
                    break;

                default:
                    System.out.println("Opcao invalida!");
            }
        }
        scanner.close();
    }

    private static Integer lerInteiro(Scanner scanner) {
        if (!scanner.hasNextLine()) {
            return null;
        }

        String entrada = scanner.nextLine().trim();
        try {
            return Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
