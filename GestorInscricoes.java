package com.mycompany.projetolpd;
import java.util.Map;

public class GestorInscricoes { // Se mantiver o ficheiro AreaPessoal.java, mude o nome da classe para AreaPessoal

    private ServicoDados service;

    public GestorInscricoes(ServicoDados service) {
        this.service = service;
    }

    public void menu() {
        while (true) {
            System.out.println("\n=== GESTÃO DE INSCRIÇÕES (ADMIN) ===");
            System.out.println("1. Nova Inscrição (Associar Participante a Evento)");
            System.out.println("2. Cancelar Inscrição");
            System.out.println("3. Ver Inscrições por Participante");
            System.out.println("4. Ver Inscrições por Evento");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");

            int op = service.lerInteiro();
            if (op == 0) break;

            switch (op) {
                case 1:
                    novaInscricao();
                    break;
                case 2:
                    cancelarInscricao();
                    break;
                case 3:
                    relatorioParticipante();
                    break;
                case 4:
                    listarParticipantesNoEvento();
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        }
    }

    private void novaInscricao() {
        // 1. Escolher Participante
        Participante p = escolherParticipante();
        if (p == null) return;

        // 2. Mostrar Eventos Disponíveis
        System.out.println("\n--- Eventos Disponíveis ---");
        boolean existemEventos = false;
        for (Evento e : service.eventos) {
            // Mostra apenas eventos não concluídos (estado != 3)
            if (e.estado != 3) { 
                String estadoDesc = service.getEstadoDesc(e.estado);
                // Verifica se já está inscrito
                String inscritoInfo = e.inscritosComTipos.containsKey(p.id) ? " [JÁ INSCRITO]" : "";
                System.out.printf("ID: %d | %s | %s%s\n", e.id, e.nome, estadoDesc, inscritoInfo);
                existemEventos = true;
            }
        }

        if (!existemEventos) {
            System.out.println("Não existem eventos disponíveis.");
            return;
        }

        // 3. Escolher Evento
        System.out.print("Digite o ID do evento para inscrever: ");
        int idEvento = service.lerInteiro();
        Evento eventoSelecionado = service.findEventoById(idEvento);

        if (eventoSelecionado == null) {
            System.out.println("Evento não encontrado.");
            return;
        }

        // 4. Efetuar Inscrição
        if (eventoSelecionado.inscritosComTipos.containsKey(p.id)) {
            System.out.println("Erro: Este participante já está inscrito neste evento.");
        } else {
            // Adiciona ao mapa usando o ID e o TIPO do participante
            eventoSelecionado.inscritosComTipos.put(p.id, p.tipo);
            System.out.println("Sucesso! " + p.nome + " foi inscrito em " + eventoSelecionado.nome);
        }
    }

    private void cancelarInscricao() {
        Participante p = escolherParticipante();
        if (p == null) return;

        System.out.println("\n--- Eventos em que " + p.nome + " está inscrito ---");
        boolean temInscricoes = false;
        
        for (Evento e : service.eventos) {
            if (e.inscritosComTipos.containsKey(p.id)) {
                System.out.printf("ID: %d | %s (%s)\n", e.id, e.nome, service.getEstadoDesc(e.estado));
                temInscricoes = true;
            }
        }

        if (!temInscricoes) {
            System.out.println("Este participante não tem inscrições.");
            return;
        }

        System.out.print("ID do evento a cancelar inscrição: ");
        int idEvento = service.lerInteiro();
        Evento eventoSelecionado = service.findEventoById(idEvento);
        
        if (eventoSelecionado != null && eventoSelecionado.inscritosComTipos.containsKey(p.id)) {
            eventoSelecionado.inscritosComTipos.remove(p.id);
            System.out.println("Inscrição cancelada com sucesso.");
        } else {
            System.out.println("Evento não encontrado ou participante não estava inscrito.");
        }
    }

    private void relatorioParticipante() {
        Participante p = escolherParticipante();
        if (p == null) return;

        System.out.println("\n=== RELATÓRIO DE: " + p.nome.toUpperCase() + " ===");
        System.out.println("Tipo: " + getTipoEmExtenso(p.tipo));
        System.out.println("Contacto: " + p.contacto);
        System.out.println("----------------------------------------");
        
        int total = 0;
        for (Evento e : service.eventos) {
            if (e.inscritosComTipos.containsKey(p.id)) {
                System.out.printf("- %s (Estado: %s)\n", e.nome, service.getEstadoDesc(e.estado));
                total++;
            }
        }
        
        if (total == 0) {
            System.out.println("Nenhuma inscrição registada.");
        } else {
            System.out.println("\nTotal de inscrições: " + total);
        }
    }
    // Método para visualizar participantes num evento
    private void listarParticipantesNoEvento() {
        // 1. Mostrar lista rápida de eventos para facilitar a escolha
        if (service.eventos.isEmpty()) {
            System.out.println("Não existem eventos registados.");
            return;
        }
        System.out.println("\n--- Lista de Eventos ---");
        for (Evento e : service.eventos) {
            System.out.printf("ID: %d | %s (Inscritos: %d)\n", e.id, e.nome, e.inscritosComTipos.size());
        }

        // 2. Pedir o ID
        System.out.print("Digite o ID do evento: ");
        Evento e = service.findEventoById(service.lerInteiro());

        if (e == null) {
            System.out.println("Evento não encontrado.");
            return;
        }

        // 3. Mostrar os Participantes
        System.out.println("\n=== PARTICIPANTES EM: " + e.nome.toUpperCase() + " ===");
        if (e.inscritosComTipos.isEmpty()) {
            System.out.println("Ainda não há ninguém inscrito neste evento.");
            return;
        }

        int contador = 1;
        // Percorrer a lista de IDs inscritos no evento
        for (Map.Entry<Integer, Integer> entrada : e.inscritosComTipos.entrySet()) {
            int idParticipante = entrada.getKey();
            int tipoNaInscricao = entrada.getValue(); // 1-Palestrante, 2-Participante, etc.
            
            // Ir buscar os dados completos da pessoa (nome, contacto)
            Participante p = service.findParticipanteById(idParticipante);
            
            if (p != null) {
                System.out.printf("%d. %s (%s) - Contacto: %s\n", 
                    contador++, 
                    p.nome, 
                    getTipoEmExtenso(tipoNaInscricao), 
                    p.contacto);
            }
        }
        System.out.println("Total: " + e.inscritosComTipos.size());
    }
    // Método auxiliar para listar e escolher participante
    private Participante escolherParticipante() {
        if (service.participantes.isEmpty()) {
            System.out.println("Não existem participantes registados no sistema.");
            return null;
        }

        System.out.println("\n--- Selecione o Participante ---");
        for (Participante p : service.participantes) {
            System.out.printf("ID: %d | %s (%s)\n", p.id, p.nome, getTipoEmExtenso(p.tipo));
        }

        System.out.print("Digite o ID do participante: ");
        int id = service.lerInteiro();
        Participante p = service.findParticipanteById(id);

        if (p == null) {
            System.out.println("Participante não encontrado.");
        }
        return p;
    }

    private String getTipoEmExtenso(int tipo) {
        switch(tipo) {
            case 1: return "Palestrante";
            case 2: return "Participante";
            case 3: return "Moderador";
            default: return "Desconhecido";
        }
    }
}
