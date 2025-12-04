package com.mycompany.projetolpd;
import java.time.LocalDateTime;


public class GestorParticipantes {
    
    private ServicoDados service;

    public GestorParticipantes(ServicoDados service) {
        this.service = service;
    }

    public void menu() {
        while(true) {
            System.out.println("\n--- MENU PARTICIPANTES ---");
            System.out.println("1. Criar Participante");
            System.out.println("2. Ver Participantes");
            System.out.println("3. Atualizar Participante");
            System.out.println("4. Eliminar Participante");
            System.out.println("5. Relatorio Participante");
            System.out.println("0. Voltar");
            
            int op = service.lerInteiro();
            if(op==0) break;
            switch(op) {
                case 1:
                    criar();
                    break;
                case 2:
                    listar();
                    break;
                case 3:
                    atualizar();
                    break;
                case 4:
                    eliminar();
                    break;
                case 5:
                    relatorio();
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        }
    }

    private void criar() {
        Participante p = new Participante();
        p.id = service.nextParticipanteId++;
        p.nome = service.lerTextoObrigatorio("Nome: ");
        System.out.print("Tipo (1-Palestrante, 2-Participante, 3-Moderador): ");
        int t = service.lerInteiro();
        p.tipo = (t>=1 && t<=3) ? t : 2;
        p.contacto = service.lerTextoObrigatorio("Contacto: ");
        p.dataInscricao = LocalDateTime.now();
        service.participantes.add(p);
        System.out.println("Participante criado ID: " + p.id);
    }

    private void listar() {
        System.out.println("\n>> Lista Participantes");
        for(Participante p : service.participantes) {
            System.out.printf("ID: %d | %s | Tipo: %s | Contacto: %s | Inscrição: %s\n", 
                p.id, p.nome, getTipoEmExtenso(p.tipo), p.contacto, formatarData(p.dataInscricao));
        }
    }
    
    private String getTipoEmExtenso(int tipo) {
        switch(tipo) {
            case 1: 
                return "Palestrante";
            case 2: 
                return "Participante";
            case 3: 
                return "Moderador";
            default: 
                return "Desconhecido";
        }
    }
    
    private String formatarData(LocalDateTime data) {
        if(data == null) return "N/A";
        return String.format("%02d/%02d/%d %02d:%02d", 
            data.getDayOfMonth(), data.getMonthValue(), data.getYear(),
            data.getHour(), data.getMinute());
    }

    private void atualizar() {
        listar();
        System.out.print("ID para atualizar: ");
        Participante p = service.findParticipanteById(service.lerInteiro());
        if(p!=null) {
            p.nome = service.lerTextoOpcional("Novo Nome", p.nome);
            p.contacto = service.lerTextoOpcional("Novo Contacto", p.contacto);
            
            System.out.print("Novo Tipo (1-Palestrante, 2-Participante, 3-Moderador, Enter mantém): ");
            String tipoStr = service.scanner.nextLine();
            if(!tipoStr.trim().isEmpty()) {
                try {
                    int novoTipo = Integer.parseInt(tipoStr);
                    p.tipo = (novoTipo >= 1 && novoTipo <= 3) ? novoTipo : p.tipo;
                } catch(NumberFormatException e) {
                    // Mantém o tipo atual
                }
            }
            
            System.out.println("Atualizado.");
        }
    }

    private void eliminar() {
        System.out.print("ID para eliminar: ");
        Participante p = service.findParticipanteById(service.lerInteiro());
        if(p!=null) { service.participantes.remove(p); System.out.println("Removido."); }
    }

    private void relatorio() {
        System.out.println("\n========== RELATÓRIO DE PARTICIPANTES ==========\n");
        
        // 1. Estatísticas sobre o número total de participantes
        relatorioEstatisticasGerais();
        
        // 2. Distribuição de participantes por tipo
        relatorioDistribuicaoPorTipo();
        
        // 3. Análise de participação
        relatorioAnaliseParticipacao();
        
        System.out.println("\n================================================\n");
    }
    
    private void relatorioEstatisticasGerais() {
        System.out.println("1. ESTATÍSTICAS GERAIS");
        System.out.println("----------------------------------------");
        
        int totalParticipantes = service.participantes.size();
        System.out.printf("  Total de participantes: %d\n", totalParticipantes);
        
        if(totalParticipantes == 0) {
            System.out.println("  Nenhum participante registado.\n");
            return;
        }
        
        // Calcular participantes inscritos em eventos
        java.util.Set<Integer> participantesEmEventos = new java.util.HashSet<>();
        for(Evento e : service.eventos) {
            participantesEmEventos.addAll(e.inscritosComTipos.keySet());
        }
        
        System.out.printf("  Participantes inscritos em eventos: %d\n", participantesEmEventos.size());
        System.out.printf("  Participantes não inscritos: %d\n\n", totalParticipantes - participantesEmEventos.size());
    }
    
    private void relatorioDistribuicaoPorTipo() {
        System.out.println("2. DISTRIBUIÇÃO DE PARTICIPANTES POR TIPO");
        System.out.println("----------------------------------------");
        
        int palest = 0, particip = 0, modera = 0;
        
        for(Participante p : service.participantes) {
            switch(p.tipo) {
                case 1: 
                    palest++;
                    break;
                case 2: 
                    particip++;
                    break;
                case 3: 
                    modera++;
                    break;
            }
        }
        
        int total = service.participantes.size();
        
        if(total == 0) {
            System.out.println("  Nenhum participante registado.\n");
            return;
        }
        
        System.out.printf("  Palestrantes: %d (%.1f%%)\n", palest, (palest * 100.0 / total));
        System.out.printf("  Participantes: %d (%.1f%%)\n", particip, (particip * 100.0 / total));
        System.out.printf("  Moderadores: %d (%.1f%%)\n\n", modera, (modera * 100.0 / total));
    }
    
    private void relatorioAnaliseParticipacao() {
        System.out.println("3. ANÁLISE DE PARTICIPAÇÃO");
        System.out.println("----------------------------------------");
        
        // Contar sessões e palestras por participante
        java.util.Map<Integer, Integer> sessoesPorParticipante = new java.util.HashMap<>();
        java.util.Map<Integer, Integer> palestrasPorParticipante = new java.util.HashMap<>();
        
        for(Evento e : service.eventos) {
            // Contar inscrições em sessões concluídas
            for(Sessao s : e.sessoes) {
                // Assumindo que todos os inscritos no evento participam das sessões
                for(Integer idPart : e.inscritosComTipos.keySet()) {
                    if(s.estado == 3) { // Sessão concluída
                        sessoesPorParticipante.put(idPart, sessoesPorParticipante.getOrDefault(idPart, 0) + 1);
                    }
                }
            }
            
            // Contar palestras por palestrante (moderador)
            for(Sessao s : e.sessoes) {
                if(s.idModerador > 0 && s.estado == 3) { // Sessão concluída
                    palestrasPorParticipante.put(s.idModerador, palestrasPorParticipante.getOrDefault(s.idModerador, 0) + 1);
                }
            }
        }
        
        if(sessoesPorParticipante.isEmpty() && palestrasPorParticipante.isEmpty()) {
            System.out.println("  Nenhuma participação em sessões concluídas.\n");
            return;
        }
        
        // Ordenar por participação
        System.out.println("  Participantes mais envolvidos (por sessões concluídas):");
        sessoesPorParticipante.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(10)
            .forEach(entrada -> {
                Participante p = service.findParticipanteById(entrada.getKey());
                if(p != null) {
                    System.out.printf("    - %s (%s): %d sessões concluídas\n", 
                        p.nome, getTipoEmExtenso(p.tipo), entrada.getValue());
                }
            });
        
        System.out.println("\n  Palestrantes mais ativos (por palestras realizadas):");
        palestrasPorParticipante.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(10)
            .forEach(entrada -> {
                Participante p = service.findParticipanteById(entrada.getKey());
                if(p != null) {
                    System.out.printf("    - %s (%s): %d palestras realizadas\n", 
                        p.nome, getTipoEmExtenso(p.tipo), entrada.getValue());
                }
            });
        
        System.out.println();
    }
}