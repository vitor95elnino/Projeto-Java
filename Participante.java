package com.mycompany.projetolpd;

import java.time.LocalDateTime;


public class Participante {
    public int id;
    public String nome;
    public String contacto;
    public int tipo; // 1-Palestrante, 2-Participante, 3-Moderador
    public LocalDateTime dataInscricao;
    
}
