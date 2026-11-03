package com.faculdade.sensor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EstrategiasTest {

    @Test
    void limiteFixoDeveClassificarPelaUltimaLeitura() {
        EstrategiaAvaliacao e = new LimiteFixo(80, 90);

        assertEquals(Nivel.NORMAL, e.avaliar(List.of(79.9)));
        assertEquals(Nivel.ALERTA, e.avaliar(List.of(80.0)));
        assertEquals(Nivel.ALERTA, e.avaliar(List.of(89.9)));
        assertEquals(Nivel.CRITICO, e.avaliar(List.of(90.0)));
    }

    @Test
    void limiteFixoDeveIgnorarLeiturasAnteriores() {
        EstrategiaAvaliacao e = new LimiteFixo(80, 90);

        assertEquals(Nivel.NORMAL, e.avaliar(List.of(120.0, 130.0, 50.0)));
    }

    @Test
    void limiteFixoDeveRejeitarLimitesInvertidosOuIguais() {
        assertThrows(IllegalArgumentException.class, () -> new LimiteFixo(90, 80));
        assertThrows(IllegalArgumentException.class, () -> new LimiteFixo(80, 80));
        assertThrows(IllegalArgumentException.class, () -> new LimiteFixo(Double.NaN, 80));
    }

    @Test
    void faixaDeveSerNormalNoMeioAlertaPertoDasBordasECriticaFora() {
        EstrategiaAvaliacao e = new FaixaOperacao(60, 100, 10);

        assertEquals(Nivel.NORMAL, e.avaliar(List.of(80.0)));
        assertEquals(Nivel.NORMAL, e.avaliar(List.of(70.0)));
        assertEquals(Nivel.ALERTA, e.avaliar(List.of(69.9)));
        assertEquals(Nivel.ALERTA, e.avaliar(List.of(90.1)));
        assertEquals(Nivel.ALERTA, e.avaliar(List.of(60.0)));
        assertEquals(Nivel.ALERTA, e.avaliar(List.of(100.0)));
        assertEquals(Nivel.CRITICO, e.avaliar(List.of(59.9)));
        assertEquals(Nivel.CRITICO, e.avaliar(List.of(100.1)));
    }

    @Test
    void faixaDeveRejeitarParametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new FaixaOperacao(100, 60, 10));
        assertThrows(IllegalArgumentException.class, () -> new FaixaOperacao(60, 100, -1));
        assertThrows(IllegalArgumentException.class, () -> new FaixaOperacao(60, 100, 20)); // margens se encontram
    }

    @Test
    void taxaDeveAvaliarSomenteAMudancaEntreAsDuasUltimasLeituras() {
        EstrategiaAvaliacao e = new TaxaVariacao(10, 20);

        assertEquals(Nivel.NORMAL, e.avaliar(List.of(70.0, 79.9)));
        assertEquals(Nivel.ALERTA, e.avaliar(List.of(70.0, 80.0)));
        assertEquals(Nivel.CRITICO, e.avaliar(List.of(70.0, 90.0)));
    }

    @Test
    void taxaDeveUsarOMóduloDaVariacao() {
        EstrategiaAvaliacao e = new TaxaVariacao(10, 20);

        assertEquals(Nivel.CRITICO, e.avaliar(List.of(90.0, 70.0)));
    }

    @Test
    void taxaNaoDeveAlarmarComValorAltoMasEstavel() {
        EstrategiaAvaliacao e = new TaxaVariacao(10, 20);

        assertEquals(Nivel.NORMAL, e.avaliar(List.of(500.0, 501.0)));
    }

    @Test
    void taxaComUmaLeituraSoDeveSerNormal() {
        assertEquals(Nivel.NORMAL, new TaxaVariacao(10, 20).avaliar(List.of(999.0)));
    }

    @Test
    void taxaDeveRejeitarParametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new TaxaVariacao(0, 20));
        assertThrows(IllegalArgumentException.class, () -> new TaxaVariacao(-5, 20));
        assertThrows(IllegalArgumentException.class, () -> new TaxaVariacao(20, 10));
    }

    @Test
    void mediaDeveUsarSomenteAsUltimasLeiturasDaJanela() {
        EstrategiaAvaliacao e = new MediaMovel(3, 80, 90);

        assertEquals(Nivel.CRITICO, e.avaliar(List.of(0.0, 100.0, 100.0, 100.0)));
        assertEquals(Nivel.ALERTA, e.avaliar(List.of(500.0, 70.0, 80.0, 90.0)));
    }

    @Test
    void mediaComMenosLeiturasQueAJanelaDeveUsarTodas() {
        EstrategiaAvaliacao e = new MediaMovel(5, 80, 90);

        assertEquals(Nivel.CRITICO, e.avaliar(List.of(95.0)));
        assertEquals(Nivel.NORMAL, e.avaliar(List.of(70.0, 80.0)));
    }

    @Test
    void mediaDeveAbsorverPicoIsoladoQueOLimiteFixoDetectaComoCritico() {
        List<Double> comPico = List.of(70.0, 71.0, 95.0);

        assertEquals(Nivel.CRITICO, new LimiteFixo(80, 90).avaliar(comPico));
        assertEquals(Nivel.NORMAL, new MediaMovel(3, 80, 90).avaliar(comPico));
    }

    @Test
    void mediaDeveRejeitarParametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new MediaMovel(0, 80, 90));
        assertThrows(IllegalArgumentException.class, () -> new MediaMovel(3, 90, 80));
    }

    @Test
    void estrategiasDiferentesDevemDarVeredictosDiferentesParaOMesmoHistorico() {
        List<Double> historico = List.of(70.0, 71.0, 95.0);

        assertEquals(Nivel.CRITICO, new LimiteFixo(80, 90).avaliar(historico));
        assertEquals(Nivel.ALERTA, new FaixaOperacao(60, 100, 10).avaliar(historico));
        assertEquals(Nivel.CRITICO, new TaxaVariacao(10, 20).avaliar(historico));
        assertEquals(Nivel.NORMAL, new MediaMovel(3, 80, 90).avaliar(historico));
    }
}
