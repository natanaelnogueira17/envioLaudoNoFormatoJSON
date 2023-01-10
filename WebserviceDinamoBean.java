package br.gov.ce.pefoce.controller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.google.gson.JsonObject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import br.gov.ce.pefoce.enumerate.TipoArmaEnum;
import br.gov.ce.pefoce.pericia.dao.EvidenciaArmamentoDao;
import br.gov.ce.pefoce.pericia.dao.EvidenciaDispositivoTecnologicoDao;
import br.gov.ce.pefoce.pericia.dao.EvidenciaDispositivoTecnologicoImeiDao;
import br.gov.ce.pefoce.pericia.dao.EvidenciaDocumentoDao;
import br.gov.ce.pefoce.pericia.dao.EvidenciaEnvolvidoPessoaDao;
import br.gov.ce.pefoce.pericia.dao.EvidenciaMaterialDao;
import br.gov.ce.pefoce.pericia.dao.EvidenciaObjetoDao;
import br.gov.ce.pefoce.pericia.dao.EvidenciaVeiculoDao;
import br.gov.ce.pefoce.pericia.dao.LaudoAnexoDao;
import br.gov.ce.pefoce.pericia.dao.PericiaEvidenciaDao;
import br.gov.ce.pefoce.pericia.dao.PericiaLaudoDao;
import br.gov.ce.pefoce.pericia.entity.EvidenciaDispositivoTecnologicoImei;
import br.gov.ce.pefoce.pericia.entity.PericiaEvidencia;
import br.gov.ce.pefoce.pericia.entity.PericiaLaudo;
import br.gov.ce.pefoce.pericia.entity.PessoaCaracteristicaFisica;
import br.gov.ce.pefoce.pericia.entity.PessoaDocumento;
import br.gov.ce.pefoce.util.FacesUtils;




@ManagedBean
@ViewScoped
public class WebserviceDinamoBean extends AbstractBean implements Serializable {
	private static final long serialVersionUID = 1L;


//	private String token ;
	
	@ManagedProperty("#{evidenciaArmamentoDao}")
	private EvidenciaArmamentoDao evidenciaArmamentoDao;

	@ManagedProperty("#{evidenciaDispositivoTecnologicoDao}")
	private EvidenciaDispositivoTecnologicoDao evidenciaDispositivoTecnologicoDao;

	@ManagedProperty("#{evidenciaDocumentoDao}")
	private EvidenciaDocumentoDao evidenciaDocumentoDao;

	@ManagedProperty("#{evidenciaMaterialDao}")
	private EvidenciaMaterialDao evidenciaMaterialDao;

	@ManagedProperty("#{evidenciaVeiculoDao}")
	private EvidenciaVeiculoDao evidenciaVeiculoDao;
	
	@ManagedProperty("#{evidenciaObjetoDao}")
	private EvidenciaObjetoDao evidenciaObjetoDao;
	
	@ManagedProperty("#{evidenciaEnvolvidoPessoaDao}")
	private EvidenciaEnvolvidoPessoaDao evidenciaEnvolvidoPessoaDao;
	
	@ManagedProperty("#{periciaLaudoDao}")
	private PericiaLaudoDao periciaLaudoDao;
	
	@ManagedProperty("#{periciaEvidenciaDao}")
	private PericiaEvidenciaDao periciaEvidenciaDao;
	
	@ManagedProperty("#{evidenciaDispositivoTecnologicoImeiDao}")
	private EvidenciaDispositivoTecnologicoImeiDao imeiDao;
	
	@ManagedProperty("#{laudoAnexoDao}")
	private LaudoAnexoDao laudoAnexoDao;
	
	//private JsonObject jsonLaudo = jsonDinamo;
	
	//metodo para gerar Token
	public String gerarToken() throws IOException, JSONException {
		String token = "";
		try {
			URL url = new URL("http://192.168.0.121:8080/dinamo/v1/auth/token");
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("POST");
			http.setDoOutput(true);
			http.setRequestProperty("Accept", "application/Json");
			http.setRequestProperty("Content-Type", "application/Json");
			http.setRequestProperty("Authorization", "Bearer {token}");

			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("username", "03352805369");
			jsonObject.addProperty("password", "Nn031407*");	
				
			

			OutputStreamWriter stream = new OutputStreamWriter(http.getOutputStream());
			stream.write(jsonObject.toString());
			stream.close();

			StringBuilder response = new StringBuilder();
			int httpResult = http.getResponseCode();
			if (httpResult == HttpURLConnection.HTTP_ACCEPTED ||httpResult == HttpURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
				String line = null;
				while ((line = br.readLine()) != null) {
					response.append(line + "\n");
				}
				br.close();
				//JSONObject jsonToken = new JSONObject(response.toString());
				token = response.toString();//jsonToken.get("token").toString();
			
				
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return token;	

	}
	
	
	//método para criar o Json com os dados das Evidências
//	@SuppressWarnings("null")
	public JSONObject criarJsonDinamo(PericiaLaudo periciaLaudo )throws IOException, JSONException {	
		
			
		
		
		//Objetos Json criados para receber as informacoes 
		JSONObject jsonDadosOb = new JSONObject();	
		JSONObject jsonPerito = new JSONObject();	
		JSONObject jsonObjeto = new JSONObject();
		//Objeto principal que carrega todas as informações
		JSONObject jsonDinamo = new JSONObject();
		
		// listas para receber as evidencias - pessoas / objetos
		List<JSONObject> listaPessoas = new ArrayList<>();
		List<JSONObject> listaObjetosEvid = new ArrayList<>();
	
		
		try {
			String msgErro = ""; // para chamar as mensagens de erros caso nao carregue as informacoes mais importantes
			
			periciaLaudo.getPericia().setUltimoPericiaLaudo(periciaLaudoDao.buscarUltimaPericiaLaudo(periciaLaudo.getPericia()));
			if(periciaLaudo.getPericia().getUltimoPericiaLaudo().getLaudo().getTipoLaudo() == null 
					|| periciaLaudo.getPericia().getUltimoPericiaLaudo().getLaudo().getNumero() ==null) {
				
				msgErro = "Erro ao informar Tipo de Laudo ou Numero do Laudo.Favor comunicar a CTI!";
			}else {
				jsonDadosOb.put("tipoLaudo", periciaLaudo.getPericia().getUltimoPericiaLaudo().getLaudo().getTipoLaudo()!= null ? periciaLaudo.getPericia().getUltimoPericiaLaudo().getLaudo().getTipoLaudo().getDescricao() : "");
				jsonDadosOb.put("numeroLaudo", periciaLaudo.getPericia().getUltimoPericiaLaudo().getLaudo().getNumero()!= null ? periciaLaudo.getPericia().getUltimoPericiaLaudo().getLaudo().getNumero(): "");
				jsonDadosOb.put("dataPericia", periciaLaudo.getPericia().getUltimoPericiaLaudo().getPericia().getDataInclusao()!= null ? periciaLaudo.getPericia().getUltimoPericiaLaudo().getPericia().getDataInclusao() : "");
				jsonDadosOb.put("dataLaudo", periciaLaudo.getPericia().getUltimoPericiaLaudo().getLaudo().getDataInclusao()!= null ? periciaLaudo.getPericia().getUltimoPericiaLaudo().getLaudo().getDataInclusao(): "");		
				
			}
			//jsonPerito.put("Perito_id",periciaLaudo.getPericia().getAuxiliarPericia().getRegistroFuncional().getId()!= null ? periciaLaudo.getPericia().getAuxiliarPericia().getRegistroFuncional().getId() :"");
			if(periciaLaudo.getUsuario() != null) {
				
				jsonPerito.put("nome", periciaLaudo.getUsuario() != null ?  periciaLaudo.getUsuario().getNome() : "");
				jsonPerito.put("cpf",periciaLaudo.getUsuario()!= null ? periciaLaudo.getPericia().getUltimoPericiaLaudo().getUsuario().getCpf():"" );
				jsonPerito.put("matricula",periciaLaudo.getUsuario().getRegistroFuncional().getMatricula()!= null ? periciaLaudo.getUsuario().getRegistroFuncional().getMatricula():"");
				
			} else {
				msgErro = "Erro ao carregar dados Perito!";
				
			}
			
			
			//lista contendo todas as evidencias 
			List<PericiaEvidencia> listaPericiaEvidencia = periciaEvidenciaDao.buscarPericiaEvidenciaPorPericia(periciaLaudo.getPericia()); 
			
			// percorrendo a lista de evidencias
			for(PericiaEvidencia pe : listaPericiaEvidencia) {
				
				
				//case para chamar a evidencia conforme está no Laudo
				switch (pe.getEvidencia().getTipoEvidencia()) {
					
					
					
					case  PESSOA_ENVOLVIDA:		 // carrega as informacoes dos envolvidos 	
						
								pe.getEvidencia().setUltimaEvidenciaEnvolvido(evidenciaEnvolvidoPessoaDao.buscarUltimoEnvolvidoPorEvidencia(pe.getEvidencia()));	 // set para setUltimaEvidenciaEnvolvido
								 
								JSONObject jsonPessoa = new JSONObject (); // cria o Objeto json para envolvidos
							
								if(pe.getEvidencia().getUltimaEvidenciaEnvolvido().getNome() == null) {	//validacao para nao gerar exception						
									
									msgErro = "Erro ao carregar dados do Envolvido";
									
								}else {
									jsonPessoa.put("nome", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getNome() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getNome() : "");
									jsonPessoa.put("nacionalidade", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getNacionalidade() != null ?  pe.getEvidencia().getUltimaEvidenciaEnvolvido().getNacionalidade().getDescricao() : "");
									jsonPessoa.put("naturalidade", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getNaturalidade() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getNaturalidade().getDescricao() : "");
									jsonPessoa.put("profissaoPessoa", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getProfissaoPessoa() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getProfissaoPessoa().getDescricao(): "");
									jsonPessoa.put("nomePai", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getNomePai() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getNomePai() : "");
									jsonPessoa.put("nomeMae", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getNomeMae() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getNomeMae() : "");
									jsonPessoa.put("sexo",pe.getEvidencia().getUltimaEvidenciaEnvolvido().getSexo() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getSexo() : "");
									jsonPessoa.put("dataNascimento", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getDataNascimento() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getDataNascimento() : "");
									jsonPessoa.put("nomeResponsavelLegal", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getNomeResponsavelLegal() != null ?  pe.getEvidencia().getUltimaEvidenciaEnvolvido().getNomeResponsavelLegal(): "");
									jsonPessoa.put("estadoCivil", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getEstadoCivil() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getEstadoCivil(): "");
									jsonPessoa.put("grauInstrucao", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getGrauInstrucao() != null ?  pe.getEvidencia().getUltimaEvidenciaEnvolvido().getGrauInstrucao(): "");
									jsonPessoa.put("perfilGenetico", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPerfilGenetico() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPerfilGenetico() : "");
									jsonPessoa.put("tipoSanguineo", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoSanguineo() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoSanguineo(): "");
									jsonPessoa.put("gemeos", pe.getEvidencia().getUltimaEvidenciaEnvolvido().isGemeos());
									jsonPessoa.put("desconhecido", pe.getEvidencia().getUltimaEvidenciaEnvolvido().isDesconhecido());
									jsonPessoa.put("feto", pe.getEvidencia().getUltimaEvidenciaEnvolvido().isFeto());
									jsonPessoa.put("crianca", pe.getEvidencia().getUltimaEvidenciaEnvolvido().isCrianca() );
									jsonPessoa.put("adulto", pe.getEvidencia().getUltimaEvidenciaEnvolvido().isAdulto() );
									jsonPessoa.put("idoso", pe.getEvidencia().getUltimaEvidenciaEnvolvido().isIdoso() );
									jsonPessoa.put("ossada", pe.getEvidencia().getUltimaEvidenciaEnvolvido().isOssada() );
									jsonPessoa.put("registroCadaverico", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getRegistroCadaver() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getRegistroCadaver() : "");
									jsonPessoa.put("tipoEnvolvimento", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoEnvolvido() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoEnvolvido().getDescricao() : "");
									jsonPessoa.put("profissao", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getProfissao() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getProfissao() : "");
									jsonPessoa.put("identificado", pe.getEvidencia().getUltimaEvidenciaEnvolvido().isIdentificado());
									jsonPessoa.put("motivoNaoIdentificado", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getMotivoNaoIdentificado() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getMotivoNaoIdentificado().getDescricao() : "");
									jsonPessoa.put("calcado", pe.getEvidencia().getUltimaEvidenciaEnvolvido().isCalcado() );
									jsonPessoa.put("situacaoCorpo", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getSituacaoCorpo() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getSituacaoCorpo(): "");
									jsonPessoa.put("tipoRigidezCadaverica", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoRigidezCadaverica() != null ?  pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoRigidezCadaverica().getDescricao() : "");
									jsonPessoa.put("tipoInstrumento", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoInstrumento() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoInstrumento().getDescricao()  : "");
									jsonPessoa.put("tipoPosicaoCorpo", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoPosicaoCorpo() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoPosicaoCorpo().getDescricao() : "");
									jsonPessoa.put("tipoFaixaEtaria", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoFaixaEtaria() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoFaixaEtaria().getDescricao() : "");
									jsonPessoa.put("tipoCorPele", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoCorPele() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoCorPele().getDescricao() : "");
									jsonPessoa.put("tipoEstatura", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoEstatura() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoEstatura().getDescricao() : "");
									jsonPessoa.put("tipoBiotipo", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoBiotipo() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoBiotipo().getDescricao() : "");
									jsonPessoa.put("tipoCabelo", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoCabelo() != null ?  pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoCabelo().getDescricao() : "");
									jsonPessoa.put("tipoComprimentoCabelo", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoComprimentoCabelo() != null ?  pe.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoComprimentoCabelo().getDescricao() : "");
									jsonPessoa.put("vesteSuperior", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getVesteSuperior() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getVesteSuperior() : "");
									jsonPessoa.put("corVesteSuperior", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getCorVesteSuperior() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getCorVesteSuperior() : "");
									jsonPessoa.put("vesteInferior", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getVesteInferior() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getVesteInferior() : "");
									jsonPessoa.put("corVesteInferior", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getCorVesteInferior() != null ?  pe.getEvidencia().getUltimaEvidenciaEnvolvido().getCorVesteInferior() : "");
									jsonPessoa.put("descricaoCalcado", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getDescricaoCalcado() != null ?  pe.getEvidencia().getUltimaEvidenciaEnvolvido().getDescricaoCalcado() : "");
									jsonPessoa.put("corCalcado", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getCorCalcado() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getCorCalcado(): "");
									jsonPessoa.put("pertences", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPertences() != null ?  pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPertences()  : "");
									jsonPessoa.put("destinoPertences", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getDestinoPertences() != null ?  pe.getEvidencia().getUltimaEvidenciaEnvolvido().getDestinoPertences().getDescricao() : "");
									jsonPessoa.put("pessoaEnderecoRua", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPessoaEndereco() != null ?pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPessoaEndereco().getNomeLogradouro() : "");
									jsonPessoa.put("pessoaEnderecoNumero", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPessoaEndereco()!= null ?pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPessoaEndereco().getNumero() : "");
									jsonPessoa.put("pessoaEnderecoComplemento", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPessoaEndereco() != null ?pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPessoaEndereco().getComplemento() : "");
									jsonPessoa.put("pessoaEnderecoCep", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPessoaEndereco() != null ?pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPessoaEndereco().getCep() : "");
									jsonPessoa.put("pessoaEnderecoMunicipio", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPessoaEndereco() != null ?pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPessoaEndereco().getMunicipio().getDescricao() : "");
									jsonPessoa.put("pessoaLesao", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPessoaLesao() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPessoaLesao().getDescricao() : "");
									jsonPessoa.put("pessoaContato", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPessoaContato() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getPessoaContato()  : "");
									jsonPessoa.put("resumoTodasCaracteristicasFisicas", pe.getEvidencia().getUltimaEvidenciaEnvolvido().getResumoTodasCaracteristicasFisicas() != null ? pe.getEvidencia().getUltimaEvidenciaEnvolvido().getResumoTodasCaracteristicasFisicas() : "");	
									
									//lista que traz as caracterisitcas fisicas
									List<PessoaCaracteristicaFisica> listaPessoaCaracteristicaFisica = 	pe.getEvidencia().getUltimaEvidenciaEnvolvido().getListaPessoaCaracteristicaFisica();								
									if(listaPessoaCaracteristicaFisica !=null ) {
											JSONObject jsonPessoaCaracteristicaFisica = new JSONObject();
											for(PessoaCaracteristicaFisica pcf : listaPessoaCaracteristicaFisica) {
												jsonPessoaCaracteristicaFisica.put("nome", pcf.getTipoCaracteristicaFisica() != null? pcf.getTipoCaracteristicaFisica().getDescricao(): "");
												jsonPessoaCaracteristicaFisica.put("regiaoDoCorpo",pcf.getRegiaoCorpo() != null ? pcf.getRegiaoCorpo().getDescricao(): "");
												jsonPessoaCaracteristicaFisica.put("cor", pcf.getCor() != null ? pcf.getCor().getDescricao() :"");
												jsonPessoaCaracteristicaFisica.put("tamanho", pcf.getTamanho() != null ? pcf.getTamanho() : "");
												jsonPessoaCaracteristicaFisica.put("descricao",pcf.getDescricao() != null ? pcf.getDescricao() :"");									
										}
										jsonPessoa.put("listaPessoaCaracteristicaFisica", jsonPessoaCaracteristicaFisica);
									}else {	
										//lista que traz os documentos do envolvido 
										List<PessoaDocumento> listaPessoaDocumento = pe.getEvidencia().getUltimaEvidenciaEnvolvido().getListaPessoaDocumento();
											if(listaPessoaDocumento != null ) {
													JSONObject jsonPessoaDocumento = new JSONObject();
													for(PessoaDocumento pd : listaPessoaDocumento) {
														jsonPessoaDocumento.put("tipoDeDocumento", pd.getPessoaTipoDocumento() !=null ? pd.getPessoaTipoDocumento().getDescricao(): "");
														jsonPessoaDocumento.put("numero", pd.getOrgaoExpeditor() !=null ? pd.getOrgaoExpeditor() : "");
														jsonPessoaDocumento.put("observacoes", pd.getNumero() != null? pd.getNumero() : "");
														
														jsonPessoa.put("listaPessoaDocumento", jsonPessoaDocumento);
												}
													}else {
											
											listaPessoas.add(jsonPessoa); // adciona os objetos JSON  a lista de pessoas envolvidas
											}
										}
								}
								
				
					
								
					break;
					
				case MATERIAL_BALISTICO:
								
					
								pe.getEvidencia().setUltimaEvidenciaArmamento(evidenciaArmamentoDao.buscarUltimoArmamentoPorEvidencia(pe.getEvidencia())); // set na UltimaEvidenciaArmamento
								
								JSONObject jsonMaterialbalisitco = new JSONObject (); // cria um objeto json para cada evidencia encontrada 
								
								if(pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoArma().getDescricao()==TipoArmaEnum.ARMA.getDescricao()) {  // valida se for  arma
									
								if (pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoTipo() == null) { // validacao para nao gerar exception
										
									
									msgErro = "Erro ao carregar dados Arma";
									
								}else {								
										
								jsonMaterialbalisitco.put("tipoArma", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoTipo() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoTipo().getDescricao(): "");
								jsonMaterialbalisitco.put("marca", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoMarca() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoMarca().getDescricao() : "");
								jsonMaterialbalisitco.put("modelo", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoModelo() != null ?  pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoModelo().getDescricao() : "");
								jsonMaterialbalisitco.put("calibre", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre().getDescricao() : "");
								jsonMaterialbalisitco.put("numeroDeSerie", pe.getEvidencia().getUltimaEvidenciaArmamento().getNumeroSerie() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getNumeroSerie() : "");
								jsonMaterialbalisitco.put("raspado", pe.getEvidencia().getUltimaEvidenciaArmamento().isRaspado() );
								jsonMaterialbalisitco.put("institucional", pe.getEvidencia().getUltimaEvidenciaArmamento().isInstitucional());
								jsonMaterialbalisitco.put("fabricacao", pe.getEvidencia().getUltimaEvidenciaArmamento().getFabricacao() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getFabricacao() : "" );
								jsonMaterialbalisitco.put("tipoAdulteracao", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAdulteracao() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAdulteracao().getDescricao(): "");
								jsonMaterialbalisitco.put("tipoFuncionamentoArma", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoFuncionamentoArma() != null ?  pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoFuncionamentoArma().getDescricao()  : "");
								jsonMaterialbalisitco.put("tipoGatilhoArma", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoGatilhoArma() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoGatilhoArma().getDescricao() : "");
								jsonMaterialbalisitco.put("tipoCanoArma", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoCanoArma() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoCanoArma().getDescricao() : "");
								jsonMaterialbalisitco.put("tipoAlmaDoCano", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAlmaDoCano() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAlmaDoCano().getDescricao() : "");
								jsonMaterialbalisitco.put("tipoSentidoRaia", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoSentidoRaia() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoSentidoRaia().getDescricao(): "");
 								jsonMaterialbalisitco.put("medidaDoCano", pe.getEvidencia().getUltimaEvidenciaArmamento().getMedidaDoCano() != null ?  pe.getEvidencia().getUltimaEvidenciaArmamento().getMedidaDoCano() : "");
								jsonMaterialbalisitco.put("comprimentoTotalDaArma", pe.getEvidencia().getUltimaEvidenciaArmamento().getComprimentoTotalDaArma() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getComprimentoTotalDaArma() : "");
								jsonMaterialbalisitco.put("alturaDaCoronha", pe.getEvidencia().getUltimaEvidenciaArmamento().getAlturaDaCoronha() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getAlturaDaCoronha(): "");
								jsonMaterialbalisitco.put("tipoAcabamentoArmacao", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcabamentoArmacao() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcabamentoArmacao().getDescricao(): "");
								jsonMaterialbalisitco.put("respostaOutroAcabamentoArmacao", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcabamentoArmacao() != null ?pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcabamentoArmacao().getDescricao() : "");
								jsonMaterialbalisitco.put("tipoAcabamentoFerrolho", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcabamentoFerrolho() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcabamentoFerrolho().getDescricao() : "");
								jsonMaterialbalisitco.put("respostaOutroAcabamentoFerrolho", pe.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroAcabamentoFerrolho() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroAcabamentoFerrolho(): "");
								jsonMaterialbalisitco.put("tipoAcabamentoCano", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcabamentoCano() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcabamentoCano().getDescricao() : "");
								jsonMaterialbalisitco.put("respostaOutroAcabamentoCano", pe.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroAcabamentoCano() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroAcabamentoCano() : "");
								jsonMaterialbalisitco.put("tipoRespostaArmacaoArticulada", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoRespostaArmacaoArticulada() != null ?  pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoRespostaArmacaoArticulada().getDescricao(): "");
								jsonMaterialbalisitco.put("tipoRespostaArmacaoRigida", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoRespostaArmacaoRigida() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoRespostaArmacaoRigida().getDescricao(): "");
								jsonMaterialbalisitco.put("tipoAparelhoDePontariaAlca", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAparelhoDePontariaAlca() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAparelhoDePontariaAlca().getDescricao(): "");
								jsonMaterialbalisitco.put("tipoAparelhoDePontariaMassa", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAparelhoDePontariaMassa() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAparelhoDePontariaMassa().getDescricao() : "");
								jsonMaterialbalisitco.put("tipoCoronha", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoCoronha() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoCoronha().getDescricao() : "");
								jsonMaterialbalisitco.put("cor", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoCoronha() != null ?  pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoCoronha().getDescricao() : "");
								jsonMaterialbalisitco.put("logomarcaDoFabricante", pe.getEvidencia().getUltimaEvidenciaArmamento().isLogomarcaDoFabricante() );
								jsonMaterialbalisitco.put("tipoAcaoArma", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcaoArma() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcaoArma().getDescricao(): "");
								jsonMaterialbalisitco.put("respostaOutroTipoDeAcao", pe.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroTipoDeAcao() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroTipoDeAcao() : "");
								jsonMaterialbalisitco.put("tipoSistemaDePercussao", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoSistemaDePercussao() != null ?  pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoSistemaDePercussao().getDescricao() : "");								
								jsonMaterialbalisitco.put("numeroCamaras", pe.getEvidencia().getUltimaEvidenciaArmamento().getNumeroCamaras() != null ?  pe.getEvidencia().getUltimaEvidenciaArmamento().getNumeroCamaras(): "");
								jsonMaterialbalisitco.put("capacidade", pe.getEvidencia().getUltimaEvidenciaArmamento().getCapacidade() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getCapacidade(): "");
								jsonMaterialbalisitco.put("tipoCarregador", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoCarregador() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoCarregador().getDescricao() : "");
								jsonMaterialbalisitco.put("tipoEstadoDeConservacaoDaArma", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoEstadoDeConservacaoDaArma() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoEstadoDeConservacaoDaArma().getDescricao() : "");
								jsonMaterialbalisitco.put("bandoleira", pe.getEvidencia().getUltimaEvidenciaArmamento().getBandoleira() != null ?  pe.getEvidencia().getUltimaEvidenciaArmamento().getBandoleira(): "");
								jsonMaterialbalisitco.put("soleira", pe.getEvidencia().getUltimaEvidenciaArmamento().getSoleira() != null ?  pe.getEvidencia().getUltimaEvidenciaArmamento().getSoleira() : "");
								jsonMaterialbalisitco.put("republica", pe.getEvidencia().getUltimaEvidenciaArmamento().isRepublica() );
								jsonMaterialbalisitco.put("brasoesEstado", pe.getEvidencia().getUltimaEvidenciaArmamento().isBrasoesEstado() );
								jsonMaterialbalisitco.put("brasoesPolicia", pe.getEvidencia().getUltimaEvidenciaArmamento().isBrasoesPolicia());
								jsonMaterialbalisitco.put("tipoPolicia", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoPolicia() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoPolicia().getDescricao(): "");
								jsonMaterialbalisitco.put("estadoPolicia", pe.getEvidencia().getUltimaEvidenciaArmamento().getEstadoPolicia() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEstadoPolicia(): "");
								jsonMaterialbalisitco.put("abreviacoes", pe.getEvidencia().getUltimaEvidenciaArmamento().getAbreviacoes() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getAbreviacoes(): "");
								jsonMaterialbalisitco.put("carregadorPistola", pe.getEvidencia().getUltimaEvidenciaArmamento().getCarregadorPistola() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getCarregadorPistola() : "");
								jsonMaterialbalisitco.put("armaOperante", pe.getEvidencia().getUltimaEvidenciaArmamento().getArmaOperante() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getArmaOperante().getDescricao(): "");
								jsonMaterialbalisitco.put("respostaMotivoDaInoperancia", pe.getEvidencia().getUltimaEvidenciaArmamento().getRespostaMotivoDaInoperancia() != null ?  pe.getEvidencia().getUltimaEvidenciaArmamento().getRespostaMotivoDaInoperancia() : "");
								
								}
								
					}else if(pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoArma().getDescricao() == TipoArmaEnum.CARTUCHO.getDescricao()) { // valida se encontrar cartucho 
								
								if(pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre()  == null) { // valida pra nao gerar exception
									
									msgErro = "Erro ao carregar dados do Cartucho";
								
								}else {
									
								jsonMaterialbalisitco.put("tipoMaterialBalistico", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoTipo() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoTipo().getDescricao(): "");
								jsonMaterialbalisitco.put("quantidade", pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade(): "");
								jsonMaterialbalisitco.put("marca", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoMarca() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoMarca().getDescricao() : "");
								jsonMaterialbalisitco.put("modelo", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoModelo() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoModelo().getDescricao() : "");
								jsonMaterialbalisitco.put("calibre", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre() != null ?  pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre().getDescricao(): "");
								jsonMaterialbalisitco.put("numeroDeSerie", pe.getEvidencia().getUltimaEvidenciaArmamento().getNumeroSerie() != null ?  pe.getEvidencia().getUltimaEvidenciaArmamento().getNumeroSerie(): "");
								jsonMaterialbalisitco.put("raspado", pe.getEvidencia().getUltimaEvidenciaArmamento().isRaspado());
								jsonMaterialbalisitco.put("institucional", pe.getEvidencia().getUltimaEvidenciaArmamento().isInstitucional() );
								jsonMaterialbalisitco.put("tipoProjetil", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoProjetil() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoProjetil().getDescricao() : "");
								jsonMaterialbalisitco.put("tipoEstadoDeConservacaoCartuchoOuEstojo", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoEstadoDeConservacaoCartuchoOuEstojo()!= null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoEstadoDeConservacaoCartuchoOuEstojo().getDescricao(): "");
								jsonMaterialbalisitco.put("lote", pe.getEvidencia().getUltimaEvidenciaArmamento().getLote() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getLote(): "");
								jsonMaterialbalisitco.put("quantidadeUtilizada", pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidadeUtilizada() != null ?pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidadeUtilizada(): "");
								jsonMaterialbalisitco.put("quantidadeRemanescente", pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidadeRemanescente() != null ?  pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidadeRemanescente() : "");
								}
								
								
								
					}else if(pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoArma().getDescricao() == TipoArmaEnum.PROJETIL.getDescricao()) { // valida para encontar projetil
						
								if (pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade()  == null) { // valida para nao gerar exception 
									
									msgErro = "Erro ao carregar dados do Projetil";
								
								}else {
									
								jsonMaterialbalisitco.put("tipoMaterialBalistico",pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoTipo() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoTipo().getDescricao() : "");
								jsonMaterialbalisitco.put("quantidadeUtilizada", pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidadeUtilizada() != null ?pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidadeUtilizada() : "");
								jsonMaterialbalisitco.put("quantidadeRemanescente", pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidadeRemanescente() != null ?pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidadeRemanescente() : "");
								jsonMaterialbalisitco.put("quantidade", pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade() != null ?pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade() : "");
								jsonMaterialbalisitco.put("calibre", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre().getDescricao() : "");
								jsonMaterialbalisitco.put("massaProjetil", pe.getEvidencia().getUltimaEvidenciaArmamento().getMassaProjetil() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getMassaProjetil().doubleValue() : "");
								jsonMaterialbalisitco.put("diametroProjetil", pe.getEvidencia().getUltimaEvidenciaArmamento().getDiametroProjetil() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getDiametroProjetil() : "");
								jsonMaterialbalisitco.put("comprimentoProjetil", pe.getEvidencia().getUltimaEvidenciaArmamento().getComprimentoProjetil() != null ?  pe.getEvidencia().getUltimaEvidenciaArmamento().getComprimentoProjetil() : "");
								jsonMaterialbalisitco.put("tipoMaterialProjetil", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoMaterialProjetil() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoMaterialProjetil().getDescricao() : "");
								jsonMaterialbalisitco.put("tipoDeformacaoProjetil", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoDeformacaoProjetil() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoDeformacaoProjetil().getDescricao(): "");					

								}
								
					
					}else if(pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoArma().getDescricao() == TipoArmaEnum.OUTROS.getDescricao()) { /// valida para encontrar  outros
								
						if(pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade()  == null) { // valida para nao gerar exception 
							
							msgErro = "Erro ao carregar dados de Outros";
						
						}else {
							
								jsonMaterialbalisitco.put("tipoMaterialBalistico", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoTipo() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoTipo().getDescricao(): "");
								jsonMaterialbalisitco.put("quantidade", pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade() != null ?  pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade() : "");
								jsonMaterialbalisitco.put("marca", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoMarca() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoMarca().getDescricao() : "");
								jsonMaterialbalisitco.put("modelo", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoModelo() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoModelo().getDescricao(): "");
								jsonMaterialbalisitco.put("calibre", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre().getDescricao(): "");
								jsonMaterialbalisitco.put("numeroDeSerie", pe.getEvidencia().getUltimaEvidenciaArmamento().getNumeroSerie() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getNumeroSerie(): "");
								jsonMaterialbalisitco.put("raspado", pe.getEvidencia().getUltimaEvidenciaArmamento().isRaspado() );
								jsonMaterialbalisitco.put("institucional", pe.getEvidencia().getUltimaEvidenciaArmamento().isInstitucional() );					
						}

										
						
					}else if(pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoArma().getDescricao() == TipoArmaEnum.CARREGADOR.getDescricao()) { /// valida para encontrar carregador
								
								if( pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoTipo()== null) {
									
									msgErro = "Erro ao carregar dados de Carregador";
								
								}else {
									
								jsonMaterialbalisitco.put("tipoMaterialbalistico", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoTipo() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoTipo().getDescricao(): "");
								jsonMaterialbalisitco.put("quantidade", pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade() != null ?  pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade(): "");
								jsonMaterialbalisitco.put("marca", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoMarca() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoMarca().getDescricao() : "");
								jsonMaterialbalisitco.put("modelo", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoModelo() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoModelo().getDescricao() : "");
								jsonMaterialbalisitco.put("calibre", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre().getDescricao(): "");
								jsonMaterialbalisitco.put("numeroDeSerie", pe.getEvidencia().getUltimaEvidenciaArmamento().getNumeroSerie() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getNumeroSerie() : "");
								jsonMaterialbalisitco.put("raspado", pe.getEvidencia().getUltimaEvidenciaArmamento().isRaspado() );
								jsonMaterialbalisitco.put("institucional", pe.getEvidencia().getUltimaEvidenciaArmamento().isInstitucional() );					
										
								}
									
					
						
					}else if(pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoArma().getDescricao() == TipoArmaEnum.ESTOJO.getDescricao()) { /// valida para encontrar estojo
								
						if(pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade() == null) {	// valida para nao gerar exception 						
							
							msgErro = "Erro ao carregar dados do Estojo";
							
						}else {
								jsonMaterialbalisitco.put("tipoMaterialBalistico", pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoArma() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getTipoArma().getDescricao() : "");
								jsonMaterialbalisitco.put("quantidade", pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade() != null ?  pe.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade(): "");
								jsonMaterialbalisitco.put("marca", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoMarca() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoMarca().getDescricao() : "");
								jsonMaterialbalisitco.put("modelo", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoModelo() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoModelo().getDescricao()  : "");
								jsonMaterialbalisitco.put("calibre", pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre().getDescricao() : "");
								jsonMaterialbalisitco.put("numeroDeSerie", pe.getEvidencia().getUltimaEvidenciaArmamento().getNumeroSerie() != null ? pe.getEvidencia().getUltimaEvidenciaArmamento().getNumeroSerie(): "");
								jsonMaterialbalisitco.put("raspado", pe.getEvidencia().getUltimaEvidenciaArmamento().isRaspado());
								jsonMaterialbalisitco.put("institucional", pe.getEvidencia().getUltimaEvidenciaArmamento().isInstitucional() );					
						}
						}
					
								listaObjetosEvid.add(jsonMaterialbalisitco); // adiciona os objetos a  lista Evidencias  
								jsonObjeto.put("materialBalistico", listaObjetosEvid); // adiciona a lista ao JSON de objetos Evidencias 
								
							
							break;
							
							
							
				case DISPOSITIVO_TECNOLOGICO: // validacao do case
					
								JSONObject jsonDispositivotecnologico = new JSONObject ();				 // cria objeto json para cada evidencia encontrada do tipo 				
								pe.getEvidencia().setUltimaEvidenciaDispositivo(evidenciaDispositivoTecnologicoDao.buscarUltimoDispositivoPorEvidencia(pe.getEvidencia()));								
									
								if(pe.getEvidencia().getUltimaEvidenciaDispositivo().getEvidenciaDispositivoTecnologicoTipo() == null) { // valida para nao gerar exception 
									
									msgErro="Erro ao carregar dados de Dispositivo Tecnologico";
								
								}else {
								jsonDispositivotecnologico.put("tipoDispositivoTecnologico", pe.getEvidencia().getUltimaEvidenciaDispositivo().getEvidenciaDispositivoTecnologicoTipo() != null ? pe.getEvidencia().getUltimaEvidenciaDispositivo().getEvidenciaDispositivoTecnologicoTipo().getDescricao() : "");
								jsonDispositivotecnologico.put("fabricante", pe.getEvidencia().getUltimaEvidenciaDispositivo().getEvidenciaDispositivoTecnologicoFabricante() != null ? pe.getEvidencia().getUltimaEvidenciaDispositivo().getEvidenciaDispositivoTecnologicoFabricante().getDescricao() : "");
								jsonDispositivotecnologico.put("cor", pe.getEvidencia().getUltimaEvidenciaDispositivo().getCor() != null ? pe.getEvidencia().getUltimaEvidenciaDispositivo().getCor().getDescricao(): "");
								jsonDispositivotecnologico.put("modelo", pe.getEvidencia().getUltimaEvidenciaDispositivo().getEvidenciaDispositivoTecnologicoModelo() != null ?  pe.getEvidencia().getUltimaEvidenciaDispositivo().getEvidenciaDispositivoTecnologicoModelo().getDescricao() : "");
								jsonDispositivotecnologico.put("numeroDeSerie", pe.getEvidencia().getUltimaEvidenciaDispositivo().getNumeroSerie() != null ? pe.getEvidencia().getUltimaEvidenciaDispositivo().getNumeroSerie(): "");
								jsonDispositivotecnologico.put("capacidade", pe.getEvidencia().getUltimaEvidenciaDispositivo().getCapacidade() != null ?  pe.getEvidencia().getUltimaEvidenciaDispositivo().getCapacidade(): "");
								jsonDispositivotecnologico.put("capacidadeUnidade", pe.getEvidencia().getUltimaEvidenciaDispositivo().getCapacidadeUnidade() != null ?  pe.getEvidencia().getUltimaEvidenciaDispositivo().getCapacidadeUnidade().getDescricao() : "");
//								List<EvidenciaDispositivoTecnologicoImei> listaDpTec = pe.getEvidencia().getUltimaEvidenciaDispositivo().getListaEvidenciaDispositivoTecnologicoImei();
								List<EvidenciaDispositivoTecnologicoImei> listaDpTec = imeiDao.buscarPorDispositivo(pe.getEvidencia().getUltimaEvidenciaDispositivo());
								
								if (listaDpTec != null) {	
									JSONObject jsonImei = new JSONObject();
									for(EvidenciaDispositivoTecnologicoImei DpTec : listaDpTec) {
										jsonImei.put("imei", DpTec.getNumero() );	
										jsonDispositivotecnologico.put("imei", jsonImei);
										}
									
										}else {
								jsonDispositivotecnologico.put("imei", "");
								}
								} // final do primeiro else
									
								listaObjetosEvid.add(jsonDispositivotecnologico); // adiciona os objetos a  lista Evidencias 
								
								jsonObjeto.put("dispositivoTecnologico", listaObjetosEvid); //  // adiciona a lista ao JSON de objetos Evidencias 
								break;							
								
							
				case DOCUMENTO:
					
							JSONObject jsonDocumento = new JSONObject ();// cria objeto json para cada evidencia encontrada do tipo 	
								pe.getEvidencia().setUltimaEvidenciaDocumento(evidenciaDocumentoDao.buscarUltimoDocumentoPorEvidencia(pe.getEvidencia()));
								
								if(pe.getEvidencia().getUltimaEvidenciaDocumento().getEvidenciaDocumentoTipo() == null) { // valida para nao gerar exception 
									
									msgErro="Erro ao carregar dados de Material Químico e Biológico";
								
								}else {
								jsonDocumento.put("tipoDocumento", pe.getEvidencia().getUltimaEvidenciaDocumento().getEvidenciaDocumentoTipo() != null ?  pe.getEvidencia().getUltimaEvidenciaDocumento().getEvidenciaDocumentoTipo().getDescricao() : "");
								jsonDocumento.put("qtdPaginas", pe.getEvidencia().getUltimaEvidenciaDocumento().getPaginas() != null ?  pe.getEvidencia().getUltimaEvidenciaDocumento().getPaginas(): "");
								jsonDocumento.put("qtdFolhas", pe.getEvidencia().getUltimaEvidenciaDocumento().getFolhas() != null ? pe.getEvidencia().getUltimaEvidenciaDocumento().getFolhas(): "");
								jsonDocumento.put("flgGrafismoOriginalPunho", pe.getEvidencia().getUltimaEvidenciaDocumento().isFlgGrafismoOriginalPunho() );
								jsonDocumento.put("paginasGrafismoOriginalPunho", pe.getEvidencia().getUltimaEvidenciaDocumento().getPaginasGrafismoOriginalPunho() != null ?  pe.getEvidencia().getUltimaEvidenciaDocumento().getPaginasGrafismoOriginalPunho(): "");
								}
								
								listaObjetosEvid.add(jsonDocumento); // adiciona os objetos a  lista Evidencias 
								
								jsonObjeto.put("documento", listaObjetosEvid); // adiciona a lista ao JSON de objetos Evidencias 
								
							break;
							
				case MATERIAL_QUIMICO_BIOLOGICO: 
								JSONObject jsonmatQuimicoBio = new JSONObject (); // cria objeto json para cada evidencia encontrada do tipo 
								
								pe.getEvidencia().setUltimaEvidenciaMaterial(evidenciaMaterialDao.buscarUltimoMaterialPorEvidencia(pe.getEvidencia()));
								// valida para nao gerar exception 
								if(pe.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaMaterialTipo() ==null || pe.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaMaterialApresentacao() == null   ) {
									
									msgErro="Erro ao carregar dados de Material Químico e Biológico";
								
								}else {
									
								jsonmatQuimicoBio.put("tipoMaterial", pe.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaMaterialTipo() != null ? pe.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaMaterialTipo().getDescricao() : "");
								jsonmatQuimicoBio.put("apresentacao", pe.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaMaterialApresentacao() != null ? pe.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaMaterialApresentacao() .getDescricao() : "") ;
								jsonmatQuimicoBio.put("quantidade", pe.getEvidencia().getUltimaEvidenciaMaterial().getQuantidade() != null ? pe.getEvidencia().getUltimaEvidenciaMaterial().getQuantidade() : "");
								jsonmatQuimicoBio.put("tipoMaterialApresentacao", pe.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaMaterialApresentacao() != null ? pe.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaMaterialApresentacao().getDescricao() : "");
								jsonmatQuimicoBio.put("embalagemFechadaPor",pe.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaFechadoPor() != null? pe.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaFechadoPor().getDescricao() : "");
								jsonmatQuimicoBio.put("embalagemFechadaPorOutroTipo", pe.getEvidencia().getUltimaEvidenciaMaterial().getEmbalagemFechadaPorOutroTipo() != null ? pe.getEvidencia().getUltimaEvidenciaMaterial().getEmbalagemFechadaPorOutroTipo(): "");
								jsonmatQuimicoBio.put("quantidadeEmbalagensContendo", pe.getEvidencia().getUltimaEvidenciaMaterial().getQuantidadeEmbalagensContendo() != null ? pe.getEvidencia().getUltimaEvidenciaMaterial().getQuantidadeEmbalagensContendo()  : "");
								jsonmatQuimicoBio.put("quantidadeEmbalagens", pe.getEvidencia().getUltimaEvidenciaMaterial().getQuantidadeEmbalagens() != null ? pe.getEvidencia().getUltimaEvidenciaMaterial().getQuantidadeEmbalagens() : "");
								jsonmatQuimicoBio.put("evidenciaContendoEmbalagem", pe.getEvidencia().getUltimaEvidenciaMaterial().getQuantidadeEmbalagens() != null ? pe.getEvidencia().getUltimaEvidenciaMaterial().getQuantidadeEmbalagens(): "");
								jsonmatQuimicoBio.put("evidenciaContendoOutroTipoEmbalagem", pe.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaContendoOutroTipoEmbalagem() != null ?  pe.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaContendoOutroTipoEmbalagem() : "");
								jsonmatQuimicoBio.put("tipoSubstancia", pe.getEvidencia().getUltimaEvidenciaMaterial().getTipoSubstancia()!= null ?  pe.getEvidencia().getUltimaEvidenciaMaterial().getTipoSubstancia().getDescricao(): "");
								jsonmatQuimicoBio.put("tipoAspectoSubstancia", pe.getEvidencia().getUltimaEvidenciaMaterial().getTipoAspectoSubstancia() != null ? pe.getEvidencia().getUltimaEvidenciaMaterial().getTipoAspectoSubstancia().getDescricao(): "");
								jsonmatQuimicoBio.put("cor", pe.getEvidencia().getUltimaEvidenciaMaterial().getCor() != null ? pe.getEvidencia().getUltimaEvidenciaMaterial().getCor().getDescricao() : "");
								jsonmatQuimicoBio.put("substanciaPrensada", pe.getEvidencia().getUltimaEvidenciaMaterial().isSubstanciaPrensada());
								jsonmatQuimicoBio.put("tipoConstituido", pe.getEvidencia().getUltimaEvidenciaMaterial().getTipoConstituido()!= null ? pe.getEvidencia().getUltimaEvidenciaMaterial().getTipoConstituido().getDescricao(): "");
								jsonmatQuimicoBio.put("substanciaFragmentada", pe.getEvidencia().getUltimaEvidenciaMaterial().isSubstanciaFragmentada() );
								jsonmatQuimicoBio.put("tipoConsistencia", pe.getEvidencia().getUltimaEvidenciaMaterial().getTipoConsistencia()!= null ? pe.getEvidencia().getUltimaEvidenciaMaterial().getTipoConsistencia().getDescricao(): "");
								jsonmatQuimicoBio.put("tipoPeso", pe.getEvidencia().getUltimaEvidenciaMaterial().getTipoPeso() != null ?  pe.getEvidencia().getUltimaEvidenciaMaterial().getTipoPeso().getDescricao(): "");
								jsonmatQuimicoBio.put("quantidadeUtilizada", pe.getEvidencia().getUltimaEvidenciaMaterial().getQuantidadeUtilizada() != null ? pe.getEvidencia().getUltimaEvidenciaMaterial().getQuantidadeUtilizada(): "");
								jsonmatQuimicoBio.put("unidadeMedida", pe.getEvidencia().getUltimaEvidenciaMaterial().getUnidadeMedida()!= null ?  pe.getEvidencia().getUltimaEvidenciaMaterial().getUnidadeMedida().getDescricao() : "");
								jsonmatQuimicoBio.put("quantidade", pe.getEvidencia().getUltimaEvidenciaMaterial().getQuantidade() != null ?  pe.getEvidencia().getUltimaEvidenciaMaterial().getQuantidade(): "");
								jsonmatQuimicoBio.put("materialTotalmenteConsumido", pe.getEvidencia().getUltimaEvidenciaMaterial().isMaterialTotalmenteConsumido());
								}
								
								listaObjetosEvid.add(jsonmatQuimicoBio);// adiciona os objetos a  lista Evidencias 
								
								jsonObjeto.put("materialQuimicoBiologico", listaObjetosEvid); // adiciona a lista ao JSON de objetos Evidencias 
								
								break;
								
				case VEICULO:
								JSONObject jsonVeiculo = new JSONObject ();  // cria objeto json para cada evidencia encontrada do tipo 
								
								pe.getEvidencia().setUltimaEvidenciaVeiculo(evidenciaVeiculoDao.buscarUltimoVeiculoPorEvidencia(pe.getEvidencia()));
								
								if(pe.getEvidencia().getUltimaEvidenciaVeiculo().getEvidenciaVeiculoTipo() == null  ) { // valida para nao gerar exception 
									
									msgErro="Erro ao carregar dados de Material Químico e Biológico";
								
								}else {
								
								jsonVeiculo.put("tipoVeiculo", pe.getEvidencia().getUltimaEvidenciaVeiculo().getEvidenciaVeiculoTipo() != null ? pe.getEvidencia().getUltimaEvidenciaVeiculo().getEvidenciaVeiculoTipo().getDescricao() : "");
								jsonVeiculo.put("marca", pe.getEvidencia().getUltimaEvidenciaVeiculo().getEvidenciaVeiculoMarca() != null ? pe.getEvidencia().getUltimaEvidenciaVeiculo().getEvidenciaVeiculoMarca().getDescricao() : "");
								jsonVeiculo.put("modelo", pe.getEvidencia().getUltimaEvidenciaVeiculo().getEvidenciaVeiculoModelo() != null ?  pe.getEvidencia().getUltimaEvidenciaVeiculo().getEvidenciaVeiculoModelo().getDescricao(): "");
								jsonVeiculo.put("cor", pe.getEvidencia().getUltimaEvidenciaVeiculo().getCor() != null ? pe.getEvidencia().getUltimaEvidenciaVeiculo().getCor().getDescricao() : "");
								jsonVeiculo.put("origem", pe.getEvidencia().getUltimaEvidenciaVeiculo().getOrigem() != null ? pe.getEvidencia().getUltimaEvidenciaVeiculo().getOrigem().getDescricao() : "");
								jsonVeiculo.put("ano", pe.getEvidencia().getUltimaEvidenciaVeiculo().getAno() != null ? pe.getEvidencia().getUltimaEvidenciaVeiculo().getAno(): "");
								jsonVeiculo.put("placa", pe.getEvidencia().getUltimaEvidenciaVeiculo().getPlaca() != null ? pe.getEvidencia().getUltimaEvidenciaVeiculo().getPlaca() : "");
								jsonVeiculo.put("placaOriginal", pe.getEvidencia().getUltimaEvidenciaVeiculo().getPlacaOriginal() != null ? pe.getEvidencia().getUltimaEvidenciaVeiculo().getPlacaOriginal(): "");
								jsonVeiculo.put("chassi", pe.getEvidencia().getUltimaEvidenciaVeiculo().getChassi() != null ? pe.getEvidencia().getUltimaEvidenciaVeiculo().getChassi() : "");
								jsonVeiculo.put("numeroMotor", pe.getEvidencia().getUltimaEvidenciaVeiculo().getNumeroMotor() != null ? pe.getEvidencia().getUltimaEvidenciaVeiculo().getNumeroMotor() : "");
								jsonVeiculo.put("orgao", pe.getEvidencia().getUltimaEvidenciaVeiculo().getOrgao() != null ? pe.getEvidencia().getUltimaEvidenciaVeiculo().getOrgao(): "");
								jsonVeiculo.put("proprietario", pe.getEvidencia().getUltimaEvidenciaVeiculo().getProprietario() != null ? pe.getEvidencia().getUltimaEvidenciaVeiculo().getProprietario() : "");
								jsonVeiculo.put("proprietarioCPF", pe.getEvidencia().getUltimaEvidenciaVeiculo().getProprietarioCpf() != null ?  pe.getEvidencia().getUltimaEvidenciaVeiculo().getProprietarioCpf() : "");
								jsonVeiculo.put("proprietarioRG", pe.getEvidencia().getUltimaEvidenciaVeiculo().getProprietarioRg() != null ?  pe.getEvidencia().getUltimaEvidenciaVeiculo().getProprietarioRg() : "");
								jsonVeiculo.put("proprietarioCNPJ", pe.getEvidencia().getUltimaEvidenciaVeiculo().getProprietarioCnpj() != null ? pe.getEvidencia().getUltimaEvidenciaVeiculo().getProprietarioCnpj() : "");
								jsonVeiculo.put("proprietarioCondutor", pe.getEvidencia().getUltimaEvidenciaVeiculo().isProprietarioCondutor());
								jsonVeiculo.put("condutor", pe.getEvidencia().getUltimaEvidenciaVeiculo().getCondutor() != null ? pe.getEvidencia().getUltimaEvidenciaVeiculo().getCondutor() : "");
								jsonVeiculo.put("condutorCPF", pe.getEvidencia().getUltimaEvidenciaVeiculo().getCondutorCpf() != null ? pe.getEvidencia().getUltimaEvidenciaVeiculo().getCondutorCpf(): "");
								jsonVeiculo.put("vistoriaVeiculo", pe.getEvidencia().getUltimaEvidenciaVeiculo().isVistoriaVeiculo());
								jsonVeiculo.put("veiculoComAvarias", pe.getEvidencia().getUltimaEvidenciaVeiculo().isVeiculoComAvarias());
								jsonVeiculo.put("unidadePolicial", pe.getEvidencia().getUltimaEvidenciaVeiculo().getLocalVistoriaVeiculo() != null ? pe.getEvidencia().getUltimaEvidenciaVeiculo().getLocalVistoriaVeiculo() : "");

								}
								listaObjetosEvid.add(jsonVeiculo); // adiciona os objetos a  lista Evidencias
								
								jsonObjeto.put("veiculo", listaObjetosEvid); // adiciona a lista ao JSON de objetos Evidencias
								
						break;
								
				case OBJETO:
								JSONObject jsonOutroObjeto = new JSONObject ();// cria objeto json para cada evidencia encontrada do tipo 
								
								pe.getEvidencia().setUltimaEvidenciaObjeto(evidenciaObjetoDao.buscarUltimoObjetoPorEvidencia(pe.getEvidencia()));
								
								if(pe.getEvidencia().getUltimaEvidenciaObjeto().getDescricao() == null) {// valida para nao gerar exception 
									
									msgErro="Erro ao carregar dados do Objeto";
									
								}else {
									
									jsonOutroObjeto.put("tipoMaterial", pe.getEvidencia().getUltimaEvidenciaObjeto().getDescricao() != null ? pe.getEvidencia().getUltimaEvidenciaObjeto().getDescricao() : " " );
									jsonOutroObjeto.put("armaBranca", pe.getEvidencia().getUltimaEvidenciaObjeto().isArmaBranca() );
									jsonOutroObjeto.put("tipoArmaBranca", pe.getEvidencia().getUltimaEvidenciaObjeto().isArmaBranca() );
									jsonOutroObjeto.put("numeroLacre", pe.getEvidencia().getUltimaEvidenciaObjeto().getNumeroLacre() != null ? pe.getEvidencia().getUltimaEvidenciaObjeto().getNumeroLacre() : "");
									jsonOutroObjeto.put("objetoApresentacao", pe.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaObjetoApresentacao() != null ? pe.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaObjetoApresentacao().getDescricao() : "");
									jsonOutroObjeto.put("objetoFechadoPor",pe.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaObjetoFechadoPor() != null ? pe.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaObjetoFechadoPor().getDescricao() : "");
									jsonOutroObjeto.put("objetoFechadoPorOutroTipo", pe.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaObjetoFechadoPorOutroTipo() != null ? pe.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaObjetoFechadoPorOutroTipo(): "");
									jsonOutroObjeto.put("tamanhototalObjeto", pe.getEvidencia().getUltimaEvidenciaObjeto().getTamanhoTotalObjeto() != null ? pe.getEvidencia().getUltimaEvidenciaObjeto().getTamanhoTotalObjeto(): "");
									jsonOutroObjeto.put("unidadeMedidaObjetoApresentado", pe.getEvidencia().getUltimaEvidenciaObjeto().getUnidadeMedidaObjetoApresentado() != null ?  pe.getEvidencia().getUltimaEvidenciaObjeto().getUnidadeMedidaObjetoApresentado().getDescricao(): "");
									jsonOutroObjeto.put("tipoCaboArmaBranca",pe.getEvidencia().getUltimaEvidenciaObjeto().getTipoCaboArmaBranca() != null ? pe.getEvidencia().getUltimaEvidenciaObjeto().getTipoCaboArmaBranca().getDescricao() : "");
									jsonOutroObjeto.put("tipoLamina", pe.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaTipoLamina() != null ? pe.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaTipoLamina().getDescricao() : "");
									jsonOutroObjeto.put("tamanhoTotalLamina", pe.getEvidencia().getUltimaEvidenciaObjeto().getTamanhoTotalLamina() != null ? pe.getEvidencia().getUltimaEvidenciaObjeto().getTamanhoTotalLamina() : "");
									jsonOutroObjeto.put("unidadeMedidaLaminaApresentada", pe.getEvidencia().getUltimaEvidenciaObjeto().getUnidadeMedidaLaminaApresentada() != null ?  pe.getEvidencia().getUltimaEvidenciaObjeto().getUnidadeMedidaLaminaApresentada().getDescricao()  : "");
									jsonOutroObjeto.put("tamanhoTotalCabo", pe.getEvidencia().getUltimaEvidenciaObjeto().getTamanhoTotalCabo() != null ? pe.getEvidencia().getUltimaEvidenciaObjeto().getTamanhoTotalCabo(): "");
									jsonOutroObjeto.put("unidadeMedidaCaboApresentado", pe.getEvidencia().getUltimaEvidenciaObjeto().getUnidadeMedidaCaboApresentado() != null ?  pe.getEvidencia().getUltimaEvidenciaObjeto().getUnidadeMedidaCaboApresentado().getDescricao() : "");
									jsonOutroObjeto.put("tipoFixacaoCaboELaminaArmaBranca", pe.getEvidencia().getUltimaEvidenciaObjeto().getTipoFixacaoCaboELmaniaArmaBranca() != null ?  pe.getEvidencia().getUltimaEvidenciaObjeto().getTipoFixacaoCaboELmaniaArmaBranca().getDescricao()  : "");
									jsonOutroObjeto.put("evidenciaObjetoFechadoPor", pe.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaObjetoFechadoPor() != null ?  pe.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaObjetoFechadoPor().getDescricao() : "");
									jsonOutroObjeto.put("cor", pe.getEvidencia().getUltimaEvidenciaObjeto().getCor() != null ? pe.getEvidencia().getUltimaEvidenciaObjeto().getCor().getDescricao(): "");
									jsonOutroObjeto.put("descricaoMarca",pe.getEvidencia().getUltimaEvidenciaObjeto().getDescricaoMarca() != null ? pe.getEvidencia().getUltimaEvidenciaObjeto().getDescricaoMarca() :  "");
									jsonOutroObjeto.put("acompanhaBainha", pe.getEvidencia().getUltimaEvidenciaObjeto().isAcompanhaBainha() );
									jsonOutroObjeto.put("embaladoEm", pe.getEvidencia().getUltimaEvidenciaObjeto().getEmbaladoEm() != null ? pe.getEvidencia().getUltimaEvidenciaObjeto().getEmbaladoEm().getDescricao(): "");
								}
						
								listaObjetosEvid.add(jsonOutroObjeto);  // adiciona os objetos a  lista Evidencias 
								
								jsonObjeto.put("objeto", listaObjetosEvid); // adiciona a lista ao JSON de objetos Evidencias 
						break;
					default:
						break;
					
					}
				}
			
							
				
				jsonDinamo.put("dadosDoLaudo",jsonDadosOb);//json principal recebendo dados obrigatorios
				jsonDinamo.put("perito",jsonPerito); //json principal recebendo dados do Perito
				
				if(listaPessoas != null && !listaPessoas.isEmpty()) {
					jsonDinamo.put("pessoa", listaPessoas); //json principal recebendo dados dos Envolvidos
				}
				if(listaObjetosEvid != null && !listaObjetosEvid.isEmpty()) {
					jsonDinamo.put("objeto", jsonObjeto); //json principal recebendo dados das Evidencias do tipo Objeto
				}
			
					periciaLaudo.getLaudo().setUltimoLaudoAnexo(laudoAnexoDao.buscarUltimoLaudoAnexoPorLaudo(periciaLaudo.getLaudo()));  // set para o ultimoLaudoAnexo				
					jsonDinamo.put("anexoLaudo", Base64.getEncoder().encodeToString(periciaLaudo.getLaudo().getUltimoLaudoAnexo().getArquivoAnexado())); //json principal recebendo dados do laudoAnexo
					
					
				
				
				System.out.println(jsonDinamo.toString()); // imprimir no JAVA
				
				if(msgErro.trim().length() > 0) { //msg de erro na tela do usuario caso nao seja preenchido os dados
					FacesUtils.addErrorMessage(msgErro);
					//return jsonDinamo.toString();
				}
				
				//meu envio ao webservice
								
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	//	return null;
		return jsonDinamo;
	
			
		
		}
	


	public LaudoAnexoDao getLaudoAnexoDao() {
		return laudoAnexoDao;
	}


	public void setLaudoAnexoDao(LaudoAnexoDao laudoAnexoDao) {
		this.laudoAnexoDao = laudoAnexoDao;
	}


	public EvidenciaArmamentoDao getEvidenciaArmamentoDao() {
		return evidenciaArmamentoDao;
	}


	public void setEvidenciaArmamentoDao(EvidenciaArmamentoDao evidenciaArmamentoDao) {
		this.evidenciaArmamentoDao = evidenciaArmamentoDao;
	}


	public EvidenciaDispositivoTecnologicoDao getEvidenciaDispositivoTecnologicoDao() {
		return evidenciaDispositivoTecnologicoDao;
	}


	public void setEvidenciaDispositivoTecnologicoDao(
			EvidenciaDispositivoTecnologicoDao evidenciaDispositivoTecnologicoDao) {
		this.evidenciaDispositivoTecnologicoDao = evidenciaDispositivoTecnologicoDao;
	}


	public EvidenciaDocumentoDao getEvidenciaDocumentoDao() {
		return evidenciaDocumentoDao;
	}


	public void setEvidenciaDocumentoDao(EvidenciaDocumentoDao evidenciaDocumentoDao) {
		this.evidenciaDocumentoDao = evidenciaDocumentoDao;
	}


	public EvidenciaMaterialDao getEvidenciaMaterialDao() {
		return evidenciaMaterialDao;
	}


	public void setEvidenciaMaterialDao(EvidenciaMaterialDao evidenciaMaterialDao) {
		this.evidenciaMaterialDao = evidenciaMaterialDao;
	}


	public EvidenciaVeiculoDao getEvidenciaVeiculoDao() {
		return evidenciaVeiculoDao;
	}


	public void setEvidenciaVeiculoDao(EvidenciaVeiculoDao evidenciaVeiculoDao) {
		this.evidenciaVeiculoDao = evidenciaVeiculoDao;
	}


	public EvidenciaObjetoDao getEvidenciaObjetoDao() {
		return evidenciaObjetoDao;
	}


	public void setEvidenciaObjetoDao(EvidenciaObjetoDao evidenciaObjetoDao) {
		this.evidenciaObjetoDao = evidenciaObjetoDao;
	}


	public EvidenciaEnvolvidoPessoaDao getEvidenciaEnvolvidoPessoaDao() {
		return evidenciaEnvolvidoPessoaDao;
	}


	public void setEvidenciaEnvolvidoPessoaDao(EvidenciaEnvolvidoPessoaDao evidenciaEnvolvidoPessoaDao) {
		this.evidenciaEnvolvidoPessoaDao = evidenciaEnvolvidoPessoaDao;
	}

	public PericiaLaudoDao getPericiaLaudoDao() {
		return periciaLaudoDao;
	}


	public void setPericiaLaudoDao(PericiaLaudoDao periciaLaudoDao) {
		this.periciaLaudoDao = periciaLaudoDao;
	}


	public PericiaEvidenciaDao getPericiaEvidenciaDao() {
		return periciaEvidenciaDao;
	}


	public void setPericiaEvidenciaDao(PericiaEvidenciaDao periciaEvidenciaDao) {
		this.periciaEvidenciaDao = periciaEvidenciaDao;
	}


	public EvidenciaDispositivoTecnologicoImeiDao getImeiDao() {
		return imeiDao;
	}


	public void setImeiDao(EvidenciaDispositivoTecnologicoImeiDao imeiDao) {
		this.imeiDao = imeiDao;
	}


	
	}
	
	
		
		

	