package com.faculdade.sensor;

import java.util.List;

public class FaixaOperacao implements EstrategiaAvaliacao {

    private final double minimo;
    private final double maximo;
    private final double margem;

    public FaixaOperacao(double minimo, double maximo, double margem) {
        if (!(minimo < maximo)) {
            throw new IllegalArgumentException("o mínimo deve ser menor que o máximo");
        }
        if (margem < 0 || margem * 2 >= maximo - minimo) {
            throw new IllegalArgumentException("margem inválida para a faixa informada");
        }
        this.minimo = minimo;
        this.maximo = maximo;
        this.margem = margem;
    }

    @Override
    public Nivel avaliar(List<Double> leituras) {
        double atual = leituras.get(leituras.size() - 1);
        if (atual < minimo || atual > maximo) return Nivel.CRITICO;
        if (atual < minimo + margem || atual > maximo - margem) return Nivel.ALERTA;
        return Nivel.NORMAL;
    }
}
