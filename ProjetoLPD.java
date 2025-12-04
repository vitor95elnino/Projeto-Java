package com.mycompany.projetolpd;


public class ProjetoLPD {

    public static void main(String[] args) {

        // 1. Inicializar Serviço de Dados
        ServicoDados servico = new ServicoDados();
        servico.carregarDados();

        // 2. Inicializar Gestores
        Eventos gestorEventos = new Eventos(servico);
        GestorParticipantes gestorParticipantes = new GestorParticipantes(servico);
        GestorRecursos gestorRecursos = new GestorRecursos(servico);
        GestorInscricoes gestorInscricoes = new GestorInscricoes(servico);

        // 3. Loop Principal
        while (true) {
            System.out.println("\n=== SISTEMA DE GESTAO DE EVENTOS ===");
            System.out.println("1. Eventos");
            System.out.println("2. Participantes");
            System.out.println("3. Recursos");
            System.out.println("4. Gerir Inscricoes");
            System.out.println("5. Sair (Guardar e Sair)");
            System.out.print("Opção: ");

            int opcao = servico.lerInteiro();

            switch (opcao) {
                case 1: 
                    gestorEventos.menu();
                    break;
                case 2:
                    gestorParticipantes.menu();
                    break;
                case 3:
                    gestorRecursos.menu();
                    break;
                case 4:
                    gestorInscricoes.menu();
                    break;
                case 5:
                    servico.guardarDados();
                    System.out.println("Dados guardados. A encerrar o sistema...");
                    return;
                
                default:
                    System.out.println("Opção invalida!");
                    break;
            }
        }
    }
}