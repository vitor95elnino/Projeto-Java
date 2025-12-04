package com.mycompany.projetolpd;
import java.util.Map;

public class Eventos {
        
    private ServicoDados service;

    public Eventos(ServicoDados service) {
        this.service = service;
    }

    public void menu() {
        while (true) {
            System.out.println("\n--- MENU EVENTOS ---");
            System.out.println("1. Criar Evento");
            System.out.println("2. Ver Evento (Gerir Sessões)");
            System.out.println("3. Atualizar Evento");
            System.out.println("4. Eliminar Evento");
            System.out.println("5. Relatorio Evento");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");

            int op = service.lerInteiro();
            if (op == 0) break;

            switch (op) {
                case 1:
                    criar();
                    break;
                case 2:
                    listarGerirSessoes();
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
                    System.out.println("Opcao invalida.");
                    break;
            }
        }
    }

    private void criar() {
        System.out.println("\n>> Criar Novo Evento");
        Evento e = new Evento();
        e.id = service.nextEventoId++;
        e.nome = service.lerTextoObrigatorio("Nome: ");
        e.local = service.lerTextoObrigatorio("Local: ");

        // Validar datas
        while (true) {
            e.dataInicio = service.lerDataValida("Data Inicio (dd/MM/yyyy HH:mm): ");
            e.dataFim = service.lerDataValida("Data Fim (dd/MM/yyyy HH:mm): ");
            if (e.dataFim.isAfter(e.dataInicio)) break;
            System.out.println("Erro: Data Fim deve ser posterior à Data Inicio.");
        }

        // --- ALTERAÇÃO AQUI: Perguntar o Estado ---
        while (true) {
            System.out.println("Estado do Evento (1-Planeado, 2-Em Progresso, 3-Concluído): ");
            int est = service.lerInteiro();
            if (est >= 1 && est <= 3) {
                e.estado = est;
                break;
            }
            System.out.println("Opção inválida. Por favor escolha 1, 2 ou 3.");
        }
        // ------------------------------------------

        service.eventos.add(e);
        System.out.println("Evento criado com sucesso! ID: " + e.id);
        
        System.out.println("Deseja associar recursos agora? (1-Sim / 0-Não)");
        if(service.lerInteiro() == 1) gerirRecursos(e);
    }

    private void listarGerirSessoes() {
        if (service.eventos.isEmpty()) {
            System.out.println("Não existem eventos registados.");
            return;
        }

        System.out.println("\n>> Lista de Eventos:");
        for (Evento e : service.eventos) {
            System.out.printf("ID: %d | Nome: %s | Estado: %s\n", e.id, e.nome, service.getEstadoDesc(e.estado));
            System.out.printf("   Local: %s\n", e.local);
            System.out.printf("   Início: %s | Fim: %s\n", formatarData(e.dataInicio), formatarData(e.dataFim));
            System.out.printf("   Sessões: %d | Participantes: %d\n", e.sessoes.size(), e.inscritosComTipos.size());
            
            // Listar sessões se existirem
            if(!e.sessoes.isEmpty()) {
                System.out.println("   Sessões:");
                for(Sessao s : e.sessoes) {
                    System.out.printf("     - %s (%s)\n", s.descricao, service.getEstadoDesc(s.estado));
                    System.out.printf("       Início: %s | Fim Previsto: %s\n", 
                        formatarData(s.inicio), formatarData(s.fimPrevisto));
                }
            }
        }

        System.out.print("\nSelecione o ID do evento para gerir Sessões (ou 0 para voltar): ");
        int id = service.lerInteiro();
        if (id == 0) return;

        Evento eventoSelecionado = service.findEventoById(id);
        if (eventoSelecionado != null) {
            menuSessoes(eventoSelecionado);
        } else {
            System.out.println("Evento não encontrado.");
        }
    }

    private void menuSessoes(Evento evento) {
        while(true) {
            System.out.println("\n--- GESTÃO DE SESSÕES: " + evento.nome + " ---");
            System.out.println("Evento: " + formatarData(evento.dataInicio) + " até " + formatarData(evento.dataFim));
            System.out.println("1. Criar Sessão");
            System.out.println("2. Listar Sessões");
            System.out.println("3. Atualizar Sessão");
            System.out.println("4. Relatório Sessões");
            System.out.println("0. Voltar");
            System.out.print("Opção: ");
            int op = service.lerInteiro();
            if (op == 0) break;

            if (op == 1) {
                criarSessao(evento);
            } else if (op == 2) {
                listarSessoes(evento);
            } else if (op == 3) {
                atualizarSessao(evento);
            } else if (op == 4) {
                relatorioSessoesEvento(evento);
            }
        }
    }

    private void relatorioSessoesEvento(Evento evento) {
        System.out.println("\n--- RELATÓRIO DE SESSÕES: " + (evento.nome == null ? "(sem nome)" : evento.nome) + " ---");

        // 1) Número total de sessões no evento
        System.out.println("1) Número total de sessões: " + evento.sessoes.size());

        // 2) Tempo médio para concluir sessões (apenas sessões concluídas)
        long tempoTotalMinutos = 0;
        int sessoesConcluidas = 0;
        for (Sessao s : evento.sessoes) {
            if (s.estado == 3 && s.inicio != null && s.fimPrevisto != null) {
                long minutos = java.time.temporal.ChronoUnit.MINUTES.between(s.inicio, s.fimPrevisto);
                tempoTotalMinutos += minutos;
                sessoesConcluidas++;
            }
        }
        if (sessoesConcluidas == 0) {
            System.out.println("2) Tempo médio para concluir sessões: Nenhuma sessão concluída ainda.");
        } else {
            long mediaMinutos = tempoTotalMinutos / sessoesConcluidas;
            long horas = mediaMinutos / 60;
            long minutos = mediaMinutos % 60;
            System.out.printf("2) Tempo médio para concluir sessões: %d horas e %d minutos (%d sessões)\n", horas, minutos, sessoesConcluidas);
        }

        // 3) Identificação de atrasos (sessões com fimPrevisto antes de agora e não concluídas)
        System.out.println("3) Sessões atrasadas:");
        boolean temAtrasos = false;
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        for (Sessao s : evento.sessoes) {
            if (s.fimPrevisto != null && s.estado != 3 && s.fimPrevisto.isBefore(agora)) {
                temAtrasos = true;
                System.out.printf("  - Sessão %d: %s | Fim Previsto: %s | Estado: %s\n",
                        s.id,
                        s.descricao == null ? "(sem descrição)" : s.descricao,
                        formatarData(s.fimPrevisto),
                        service.getEstadoDesc(s.estado)
                );
            }
        }
        if (!temAtrasos) System.out.println("  Nenhuma sessão atrasada neste evento.");
        System.out.println("----------------------------------------");
    }
    
    private void criarSessao(Evento evento) {
        Sessao s = new Sessao();
        s.id = service.nextSessaoId++;
        s.idEvento = evento.id;
        s.descricao = service.lerTextoObrigatorio("Descrição da Sessão: ");
        
        // Validar datas da sessão
        while(true) {
            s.inicio = service.lerDataValida("Início (dd/MM/yyyy HH:mm): ");
            s.fimPrevisto = service.lerDataValida("Fim Previsto (dd/MM/yyyy HH:mm): ");
            
            // Validações
            if(!s.fimPrevisto.isAfter(s.inicio)) {
                System.out.println("Erro: Fim Previsto deve ser posterior ao Início.");
                continue;
            }
            
            if(s.inicio.isBefore(evento.dataInicio)) {
                System.out.println("Erro: Início da sessão (" + formatarData(s.inicio) + ") é anterior ao início do evento (" + formatarData(evento.dataInicio) + ").");
                continue;
            }
            
            if(s.fimPrevisto.isAfter(evento.dataFim)) {
                System.out.println("Erro: Fim previsto da sessão (" + formatarData(s.fimPrevisto) + ") é posterior ao fim do evento (" + formatarData(evento.dataFim) + ").");
                continue;
            }
            
            break; // Todas as validações passaram
        }
        
        // Selecionar Estado
        while(true) {
            System.out.println("Estado da Sessão (1-Planeada, 2-Em Progresso, 3-Concluída): ");
            int est = service.lerInteiro();
            if(est >= 1 && est <= 3) {
                s.estado = est;
                break;
            }
            System.out.println("Opção inválida. Por favor escolha 1, 2 ou 3.");
        }
        
        // Selecionar Moderador
        s.idModerador = selecionarModerador(evento);
        
        evento.sessoes.add(s);
        System.out.println("Sessão adicionada com sucesso!");
    }
    
    private void listarSessoes(Evento evento) {
        if (evento.sessoes.isEmpty()) System.out.println("Nenhuma sessão.");
        else {
            for (Sessao s : evento.sessoes) {
                System.out.printf("ID: %d | %s | %s\n", s.id, s.descricao, service.getEstadoDesc(s.estado));
                System.out.printf("  Início: %s | Fim: %s\n", formatarData(s.inicio), formatarData(s.fimPrevisto));
                
                Participante mod = service.findParticipanteById(s.idModerador);
                if(mod != null) {
                    System.out.printf("  Moderador: %s\n", mod.nome);
                }
            }
        }
    }
    
    private void atualizarSessao(Evento evento) {
        if(evento.sessoes.isEmpty()) {
            System.out.println("Nenhuma sessão para atualizar.");
            return;
        }
        
        listarSessoes(evento);
        
        System.out.print("\nID da Sessão para atualizar: ");
        int idSessao = service.lerInteiro();
        
        Sessao sessaoAtualizar = null;
        for(Sessao s : evento.sessoes) {
            if(s.id == idSessao) {
                sessaoAtualizar = s;
                break;
            }
        }
        
        if(sessaoAtualizar == null) {
            System.out.println("Sessão não encontrada.");
            return;
        }
        
        System.out.println("\n--- Atualizar Sessão ---");
        
        // Atualizar descrição
        sessaoAtualizar.descricao = service.lerTextoOpcional("Nova Descrição", sessaoAtualizar.descricao);
        
        // Atualizar datas
        System.out.println("Atualizar datas? (1-Sim / 0-Não): ");
        if(service.lerInteiro() == 1) {
            while(true) {
                sessaoAtualizar.inicio = service.lerDataOpcional("Novo Início", sessaoAtualizar.inicio);
                sessaoAtualizar.fimPrevisto = service.lerDataOpcional("Novo Fim Previsto", sessaoAtualizar.fimPrevisto);
                
                // Validações
                if(!sessaoAtualizar.fimPrevisto.isAfter(sessaoAtualizar.inicio)) {
                    System.out.println("Erro: Fim Previsto deve ser posterior ao Início.");
                    continue;
                }
                
                if(sessaoAtualizar.inicio.isBefore(evento.dataInicio)) {
                    System.out.println("Erro: Início da sessão é anterior ao início do evento.");
                    continue;
                }
                
                if(sessaoAtualizar.fimPrevisto.isAfter(evento.dataFim)) {
                    System.out.println("Erro: Fim previsto da sessão é posterior ao fim do evento.");
                    continue;
                }
                
                break;
            }
        }
        
        // Atualizar estado
        System.out.println("Alterar Estado? (1-Sim / 0-Não): ");
        if(service.lerInteiro() == 1) {
            while(true) {
                System.out.println("Novo Estado (1-Planeada, 2-Em Progresso, 3-Concluída): ");
                int est = service.lerInteiro();
                if(est >= 1 && est <= 3) {
                    sessaoAtualizar.estado = est;
                    break;
                }
                System.out.println("Opção inválida. Por favor escolha 1, 2 ou 3.");
            }
        }
        
        // Atualizar moderador
        System.out.println("Alterar Moderador? (1-Sim / 0-Não): ");
        if(service.lerInteiro() == 1) {
            sessaoAtualizar.idModerador = selecionarModerador(evento);
        }
        
        System.out.println("Sessão atualizada com sucesso!");
    }
    
    private int selecionarModerador(Evento evento) {
        // Listar moderadores (tipo 3) e palestrantes (tipo 1) inscritos no evento
        java.util.List<Participante> moderadores = new java.util.ArrayList<>();
        
        for(Integer idPart : evento.inscritosComTipos.keySet()) {
            int tipo = evento.inscritosComTipos.get(idPart);
            if(tipo == 1 || tipo == 3) { // Palestrante ou Moderador
                Participante p = service.findParticipanteById(idPart);
                if(p != null) {
                    moderadores.add(p);
                }
            }
        }
        
        if(moderadores.isEmpty()) {
            System.out.println("[!] Não existem palestrantes ou moderadores inscritos neste evento.");
            System.out.println("    Por favor, inscreva um palestrante ou moderador primeiro.");
            return -1;
        }
        
        System.out.println("\nModerador responsável pela sessão:");
        for(int i = 0; i < moderadores.size(); i++) {
            Participante p = moderadores.get(i);
            String tipo = (p.tipo == 1) ? "Palestrante" : "Moderador";
            System.out.printf("%d. %s (%s)\n", i+1, p.nome, tipo);
        }
        
        System.out.print("Selecione o moderador (número): ");
        int escolha = service.lerInteiro();
        
        if(escolha >= 1 && escolha <= moderadores.size()) {
            return moderadores.get(escolha - 1).id;
        } else {
            System.out.println("Opção inválida. Moderador não definido.");
            return -1;
        }
    }

    // --- ALTERAÇÕES: mostrar lista antes de pedir ID ---
    private void atualizar() {
        mostrarListaEventos();
        System.out.print("ID do Evento a atualizar: ");
        Evento e = service.findEventoById(service.lerInteiro());
        if (e == null) {
            System.out.println("Evento não encontrado.");
            return;
        }

        System.out.println("Atualizar dados (Enter mantem actual):");
        e.nome = service.lerTextoOpcional("Novo Nome", e.nome);
        e.local = service.lerTextoOpcional("Novo Local", e.local);
        
        e.dataInicio = service.lerDataOpcional("Novo Início", e.dataInicio);
        e.dataFim = service.lerDataOpcional("Novo Fim", e.dataFim);

        // Opcional: Permitir atualizar o estado aqui também
        System.out.println("Estado Atual: " + service.getEstadoDesc(e.estado));
        System.out.print("Alterar Estado? (1-Sim / 0-Não): ");
        if(service.lerInteiro() == 1) {
             while (true) {
                System.out.println("Novo Estado (1-Planeado, 2-Em Progresso, 3-Concluído): ");
                int est = service.lerInteiro();
                if (est >= 1 && est <= 3) { e.estado = est; break; }
            }
        }

        System.out.print("Gerir Recursos alocados? (1-Sim / 0-Não): ");
        if (service.lerInteiro() == 1) gerirRecursos(e);
        
        System.out.println("Evento atualizado.");
    }

    private void gerirRecursos(Evento e) {
        System.out.println("--- Alocação de Recursos ---");
        for(Recurso r : service.recursos) System.out.printf("ID %d: %s (Stock: %d)\n", r.id, r.nome, r.quantidade);
        
        System.out.print("ID do Recurso a adicionar/atualizar: ");
        Recurso r = service.findRecursoById(service.lerInteiro());
        
        if (r == null) {
            System.out.println("Recurso inválido.");
            return;
        }

        System.out.print("Quantidade a usar: ");
        int qtd = service.lerInteiro();

        if (qtd > r.quantidade) {
            System.out.println("[!] Quantidade solicitada maior que stock (" + r.quantidade + "). Confirmar? (1-Sim / 0-Não)");
            if (service.lerInteiro() != 1) return;
        }

        e.recursosUsados.put(r.id, qtd);
        System.out.println("Recurso alocado.");
    }

    private void eliminar() {
        mostrarListaEventos();
        System.out.print("ID do Evento a eliminar: ");
        Evento e = service.findEventoById(service.lerInteiro());
        if (e != null) {
            service.eventos.remove(e);
            System.out.println("Evento removido.");
        } else {
            System.out.println("Evento não encontrado.");
        }
    }

    // método auxiliar para listar eventos (usado em atualizar e eliminar)
    private void mostrarListaEventos() {
        if (service.eventos == null || service.eventos.isEmpty()) {
            System.out.println("Não existem eventos registados.");
            return;
        }
        System.out.println("\n-- Lista de Eventos --");
        for (Evento ev : service.eventos) {
            System.out.printf("ID: %d | Nome: %s | Estado: %s\n", ev.id, ev.nome, service.getEstadoDesc(ev.estado));
            System.out.printf("   Local: %s | Início: %s | Fim: %s\n", ev.local, formatarData(ev.dataInicio), formatarData(ev.dataFim));
            
            // Listar sessões se existirem
            if(!ev.sessoes.isEmpty()) {
                System.out.println("   Sessões:");
                for(Sessao s : ev.sessoes) {
                    System.out.printf("     - %s (%s)\n", s.descricao, service.getEstadoDesc(s.estado));
                    System.out.printf("       Início: %s | Fim Previsto: %s\n", 
                        formatarData(s.inicio), formatarData(s.fimPrevisto));
                }
            }
        }
        System.out.println("----------------------");
    }

    private void relatorio() {
        System.out.println("\n========== RELATÓRIO DE EVENTOS ==========\n");
        
        // 1. Total de eventos ativos
        relatorioEventosAtivos();
        
        // 2. Eventos por estado
        relatorioEventosPorEstado();
        
        // 3. Análise de desvios de cronograma
        relatorioDesviosCronograma();
        
        // 4. Número total de sessões por evento
        relatorioSessoesPorEvento();
        
        // 5. Tempo médio para concluir sessões
        relatorioTempoMedioSessoes();
        
        // 6. Identificação de atrasos
        relatorioAtrasosSessionoes();
        
        // 7. Participantes por evento
        relatorioParticipantesPorEvento();
        
        // 8. Custo dos eventos
        relatorioCustoEventos();
        
        System.out.println("\n==========================================\n");
    }
    
    private void relatorioEventosAtivos() {
        System.out.println("1. EVENTOS ATIVOS");
        System.out.println("----------------------------------------");
        
        int totalEventos = service.eventos.size();
        int eventosAtivos = 0;
        
        for(Evento e : service.eventos) {
            if(e.estado == 2) { // Em Progresso
                eventosAtivos++;
            }
        }
        
        System.out.printf("  Total de eventos: %d\n", totalEventos);
        System.out.printf("  Eventos ativos (Em Progresso): %d\n\n", eventosAtivos);
    }
    
    private void relatorioEventosPorEstado() {
        System.out.println("2. EVENTOS POR ESTADO");
        System.out.println("----------------------------------------");
        
        int planeado = 0, emProgresso = 0, concluido = 0;
        
        for(Evento e : service.eventos) {
            switch(e.estado) {
                case 1:
                    planeado++;
                    break;
                case 2:
                    emProgresso++;
                    break;
                case 3:
                    concluido++;
                    break;
            }
        }
        
        int total = service.eventos.size();
        if(total == 0) {
            System.out.println("  Nenhum evento registado.\n");
            return;
        }
        
        System.out.printf("  Planeado: %d (%.1f%%)\n", planeado, (planeado * 100.0 / total));
        System.out.printf("  Em Progresso: %d (%.1f%%)\n", emProgresso, (emProgresso * 100.0 / total));
        System.out.printf("  Concluído: %d (%.1f%%)\n\n", concluido, (concluido * 100.0 / total));
    }
    
    private void relatorioDesviosCronograma() {
        System.out.println("3. ANÁLISE DE DESVIOS DE CRONOGRAMA");
        System.out.println("----------------------------------------");
        
        java.time.LocalDateTime agora = java.time.LocalDateTime.now();
        boolean temAtrasos = false;
        
        for(Evento e : service.eventos) {
            if(e.estado != 3 && e.dataFim.isBefore(agora)) { // Não concluído e data fim passou
                System.out.printf("  [ATRASO] %s\n", e.nome);
                System.out.printf("    - Fim previsto: %s\n", formatarData(e.dataFim));
                System.out.printf("    - Dias de atraso: %d\n", java.time.temporal.ChronoUnit.DAYS.between(e.dataFim, agora));
                temAtrasos = true;
            }
        }
        
        if(!temAtrasos) {
            System.out.println("  Todos os eventos estão dentro do cronograma.\n");
        } else {
            System.out.println();
        }
    }
    
    private void relatorioSessoesPorEvento() {
        System.out.println("4. NÚMERO TOTAL DE SESSÕES POR EVENTO");
        System.out.println("----------------------------------------");
        
        int totalSessoes = 0;
        
        if(service.eventos.isEmpty()) {
            System.out.println("  Nenhum evento registado.\n");
            return;
        }
        
        for(Evento e : service.eventos) {
            System.out.printf("  %s: %d sessões\n", e.nome, e.sessoes.size());
            totalSessoes += e.sessoes.size();
        }
        
        System.out.printf("  Total de sessões (todos os eventos): %d\n\n", totalSessoes);
    }
    
    private void relatorioTempoMedioSessoes() {
        System.out.println("5. TEMPO MÉDIO PARA CONCLUIR SESSÕES");
        System.out.println("----------------------------------------");
        
        long tempoTotalMinutos = 0;
        int sessoesConcluidas = 0;
        
        for(Evento e : service.eventos) {
            for(Sessao s : e.sessoes) {
                if(s.estado == 3 && s.fimPrevisto != null && s.inicio != null) { // Concluída
                    long minutos = java.time.temporal.ChronoUnit.MINUTES.between(s.inicio, s.fimPrevisto);
                    tempoTotalMinutos += minutos;
                    sessoesConcluidas++;
                }
            }
        }
        
        if(sessoesConcluidas == 0) {
            System.out.println("  Nenhuma sessão concluída ainda.\n");
        } else {
            long mediaMinutos = tempoTotalMinutos / sessoesConcluidas;
            long horas = mediaMinutos / 60;
            long minutos = mediaMinutos % 60;
            
            System.out.printf("  Sessões concluídas: %d\n", sessoesConcluidas);
            System.out.printf("  Tempo médio de duração: %d horas e %d minutos\n\n", horas, minutos);
        }
    }
    
    private void relatorioAtrasosSessionoes() {
        System.out.println("6. IDENTIFICAÇÃO DE ATRASOS EM SESSÕES");
        System.out.println("----------------------------------------");
        
        boolean temAtrasosSessionoes = false;
        
        for(Evento e : service.eventos) {
            for(Sessao s : e.sessoes) {
                if(s.estado != 3 && s.fimPrevisto != null) { // Não concluída
                    java.time.LocalDateTime agora = java.time.LocalDateTime.now();
                    if(s.fimPrevisto.isBefore(agora)) {
                        System.out.printf("  [ATRASO] Sessão: %s (Evento: %s)\n", s.descricao, e.nome);
                        System.out.printf("    - Fim previsto: %s\n", formatarData(s.fimPrevisto));
                        System.out.printf("    - Dias de atraso: %d\n", java.time.temporal.ChronoUnit.DAYS.between(s.fimPrevisto, agora));
                        temAtrasosSessionoes = true;
                    }
                }
            }
        }
        
        if(!temAtrasosSessionoes) {
            System.out.println("  Nenhuma sessão atrasada.\n");
        } else {
            System.out.println();
        }
    }
    
    private void relatorioParticipantesPorEvento() {
        System.out.println("7. PARTICIPANTES POR EVENTO");
        System.out.println("----------------------------------------");
        
        if(service.eventos.isEmpty()) {
            System.out.println("  Nenhum evento registado.\n");
            return;
        }
        
        int totalParticipantesUnicos = 0;
        java.util.Set<Integer> todosPalestantes = new java.util.HashSet<>();
        java.util.Set<Integer> todosParticipantes = new java.util.HashSet<>();
        java.util.Set<Integer> todosModerador = new java.util.HashSet<>();
        
        for(Evento e : service.eventos) {
            System.out.printf("  %s: %d participantes\n", e.nome, e.inscritosComTipos.size());
            totalParticipantesUnicos += e.inscritosComTipos.size();
            
            // Agrupar por tipo
            System.out.println("    Distribuição por tipo:");
            for(Map.Entry<Integer, Integer> entrada : e.inscritosComTipos.entrySet()) {
                int tipo = entrada.getValue();
                Participante p = service.findParticipanteById(entrada.getKey());
                
                if(p != null) {
                    String tipoNome = getTipoParticipanteEmExtenso(tipo);
                    System.out.printf("      - %s (%s)\n", p.nome, tipoNome);
                    
                    // Contabilizar para estatísticas globais
                    switch(tipo) {
                        case 1:
                            todosPalestantes.add(entrada.getKey());
                            break;
                        case 2:
                            todosParticipantes.add(entrada.getKey());
                            break;
                        case 3:
                            todosModerador.add(entrada.getKey());
                            break;
                    }
                }
            }
            System.out.println();
        }
        
        System.out.printf("  Total de inscrições em eventos: %d\n", totalParticipantesUnicos);
        System.out.printf("  Participantes únicos por tipo:\n");
        System.out.printf("    - Palestrantes: %d\n", todosPalestantes.size());
        System.out.printf("    - Participantes: %d\n", todosParticipantes.size());
        System.out.printf("    - Moderadores: %d\n\n", todosModerador.size());
    }
    
    private String getTipoParticipanteEmExtenso(int tipo) {
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
    
    private void relatorioCustoEventos() {
        System.out.println("8. CUSTO DOS EVENTOS");
        System.out.println("----------------------------------------");
        
        double custoTotalGlobal = 0;
        
        if(service.eventos.isEmpty()) {
            System.out.println("  Nenhum evento registado.\n");
            return;
        }
        
        for(Evento e : service.eventos) {
            double custoEvento = 0;
            for(Map.Entry<Integer, Integer> entry : e.recursosUsados.entrySet()) {
                Recurso r = service.findRecursoById(entry.getKey());
                if(r != null) custoEvento += (r.custoUnitario * entry.getValue());
            }
            custoTotalGlobal += custoEvento;
            System.out.printf("  %s: %.2f€\n", e.nome, custoEvento);
        }
        
        System.out.printf("  ---\n");
        System.out.printf("  Custo total global: %.2f€\n\n", custoTotalGlobal);
    }
    
    private String formatarData(java.time.LocalDateTime data) {
        if(data == null) return "N/A";
        return String.format("%02d/%02d/%d %02d:%02d", 
            data.getDayOfMonth(), data.getMonthValue(), data.getYear(),
            data.getHour(), data.getMinute());
    }
}