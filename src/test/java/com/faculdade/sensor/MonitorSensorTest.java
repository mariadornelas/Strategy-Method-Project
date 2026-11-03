package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** O contexto: delega à estratégia, guarda histórico e aceita troca em tempo de execução. */
class MonitorSensorTest {

    @Test
    void deveDelegarAAvaliacaoParaAEstrategia() {
        List<List<Double>> recebidos = new ArrayList<>();
        EstrategiaAvaliacao espia = l -> {
            recebidos.add(new ArrayList<>(l));
            return Nivel.ALERTA;
        };
        MonitorSensor monitor = new MonitorSensor("TEMPERATURA", espia);

        Nivel nivel = monitor.registrar(70.0);
        monitor.registrar(75.0);

        assertEquals(Nivel.ALERTA, nivel);   // o contexto devolve exatamente o que a estratégia decidiu
        assertEquals(List.of(List.of(70.0), List.of(70.0, 75.0)), recebidos);
    }

    @Test
    void deveRegistrarOHistoricoNaOrdemDeChegada() {
        MonitorSensor monitor = new MonitorSensor("TEMPERATURA", new LimiteFixo(80, 90));

        monitor.registrar(70.0);
        monitor.registrar(85.0);

        assertEquals(List.of(70.0, 85.0), monitor.getLeituras());
    }

    @Test
    void trocarEstrategiaDeveMudarOVeredictoSemPerderOHistorico() {
        MonitorSensor monitor = new MonitorSensor("TEMPERATURA", new LimiteFixo(80, 90));
        monitor.registrar(70.0);
        monitor.registrar(71.0);
        assertEquals(Nivel.CRITICO, monitor.registrar(95.0));

        monitor.trocarEstrategia(new MediaMovel(3, 80, 90));

        assertEquals(Nivel.NORMAL, monitor.avaliarAgora());
        assertEquals(List.of(70.0, 71.0, 95.0), monitor.getLeituras());
    }

    @Test
    void trocarEstrategiaDeveValerParaAsProximasLeituras() {
        MonitorSensor monitor = new MonitorSensor("TEMPERATURA", new LimiteFixo(80, 90));
        monitor.trocarEstrategia(new TaxaVariacao(10, 20));

        monitor.registrar(70.0);
        assertEquals(Nivel.CRITICO, monitor.registrar(95.0));   // salto de 25, e o valor 95 sozinho seria só CRITICO por limite
        assertEquals(Nivel.NORMAL, monitor.registrar(96.0));    // estável, mesmo alto: só a taxa importa agora
    }

    @Test
    void getEstrategiaDeveRefletirATrocaFeita() {
        EstrategiaAvaliacao a = new LimiteFixo(80, 90);
        EstrategiaAvaliacao b = new MediaMovel(3, 80, 90);
        MonitorSensor monitor = new MonitorSensor("TEMPERATURA", a);
        assertSame(a, monitor.getEstrategia());

        monitor.trocarEstrategia(b);

        assertSame(b, monitor.getEstrategia());
    }

    @Test
    void doisMonitoresDevemTerEstrategiasEHistoricosIndependentes() {
        MonitorSensor m1 = new MonitorSensor("TEMPERATURA", new LimiteFixo(80, 90));
        MonitorSensor m2 = new MonitorSensor("PRESSAO", new TaxaVariacao(1, 2));

        m1.registrar(95.0);
        m2.registrar(10.0);

        assertEquals(List.of(95.0), m1.getLeituras());
        assertEquals(List.of(10.0), m2.getLeituras());
        assertEquals(Nivel.CRITICO, m1.avaliarAgora());
        assertEquals(Nivel.NORMAL, m2.avaliarAgora());
    }

    @Test
    void estrategiaPodeSerUmaLambda() {
        MonitorSensor monitor = new MonitorSensor("TEMPERATURA", l -> Nivel.CRITICO);

        assertEquals(Nivel.CRITICO, monitor.registrar(1.0));
    }

    @Test
    void estrategiaNaoDevePoderAlterarOHistoricoDoContexto() {
        EstrategiaAvaliacao maliciosa = l -> {
            l.clear();   // tentativa de apagar o histórico
            return Nivel.NORMAL;
        };
        MonitorSensor monitor = new MonitorSensor("TEMPERATURA", maliciosa);

        assertThrows(UnsupportedOperationException.class, () -> monitor.registrar(70.0));
    }

    @Test
    void historicoExpostoDeveSerSomenteLeitura() {
        MonitorSensor monitor = new MonitorSensor("TEMPERATURA", new LimiteFixo(80, 90));
        monitor.registrar(70.0);

        assertThrows(UnsupportedOperationException.class, () -> monitor.getLeituras().add(1.0));
    }

    @Test
    void avaliarSemLeiturasDeveFalharComMensagemClara() {
        MonitorSensor monitor = new MonitorSensor("TEMPERATURA", new LimiteFixo(80, 90));

        assertThrows(IllegalStateException.class, monitor::avaliarAgora);
    }

    @Test
    void deveRejeitarLeiturasInvalidasSemContaminarOHistorico() {
        MonitorSensor monitor = new MonitorSensor("TEMPERATURA", new LimiteFixo(80, 90));

        assertThrows(IllegalArgumentException.class, () -> monitor.registrar(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> monitor.registrar(Double.POSITIVE_INFINITY));
        assertTrue(monitor.getLeituras().isEmpty());
    }

    @Test
    void deveRejeitarArgumentosNulos() {
        assertThrows(NullPointerException.class, () -> new MonitorSensor(null, new LimiteFixo(80, 90)));
        assertThrows(NullPointerException.class, () -> new MonitorSensor("TEMPERATURA", null));
        MonitorSensor monitor = new MonitorSensor("TEMPERATURA", new LimiteFixo(80, 90));
        assertThrows(NullPointerException.class, () -> monitor.trocarEstrategia(null));
    }

    @Test
    void trocaComArgumentoNuloNaoDeveAlterarAEstrategiaAtual() {
        EstrategiaAvaliacao original = new LimiteFixo(80, 90);
        MonitorSensor monitor = new MonitorSensor("TEMPERATURA", original);

        assertThrows(NullPointerException.class, () -> monitor.trocarEstrategia(null));

        assertSame(original, monitor.getEstrategia());
    }
}
