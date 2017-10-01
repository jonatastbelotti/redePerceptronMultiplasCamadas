package Controle;

import Model.MLP;
import Recursos.Arquivo;
import javax.swing.JOptionPane;

/**
 *
 * @author Jônatas Trabuco Belotti [jonatas.t.belotti@hotmail.com]
 */
public class ThreadAcao extends Thread {
  private MLP redeMLP = null;
  private Arquivo arquivoTreinamento;

  public ThreadAcao(MLP redeMLP) {
    this.redeMLP = redeMLP;
  }

  public void setArquivoTreinamento(Arquivo arquivoTreinamento) {
    this.arquivoTreinamento = arquivoTreinamento;
  }

  @Override
  public void run() {
    if (redeMLP != null && arquivoTreinamento != null){
      if (redeMLP.treinar(arquivoTreinamento)) {
        JOptionPane.showMessageDialog(null, "Rede MLP treinada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        Comunicador.setEnabledBotaoTestar(true);
        Comunicador.setEnabledBotaoSalvar(true);
      } else {
        JOptionPane.showMessageDialog(null, "Houve um erro no treinamento da rede!", "Erro", JOptionPane.ERROR_MESSAGE);
        Comunicador.setEnabledBotaoTestar(false);
        Comunicador.setEnabledBotaoSalvar(false);
      }
      
      stop();
    }
  }

}
