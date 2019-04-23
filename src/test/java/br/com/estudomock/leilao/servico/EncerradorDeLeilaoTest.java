package br.com.estudomock.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import br.com.estudomock.leilao.builder.CriadorDeLeilao;
import br.com.estudomock.leilao.dominio.Leilao;
import br.com.estudomock.leilao.infra.dao.LeilaoDao;
import br.com.estudomock.leilao.infra.repositorio.RepositorioDeLeiloes;
import br.com.estudomock.leilao.servico.util.EnviadorDeEmail;

public class EncerradorDeLeilaoTest {
	
	Leilao leilao1, leilao2, leilaoMaiorQue7Dias, leilaoDeOntem, leilaoAntesDeOntem;
	
	RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);        
	EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);
	
	@Before
	public void inicializadorDeTeste() {
	   // CRIACAO DO CENARIO GERAL
		Calendar dataAntiga = Calendar.getInstance();
		dataAntiga.set(1999, 1, 20);
		
		Calendar dataMaiorQue7Dias = Calendar.getInstance();
		dataMaiorQue7Dias.add(Calendar.DAY_OF_MONTH, -8);

		Calendar dataOntem = Calendar.getInstance();
		dataOntem.add(Calendar.DAY_OF_MONTH, -1);
		
		Calendar dataAntesOntem = Calendar.getInstance();
		dataAntesOntem.add(Calendar.DAY_OF_MONTH, -2);

		leilao1 = new CriadorDeLeilao().para("TV de Plasma").naData(dataAntiga).constroi();
		leilao2 = new CriadorDeLeilao().para("Geladeira").naData(dataAntiga).constroi();
		
		leilaoMaiorQue7Dias = new CriadorDeLeilao().para("Fogao").naData(dataMaiorQue7Dias).constroi();
		
		leilaoDeOntem = new CriadorDeLeilao().para("Gutarra").naData(dataOntem).constroi();
		leilaoAntesDeOntem = new CriadorDeLeilao().para("Gutarra").naData(dataAntesOntem).constroi();
	}

	@Test
	public void deveEncerrarLeiloesQueComecaraUmaSemanaAntes() {

		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		// ACAO A SER TOMADA		
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();

		// VERIFICAÇÃO DO CENARIO
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());
		assertEquals(2, encerrador.getTotalEncerrados());

	}

	@Test
	public void naoDeveEncerrarLeiloesQueComecaramOntem() {

		List<Leilao> leiloesAntigosDeOntem = Arrays.asList(leilaoDeOntem);

		// ACAO A SER TOMADA
	    when(daoFalso.correntes()).thenReturn(leiloesAntigosDeOntem);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();

		// VERIFICAÇÃO DO CENARIO
		assertFalse(leilaoDeOntem.isEncerrado());
 

	}

	@Test
	public void naoDeveEncerrarLeiloesQueComecaramOntemEPassarComMaisDe7Dias() {

		List<Leilao> leiloesAntigosDeOntemEMais7Dias = Arrays.asList(leilaoDeOntem, leilaoMaiorQue7Dias);

		// ACAO A SER TOMADA
		when(daoFalso.correntes()).thenReturn(leiloesAntigosDeOntemEMais7Dias);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();

		// VERIFICAÇÃO DO CENARIO
		assertFalse(leilaoDeOntem.isEncerrado());
		assertTrue(leilaoMaiorQue7Dias.isEncerrado());
		// porque o leilao de ontem não foi finalizado
		assertEquals(1, encerrador.getTotalEncerrados());
	}

	@Test
	public void naoDeveEncerrarLeiloesCasoNaoHajaNunhum() {

		// CRIACAO DO CENARIO

		// nesse caso não contem pois é tratado na lista.

		// ACAO A SER TOMADA
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
        // lista deve vir vazia	
		when(daoFalso.correntes()).thenReturn(new ArrayList<Leilao>());

	    EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();

		// VERIFICAÇÃO DO CENARIO
		assertEquals(0, encerrador.getTotalEncerrados());

	}

	@Test
	public void deveAtualizarLeiloesEncerrados() {

		// ACAO A SER TOMADA	    
	    when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();

		// VERIFICAÇÃO DO CENARIO
		verify(daoFalso, times(1)).atualiza(leilao1);

	}

	@Test
	public void deveGarantirQueOMetodoAtualizaEChamadoPeloEncerradorDeLeiloes() {

		// ACAO A SER TOMADA
	    when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();

		// VERIFICAÇÃO DO CENARIO
		verify(daoFalso, times(1)).atualiza(leilao1);

	}
	
	@Test
    public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras() {

	    // ACAO A SER TOMADA
	    when(daoFalso.correntes()).thenReturn(Arrays.asList(leilaoDeOntem, leilaoAntesDeOntem));
	    
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
        assertFalse(leilaoDeOntem.isEncerrado());
        assertFalse(leilaoAntesDeOntem.isEncerrado());

        verify(daoFalso,times(0)).atualiza(leilao1);
        verify(daoFalso,times(0)).atualiza(leilao2);
        
        
        //verify(daoFalso, atLeastOnce()).atualiza(leilao1); // se é chamando uma vez, duas ou tres ele passa
        //verify(daoFalso, atLeast(1)).atualiza(leilao1); // se é chamando pelo menos uma vez
        //verify(daoFalso, atMost(2)).atualiza(leilao1); // se é chamando maximo de vezes
	}
	
	
	@Test
	public void deveEnviarEmailAposPersistirLeilaoEncerrado() {

		// ACAO A SER TOMADA
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();
		
		 // passamos os mocks que serao verificados
        InOrder inOrder = Mockito.inOrder(daoFalso, carteiroFalso);
        // a primeira invocação
        inOrder.verify(daoFalso, times(1)).atualiza(leilao1);    
        // a segunda invocação
        inOrder.verify(carteiroFalso, times(1)).envia(leilao1);
	}
	
	@Test
	public void deveContinuarAExecucaoMesmoQuandoODaoFalha() {

		// ACAO A SER TOMADA
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
        doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);	
        
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();
		
		verify(daoFalso).atualiza(leilao2);
		verify(carteiroFalso).envia(leilao2);
		
		verify(carteiroFalso, times(0)).envia(leilao1);
	}
	
}
