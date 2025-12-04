package com.mycompany.projetolpd;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Evento {
    
    public int id;
    public String nome;
    public String local;
    public LocalDateTime dataInicio;
    public LocalDateTime dataFim;
    public int estado; // 1-Planeado, 2-Em Progresso, 3-Concluído
    
    // Lista de sessões dentro do evento
    public List<Sessao> sessoes = new ArrayList<>();
    
    // ID Participante -> Tipo (para saber quem é quem neste evento)
    public Map<Integer, Integer> inscritosComTipos = new HashMap<>(); 
    
    // ID Recurso -> Quantidade usada
    public Map<Integer, Integer> recursosUsados = new HashMap<>(); 

}
