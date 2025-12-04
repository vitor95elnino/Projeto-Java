package com.mycompany.projetolpd;

import java.time.LocalDateTime;

public class Sessao {
    public int id;
    public int idEvento;
    public int idModerador;
    public String descricao;
    public LocalDateTime inicio;
    public LocalDateTime fimPrevisto;
    public int estado; // 1-Planeado, 2-Em Progresso, 3-Concluído
    
}
