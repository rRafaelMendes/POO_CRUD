package br.com.POO_CRUD;

import br.com.POO_CRUD.dao.ClienteDAO;
import br.com.POO_CRUD.dao.ContaBancariaDAO;
import br.com.POO_CRUD.model.Cliente;
import br.com.POO_CRUD.model.ContaBancaria;
import br.com.POO_CRUD.service.AlphaVantageService;
import br.com.POO_CRUD.service.ViaCepService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    private static final BigDecimal SALDO_INICIAL = new BigDecimal("0.00");

    public static void main(String[] args) {
        ClienteDAO clienteDAO = new ClienteDAO();
        ContaBancariaDAO contaDAO = new ContaBancariaDAO();
        AlphaVantageService alphaVantageService = new AlphaVantageService();
        ViaCepService viaCepService = new ViaCepService();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                exibirMenu();

                Integer opcao = lerInteiro(scanner);
                if (opcao == null) {
                    System.out.println("\nEntrada encerrada. Encerrando o sistema...");
                    return;
                }

                if (opcao < 1 || opcao > 5) {
                    System.out.println("Por favor, digite um numero valido.");
                    continue;
                }

                switch (opcao) {
                    case 1 -> cadastrarCliente(scanner, clienteDAO, contaDAO, viaCepService, alphaVantageService);
                    case 2 -> listarClientes(clienteDAO);
                    case 3 -> atualizarCliente(scanner, clienteDAO);
                    case 4 -> deletarCliente(scanner, clienteDAO);
                    case 5 -> {
                        System.out.println("Encerrando o sistema...");
                        return;
                    }
                    default -> System.out.println("Opcao invalida!");
                }
            }
        }
    }

    private static void exibirMenu() {
        System.out.println("\n=== BANCO CONSOLE ===");
        System.out.println("1. Cadastrar Novo Cliente");
        System.out.println("2. Listar Clientes");
        System.out.println("3. Atualizar Cliente");
        System.out.println("4. Deletar Cliente");
        System.out.println("5. Sair");
        System.out.print("Escolha uma opcao: ");
    }

    private static void cadastrarCliente(
            Scanner scanner,
            ClienteDAO clienteDAO,
            ContaBancariaDAO contaDAO,
            ViaCepService viaCepService,
            AlphaVantageService alphaVantageService) {

        System.out.println("\n=== NOVO CLIENTE ===");
        String nome = lerTextoObrigatorio(scanner, "Nome: ");
        String cpf = lerCpfValido(scanner, clienteDAO);
        EnderecoConsulta enderecoConsulta = lerEnderecoValido(scanner, viaCepService);

        Cliente novoCliente = new Cliente(
                nome,
                cpf,
                enderecoConsulta.cep(),
                enderecoConsulta.endereco().rua(),
                enderecoConsulta.endereco().cidade()
        );
        clienteDAO.salvar(novoCliente);

        if (novoCliente.getId() == null) {
            return;
        }

        String numeroContaGerado = gerarNumeroConta(novoCliente.getId());
        ContaBancaria novaConta = new ContaBancaria(numeroContaGerado, SALDO_INICIAL, novoCliente.getId());
        contaDAO.salvar(novaConta);

        exibirResumoCadastro(novoCliente, numeroContaGerado);
        exibirCotacoes(alphaVantageService);
    }

    private static String lerTextoObrigatorio(Scanner scanner, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = scanner.nextLine().trim();
            if (!entrada.isBlank()) {
                return entrada;
            }
            System.out.println("Erro: O campo nao pode ser vazio.");
        }
    }

    private static String lerCpfValido(Scanner scanner, ClienteDAO clienteDAO) {
        while (true) {
            System.out.print("CPF (somente numeros ou com pontuacao): ");
            String cpf = scanner.nextLine().replaceAll("\\D", "");

            if (cpf.length() != 11) {
                System.out.println("Erro: O CPF deve conter exatamente 11 numeros.");
                continue;
            }

            if (clienteDAO.cpfJaCadastrado(cpf)) {
                System.out.println("Erro: Este CPF ja esta vinculado a uma conta. Digite outro.");
                continue;
            }

            return cpf;
        }
    }

    private static EnderecoConsulta lerEnderecoValido(Scanner scanner, ViaCepService viaCepService) {
        while (true) {
            System.out.print("CEP (somente numeros ou com traco): ");
            String cep = scanner.nextLine().replaceAll("\\D", "");

            if (cep.length() != 8) {
                System.out.println("Erro: O CEP deve conter exatamente 8 numeros.");
                continue;
            }

            Optional<ViaCepService.Endereco> endereco = viaCepService.buscarEnderecoPorCep(cep);
            if (endereco.isPresent()) {
                ViaCepService.Endereco dadosEndereco = endereco.get();
                System.out.println("Endereco encontrado: " + dadosEndereco.rua() + " - " + dadosEndereco.cidade());
                return new EnderecoConsulta(cep, dadosEndereco);
            }

            System.out.println("Erro: CEP nao encontrado no ViaCEP. Digite outro.");
        }
    }

    private static String gerarNumeroConta(Integer clienteId) {
        int digito = ThreadLocalRandom.current().nextInt(10);
        return "100" + clienteId + "-" + digito;
    }

    private static void exibirResumoCadastro(Cliente cliente, String numeroConta) {
        System.out.println("Conta gerada: " + numeroConta);
        System.out.println("\n=== CADASTRO CONCLUIDO ===");
        System.out.println("ID: " + cliente.getId());
        System.out.println("Nome: " + cliente.getNome());
        System.out.println("CPF: " + cliente.getCpf());
        System.out.println("CEP: " + cliente.getCep());
        System.out.println("Rua: " + cliente.getRua());
        System.out.println("Cidade: " + cliente.getCidade());
        System.out.println("Conta: " + numeroConta);
        System.out.println("Saldo inicial: R$ 0.00");
    }

    private static void exibirCotacoes(AlphaVantageService alphaVantageService) {
        List<String> cotacoes = alphaVantageService.buscarTop4AcoesB3();
        if (cotacoes.isEmpty()) {
            System.out.println("\nAPI_KEY_ALPHA nao configurada. Consulta de cotacoes ignorada.");
            return;
        }

        System.out.println("\nConsultando as principais acoes da B3 no momento...");
        cotacoes.forEach(System.out::println);
    }

    private static void listarClientes(ClienteDAO clienteDAO) {
        System.out.println("\nBuscando registros no banco de dados...");
        clienteDAO.listarClientesComConta();
    }

    private static void atualizarCliente(Scanner scanner, ClienteDAO clienteDAO) {
        System.out.println("\n=== ATUALIZAR CLIENTE ===");
        System.out.print("Digite o ID do cliente que deseja alterar: ");
        Integer idAtualizar = lerInteiro(scanner);
        if (idAtualizar == null) {
            System.out.println("Entrada encerrada.");
            return;
        }

        String novoNome = lerTextoObrigatorio(scanner, "Digite o NOVO nome para este cliente: ");
        clienteDAO.atualizarNome(idAtualizar, novoNome);
    }

    private static void deletarCliente(Scanner scanner, ClienteDAO clienteDAO) {
        System.out.println("\n=== DELETAR CLIENTE ===");
        System.out.print("Digite o ID do cliente que deseja EXCLUIR: ");
        Integer idDeletar = lerInteiro(scanner);
        if (idDeletar == null) {
            System.out.println("Entrada encerrada.");
            return;
        }

        System.out.print("ATENCAO: Isso excluira o cliente e a conta permanentemente. Confirma? (S/N): ");
        String confirma = scanner.nextLine().trim();

        if (confirma.equalsIgnoreCase("S")) {
            clienteDAO.deletar(idDeletar);
        } else {
            System.out.println("Operacao cancelada.");
        }
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

    private record EnderecoConsulta(String cep, ViaCepService.Endereco endereco) {}
}
