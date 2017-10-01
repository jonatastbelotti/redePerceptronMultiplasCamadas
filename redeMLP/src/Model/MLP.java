package Model;

import Controle.Comunicador;
import Recursos.Arquivo;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

/**
 *
 * @author Jônatas Trabuco Belotti [jonatas.t.belotti@hotmail.com]
 */
public class MLP {

  public static final int NUM_SINAIS_ENTRADA = 3;
  private final double TAXA_APRENDIZAGEM = 0.1;
  private final double PRECISAO = 0.000001;
  private final double BETA = 1.0;
  private final int NUM_NEU_CAMADA_ESCONDIDA = 10;
  private final int NUM_NEU_CAMADA_SAIDA = 1;

  private int numEpocas;
  private double[] entradas;
  private double[][] pesosCamadaEscondida;
  private double[] pesosCamadaSaida;
  private double[] potencialCamadaEscondida;
  private double[] saidaCamadaEscondida;
  private double saida;

  public MLP() {
    Random random;

    entradas = new double[NUM_SINAIS_ENTRADA + 1];
    pesosCamadaEscondida = new double[NUM_NEU_CAMADA_ESCONDIDA][NUM_SINAIS_ENTRADA + 1];
    pesosCamadaSaida = new double[NUM_NEU_CAMADA_ESCONDIDA + 1];
    saidaCamadaEscondida = new double[NUM_NEU_CAMADA_ESCONDIDA + 1];
    potencialCamadaEscondida = new double[NUM_NEU_CAMADA_ESCONDIDA + 1];

    //Iniciando os pesos sinpticos
    random = new Random();
    for (int i = 0; i < NUM_NEU_CAMADA_ESCONDIDA; i++) {
      for (int j = 0; j < NUM_SINAIS_ENTRADA + 1; j++) {
        pesosCamadaEscondida[i][j] = random.nextDouble();
      }

      pesosCamadaSaida[i] = random.nextDouble();
    }
    pesosCamadaSaida[NUM_NEU_CAMADA_ESCONDIDA] = random.nextDouble();
  }

  public boolean treinar(Arquivo arquivoTreinamento) {
    FileReader arq;
    BufferedReader lerArq;
    String linha;
    String[] vetor;
    int i;
    double saidaEsperada;
    double erroAtual;
    double erroAnterior;
    double valorParcial;
    double gradienteCamadaSaida;
    double gradienteCamadaEscondida;

    numEpocas = 0;
    erroAtual = erroQuadraticoMedio(arquivoTreinamento);

    Comunicador.iniciarLog("Início treinamento da MLP");
    Comunicador.addLog(String.format("Erro inicial: %.10f", erroAtual).replace(".", ","));
    Comunicador.addLog("Época Eqm");

    try {
      do {
        this.numEpocas++;
        erroAnterior = erroAtual;
        arq = new FileReader(arquivoTreinamento.getCaminhoCompleto());
        lerArq = new BufferedReader(arq);

        linha = lerArq.readLine();
        if (linha.contains("x1")) {
          linha = lerArq.readLine();
        }

        while (linha != null) {
          vetor = linha.split("\\s+");
          i = 0;

          if (vetor[0].equals("")) {
            i = 1;
          }

          entradas[0] = -1.0;
          entradas[1] = Double.parseDouble(vetor[i++].replace(",", "."));
          entradas[2] = Double.parseDouble(vetor[i++].replace(",", "."));
          entradas[3] = Double.parseDouble(vetor[i++].replace(",", "."));
          saidaEsperada = Double.parseDouble(vetor[i].replace(",", "."));

          //Calculando saidas da camada escondida
          saidaCamadaEscondida[0] = potencialCamadaEscondida[0] = -1D;
          for (i = 1; i < saidaCamadaEscondida.length; i++) {
            valorParcial = 0D;

            for (int j = 0; j < NUM_SINAIS_ENTRADA + 1; j++) {
              valorParcial += entradas[j] * pesosCamadaEscondida[i - 1][j];
            }

            potencialCamadaEscondida[i] = valorParcial;
            saidaCamadaEscondida[i] = funcaoLogistica(valorParcial);
          }

          //Calculando saida da camada de saída
          valorParcial = -1D * pesosCamadaSaida[0];
          for (i = 1; i < NUM_NEU_CAMADA_ESCONDIDA + 1; i++) {
            valorParcial += saidaCamadaEscondida[i - 1] * pesosCamadaSaida[i];
          }
          saida = funcaoLogistica(valorParcial);

          //Ajustando pesos sinapticos da camada de saida
          gradienteCamadaSaida = (saidaEsperada - saida) * funcaoLogisticaDerivada(valorParcial);

          for (i = 0; i < pesosCamadaSaida.length; i++) {
            pesosCamadaSaida[i] = pesosCamadaSaida[i] + (TAXA_APRENDIZAGEM * gradienteCamadaSaida * saidaCamadaEscondida[i]);
          }

          //Ajustando pesos sinapticos da camada escondida
          for (i = 0; i < NUM_NEU_CAMADA_ESCONDIDA; i++) {
            gradienteCamadaEscondida = gradienteCamadaSaida * pesosCamadaSaida[i + 1] * funcaoLogisticaDerivada(potencialCamadaEscondida[i + 1]);

            for (int j = 0; j < NUM_SINAIS_ENTRADA + 1; j++) {
              pesosCamadaEscondida[i][j] = pesosCamadaEscondida[i][j] + (TAXA_APRENDIZAGEM * gradienteCamadaEscondida * entradas[j]);
            }
          }

          linha = lerArq.readLine();
        }

        arq.close();
        erroAtual = erroQuadraticoMedio(arquivoTreinamento);
        Comunicador.addLog(String.format("%d   %.10f", numEpocas, erroAtual).replace(".", ","));
      } while (Math.abs(erroAtual - erroAnterior) > PRECISAO && numEpocas < 10000);

      Comunicador.addLog("Fim do treinamento.");
    } catch (FileNotFoundException ex) {
      return false;
    } catch (IOException ex) {
      return false;
    }

    return true;
  }

  public void testar(Arquivo arquivoTeste) {
    FileReader arq;
    BufferedReader lerArq;
    String linha;
    String[] vetor;
    int i;
    int numAmostras;
    double saidaEsperada;
    double valorParcial;
    double erroRelativo;

    Comunicador.iniciarLog("Início teste da MLP");
    Comunicador.addLog("Resposta - Saída rede          Erro");
    erroRelativo = 0D;
    numAmostras = 0;

    try {
        arq = new FileReader(arquivoTeste.getCaminhoCompleto());
        lerArq = new BufferedReader(arq);

        linha = lerArq.readLine();
        if (linha.contains("x1")) {
          linha = lerArq.readLine();
        }

        while (linha != null) {
          vetor = linha.split("\\s+");
          i = 0;

          if (vetor[0].equals("")) {
            i = 1;
          }

          numAmostras++;
          entradas[0] = -1.0;
          entradas[1] = Double.parseDouble(vetor[i++].replace(",", "."));
          entradas[2] = Double.parseDouble(vetor[i++].replace(",", "."));
          entradas[3] = Double.parseDouble(vetor[i++].replace(",", "."));
          saidaEsperada = Double.parseDouble(vetor[i].replace(",", "."));

          //Calculando saidas da camada escondida
          saidaCamadaEscondida[0] = potencialCamadaEscondida[0] = -1D;
          for (i = 1; i < saidaCamadaEscondida.length; i++) {
            valorParcial = 0D;

            for (int j = 0; j < NUM_SINAIS_ENTRADA + 1; j++) {
              valorParcial += entradas[j] * pesosCamadaEscondida[i - 1][j];
            }

            potencialCamadaEscondida[i] = valorParcial;
            saidaCamadaEscondida[i] = funcaoLogistica(valorParcial);
          }

          //Calculando saida da camada de saída
          valorParcial = -1D * pesosCamadaSaida[0];
          for (i = 1; i < NUM_NEU_CAMADA_ESCONDIDA + 1; i++) {
            valorParcial += saidaCamadaEscondida[i - 1] * pesosCamadaSaida[i];
          }
          saida = funcaoLogistica(valorParcial);

          //Porcentagem de erro em cada amostra
          valorParcial = (100D / saidaEsperada) * (Math.abs(saidaEsperada - saida));
          erroRelativo += valorParcial;
          Comunicador.addLog(String.format("%.4f         %.10f   %.10f%%", saidaEsperada, saida, valorParcial));

          linha = lerArq.readLine();
        }
        
        erroRelativo = erroRelativo / (double) numAmostras;
        Comunicador.addLog("Fim do teste");
        Comunicador.addLog(String.format("Erro relativo medio: %.10f%%", erroRelativo));

        arq.close();
    } catch (FileNotFoundException ex) {
    } catch (IOException ex) {
    }
  }

  private double erroQuadraticoMedio(Arquivo arquivo) {
    FileReader arq;
    BufferedReader lerArq;
    String linha;
    String[] vetor;
    int i;
    int numAmostras;
    double saidaEsperada;
    double valorParcial;
    double erro;

    erro = 0D;
    numAmostras = 0;

    try {
      arq = new FileReader(arquivo.getCaminhoCompleto());
      lerArq = new BufferedReader(arq);

      linha = lerArq.readLine();
      if (linha.contains("x1")) {
        linha = lerArq.readLine();
      }

      while (linha != null) {
        vetor = linha.split("\\s+");
        i = 0;

        if (vetor[0].equals("")) {
          i = 1;
        }

        numAmostras++;
        entradas[0] = -1.0;
        entradas[1] = Double.parseDouble(vetor[i++].replace(",", "."));
        entradas[2] = Double.parseDouble(vetor[i++].replace(",", "."));
        entradas[3] = Double.parseDouble(vetor[i++].replace(",", "."));
        saidaEsperada = Double.parseDouble(vetor[i].replace(",", "."));

        //Calculando saidas da camada escondida
        saidaCamadaEscondida[0] = potencialCamadaEscondida[0] = -1D;
        for (i = 1; i < saidaCamadaEscondida.length; i++) {
          valorParcial = 0D;

          for (int j = 0; j < NUM_SINAIS_ENTRADA + 1; j++) {
            valorParcial += entradas[j] * pesosCamadaEscondida[i - 1][j];
          }

          potencialCamadaEscondida[i] = valorParcial;
          saidaCamadaEscondida[i] = funcaoLogistica(valorParcial);
        }

        //Calculando saida da camada de saída
        valorParcial = -1D * pesosCamadaSaida[0];
        for (i = 1; i < NUM_NEU_CAMADA_ESCONDIDA + 1; i++) {
          valorParcial += saidaCamadaEscondida[i - 1] * pesosCamadaSaida[i];
        }
        saida = funcaoLogistica(valorParcial);

        //Calculando erro
        erro = erro + (Math.pow((saidaEsperada - this.saida), 2D) / 2D);

        linha = lerArq.readLine();
      }

      arq.close();
      erro = erro / (double) numAmostras;

    } catch (FileNotFoundException ex) {
    } catch (IOException ex) {
    }

    return erro;
  }

  private double funcaoLogistica(double valor) {
    return 1D / (1D + Math.pow(Math.E, -1D * BETA * valor));
  }

  private double funcaoLogisticaDerivada(double valor) {
    return (BETA * Math.pow(Math.E, -1D * BETA * valor)) / Math.pow((Math.pow(Math.E, -1D * BETA * valor) + 1D), 2D);
  }

}