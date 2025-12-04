package com.mycompany.projetolpd;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ServicoDados {
    
    // --- Listas de Dados ---
    public List<Evento> eventos = new ArrayList<>();
    public List<Participante> participantes = new ArrayList<>();
    public List<Recurso> recursos = new ArrayList<>();

    // --- Utilitários ---
    public final Scanner scanner = new Scanner(System.in);
    public final DateTimeFormatter dataFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    // Mantemos .txt para não perder os dados antigos!
    private final String NOME_FICHEIRO = "dados_gestor_eventos.txt";

    // IDs (Auto-incremento)
    public int nextEventoId = 1;
    public int nextSessaoId = 1;
    public int nextParticipanteId = 1;
    public int nextRecursoId = 1;

    // =========================================================================
    //  PERSISTÊNCIA (TXT) - VERSÃO SIMPLIFICADA E ORGANIZADA
    // =========================================================================

    public void guardarDados() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(NOME_FICHEIRO))) {
            
            // Agora o código está dividido em blocos lógicos fáceis de ler
            guardarParticipantes(writer);
            guardarRecursos(writer);
            guardarEventos(writer);
            
            System.out.println("Dados guardados em TXT com sucesso!");
            
        } catch (IOException e) {
            System.out.println("Erro ao guardar dados: " + e.getMessage());
        }
    }

    private void guardarParticipantes(BufferedWriter writer) throws IOException {
        for (Participante p : participantes) {
            String data = p.dataInscricao != null ? p.dataInscricao.format(dataFormat) : "";
            // Formato: P;id;nome;tipo;contacto;data
            writer.write(String.format("P;%d;%s;%d;%s;%s", p.id, p.nome, p.tipo, p.contacto, data));
            writer.newLine();
        }
    }

    private void guardarRecursos(BufferedWriter writer) throws IOException {
        for (Recurso r : recursos) {
            // Formato: R;id;nome;categoria;quantidade;custo
            writer.write(String.format("R;%d;%s;%s;%d;%s", r.id, r.nome, r.categoria, r.quantidade, r.custoUnitario));
            writer.newLine();
        }
    }

    private void guardarEventos(BufferedWriter writer) throws IOException {
        for (Evento e : eventos) {
            String inicio = e.dataInicio != null ? e.dataInicio.format(dataFormat) : "";
            String fim = e.dataFim != null ? e.dataFim.format(dataFormat) : "";
            
            // 1. Guardar o Evento Principal
            writer.write(String.format("E;%d;%s;%s;%s;%s;%d", e.id, e.nome, e.local, inicio, fim, e.estado));
            writer.newLine();
            
            // 2. Guardar as Sessões deste evento
            for (Sessao s : e.sessoes) {
                String sIni = s.inicio != null ? s.inicio.format(dataFormat) : "";
                String sFim = s.fimPrevisto != null ? s.fimPrevisto.format(dataFormat) : "";
                writer.write(String.format("S;%d;%d;%s;%s;%s;%d;%d", e.id, s.id, s.descricao, sIni, sFim, s.estado, s.idModerador));
                writer.newLine();
            }

            // 3. Guardar Recursos usados (ER)
            for(Integer rId : e.recursosUsados.keySet()) {
                int qtd = e.recursosUsados.get(rId);
                writer.write(String.format("ER;%d;%d;%d", e.id, rId, qtd));
                writer.newLine();
            }

            // 4. Guardar Inscritos (I)
            for(Integer pId : e.inscritosComTipos.keySet()) {
                int tipo = e.inscritosComTipos.get(pId);
                writer.write(String.format("I;%d;%d;%d", e.id, pId, tipo));
                writer.newLine();
            }
        }
    }

    public void carregarDados() {
        File file = new File(NOME_FICHEIRO);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linha;
            participantes.clear(); recursos.clear(); eventos.clear();

            while ((linha = reader.readLine()) != null) {
                String[] p = linha.split(";");
                if (p.length == 0) continue;

                // Switch simples para decidir que tipo de linha estamos a ler
                switch (p[0]) {
                    case "P": lerParticipante(p); break;
                    case "R": lerRecurso(p); break;
                    case "E": lerEvento(p); break;
                    case "S": lerSessao(p); break;
                    case "ER": lerEventoRecurso(p); break;
                    case "I": lerEventoInscricao(p); break;
                }
            }
            atualizarIds();
            
        } catch (Exception e) {
            System.out.println("Erro ao carregar dados: " + e.getMessage());
        }
    }

    // --- Métodos Auxiliares de Leitura (Para limpar o código principal) ---

    private void lerParticipante(String[] dados) {
        Participante p = new Participante();
        p.id = Integer.parseInt(dados[1]);
        p.nome = dados[2];
        p.tipo = Integer.parseInt(dados[3]);
        p.contacto = dados.length > 4 ? dados[4] : "";
        if (dados.length > 5 && !dados[5].isEmpty()) p.dataInscricao = LocalDateTime.parse(dados[5], dataFormat);
        participantes.add(p);
    }

    private void lerRecurso(String[] dados) {
        Recurso r = new Recurso();
        r.id = Integer.parseInt(dados[1]);
        r.nome = dados[2];
        r.categoria = dados[3];
        r.quantidade = Integer.parseInt(dados[4]);
        r.custoUnitario = Double.parseDouble(dados[5]);
        recursos.add(r);
    }

    private void lerEvento(String[] dados) {
        Evento e = new Evento();
        e.id = Integer.parseInt(dados[1]);
        e.nome = dados[2];
        e.local = dados[3];
        if (!dados[4].isEmpty()) e.dataInicio = LocalDateTime.parse(dados[4], dataFormat);
        if (!dados[5].isEmpty()) e.dataFim = LocalDateTime.parse(dados[5], dataFormat);
        e.estado = Integer.parseInt(dados[6]);
        eventos.add(e);
    }

    private void lerSessao(String[] dados) {
        Sessao s = new Sessao();
        int eventoId = Integer.parseInt(dados[1]);
        s.id = Integer.parseInt(dados[2]);
        s.descricao = dados[3];
        if (!dados[4].isEmpty()) s.inicio = LocalDateTime.parse(dados[4], dataFormat);
        if (!dados[5].isEmpty()) s.fimPrevisto = LocalDateTime.parse(dados[5], dataFormat);
        s.estado = Integer.parseInt(dados[6]);
        s.idModerador = dados.length > 7 ? Integer.parseInt(dados[7]) : 0;
        s.idEvento = eventoId;
        
        Evento ev = findEventoById(eventoId);
        if (ev != null) ev.sessoes.add(s);
    }

    private void lerEventoRecurso(String[] dados) {
        Evento ev = findEventoById(Integer.parseInt(dados[1]));
        if (ev != null) ev.recursosUsados.put(Integer.parseInt(dados[2]), Integer.parseInt(dados[3]));
    }

    private void lerEventoInscricao(String[] dados) {
        Evento ev = findEventoById(Integer.parseInt(dados[1]));
        if (ev != null) ev.inscritosComTipos.put(Integer.parseInt(dados[2]), Integer.parseInt(dados[3]));
    }

    private void atualizarIds() {
        if(!eventos.isEmpty()) nextEventoId = eventos.stream().mapToInt(e -> e.id).max().orElse(0) + 1;
        if(!participantes.isEmpty()) nextParticipanteId = participantes.stream().mapToInt(p -> p.id).max().orElse(0) + 1;
        if(!recursos.isEmpty()) nextRecursoId = recursos.stream().mapToInt(r -> r.id).max().orElse(0) + 1;
        
        int maxSessao = 0;
        for(Evento e : eventos) {
            int m = e.sessoes.stream().mapToInt(s -> s.id).max().orElse(0);
            if(m > maxSessao) maxSessao = m;
        }
        nextSessaoId = maxSessao + 1;
    }

    // =========================================================================
    //  INPUTS E UTILITÁRIOS (MANTIDOS IGUAIS)
    // =========================================================================

    public int lerInteiro() {
        try {
            String line = scanner.nextLine();
            return Integer.parseInt(line.trim());
        } catch (NumberFormatException e) { return -1; }
    }

    public String lerTextoObrigatorio(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine();
            if (s != null && !s.trim().isEmpty()) return s.trim();
            System.out.println("Valor obrigatório.");
        }
    }

    public String lerTextoOpcional(String prompt, String atual) {
        System.out.print(prompt + " (Atual: " + (atual == null ? "" : atual) + ", Enter mantem): ");
        String s = scanner.nextLine();
        return s.isEmpty() ? atual : s.trim();
    }
    
    public LocalDateTime lerDataValida(String msg) {
        while (true) {
            System.out.print(msg);
            try { return LocalDateTime.parse(scanner.nextLine(), dataFormat); }
            catch (Exception e) { System.out.println("Formato inválido (dd/MM/yyyy HH:mm)."); }
        }
    }

    public LocalDateTime lerDataOpcional(String msg, LocalDateTime atual) {
        String atualStr = atual != null ? atual.format(dataFormat) : "N/D";
        while (true) {
            System.out.print(msg + " (Atual: " + atualStr + ", Enter mantem): ");
            String linha = scanner.nextLine();
            if (linha.isEmpty()) return atual;
            try { return LocalDateTime.parse(linha, dataFormat); }
            catch (Exception e) { System.out.println("Formato inválido."); }
        }
    }

    public double lerDoubleValida(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Double.parseDouble(scanner.nextLine().replace(',', '.')); }
            catch (NumberFormatException e) { System.out.println("Número inválido."); }
        }
    }

    // --- Métodos de Busca ---
    public Evento findEventoById(int id) {
        return eventos.stream().filter(e -> e.id == id).findFirst().orElse(null);
    }
    public Participante findParticipanteById(int id) {
        return participantes.stream().filter(p -> p.id == id).findFirst().orElse(null);
    }
    public Recurso findRecursoById(int id) {
        return recursos.stream().filter(r -> r.id == id).findFirst().orElse(null);
    }

    public String getEstadoDesc(int estado) {
        switch (estado) {
            case 1: return "Planeado";
            case 2: return "Em Progresso";
            case 3: return "Concluído";
            default: return "Desconhecido";
        }
    }
}