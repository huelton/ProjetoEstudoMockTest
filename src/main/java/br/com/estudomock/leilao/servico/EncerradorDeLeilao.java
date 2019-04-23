package br.com.estudomock.leilao.servico;

import java.util.Calendar;
import java.util.List;

import br.com.estudomock.leilao.dominio.Leilao;
import br.com.estudomock.leilao.infra.dao.LeilaoDao;
import br.com.estudomock.leilao.infra.repositorio.RepositorioDeLeiloes;
import br.com.estudomock.leilao.servico.util.EnviadorDeEmail;


public class EncerradorDeLeilao {

	private int encerrados;
	private final RepositorioDeLeiloes dao;
	private final EnviadorDeEmail carteiro;
	
    public EncerradorDeLeilao(RepositorioDeLeiloes dao, EnviadorDeEmail carteiro) {
    	this.dao = dao;
    	this.carteiro = carteiro;
    }

	public void encerra() {
		List<Leilao> todosLeiloesCorrentes = dao.correntes();

		for (Leilao leilao : todosLeiloesCorrentes) {
			try{
					if (comecouSemanaPassada(leilao)) {
						encerrados++;
						leilao.encerra();
						dao.salva(leilao);
						dao.atualiza(leilao);
						carteiro.envia(leilao);
					}
			}catch(Exception e){
				//salva excessao em algum local
			}
		}
	}

	private boolean comecouSemanaPassada(Leilao leilao) {
		return diasEntre(leilao.getData(), Calendar.getInstance()) >= 7;
	}

	private int diasEntre(Calendar inicio, Calendar fim) {
		Calendar data = (Calendar) inicio.clone();
		int diasNoIntervalo = 0;
		while (data.before(fim)) {
			data.add(Calendar.DAY_OF_MONTH, 1);
			diasNoIntervalo++;
		}

		return diasNoIntervalo;
	}

	public int getTotalEncerrados() {
		return encerrados;
	}
}

