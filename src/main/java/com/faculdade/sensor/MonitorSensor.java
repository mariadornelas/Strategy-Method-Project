package com.faculdade.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MonitorSensor {

    private final String nome;
    private final List<Double> leituras = new ArrayList<>();
    private EstrategiaAvaliacao estrategia;

    public MonitorSensor(String nome, EstrategiaAvaliacao estrategia) {
        this.nome = Objects.requireNonNull(nome, "nome não pode ser nulo");
        this.estrategia = Objects.requireNonNull(estrategia, "estratégia não pode ser nula");
    }

    public Nivel registrar(double valor) {
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            throw new IllegalArgumentException("leitura inválida: " + valor);
        }
        leituras.add(valor);
        return avaliarAgora();
    }

    public Nivel avaliarAgora() {
        if (leituras.isEmpty()) {
            throw new IllegalStateException("nenhuma leitura registrada");
        }
        return estrategia.avaliar(Collections.unmodifiableList(leituras));
    }

    public void trocarEstrategia(EstrategiaAvaliacao nova) {
        this.estrategia = Objects.requireNonNull(nova, "estratégia não pode ser nula");
    }

    public EstrategiaAvaliacao getEstrategia() {
        return estrategia;
    }

    public String getNome() {
        return nome;
    }

    public List<Double> getLeituras() {
        return Collections.unmodifiableList(leituras);
    }
}
