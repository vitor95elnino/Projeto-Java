package com.mycompany.projetolpd;


public class GestorRecursos {
    
    private ServicoDados service;

    public GestorRecursos(ServicoDados service) {
        this.service = service;
    }

    public void menu() {
        while(true) {
            System.out.println("\n--- MENU RECURSOS ---");
            System.out.println("1. Criar Recurso");

            System.out.println("2. Ver Recurso");
            System.out.println("3. Atualizar Recurso");
            System.out.println("4. Eliminar Recurso");
            System.out.println("5. Relatorio Recurso");
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
        Recurso r = new Recurso();
        r.id = service.nextRecursoId++;
        r.nome = service.lerTextoObrigatorio("Nome: ");
        System.out.println("Categoria (1-Equipamento, 2-Espaço, 3-Suporte): ");
        int cat = service.lerInteiro();
        r.categoria = (cat==2 ? "Espaço" : (cat==3 ? "Suporte Técnico" : "Equipamento"));
        r.quantidade = Math.max(0, Integer.parseInt(service.lerTextoOpcional("Quantidade", "0")));
        r.custoUnitario = service.lerDoubleValida("Custo Unitário: ");
        service.recursos.add(r);
        System.out.println("Recurso criado.");
    }

    private void listar() {
        System.out.println("\n>> Stock Recursos");
        for(Recurso r : service.recursos) {
            System.out.printf("ID: %d | %s (%s) | Qtd: %d | Custo: %.2f\n", r.id, r.nome, r.categoria, r.quantidade, r.custoUnitario);
        }
    }

    private void atualizar() {
        listar();
        System.out.print("ID para atualizar: ");
        Recurso r = service.findRecursoById(service.lerInteiro());
        if(r == null) return;
        r.nome = service.lerTextoOpcional("Novo Nome", r.nome);
        try {
             String q = service.lerTextoOpcional("Nova Qtd", String.valueOf(r.quantidade));
             r.quantidade = Integer.parseInt(q);
        } catch(Exception e){}
        
        System.out.print("Nova Categoria (1-Equipamento, 2-Espaço, 3-Suporte Técnico, Enter mantém): ");
        String catStr = service.scanner.nextLine();
        if(!catStr.trim().isEmpty()) {
            try {
                int cat = Integer.parseInt(catStr);
                r.categoria = (cat==2 ? "Espaço" : (cat==3 ? "Suporte Técnico" : "Equipamento"));
            } catch(NumberFormatException e) {
                // Mantém a categoria atual
            }
        }
        
        System.out.println("Recurso atualizado.");
    }

    private void eliminar() {
        System.out.print("ID para eliminar: ");
        Recurso r = service.findRecursoById(service.lerInteiro());
        if(r != null) { service.recursos.remove(r); System.out.println("Removido."); }
    }

    private void relatorio() {
        System.out.println("\n========== RELATÓRIO DE RECURSOS ==========\n");
        
        // 1. Total de recursos disponíveis em stock
        relatorioTotalStock();
        
        // 2. Recursos por categoria
        relatorioRecursosPorCategoria();
        
        // 3. Análise de uso por evento
        relatorioUsoRecursosPorEvento();
        
        System.out.println("\n==========================================\n");
    }
    
    private void relatorioTotalStock() {
        System.out.println("1. TOTAL DE RECURSOS DISPONÍVEIS EM STOCK");
        System.out.println("----------------------------------------");
        
        int totalItens = 0;
        double valorTotalStock = 0.0;
        
        for(Recurso r : service.recursos) {
            totalItens += r.quantidade;
            valorTotalStock += r.quantidade * r.custoUnitario;
        }
        
        System.out.printf("  Total de itens em stock: %d\n", totalItens);
        System.out.printf("  Valor total do stock: %.2f€\n", valorTotalStock);
        System.out.printf("  Número de tipos de recursos: %d\n\n", service.recursos.size());
    }
    
    private void relatorioRecursosPorCategoria() {
        System.out.println("2. RECURSOS POR CATEGORIA");
        System.out.println("----------------------------------------");
        
        // Agrupar por categoria
        java.util.Map<String, Integer> categoriaStock = new java.util.HashMap<>();
        java.util.Map<String, Double> categoriaCusto = new java.util.HashMap<>();
        
        for(Recurso r : service.recursos) {
            categoriaStock.put(r.categoria, categoriaStock.getOrDefault(r.categoria, 0) + r.quantidade);
            categoriaCusto.put(r.categoria, categoriaCusto.getOrDefault(r.categoria, 0.0) + (r.quantidade * r.custoUnitario));
        }
        
        for(String categoria : categoriaStock.keySet()) {
            System.out.printf("  %s:\n", categoria);
            System.out.printf("    - Quantidade total: %d itens\n", categoriaStock.get(categoria));
            System.out.printf("    - Valor total: %.2f€\n", categoriaCusto.get(categoria));
            
            // Listar recursos desta categoria
            System.out.print("    - Recursos: ");
            service.recursos.stream()
                .filter(r -> r.categoria.equals(categoria))
                .forEach(r -> System.out.print(r.nome + " (" + r.quantidade + ") "));
            System.out.println("\n");
        }
    }
    
    private void relatorioUsoRecursosPorEvento() {
        System.out.println("3. ANÁLISE DE USO DE RECURSOS POR EVENTO");
        System.out.println("----------------------------------------");
        
        // Contabilizar uso total por recurso
        java.util.Map<Integer, Integer> usoTotalPorRecurso = new java.util.HashMap<>();
        java.util.Map<Integer, java.util.List<String>> eventosPorRecurso = new java.util.HashMap<>();
        
        for(Evento e : service.eventos) {
            for(java.util.Map.Entry<Integer, Integer> uso : e.recursosUsados.entrySet()) {
                int recursoId = uso.getKey();
                int quantidade = uso.getValue();
                
                usoTotalPorRecurso.put(recursoId, usoTotalPorRecurso.getOrDefault(recursoId, 0) + quantidade);
                
                if(!eventosPorRecurso.containsKey(recursoId)) {
                    eventosPorRecurso.put(recursoId, new java.util.ArrayList<>());
                }
                eventosPorRecurso.get(recursoId).add(e.nome);
            }
        }
        
        if(usoTotalPorRecurso.isEmpty()) {
            System.out.println("  Nenhum recurso foi utilizado em eventos.\n");
            return;
        }
        
        // Ordenar por uso (mais requisitados)
        usoTotalPorRecurso.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .forEach(entrada -> {
                int recursoId = entrada.getKey();
                int totalUsado = entrada.getValue();
                Recurso r = service.findRecursoById(recursoId);
                
                if(r != null) {
                    System.out.printf("  %s:\n", r.nome);
                    System.out.printf("    - Total requisitado em eventos: %d unidades\n", totalUsado);
                    System.out.printf("    - Disponível em stock: %d unidades\n", r.quantidade);
                    
                    // Verificar se precisa reposição
                    if(totalUsado > r.quantidade) {
                        System.out.printf("    - [!] ALERTA: NECESSÁRIO REPOSIÇÃO DE %d UNIDADES\n", totalUsado - r.quantidade);
                    } else if(r.quantidade - totalUsado <= 5) {
                        System.out.printf("    - [⚠] AVISO: Stock baixo (%d unidades disponíveis)\n", r.quantidade - totalUsado);
                    }
                    
                    // Eventos onde foi usado
                    System.out.print("    - Usado em eventos: ");
                    eventosPorRecurso.get(recursoId).forEach(e -> System.out.print(e + " | "));
                    System.out.println("\n");
                }
            });
    }
}
