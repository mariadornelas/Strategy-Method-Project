package com.faculdade.sensor;

import java.util.List;

public class App {

    public static void main(String[] args) {
        List<Double> leituras = List.of(70.0, 71.0, 95.0, 72.0, 73.0);

        System.out.println("Leituras: " + leituras);
        System.out.println();

        comparar("Limite fixo (alerta 80, crítico 90)", new LimiteFixo(80, 90), leituras);
        comparar("Faixa 60-100, margem 10", new FaixaOperacao(60, 100, 10), leituras);
        comparar("Taxa de variação (alerta 10, crítica 20)", new TaxaVariacao(10, 20), leituras);
        comparar("Média móvel de 3 (alerta 80, crítico 90)", new MediaMovel(3, 80, 90), leituras);

        System.out.println();
        System.out.println("=== Troca de estratégia em tempo de execução, no mesmo monitor ===");
        MonitorSensor monitor = new MonitorSensor("TEMPERATURA", new LimiteFixo(80, 90));
        leituras.forEach(monitor::registrar);
        System.out.println("Histórico guardado: " + monitor.getLeituras());
        System.out.println("Com limite fixo, a última avaliação:  " + monitor.avaliarAgora());

        monitor.trocarEstrategia(new TaxaVariacao(10, 20));
        System.out.println("Com taxa de variação, reavaliando:    " + monitor.avaliarAgora());

        System.out.println();
        System.out.println("=== Estratégia como lambda (interface funcional) ===");
        monitor.trocarEstrategia(l -> l.get(l.size() - 1) > 72 ? Nivel.ALERTA : Nivel.NORMAL);
        System.out.println("Critério improvisado, reavaliando:    " + monitor.avaliarAgora());
    }

    private static void comparar(String titulo, EstrategiaAvaliacao estrategia, List<Double> leituras) {
        MonitorSensor monitor = new MonitorSensor("TEMPERATURA", estrategia);
        StringBuilder linha = new StringBuilder();
        for (double valor : leituras) {
            linha.append(String.format("%-8s", monitor.registrar(valor)));
        }
        System.out.println(titulo);
        System.out.println("  " + linha.toString().stripTrailing());
    }
}
