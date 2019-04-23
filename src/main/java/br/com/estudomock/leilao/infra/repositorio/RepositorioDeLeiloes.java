package br.com.estudomock.leilao.infra.repositorio;

import java.util.List;

import br.com.estudomock.leilao.dominio.Leilao;

public interface RepositorioDeLeiloes {
	void salva(Leilao leilao);

	List<Leilao> encerrados();

	List<Leilao> correntes();

	void atualiza(Leilao leilao);
}