package br.com.estudomock.leilao.servico.util;

import br.com.estudomock.leilao.dominio.Leilao;

public interface EnviadorDeEmail {
	void envia(Leilao leilao);
}
