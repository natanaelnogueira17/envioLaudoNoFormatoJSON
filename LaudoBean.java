package br.gov.ce.pefoce.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.protocol.HTTP;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.odftoolkit.odfdom.dom.element.style.StyleStyleElement;
import org.odftoolkit.odfdom.dom.element.style.StyleTextPropertiesElement;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeAutomaticStyles;
//import org.odftoolkit.odfdom.converter.pdf.PdfConverter;
//import org.odftoolkit.odfdom.converter.pdf.PdfOptions;
//import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.type.Color;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.common.field.VariableField;
import org.odftoolkit.simple.draw.FrameRectangle;
import org.odftoolkit.simple.draw.FrameStyleHandler;
import org.odftoolkit.simple.draw.Image;
import org.odftoolkit.simple.draw.Textbox;
import org.odftoolkit.simple.style.Border;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.style.StyleTypeDefinitions.AnchorType;
import org.odftoolkit.simple.style.StyleTypeDefinitions.CellBordersType;
import org.odftoolkit.simple.style.StyleTypeDefinitions.FontStyle;
import org.odftoolkit.simple.style.StyleTypeDefinitions.HorizontalAlignmentType;
import org.odftoolkit.simple.style.StyleTypeDefinitions.SupportedLinearMeasure;
import org.odftoolkit.simple.style.StyleTypeDefinitions.VerticalAlignmentType;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.text.Paragraph;
import org.odftoolkit.simple.text.Section;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.UploadedFile;

import br.gov.ce.pefoce.auditoria.dao.AuditoriaLogDao;
import br.gov.ce.pefoce.enumerate.SetorEnum;
import br.gov.ce.pefoce.enumerate.TipoAcaoEnum;
import br.gov.ce.pefoce.enumerate.TipoAmostraEnum;
import br.gov.ce.pefoce.enumerate.TipoArmaEnum;
import br.gov.ce.pefoce.enumerate.TipoDocumentoAnexoEnum;
import br.gov.ce.pefoce.enumerate.TipoDocumentoPericialEnum;
import br.gov.ce.pefoce.enumerate.TipoEvidenciaContendoEmbalagemEnum;
import br.gov.ce.pefoce.enumerate.TipoEvidenciaEnum;
import br.gov.ce.pefoce.enumerate.TipoExameEnum;
import br.gov.ce.pefoce.enumerate.TipoFechamentoEnum;
import br.gov.ce.pefoce.enumerate.TipoKitEnum;
import br.gov.ce.pefoce.enumerate.TipoLaudoEnum;
import br.gov.ce.pefoce.enumerate.TipoMetodoAnaliticoEnum;
import br.gov.ce.pefoce.enumerate.TipoRespostaEnum;
import br.gov.ce.pefoce.enumerate.TipoRespostaLocalDaColetaEnum;
import br.gov.ce.pefoce.enumerate.TipoRespostaLocalNumeroDeSerieEnum;
import br.gov.ce.pefoce.enumerate.TipoRespostaLocalVistoriaLateralVeiculoEnum;
import br.gov.ce.pefoce.enumerate.TipoRespostaLocalVistoriaTercoEnum;
import br.gov.ce.pefoce.enumerate.TipoRespostaMaterialProjetilEnum;
import br.gov.ce.pefoce.enumerate.TipoResultadoAlcoolemiaEnum;
import br.gov.ce.pefoce.enumerate.TipoSistemaDeSegurancaDaArmaEnum;
import br.gov.ce.pefoce.enumerate.TipoSubstanciaEnum;
import br.gov.ce.pefoce.exception.DadosInvalidosException;
import br.gov.ce.pefoce.exception.DocumentoInvalidoException;
import br.gov.ce.pefoce.exception.LaudoJaExisteException;
import br.gov.ce.pefoce.exception.LaudoNaoAnexadoException;
import br.gov.ce.pefoce.exception.LaudoSemConclusaoException;
import br.gov.ce.pefoce.exception.LaudoSemDatasException;
import br.gov.ce.pefoce.exception.LaudoSemMetodologiaException;
import br.gov.ce.pefoce.exception.LaudoSemNotasException;
import br.gov.ce.pefoce.exception.LaudoSemQuesitosException;
import br.gov.ce.pefoce.exception.ProcedimentoNaoRecebidoNoSIPException;
import br.gov.ce.pefoce.exception.SetorSemIDSIPException;
import br.gov.ce.pefoce.pericia.dao.BlocoDao;
import br.gov.ce.pefoce.pericia.dao.ConclusaoDao;
import br.gov.ce.pefoce.pericia.dao.DeclaracaoConsentimentoArquivoDao;
import br.gov.ce.pefoce.pericia.dao.DeclaracaoConsentimentoDao;
import br.gov.ce.pefoce.pericia.dao.DocumentoProcedimentoPericialDao;
import br.gov.ce.pefoce.pericia.dao.EvidenciaArmamentoDao;
import br.gov.ce.pefoce.pericia.dao.EvidenciaDispositivoTecnologicoDao;
import br.gov.ce.pefoce.pericia.dao.EvidenciaDispositivoTecnologicoImeiDao;
import br.gov.ce.pefoce.pericia.dao.EvidenciaDocumentoDao;
import br.gov.ce.pefoce.pericia.dao.EvidenciaEnvolvidoPessoaDao;
import br.gov.ce.pefoce.pericia.dao.EvidenciaFotoDao;
import br.gov.ce.pefoce.pericia.dao.EvidenciaMaterialDao;
import br.gov.ce.pefoce.pericia.dao.EvidenciaObjetoDao;
import br.gov.ce.pefoce.pericia.dao.EvidenciaVeiculoDao;
import br.gov.ce.pefoce.pericia.dao.ExameSolicitacaoDao;
import br.gov.ce.pefoce.pericia.dao.HistoricoProcedimentoSIPDao;
import br.gov.ce.pefoce.pericia.dao.LaudoAnexoDao;
import br.gov.ce.pefoce.pericia.dao.LaudoConclusaoDao;
import br.gov.ce.pefoce.pericia.dao.LaudoDao;
import br.gov.ce.pefoce.pericia.dao.LaudoFotoDao;
import br.gov.ce.pefoce.pericia.dao.LaudoMetodologiaDao;
import br.gov.ce.pefoce.pericia.dao.LaudoModeloAnexoDao;
import br.gov.ce.pefoce.pericia.dao.LaudoModeloConclusaoDao;
import br.gov.ce.pefoce.pericia.dao.LaudoModeloDao;
import br.gov.ce.pefoce.pericia.dao.LaudoModeloMetodologiaDao;
import br.gov.ce.pefoce.pericia.dao.LaudoModeloNotasDao;
import br.gov.ce.pefoce.pericia.dao.LaudoModeloQuesitoDao;
import br.gov.ce.pefoce.pericia.dao.LaudoModeloTipoExameSetorDao;
import br.gov.ce.pefoce.pericia.dao.LaudoNotasDao;
import br.gov.ce.pefoce.pericia.dao.LaudoParecerDao;
import br.gov.ce.pefoce.pericia.dao.LaudoQuesitoDao;
import br.gov.ce.pefoce.pericia.dao.MetodologiaDao;
import br.gov.ce.pefoce.pericia.dao.PericiaDao;
import br.gov.ce.pefoce.pericia.dao.PericiaEvidenciaDao;
import br.gov.ce.pefoce.pericia.dao.PericiaLaudoDao;
import br.gov.ce.pefoce.pericia.dao.PessoaDocumentoDao;
import br.gov.ce.pefoce.pericia.dao.PessoaLesaoDao;
import br.gov.ce.pefoce.pericia.dao.ProcedimentoSolicitacaoOcorrenciaDao;
import br.gov.ce.pefoce.pericia.dao.QuesitoDao;
import br.gov.ce.pefoce.pericia.dao.QuesitoRespostaPadraoDao;
import br.gov.ce.pefoce.pericia.dao.RespostaPadraoDao;
import br.gov.ce.pefoce.pericia.dao.SolicitacaoEvidenciaDao;
import br.gov.ce.pefoce.pericia.dao.SolicitacaoProcedimentoPericialDao;
import br.gov.ce.pefoce.pericia.dao.SolicitacaoProcedimentoPericialFormularioDao;
import br.gov.ce.pefoce.pericia.dao.SolicitacaoResultadoDao;
import br.gov.ce.pefoce.pericia.dao.SolicitacaoTramitacaoDao;
import br.gov.ce.pefoce.pericia.dao.UsuarioLacreCustodiaDao;
import br.gov.ce.pefoce.pericia.entity.Bloco;
import br.gov.ce.pefoce.pericia.entity.Conclusao;
import br.gov.ce.pefoce.pericia.entity.DeclaracaoConsentimento;
import br.gov.ce.pefoce.pericia.entity.DocumentoProcedimentoPericial;
import br.gov.ce.pefoce.pericia.entity.EvidenciaArmamento;
import br.gov.ce.pefoce.pericia.entity.EvidenciaDispositivoTecnologico;
import br.gov.ce.pefoce.pericia.entity.EvidenciaDispositivoTecnologicoImei;
import br.gov.ce.pefoce.pericia.entity.EvidenciaDocumento;
import br.gov.ce.pefoce.pericia.entity.EvidenciaEnvolvidoPessoa;
import br.gov.ce.pefoce.pericia.entity.EvidenciaFoto;
import br.gov.ce.pefoce.pericia.entity.EvidenciaMaterial;
import br.gov.ce.pefoce.pericia.entity.EvidenciaObjeto;
import br.gov.ce.pefoce.pericia.entity.EvidenciaVeiculo;
import br.gov.ce.pefoce.pericia.entity.ExameSolicitacao;
import br.gov.ce.pefoce.pericia.entity.HistoricoProcedimentoSIP;
import br.gov.ce.pefoce.pericia.entity.Laudo;
import br.gov.ce.pefoce.pericia.entity.LaudoAnexo;
import br.gov.ce.pefoce.pericia.entity.LaudoConclusao;
import br.gov.ce.pefoce.pericia.entity.LaudoFoto;
import br.gov.ce.pefoce.pericia.entity.LaudoMetodologia;
import br.gov.ce.pefoce.pericia.entity.LaudoModeloConclusao;
import br.gov.ce.pefoce.pericia.entity.LaudoModeloMetodologia;
import br.gov.ce.pefoce.pericia.entity.LaudoModeloNotas;
import br.gov.ce.pefoce.pericia.entity.LaudoModeloQuesito;
import br.gov.ce.pefoce.pericia.entity.LaudoModeloTipoExameSetor;
import br.gov.ce.pefoce.pericia.entity.LaudoNotas;
import br.gov.ce.pefoce.pericia.entity.LaudoParecer;
import br.gov.ce.pefoce.pericia.entity.LaudoQuesito;
import br.gov.ce.pefoce.pericia.entity.Metodologia;
import br.gov.ce.pefoce.pericia.entity.Pericia;
import br.gov.ce.pefoce.pericia.entity.PericiaEvidencia;
import br.gov.ce.pefoce.pericia.entity.PericiaLaudo;
import br.gov.ce.pefoce.pericia.entity.PessoaDocumento;
import br.gov.ce.pefoce.pericia.entity.PessoaLesao;
import br.gov.ce.pefoce.pericia.entity.ProcedimentoSolicitacaoOcorrencia;
import br.gov.ce.pefoce.pericia.entity.Quesito;
import br.gov.ce.pefoce.pericia.entity.QuesitoRespostaPadrao;
import br.gov.ce.pefoce.pericia.entity.RespostaPadrao;
import br.gov.ce.pefoce.pericia.entity.SolicitacaoEvidencia;
import br.gov.ce.pefoce.pericia.entity.SolicitacaoProcedimentoPericial;
import br.gov.ce.pefoce.pericia.entity.SolicitacaoProcedimentoPericialFormulario;
import br.gov.ce.pefoce.pericia.entity.SolicitacaoTramitacao;
import br.gov.ce.pefoce.pericia.util.EvidenciaUtil;
import br.gov.ce.pefoce.sistema.dao.ConfiguracaoUsuarioDao;
import br.gov.ce.pefoce.sistema.dao.SetorDao;
import br.gov.ce.pefoce.sistema.dao.TokenDao;
import br.gov.ce.pefoce.sistema.dao.UsuarioDao;
import br.gov.ce.pefoce.sistema.entity.ConfiguracaoUsuario;
import br.gov.ce.pefoce.sistema.entity.Usuario;
import br.gov.ce.pefoce.util.FacesUtils;
import br.gov.ce.pefoce.util.ImageUtil;
import br.gov.ce.pefoce.util.StrUtil;

@ManagedBean
@ViewScoped
public class LaudoBean extends AbstractBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public String getPerguntaCOVID19() {
		return "O periciando refere sintomas de síndrome gripal aguda (febre, tosse, dor na garganta, dificuldade de respirar...)?";
	}

	public String getPerguntaPatologias() {
		return "O periciando refere tratamento contínuo ou ser portador de patologias crônicas (Diabetes, Hipertensão, Cardiopatia, Doença Renal, Doença Respiratória, Neoplasia, Doença autoimune, Imunossupressão, Tuberculose, HIV/SIDA...)?";
	}

	public String getPerguntaDocumentoMedico() {
		return "O periciando porta algum documento médico relativo a patologias crônicas ou atuais (receita médica, atestado, relatório...)?";
	}

	private SolicitacaoProcedimentoPericial solicitacaoProcedimentoPericial;
	private SolicitacaoProcedimentoPericial solicitacao;
//	private SolicitacaoResultado resultadosAlcoolemia;

	private Laudo laudo;
	private Pericia pericia;
//	private Pericia periciaResultados;
	private LaudoModeloMetodologia laudoModeloMetodologiaSelecionada;
	private LaudoModeloConclusao laudoModeloConclusaoSelecionada;
	private LaudoModeloQuesito laudoModeloQuesitoSelecionado;
	private LaudoModeloNotas laudoModeloNotasSelecionadas;
	private LaudoParecer laudoParecer;
	private String parecer;
	private String descricaoLaudoFoto;
	private byte[] laudoFotoAnexada;
	private PericiaLaudo periciaLaudo;
	private UploadedFile laudoUpload;
	private String nomeArquivo;
	private LaudoAnexo ultimoLaudoAnexo;
	private DefaultStreamedContent downloadPDF;
	private DefaultStreamedContent downloadLaudoModelo;
	private TipoLaudoEnum tipoLaudo;
	private LaudoFoto laudoFoto;
	private QuesitoRespostaPadrao quesitoRespostaPadraoSelecionada;
	private RespostaPadrao respostaPadraoSelecionada;
	private Usuario peritoRevisorSelecionado;
	private QuesitoRespostaPadrao respostaQuesito;
	private PessoaDocumento pessoaDocumento;
	private EvidenciaEnvolvidoPessoa evidenciaEnvolvidoPessoa;
	private PessoaLesao pessoaLesao;
	private Integer contQuesito;
	private Integer contQuesitoBloco;
	private Bloco blocoAnterior;
	private Quesito quesitoExtra;
	private boolean possuiQuesitoExtra;
	private boolean inserindoQuesitoExtra;
	private String descricaoQuesitoExtra;
	private String ordemQuesitoExtra;
	private boolean possuiEsquemaCorporal;
	private boolean desejaVerTutorialLaudoTorturaIstambul;

	private List<LaudoModeloTipoExameSetor> listaLaudoModeloTipoExameSetor;
	private List<LaudoModeloMetodologia> listaLaudoModeloMetodologia;
	private List<LaudoModeloQuesito> listaLaudoModeloQuesito;
	private List<LaudoModeloConclusao> listaLaudoModeloConclusao;
	private List<LaudoModeloNotas> listaLaudoModeloNotas;
	private List<SolicitacaoEvidencia> listaSolicitacaoEvidenciaSelecionada;
	private List<SolicitacaoProcedimentoPericial> listaSolicitacaoProcedimentoPericial;
	private List<PessoaLesao> listaPessoaLesao;

	@ManagedProperty("#{laudoModeloDao}")
	private LaudoModeloDao laudoModeloDao;

	@ManagedProperty("#{laudoModeloTipoExameSetorDao}")
	private LaudoModeloTipoExameSetorDao laudoModeloTipoExameSetorDao;

	@ManagedProperty("#{laudoModeloConclusaoDao}")
	private LaudoModeloConclusaoDao laudoModeloConclusaoDao;

	@ManagedProperty("#{laudoModeloNotasDao}")
	private LaudoModeloNotasDao laudoModeloNotasDao;

	@ManagedProperty("#{laudoModeloMetodologiaDao}")
	private LaudoModeloMetodologiaDao laudoModeloMetodologiaDao;

	@ManagedProperty("#{laudoModeloQuesitoDao}")
	private LaudoModeloQuesitoDao laudoModeloQuesitoDao;

	@ManagedProperty("#{exameSolicitacaoDao}")
	private ExameSolicitacaoDao exameSolicitacaoDao;

	@ManagedProperty("#{solicitacaoEvidenciaDao}")
	private SolicitacaoEvidenciaDao solicitacaoEvidenciaDao;

	@ManagedProperty("#{periciaDao}")
	private PericiaDao periciaDao;

	@ManagedProperty("#{laudoDao}")
	private LaudoDao laudoDao;

	@ManagedProperty("#{laudoModeloAnexoDao}")
	private LaudoModeloAnexoDao laudoModeloAnexoDao;

	@ManagedProperty("#{laudoMetodologiaDao}")
	private LaudoMetodologiaDao laudoMetodologiaDao;

	@ManagedProperty("#{laudoQuesitoDao}")
	private LaudoQuesitoDao laudoQuesitoDao;

	@ManagedProperty("#{laudoConclusaoDao}")
	private LaudoConclusaoDao laudoConclusaoDao;

	@ManagedProperty("#{laudoNotasDao}")
	private LaudoNotasDao laudoNotasDao;

	@ManagedProperty("#{laudoParecerDao}")
	private LaudoParecerDao laudoParecerDao;

	@ManagedProperty("#{laudoFotoDao}")
	private LaudoFotoDao laudoFotoDao;

	@ManagedProperty("#{evidenciaEnvolvidoPessoaDao}")
	private EvidenciaEnvolvidoPessoaDao evidenciaEnvolvidoPessoaDao;

	@ManagedProperty("#{historicoProcedimentoSIPDao}")
	private HistoricoProcedimentoSIPDao historicoProcedimentoSIPDao;

	@ManagedProperty("#{periciaEvidenciaDao}")
	private PericiaEvidenciaDao periciaEvidenciaDao;

	@ManagedProperty("#{usuarioDao}")
	private UsuarioDao usuarioDao;

	@ManagedProperty("#{laudoAnexoDao}")
	private LaudoAnexoDao laudoAnexoDao;

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

	@ManagedProperty("#{evidenciaDispositivoTecnologicoImeiDao}")
	private EvidenciaDispositivoTecnologicoImeiDao evidenciaDispositivoTecnologicoImeiDao;

	@ManagedProperty("#{evidenciaFotoDao}")
	private EvidenciaFotoDao evidenciaFotoDao;

	@ManagedProperty("#{usuarioLacreCustodiaDao}")
	private UsuarioLacreCustodiaDao usuarioLacreCustodiaDao;

	@ManagedProperty("#{periciaLaudoDao}")
	private PericiaLaudoDao periciaLaudoDao;

	@ManagedProperty("#{solicitacaoTramitacaoDao}")
	private SolicitacaoTramitacaoDao solicitacaoTramitacaoDao;

	@ManagedProperty("#{documentoProcedimentoPericialDao}")
	private DocumentoProcedimentoPericialDao documentoProcedimentoPericialDao;

	@ManagedProperty("#{auditoriaLogDao}")
	private AuditoriaLogDao auditoriaLogDao;

	@ManagedProperty("#{configuracaoUsuarioDao}")
	ConfiguracaoUsuarioDao configuracaoUsuarioDao;

	@ManagedProperty("#{tokenDao}")
	TokenDao tokenDao;

	@ManagedProperty("#{metodologiaDao}")
	MetodologiaDao metodologiaDao;

	@ManagedProperty("#{conclusaoDao}")
	ConclusaoDao conclusaoDao;

	@ManagedProperty("#{respostaPadraoDao}")
	RespostaPadraoDao respostaPadraoDao;

	@ManagedProperty("#{blocoDao}")
	BlocoDao blocoDao;
	
	@ManagedProperty("#{quesitoDao}")
	QuesitoDao quesitoDao;

	@ManagedProperty("#{quesitoRespostaPadraoDao}")
	QuesitoRespostaPadraoDao quesitoRespostaPadraoDao;

	@ManagedProperty("#{solicitacaoProcedimentoPericialFormularioDao}")
	private SolicitacaoProcedimentoPericialFormularioDao solicitacaoProcedimentoFormularioDao;

	@ManagedProperty("#{solicitacaoResultadoDao}")
	private SolicitacaoResultadoDao solicitacaoResultadoDao;

	@ManagedProperty("#{procedimentoSolicitacaoOcorrenciaDao}")
	private ProcedimentoSolicitacaoOcorrenciaDao procedimentoSolicitacaoOcorrenciaDao;

	@ManagedProperty("#{pessoaDocumentoDao}")
	private PessoaDocumentoDao pessoaDocumentoDao;

	@ManagedProperty("#{pessoaLesaoDao}")
	private PessoaLesaoDao pessoaLesaoDao;

	@ManagedProperty("#{setorDao}")
	private SetorDao setorDao;

	@ManagedProperty("#{solicitacaoProcedimentoPericialDao}")
	private SolicitacaoProcedimentoPericialDao solicitacaoProcedimentoPericialDao;
	
	@ManagedProperty("#{declaracaoConsentimentoDao}")
	private DeclaracaoConsentimentoDao declaracaoConsentimentoDao;
	
	@ManagedProperty("#{declaracaoConsentimentoArquivoDao}")
	private DeclaracaoConsentimentoArquivoDao declaracaoConsentimentoArquivoDao;

	@PostConstruct
	public void init() {
		setBrowsing(true);
		contQuesito = 1;
		contQuesitoBloco = 1;
		possuiQuesitoExtra = false;
	}

	public void visualizaLaudos(SolicitacaoProcedimentoPericial solicitacaoProcedimentoPericial, TipoLaudoEnum tipoLaudo) {
		try {
			setBrowsing(true);
			this.tipoLaudo = tipoLaudo;
			this.solicitacaoProcedimentoPericial = solicitacaoProcedimentoPericial;
			this.solicitacaoProcedimentoPericial.setListaSolicitacaoProcedimentoPericial(null);
			this.solicitacaoProcedimentoPericial.setListaSolicitacaoEvidencia(solicitacaoEvidenciaDao.buscarEvidenciasPorSolicitacao(this.solicitacaoProcedimentoPericial));
			this.solicitacaoProcedimentoPericial.setUltimoExameSolicitacao(exameSolicitacaoDao.buscarUltimoExamePorSolicitacao(solicitacaoProcedimentoPericial));
			this.pericia = periciaDao.buscaUltimaPericiaPorSolicitacao(solicitacaoProcedimentoPericial);
			this.pericia.setListPericiaLaudo(periciaDao.buscarPericiaLaudoPorPericia(this.pericia));
			this.pericia.setListPericiaEvidencia(periciaEvidenciaDao.buscarPericiaEvidenciaPorPericia(this.pericia));

			if (this.solicitacaoProcedimentoPericial.getListaSolicitacaoEvidencia() != null && !this.solicitacaoProcedimentoPericial.getListaSolicitacaoEvidencia().isEmpty()) {
				for (SolicitacaoEvidencia soe : this.solicitacaoProcedimentoPericial.getListaSolicitacaoEvidencia()) {
					if (this.pericia.getListPericiaEvidencia() == null)
						this.pericia.setListPericiaEvidencia(new ArrayList<PericiaEvidencia>());

					boolean cadastrar = true;
					for (PericiaEvidencia pee : this.pericia.getListPericiaEvidencia()) {
						if (soe.getEvidencia().getId().equals(pee.getEvidencia().getId())) {
							cadastrar = false;
						}
					}

					if (cadastrar) {
						PericiaEvidencia periciaEvidencia = new PericiaEvidencia();
						periciaEvidencia.setEvidencia(soe.getEvidencia());
						periciaEvidencia.setDataInclusao(soe.getDataInclusao());
						periciaEvidencia.setPericia(this.pericia);
						periciaEvidencia.setUsuario(getUsuarioAutenticado());
						periciaEvidencia.setUtilizaNoLaudo(false);
						periciaEvidencia = periciaEvidenciaDao.save(periciaEvidencia);

						this.pericia.getListPericiaEvidencia().add(periciaEvidencia);
					}
				}
			}
			EvidenciaUtil.gerarResumoEvidencia(this.pericia.getListPericiaEvidencia());

			for (PericiaLaudo pl : this.pericia.getListPericiaLaudo()) {
				pl.getLaudo().setUltimoLaudoAnexo(laudoAnexoDao.buscarUltimoLaudoAnexoPorLaudo(pl.getLaudo()));
				pl.getLaudo().setListaLaudoConclusao(null);
				pl.getLaudo().setListaLaudoNotas(null);
				pl.getLaudo().setListaLaudoMetodologia(null);
				pl.getLaudo().setListaLaudoQuesito(null);
				pl.getLaudo().setUltimoLaudoParecer(null);

			}
			exibirDialogo("modalPericiaLaudoDlg");
		} catch (Exception e) {
			FacesUtils.addErrorMessage("Ocorreu um erro ao transportar Vestígios/Pessoas da solicitação para a Perícia!");
			e.printStackTrace();
		}
	}

	/**
	 * Verifica se o laudo foi assinado.
	 * 
	 * @param periciaLaudo
	 * @return
	 */
	public boolean possuiLaudoAnexoAssinado(PericiaLaudo periciaLaudo) {
		if (!periciaLaudo.isPossuiLaudoAnexoAssinado())
			periciaLaudo.setPossuiLaudoAnexoAssinado(laudoAnexoDao.possuiLaudoAnexoAssinado(periciaLaudo.getLaudo()));

		return periciaLaudo.isPossuiLaudoAnexoAssinado();
	}

	/**
	 * Verifica se o laudo foi assinado na perícia.
	 * 
	 * @param periciaLaudo
	 * @return
	 */
	public boolean possuiLaudoAssinado(Pericia pericia) {

		Laudo laudo = laudoDao.buscarUltimoLaudoPorPericiaResumido(pericia);
		if (laudo != null)
			return laudoAnexoDao.possuiLaudoAnexoAssinado(laudo);

		return false;
	}

	/**
	 * Verifica se o laudo é o último feito pelo perito.
	 * 
	 * @param periciaLaudo
	 * @return
	 */
	public boolean isUltimoLaudoValido(PericiaLaudo periciaLaudo) {

		return periciaLaudo.getId().equals(periciaLaudoDao.buscarUltimaPericiaLaudo(periciaLaudo.getPericia()).getId());
	}

	public void prepararInclusaoLaudo() {
		setInserting(true);
		laudo = new Laudo();

		if (solicitacaoProcedimentoPericial.getSetor().isNecropapiloscopia()) {
			laudo.setTipoLaudo(TipoLaudoEnum.INFORMATIVO_TECNICO);
		} else {
			laudo.setTipoLaudo(TipoLaudoEnum.ORIGINAL);
		}
		laudo.setUsuario(getUsuarioAutenticado());

		periciaLaudo = new PericiaLaudo();
		periciaLaudo.setPericia(pericia);
		periciaLaudo.setLaudo(laudo);
		periciaLaudo.setUsuario(getUsuarioAutenticado());

		laudoModeloMetodologiaSelecionada = null;
		laudoModeloQuesitoSelecionado = null;
		laudoModeloConclusaoSelecionada = null;
		laudoModeloNotasSelecionadas = null;
		laudoUpload = null;
		nomeArquivo = null;
		listaLaudoModeloTipoExameSetor = laudoModeloTipoExameSetorDao.buscarPorExame(solicitacaoProcedimentoPericial.getUltimoExameSolicitacao().getTipoExameSetor());
		adicionarNotasPadroesNoLaudo();

	}

	/**
	 * Carrega as informações do Laudo para novo laudo do tipo Aditamento.
	 * 
	 * @throws JSONException
	 */
	public void prepararAditamento() throws JSONException {
		periciaLaudo = periciaLaudoDao.buscarUltimaPericiaLaudo(pericia);
		preparaAlteracao(periciaLaudo);

		periciaLaudo.getLaudo().setUsuario(getUsuarioAutenticado());
		periciaLaudo.getLaudo().setTipoLaudo(TipoLaudoEnum.ADITAMENTO);
		periciaLaudo.setUsuario(getUsuarioAutenticado());

		laudo.setListaLaudoAnexo(new ArrayList<LaudoAnexo>());
		laudo.setUltimoLaudoAnexo(laudoAnexoDao.buscarUltimolaudoAnexoODT(periciaLaudo));
		periciaLaudo.setId(null);
		laudo.setId(null);
		if (laudo.getUltimoLaudoParecer() != null)
			laudo.getUltimoLaudoParecer().setId(null);
		laudo.setListaLaudoParecer(null);

		for (LaudoMetodologia l : laudo.getListaLaudoMetodologia()) {
			l.setId(null);
		}

		for (LaudoQuesito l : laudo.getListaLaudoQuesito()) {
			l.setId(null);
		}

		for (LaudoConclusao l : laudo.getListaLaudoConclusao()) {
			l.setId(null);
		}

		for (LaudoNotas l : laudo.getListaLaudoNotas()) {
			l.setId(null);
		}

		for (LaudoFoto l : laudo.getListaLaudoFoto()) {
			l.setId(null);
		}
	}

	public void testarBotao() {
		FacesUtils.addErrorMessage("Enviou!");
	}

	public void downloadTeste() {
		FacesUtils.addInfoMessage("Deu certo!");
	}

	/**
	 * Prepara alteração do laudo
	 * 
	 * @param periciaLaudo
	 * @throws JSONException
	 */
	public void preparaAlteracao(PericiaLaudo periciaLaudo) throws JSONException {
		setUpdating(true);
		this.periciaLaudo = periciaLaudo;
		this.laudo = periciaLaudo.getLaudo();
		this.laudo.setListaLaudoMetodologia(laudoMetodologiaDao.buscarPorLaudo(this.laudo));
		//Quando inicializa e já existem metodologias selecionadas, elas devem ser formatadas para apresentar resposta dinâmica
		for(LaudoMetodologia metodologia : this.laudo.getListaLaudoMetodologia()) {
			formatarRepostaDinamicaNoModelo(metodologia);
		}
		this.laudo.setListaLaudoQuesito(laudoQuesitoDao.buscarPorLaudo(this.laudo));
		ordenarListaLaudoQuesito(this.laudo.getListaLaudoQuesito());
		if(laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.TORTURA.getId())
				|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.TORTURA_EM_FLAGRANTE.getId())) {
			Bloco blocoCinco = blocoDao.find(5);
			Quesito quesitoBlocoCinco = quesitoDao.find(108);
			for (LaudoQuesito lq : this.laudo.getListaLaudoQuesito()) {
				if(lq.getQuesito().getBloco().equals(blocoCinco) && !lq.getQuesito().equals(quesitoBlocoCinco))
					possuiQuesitoExtra = true;
			}
		}
		this.laudo.setListaLaudoConclusao(laudoConclusaoDao.buscarPorLaudo(this.laudo));
		this.laudo.setListaLaudoNotas(laudoNotasDao.buscarPorLaudo(this.laudo));
		this.laudo.setListaLaudoFoto(laudoFotoDao.buscarPorLaudo(this.laudo));
		this.laudo.setListaLaudoParecer(laudoParecerDao.buscarPorLaudo(this.laudo));
		this.laudo.setListaLaudoAnexo(laudoAnexoDao.buscarListaLaudoAnexoPorLaudo(this.laudo));
		this.laudo.setUltimoLaudoAnexo(laudoAnexoDao.buscarUltimolaudoAnexoODT(this.laudo));
		this.laudo.setUltimoLaudoParecer(laudoParecerDao.buscarUltimoPorLaudo(this.laudo));
		if (this.laudo.getUltimoLaudoParecer() == null) {
			this.laudo.setUltimoLaudoParecer(new LaudoParecer());
			this.laudo.getUltimoLaudoParecer().setUsuario(getUsuarioAutenticado());
			this.laudo.getUltimoLaudoParecer().setDataInclusao(new Date());
			this.laudo.getUltimoLaudoParecer().setLaudo(this.laudo);
		}
		this.laudo.setListPericiaLaudo(null);
		this.listaLaudoModeloTipoExameSetor = laudoModeloTipoExameSetorDao.buscarPorExame(solicitacaoProcedimentoPericial.getUltimoExameSolicitacao().getTipoExameSetor());
		adicionarRespostaDinamicaNoModelo();
		carregarListasModelo();

		laudoModeloMetodologiaSelecionada = null;
		laudoModeloQuesitoSelecionado = null;
		laudoModeloConclusaoSelecionada = null;
		laudoModeloNotasSelecionadas = null;

		laudoUpload = null;
		nomeArquivo = null;
	}

	public void adicionarMetodologiaPadraoNoLaudo() {

//		if (isInserting()) {
		laudo.setListaLaudoMetodologia(new ArrayList<LaudoMetodologia>());
		if (listaLaudoModeloMetodologia != null) {
			for (LaudoModeloMetodologia metodologiaPadrao : listaLaudoModeloMetodologia) {
				if (metodologiaPadrao.isMetodologiaPadrao()) {
					LaudoMetodologia laudoMetodologia = new LaudoMetodologia();
					laudoMetodologia.setLaudo(laudo);
					laudoMetodologia.setUsuario(getUsuarioAutenticado());
					laudoMetodologia.setMetodologia(metodologiaPadrao.getMetodologia());

					laudo.getListaLaudoMetodologia().add(laudoMetodologia);
				}
			}
		}
//		}
	}

	public void adicionarConclusaoPadraoNoLaudo() {

		laudo.setListaLaudoConclusao(new ArrayList<LaudoConclusao>());
		if (listaLaudoModeloConclusao != null) {
			Iterator<LaudoModeloConclusao> iterator = listaLaudoModeloConclusao.iterator();

			while (iterator.hasNext()) {
				LaudoModeloConclusao conclusaoPadrao = iterator.next();
				if (conclusaoPadrao.isConclusaoPadrao()) {
					LaudoConclusao laudoConclusao = new LaudoConclusao();
					laudoConclusao.setLaudo(laudo);
					laudoConclusao.setUsuario(getUsuarioAutenticado());
					laudoConclusao.setConclusao(conclusaoPadrao.getConclusao());

					laudo.getListaLaudoConclusao().add(laudoConclusao);
					iterator.remove();
				}
			}
		}
	}

	public void adicionarNotasPadroesNoLaudo() {
		laudo.setListaLaudoNotas(new ArrayList<LaudoNotas>());

		if (listaLaudoModeloNotas != null) {
			Iterator<LaudoModeloNotas> iterator = listaLaudoModeloNotas.iterator();

			while (iterator.hasNext()) {
				LaudoModeloNotas notasPadrao = iterator.next();
				if (notasPadrao.isNotasPadrao()) {
					LaudoNotas laudoNotas = new LaudoNotas();
					laudoNotas.setLaudo(laudo);
					laudoNotas.setUsuario(getUsuarioAutenticado());
					laudoNotas.setNotas(notasPadrao.getNotas());

					laudo.getListaLaudoNotas().add(laudoNotas);
					iterator.remove();
				}
			}
		}
	}
	
	//Este método deve substituir campos genéricos do modelo por respostas dinâmicas baseadas na perícia.
	public void formatarRepostaDinamicaNoModelo(LaudoMetodologia laudoMetodologia) throws JSONException {
		String m1 = laudoMetodologia.getMetodologia().getDescricao();
		if(m1 != null) {
			m1 = m1.replace("[:sensibilidadeAnalitica]", pericia.getSensibilidadeAnalitica() != null ? pericia.getSensibilidadeAnalitica().toString() : "");
		}
		laudoMetodologia.getMetodologia().setDescricao(m1);
	}

	public void adicionarRespostaDinamicaNoModelo() throws JSONException {

		if (laudo.getLaudoModeloTipoExameSetor() != null) {
//			laudo.setListaLaudoConclusao(null);
//			laudo.setListaLaudoMetodologia(null);

//			carregarListasModelo();
//			adicionarMetodologiaPadraoNoLaudo();
//			adicionarConclusaoPadraoNoLaudo();

			// Adiciona respostas dinamicas na conclusão
			List<LaudoModeloConclusao> listaLaudoModeloConclusaoPadraoASerRemovido = new ArrayList<LaudoModeloConclusao>();

			if (listaLaudoModeloConclusao != null) {

				for (LaudoModeloConclusao lmc : listaLaudoModeloConclusao) {
					laudoModeloConclusaoSelecionada = lmc;
					LaudoConclusao laudoConclusao = new LaudoConclusao();
					laudoConclusao.setLaudo(laudo);
					laudoConclusao.setUsuario(getUsuarioAutenticado());
					laudoConclusao.setConclusao(laudoModeloConclusaoSelecionada.getConclusao());

					if (lmc.getConclusao() != null && lmc.getConclusao().getDescricao() != null && !lmc.getConclusao().getDescricao().isEmpty()) {

						Conclusao conc = conclusaoDao.buscarConclusaoPadrao(lmc);

						if (conc != null && conc.getDescricao() != null && !conc.getDescricao().isEmpty()) {

							String c1 = "";
							c1 = conc.getDescricao();

							// SUBSTITUO AS VARIAVEIS COM REPLACE
							c1 = c1.replace("[:valorDetectado]", pericia.getValorDetectado() != null ? pericia.getValorDetectado() : "");
							
							String valorEstimado = null;
							if(pericia.getValorDetectado() != null && !pericia.getValorDetectado().isEmpty()) {
								String stringFormatada = pericia.getValorDetectado();
								stringFormatada = stringFormatada.replace(",", ".");
								double valEst = Double.parseDouble(stringFormatada) / 1.20;
								valorEstimado = String.format("%.1f", valEst) + " dg/L";
							}
							c1 = c1.replace("[:valorEstimado]", valorEstimado != null ? valorEstimado : "");
							
							laudoConclusao.getConclusao().setDescricao(c1);

							List<PericiaEvidencia> listaEnvolvidoPessoa = new ArrayList<PericiaEvidencia>();
							String cadaver = "";
							int numeroCadaver = 0;
							listaEnvolvidoPessoa = periciaEvidenciaDao.buscarQuantidadeEnvolvidosUtilizadoNoLaudo(pericia);

							// SE NA CONCLUSÃO LAUDO HOMÍCIDIO HOUVER UMA VÍTIMA
							if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_VIDA_HOMICIDIO.getId())) {
								for (PericiaEvidencia periciaEvidenciaPessoa : pericia.getListPericiaEvidencia()) {
									if (periciaEvidenciaPessoa.isUtilizaNoLaudo()
											&& periciaEvidenciaPessoa.getEvidencia().getTipoEvidencia().getId().equals(TipoEvidenciaEnum.PESSOA_ENVOLVIDA.getId())) {
										c1 = conc.getDescricao();

										if (listaEnvolvidoPessoa.size() > 1) {
											cadaver = cadaver + ("\nCADÁVER " + (StrUtil.lpad(Integer.toString(++numeroCadaver), 2, '0') + "\n"));
										}

										c1 = c1.replace("[:nomeVitima]",
												!periciaEvidenciaPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().isDesconhecido()
														&& periciaEvidenciaPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getNome() != null
																? periciaEvidenciaPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getNome()
																: " ");
										if (!periciaEvidenciaPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().isDesconhecido()) {
											for (PessoaDocumento evidenciaPessoaDocumento : periciaEvidenciaPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getListaPessoaDocumento()) {
												c1 = c1.replace("[:rgEnvolvido]", evidenciaPessoaDocumento.getPessoaTipoDocumento() != null ? evidenciaPessoaDocumento.getNumero() : " ");
											}
										}

										c1 = c1.replace("[:tipoInstrumento]",
												periciaEvidenciaPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoInstrumento() != null
														? periciaEvidenciaPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoInstrumento().getDescricao().toLowerCase()
														: " ");

										cadaver = cadaver + c1;
									}
								} // Seta conclusao dinâmica do Modelo de laudo de homicidio da NUPEX
								laudoConclusao.getConclusao().setDescricao(cadaver);
							}

							if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_DROGAS_EM_SANGUE.getId())
									|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_DROGAS_EM_URINA.getId())) {
								String substancia = "";
								if (pericia.isConfirmatorio()) {
									JSONObject json2 = new JSONObject(pericia.getSolicitacaoResultadoConfirmatorio());

									String s = "";
									List<String> listaSubstancias = new ArrayList<String>();

									@SuppressWarnings("unchecked")
									Iterator<String> iter2 = json2.keys();

									if (iter2 != null && iter2.hasNext()) {
										String key = iter2.next();
										for (int i = 0; i < ((JSONArray) json2.get(key)).length(); ++i) {
											String descricaoSubstancia = "";
											String resultado = "";

											descricaoSubstancia += ((JSONObject) ((JSONArray) json2.get(key)).get(i)).get("descricaoClasseSubstancia");
											resultado += ((JSONObject) ((JSONArray) json2.get(key)).get(i)).get("resultado");

											if (resultado.equals("CONFIRMADO")) {
												listaSubstancias.add(descricaoSubstancia);
											}
										}

										for (String str : listaSubstancias) {
											s += (str + ", ");
										}

										c1 = c1.replace("[:tipoSubstanciaConfirmatorio]", s);
										c1 = c1.replace(", .", ".");

										substancia = substancia + c1;
									}
								}
							}
							// Seta conclusao dinâmica do modelo de laudo de Alcoolemia NUTOF
							break;
						} else {
							laudoConclusao.setDescricao(lmc.getConclusao().getDescricao());
						}
					}
					if (laudo.getListaLaudoConclusao() != null)
						laudo.setListaLaudoConclusao(new ArrayList<LaudoConclusao>());

					if (laudoModeloConclusaoSelecionada.isConclusaoPadrao()) {
						laudo.getListaLaudoConclusao().add(laudoConclusao);
						listaLaudoModeloConclusaoPadraoASerRemovido.add(laudoModeloConclusaoSelecionada);

					}
				}
				laudoModeloConclusaoSelecionada = null;
			}

			if (laudo.getListaLaudoConclusao() == null) {
				laudo.setListaLaudoConclusao(new ArrayList<LaudoConclusao>());
			}

			List<LaudoModeloMetodologia> listaLaudoModeloMetodologiaPadraoASerRemovido = new ArrayList<LaudoModeloMetodologia>();

			if (listaLaudoModeloMetodologia != null) {

				for (LaudoModeloMetodologia lmm : listaLaudoModeloMetodologia) {
					laudoModeloMetodologiaSelecionada = lmm;
					LaudoMetodologia laudoMetodologia = new LaudoMetodologia();
					laudoMetodologia.setLaudo(laudo);
					laudoMetodologia.setUsuario(getUsuarioAutenticado());
					laudoMetodologia.setMetodologia(laudoModeloMetodologiaSelecionada.getMetodologia());

					if (lmm.getMetodologia() != null && lmm.getMetodologia().getDescricao() != null && !lmm.getMetodologia().getDescricao().isEmpty()) {

						Metodologia met = metodologiaDao.buscarMetodologiaPadrao(lmm);

						if (met != null && met.getDescricao() != null && !met.getDescricao().isEmpty()) {

							String m1 = "";
							m1 = met.getDescricao();

							// Substituo as variaveis com replace
							m1 = m1.replace("[:sensibilidadeAnalitica]", pericia.getSensibilidadeAnalitica() != null ? pericia.getSensibilidadeAnalitica().toString() : "");

							laudoMetodologia.getMetodologia().setDescricao(m1);

						} else {
							laudoMetodologia.setDescricao(lmm.getMetodologia().getDescricao());
						}
					}
					if (laudo.getListaLaudoMetodologia() == null)
						laudo.setListaLaudoMetodologia(new ArrayList<LaudoMetodologia>());

					if (laudoModeloMetodologiaSelecionada.isMetodologiaPadrao()) {
						laudo.getListaLaudoMetodologia().add(laudoMetodologia);
						listaLaudoModeloMetodologiaPadraoASerRemovido.add(laudoModeloMetodologiaSelecionada);
					}
				}
				laudoModeloMetodologiaSelecionada = null;
			}
			if (laudo.getListaLaudoMetodologia() == null) {
				laudo.setListaLaudoMetodologia(new ArrayList<LaudoMetodologia>());
			}
		}

	}

//	private void populaLaudoQuesito(List<Object> listaLaudoQuesito) {
//		for (Object lmq : listaLaudoQuesito) {
//				for (PericiaEvidencia periciaEvidencia : pericia.getListPericiaEvidencia()) {
//					if (periciaEvidencia.getEvidencia().getTipoEvidencia().getId().equals(TipoEvidenciaEnum.MATERIAL_QUIMICO_BIOLOGICO.getId())) {
//
//						String q1 = "";
//						q1 = qrp.getRespostaQuesito();
//
//						// Substituo as variaveis com replace
//						q1 = q1.replace("[:tipoAspectoSubstancia]",
//								periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoAspectoSubstancia() != null
//										? periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoAspectoSubstancia().getDescricao().toLowerCase()
//										: "");
//
//						q1 = q1.replace("[:cor]",
//								periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getCor() != null
//										? periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getCor().getDescricao().toLowerCase()
//										: "");
//
//						if (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().isSubstanciaPrensada())
//							q1 = q1.replace("[:isSubstanciaPrensada]", ", prensada");
//						else
//							q1 = q1.replace("[:isSubstanciaPrensada]", "");
//
//						if (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().isSubstanciaFragmentada())
//							q1 = q1.replace("[:isSubstanciaFragmentada]", ", fragmentada");
//						else
//							q1 = q1.replace("[:isSubstanciaFragmentada]", "");
//
//						q1 = q1.replace("[:tipoConstituido]",
//								periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoConstituido() != null
//										? ",  constituído por " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoConstituido().getDescricao().toLowerCase()
//										: "");
//
//						q1 = q1.replace("[:tipoConsistencia]",
//								periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoConsistencia() != null
//										? ", de consistência " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoConsistencia().getDescricao().toLowerCase()
//										: "");
//
//						laudoQuesito.getQuesito().setRespostaPadrao(qrp.getRespostaPadrao());
//						laudoQuesito.setResposta(q1);
//						break;
//					}
//				}
//			} else {
//				laudoQuesito.setResposta(lmq.getRespostaPadrao().getDescricao());
//
//			}
//
//	}

	public void selecionarModelo() throws JSONException {
		if (laudo.getLaudoModeloTipoExameSetor() != null) {
			laudo.setListaLaudoConclusao(null);
			laudo.setListaLaudoNotas(null);
			laudo.setListaLaudoMetodologia(null);
			laudo.setListaLaudoQuesito(null);
			laudo.setUltimoLaudoParecer(null);
			reiniciaContador();
			
			if (laudo.getLaudoModeloTipoExameSetor().getLaudoModelo().isPossuiParecer()) {
				laudo.setUltimoLaudoParecer(new LaudoParecer());
				laudo.getUltimoLaudoParecer().setLaudo(laudo);
				laudo.getUltimoLaudoParecer().setUsuario(getUsuarioAutenticado());
				if (laudo.getLaudoModeloTipoExameSetor().getLaudoModelo().getParecer() != null && !laudo.getLaudoModeloTipoExameSetor().getLaudoModelo().getParecer().isEmpty()) {
					laudo.getUltimoLaudoParecer().setDescricao(laudo.getLaudoModeloTipoExameSetor().getLaudoModelo().getParecer());
				}
			}

//			adicionarMetodologiaPadraoNoLaudo();
			carregarListasModelo();

			if (isInserting() && laudo.getTipoLaudo().equals(TipoLaudoEnum.ORIGINAL)) {

				List<LaudoModeloQuesito> listaLaudoModeloQuesitoPadraoASerRemovido = new ArrayList<LaudoModeloQuesito>();

				if (listaLaudoModeloQuesito != null) {

					for (LaudoModeloQuesito lmq : listaLaudoModeloQuesito) {
						laudoModeloQuesitoSelecionado = lmq;
						LaudoQuesito laudoQuesito = new LaudoQuesito();
						laudoQuesito.setLaudo(laudo);
						laudoQuesito.setUsuario(getUsuarioAutenticado());
						laudoQuesito.setQuesito(laudoModeloQuesitoSelecionado.getQuesito());

						if (lmq.getRespostaPadrao() != null && lmq.getRespostaPadrao().getDescricao() != null && !lmq.getRespostaPadrao().getDescricao().isEmpty()) {
							QuesitoRespostaPadrao qrp = quesitoRespostaPadraoDao.buscarRespostaPadrao(lmq);

							if (qrp != null && qrp.getRespostaQuesito() != null && !qrp.getRespostaQuesito().isEmpty()) {
								for (PericiaEvidencia periciaEvidencia : pericia.getListPericiaEvidencia()) {
									if (periciaEvidencia.getEvidencia().getTipoEvidencia().getId().equals(TipoEvidenciaEnum.MATERIAL_QUIMICO_BIOLOGICO.getId())) {

										String q1 = "";
										q1 = qrp.getRespostaQuesito();

										// Substituo as variaveis com replace
										q1 = q1.replace("[:tipoAspectoSubstancia]",
												periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoAspectoSubstancia() != null
														? periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoAspectoSubstancia().getDescricao().toLowerCase()
														: "");

										q1 = q1.replace("[:cor]",
												periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getCor() != null
														? periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getCor().getDescricao().toLowerCase()
														: "");

										if (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().isSubstanciaPrensada())
											q1 = q1.replace("[:isSubstanciaPrensada]", ", prensada");
										else
											q1 = q1.replace("[:isSubstanciaPrensada]", "");

										if (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().isSubstanciaFragmentada())
											q1 = q1.replace("[:isSubstanciaFragmentada]", ", fragmentada");
										else
											q1 = q1.replace("[:isSubstanciaFragmentada]", "");

										q1 = q1.replace("[:tipoConstituido]",
												periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoConstituido() != null
														? ",  constituído por " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoConstituido().getDescricao().toLowerCase()
														: "");

										q1 = q1.replace("[:tipoConsistencia]",
												periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoConsistencia() != null
														? ", de consistência " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoConsistencia().getDescricao().toLowerCase()
														: "");

										laudoQuesito.getQuesito().setRespostaPadrao(qrp.getRespostaPadrao());
										laudoQuesito.setResposta(q1);
										break;
									}
								}
								if(laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.TORTURA.getId()) 
										|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.TORTURA_EM_FLAGRANTE.getId())) {
									
									String q1 = "";
									q1 = qrp.getRespostaQuesito();
									Date dataAtual = new Date();
									
									if(q1.contains("[:dataAtual]"))
										q1 = q1.replace("[:dataAtual]", StrUtil.dataFormatada(dataAtual));
									if(q1.contains("[:horaInicioPericia]"))
										q1 = q1.replace("[:horaInicioPericia]", StrUtil.horaFormatada(pericia.getDataInclusao()));
									if(q1.contains("[:horaConclusaoPericia]"))
										q1 = q1.replace("[:horaConclusaoPericia]", StrUtil.horaFormatada(pericia.getDataConclusao()));
									
									laudoQuesito.getQuesito().setRespostaPadrao(qrp.getRespostaPadrao());
									laudoQuesito.setResposta(q1);
								}
							} else {
								laudoQuesito.setResposta(lmq.getRespostaPadrao().getDescricao());

							}

						}
						if (laudo.getListaLaudoQuesito() == null)
							laudo.setListaLaudoQuesito(new ArrayList<LaudoQuesito>());

						if (laudoModeloQuesitoSelecionado.isQuesitoPadrao()) {
							laudo.getListaLaudoQuesito().add(laudoQuesito);
							listaLaudoModeloQuesitoPadraoASerRemovido.add(laudoModeloQuesitoSelecionado);

						}
					}
//					for (LaudoModeloQuesito laudoModeloQuesitoPadraoASerRemovido : listaLaudoModeloQuesitoPadraoASerRemovido) {
//						listaLaudoModeloQuesito.remove(laudoModeloQuesitoPadraoASerRemovido);
//					}

					laudoModeloQuesitoSelecionado = null;
				}

				if (laudo.getListaLaudoQuesito() == null) {
					laudo.setListaLaudoQuesito(new ArrayList<LaudoQuesito>());
				}

				List<LaudoModeloConclusao> listaLaudoModeloConclusaoPadraoASerRemovido = new ArrayList<LaudoModeloConclusao>();

				if (listaLaudoModeloConclusao != null) {

					for (LaudoModeloConclusao lmc : listaLaudoModeloConclusao) {
						laudoModeloConclusaoSelecionada = lmc;
						LaudoConclusao laudoConclusao = new LaudoConclusao();
						laudoConclusao.setLaudo(laudo);
						laudoConclusao.setUsuario(getUsuarioAutenticado());
						laudoConclusao.setConclusao(laudoModeloConclusaoSelecionada.getConclusao());

						if (lmc.getConclusao() != null && lmc.getConclusao().getDescricao() != null && !lmc.getConclusao().getDescricao().isEmpty()) {

							Conclusao conc = conclusaoDao.buscarConclusaoPadrao(lmc);

							if (conc != null && conc.getDescricao() != null && !conc.getDescricao().isEmpty()) {

								String c1 = "";
								c1 = conc.getDescricao();

								// SUBSTITUO AS VARIAVEIS COM REPLACE
								c1 = c1.replace("[:valorDetectado]", pericia.getValorDetectado() != null ? pericia.getValorDetectado() : "");
								
								String valorEstimado = null;
								if(pericia.getValorDetectado() != null && !pericia.getValorDetectado().isEmpty()) {                                                                                                                                                                              
									String stringFormatada = pericia.getValorDetectado();
									stringFormatada = stringFormatada.replace(",", ".");
									double valEst = Double.parseDouble(stringFormatada) / 1.20;
									valorEstimado = String.format("%.1f", valEst) + " dg/L";
								}
								c1 = c1.replace("[:valorEstimado]", valorEstimado != null ? valorEstimado : "");
								
								List<PericiaEvidencia> listaEnvolvidoPessoa = new ArrayList<PericiaEvidencia>();
								String cadaver = "";
								int numeroCadaver = 0;

								listaEnvolvidoPessoa = periciaEvidenciaDao.buscarQuantidadeEnvolvidosUtilizadoNoLaudo(pericia);

								// Se na conclusão Laudo homícidio houver uma vítima
								if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_VIDA_HOMICIDIO.getId())) {
									for (PericiaEvidencia periciaEvidenciaPessoa : pericia.getListPericiaEvidencia()) {
										if (periciaEvidenciaPessoa.isUtilizaNoLaudo()
												&& periciaEvidenciaPessoa.getEvidencia().getTipoEvidencia().getId().equals(TipoEvidenciaEnum.PESSOA_ENVOLVIDA.getId())) {
											c1 = conc.getDescricao();

											if (listaEnvolvidoPessoa.size() > 1) {
												cadaver = cadaver + ("\nCADÁVER " + (StrUtil.lpad(Integer.toString(++numeroCadaver), 2, '0') + "\n"));
											}

											c1 = c1.replace("[:nomeVitima]",
													!periciaEvidenciaPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().isDesconhecido()
															&& periciaEvidenciaPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getNome() != null
																	? periciaEvidenciaPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getNome()
																	: " ");

											for (PessoaDocumento evidenciaPessoaDocumento : periciaEvidenciaPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getListaPessoaDocumento()) {
												c1 = c1.replace("[:rgEnvolvido]", evidenciaPessoaDocumento.getPessoaTipoDocumento() != null ? evidenciaPessoaDocumento.getNumero() : " ");
											}

											c1 = c1.replace("[:tipoInstrumento]",
													periciaEvidenciaPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoInstrumento() != null
															? periciaEvidenciaPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoInstrumento().getDescricao().toLowerCase()
															: " ");

											cadaver = cadaver + c1;
										}
									} // Seta conclusao dinâmica do Modelo de laudo de homicidio da NUPEX
									laudoConclusao.getConclusao().setDescricao(cadaver);
								}

								// Seta conclusao dinâmica do modelo de laudo de Alcoolemia NUTOF
								laudoConclusao.getConclusao().setDescricao(c1);
								break;
							} else {
								laudoConclusao.setDescricao(lmc.getConclusao().getDescricao());
							}
						}
						if (laudo.getListaLaudoConclusao() == null)
							laudo.setListaLaudoConclusao(new ArrayList<LaudoConclusao>());

						if (laudoModeloConclusaoSelecionada.isConclusaoPadrao()) {
							laudo.getListaLaudoConclusao().add(laudoConclusao);
							listaLaudoModeloConclusaoPadraoASerRemovido.add(laudoModeloConclusaoSelecionada);

						}
					}
//					for (LaudoModeloQuesito laudoModeloQuesitoPadraoASerRemovido : listaLaudoModeloQuesitoPadraoASerRemovido) {
//						listaLaudoModeloQuesito.remove(laudoModeloQuesitoPadraoASerRemovido);
//					}

					laudoModeloConclusaoSelecionada = null;
				}

				if (laudo.getListaLaudoConclusao() == null) {
					laudo.setListaLaudoConclusao(new ArrayList<LaudoConclusao>());
				}

				if (listaLaudoModeloMetodologia != null) {

					Iterator<LaudoModeloMetodologia> iterator = listaLaudoModeloMetodologia.iterator();

					while (iterator.hasNext()) {
						LaudoModeloMetodologia lmm = iterator.next();

						laudoModeloMetodologiaSelecionada = lmm;
						LaudoMetodologia laudoMetodologia = new LaudoMetodologia();
						laudoMetodologia.setLaudo(laudo);
						laudoMetodologia.setUsuario(getUsuarioAutenticado());
						laudoMetodologia.setMetodologia(laudoModeloMetodologiaSelecionada.getMetodologia());

						if (lmm.getMetodologia() != null && lmm.getMetodologia().getDescricao() != null && !lmm.getMetodologia().getDescricao().isEmpty()) {

							Metodologia met = metodologiaDao.buscarMetodologiaPadrao(lmm);

							if (met != null && met.getDescricao() != null && !met.getDescricao().isEmpty()) {

								String m1 = "";
								m1 = met.getDescricao();

								// SUBSTITUO AS VARIAVEIS COM REPLACE
								m1 = m1.replace("[:sensibilidadeAnalitica]", pericia.getSensibilidadeAnalitica() != null ? pericia.getSensibilidadeAnalitica().toString() : "");

								laudoMetodologia.getMetodologia().setDescricao(m1);

							} else {
								laudoMetodologia.setDescricao(lmm.getMetodologia().getDescricao());
							}
						}
						if (laudo.getListaLaudoMetodologia() == null)
							laudo.setListaLaudoMetodologia(new ArrayList<LaudoMetodologia>());

						if (laudoModeloMetodologiaSelecionada.isMetodologiaPadrao()) {
							laudo.getListaLaudoMetodologia().add(laudoMetodologia);
							iterator.remove();
						}
					}
					laudoModeloMetodologiaSelecionada = null;
				}
				if (laudo.getListaLaudoMetodologia() == null) {
					laudo.setListaLaudoMetodologia(new ArrayList<LaudoMetodologia>());
				}

			}
			
			adicionarConclusaoPadraoNoLaudo();
			adicionarNotasPadroesNoLaudo();

		}

	}

	private void carregarListasModelo() {
		laudo.setUltimoLaudoModeloAnexo(laudoModeloAnexoDao.buscarUltimoLaudoAnexo(laudo.getLaudoModeloTipoExameSetor()));
		if (laudo.getLaudoModeloTipoExameSetor().getLaudoModelo().isPossuiConclusao()) {
			if (laudo.getListaLaudoConclusao() != null && !laudo.getListaLaudoConclusao().isEmpty())
				setListaLaudoModeloConclusao(laudoModeloConclusaoDao.buscarPorLaudoModeloExcluindoConclusao(laudo.getLaudoModeloTipoExameSetor().getLaudoModelo(), laudo.getListaLaudoConclusao()));
			else
				setListaLaudoModeloConclusao(laudoModeloConclusaoDao.buscarPorLaudoModelo(laudo.getLaudoModeloTipoExameSetor().getLaudoModelo()));
		}

		if (laudo.getLaudoModeloTipoExameSetor().getLaudoModelo().isPossuiMetodologia()) {
			if (laudo.getListaLaudoMetodologia() != null && !laudo.getListaLaudoMetodologia().isEmpty())
				setListaLaudoModeloMetodologia(
						laudoModeloMetodologiaDao.buscarPorLaudoModeloExcluindoMetodologias(laudo.getLaudoModeloTipoExameSetor().getLaudoModelo(), laudo.getListaLaudoMetodologia()));
			else
				setListaLaudoModeloMetodologia(laudoModeloMetodologiaDao.buscarPorLaudoModelo(laudo.getLaudoModeloTipoExameSetor().getLaudoModelo()));
		}

		if (laudo.getLaudoModeloTipoExameSetor().getLaudoModelo().isPossuiQuesito()) {
			if (laudo.getListaLaudoQuesito() != null && !laudo.getListaLaudoQuesito().isEmpty())
				setListaLaudoModeloQuesito(laudoModeloQuesitoDao.buscarPorLaudoModeloExcluindoQuesito(laudo.getLaudoModeloTipoExameSetor().getLaudoModelo(), laudo.getListaLaudoQuesito()));
			else
				setListaLaudoModeloQuesito(laudoModeloQuesitoDao.buscarPorLaudoModelo(laudo.getLaudoModeloTipoExameSetor().getLaudoModelo()));
		}

		if (laudo.getLaudoModeloTipoExameSetor().getLaudoModelo().isPossuiNotas()) {
			if (laudo.getListaLaudoNotas() != null && !laudo.getListaLaudoNotas().isEmpty())
				setListaLaudoModeloNotas(laudoModeloNotasDao.buscarPorLaudoModeloExcluindoNotas(laudo.getLaudoModeloTipoExameSetor().getLaudoModelo(), laudo.getListaLaudoNotas()));
			else
				setListaLaudoModeloNotas(laudoModeloNotasDao.buscarPorLaudoModelo(laudo.getLaudoModeloTipoExameSetor().getLaudoModelo()));
		}
	}

	public void adicionarMetodologia() {
		if (laudoModeloMetodologiaSelecionada != null) {
			try {
				LaudoMetodologia laudoMetodologia = new LaudoMetodologia();
				laudoMetodologia.setLaudo(laudo);
				laudoMetodologia.setUsuario(getUsuarioAutenticado());
				if (laudoModeloMetodologiaSelecionada.getMetodologia().isPermiteEdicao()) {
					laudoMetodologia.setDescricao(laudoModeloMetodologiaSelecionada.getMetodologia().getDescricao());
				}
				laudoMetodologia.setMetodologia(laudoModeloMetodologiaSelecionada.getMetodologia());
				//Substitui o laudo metodologia para apresentar o conteúdo dinâmico baseado nas perícias realizadas
				formatarRepostaDinamicaNoModelo(laudoMetodologia);
			
				if (laudo.getListaLaudoMetodologia() == null)
					laudo.setListaLaudoMetodologia(new ArrayList<LaudoMetodologia>());
				laudo.getListaLaudoMetodologia().add(laudoMetodologia);
	
				// Remover LaudoMetodologia do select de escolha
				listaLaudoModeloMetodologia.remove(laudoModeloMetodologiaSelecionada);
	
				laudoModeloMetodologiaSelecionada = null;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void excluirMetodologia(LaudoMetodologia laudoMetodologia) {
		laudo.getListaLaudoMetodologia().remove(laudoMetodologia);
		listaLaudoModeloMetodologia.add(laudoModeloMetodologiaDao.buscarPorLaudoMetodologia(laudoMetodologia, laudo.getLaudoModeloTipoExameSetor()));
	}

	public void preparaAdicionarQuesitoExtra() {
		inserindoQuesitoExtra = true;
		quesitoExtra = new Quesito();
	}
	
	public void adicionarQuesitoExtra() {
		reiniciaContador();
		try {
			if(possuiQuesitoExtra) {
				if(descricaoQuesitoExtra != null && !descricaoQuesitoExtra.isEmpty() && ordemQuesitoExtra != null && !ordemQuesitoExtra.isEmpty()) {
					
					quesitoExtra.setDataInclusao(new Date());
					quesitoExtra.setUsuario(getUsuarioAutenticado());
					quesitoExtra.setBloco(blocoDao.find(5));
					quesitoExtra.setDescricao(descricaoQuesitoExtra);
					quesitoExtra.setOrdem(Integer.parseInt(ordemQuesitoExtra));
					quesitoExtra.setApelido("Quesito Extra LCT/LCFT");
					quesitoDao.save(quesitoExtra);
					
					LaudoQuesito laudoQuesito = new LaudoQuesito();
					laudoQuesito.setLaudo(laudo);
					laudoQuesito.setUsuario(getUsuarioAutenticado());
					laudoQuesito.setQuesito(quesitoExtra);
					if (laudo.getListaLaudoQuesito() == null)
						laudo.setListaLaudoQuesito(new ArrayList<LaudoQuesito>());
					laudo.getListaLaudoQuesito().add(laudoQuesito);
					
					ordenarListaLaudoQuesito(laudo.getListaLaudoQuesito());
					
					inserindoQuesitoExtra = false;
					descricaoQuesitoExtra = null;
					ordemQuesitoExtra = null;
					FacesUtils.addInfoMessage("Quesito adicionado! Responda este o os outros quesitos na tabela abaixo.");
				}else
					FacesUtils.addInfoMessage("É necessário o preenchimento dos campos para adicionar o quesito!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void adicionarOuRemoverQuesitoBlocoCinco(){
		reiniciaContador();
		Bloco blocoCinco = blocoDao.find(5);
		Quesito quesitoBlocoCinco = quesitoDao.find(108);
		quesitoBlocoCinco.setListaQuesitoRespostaPadrao(quesitoRespostaPadraoDao.buscarPorQuesito(quesitoBlocoCinco));
		
		if(!possuiQuesitoExtra) {
			for (LaudoQuesito ld : laudo.getListaLaudoQuesito()) {
				if(ld.getQuesito().getBloco().equals(blocoCinco))
					laudo.getListaLaudoQuesito().remove(ld);
			}
			LaudoQuesito laudoQuesito = new LaudoQuesito();
			laudoQuesito.setLaudo(laudo);
			laudoQuesito.setUsuario(getUsuarioAutenticado());
			laudoQuesito.setQuesito(quesitoBlocoCinco);
			laudoQuesito.setResposta(quesitoBlocoCinco.getListaQuesitoRespostaPadrao().get(0).getRespostaQuesito());
			if (laudo.getListaLaudoQuesito() == null)
				laudo.setListaLaudoQuesito(new ArrayList<LaudoQuesito>());
			laudo.getListaLaudoQuesito().add(laudoQuesito);
			
			ordenarListaLaudoQuesito(laudo.getListaLaudoQuesito());
		}
		if(possuiQuesitoExtra){
			for (LaudoQuesito lq : laudo.getListaLaudoQuesito()) {
				if(lq.getQuesito().getId().equals(quesitoBlocoCinco.getId()))
					laudo.getListaLaudoQuesito().remove(lq);
			}
			ordenarListaLaudoQuesito(laudo.getListaLaudoQuesito());
		}
	}
	
	public void cancelarQuesitoExtra() {
		reiniciaContador();
		inserindoQuesitoExtra = false;
		descricaoQuesitoExtra = null;
	}
	
	public void adicionarQuesito() {
		reiniciaContador();
		if (laudoModeloQuesitoSelecionado != null) {
			LaudoQuesito laudoQuesito = new LaudoQuesito();
			laudoQuesito.setLaudo(laudo);
			laudoQuesito.setUsuario(getUsuarioAutenticado());
			laudoQuesito.setQuesito(laudoModeloQuesitoSelecionado.getQuesito());
			if (laudo.getListaLaudoQuesito() == null)
				laudo.setListaLaudoQuesito(new ArrayList<LaudoQuesito>());
			laudo.getListaLaudoQuesito().add(laudoQuesito);
		      
			ordenarListaLaudoQuesito(laudo.getListaLaudoQuesito());
			
			// Remover LaudoQuesito do select de escolha
			listaLaudoModeloQuesito.remove(laudoModeloQuesitoSelecionado);

			laudoModeloQuesitoSelecionado = null;

		}
	}

	public void excluirQuesito(LaudoQuesito laudoQuesito) {
		reiniciaContador();
		laudo.getListaLaudoQuesito().remove(laudoQuesito);
		if(!listaLaudoModeloQuesito.contains(laudoModeloQuesitoDao.buscarPorLaudoQuesito(laudoQuesito, laudo.getLaudoModeloTipoExameSetor()))
				&& !laudoQuesito.getQuesito().getBloco().equals(blocoDao.find(5)))
			listaLaudoModeloQuesito.add(laudoModeloQuesitoDao.buscarPorLaudoQuesito(laudoQuesito, laudo.getLaudoModeloTipoExameSetor()));
	}

	public void adicionarConclusao() throws JSONException {
		try {
			if (laudoModeloConclusaoSelecionada != null) {
				LaudoConclusao laudoConclusao = new LaudoConclusao();
				laudoConclusao.setLaudo(laudo);
				laudoConclusao.setUsuario(getUsuarioAutenticado());
				laudoConclusao.setConclusao(laudoModeloConclusaoSelecionada.getConclusao());
				if (laudoModeloConclusaoSelecionada.getConclusao().isPermiteEdicao()) {
					laudoConclusao.setDescricao(laudoModeloConclusaoSelecionada.getConclusao().getDescricao());
				}

				if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_DROGAS_EM_SANGUE.getId())
						|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_DROGAS_EM_URINA.getId())) {

					if (!pericia.isConfirmatorio() && !pericia.isTriagem())
						throw new DadosInvalidosException();

					String c1 = "";
					String descricaoConclusaoLaudo = "";

					c1 = laudoModeloConclusaoSelecionada.getConclusao().getDescricao();

					if (pericia.isConfirmatorio() && laudoModeloConclusaoSelecionada.getConclusao().getApelido().equals("CONFIRMADO")) {

						JSONObject json2 = new JSONObject(pericia.getSolicitacaoResultadoConfirmatorio());

						String s = "";
						List<String> listaSubstancias = new ArrayList<String>();

						@SuppressWarnings("unchecked")
						Iterator<String> iter2 = json2.keys();

						if (iter2 != null && iter2.hasNext()) {
							String key = iter2.next();
							for (int i = 0; i < ((JSONArray) json2.get(key)).length(); ++i) {
								String descricaoSubstancia = "";
								String resultado = "";

								descricaoSubstancia += ((JSONObject) ((JSONArray) json2.get(key)).get(i)).get("descricaoClasseSubstancia");
								resultado += ((JSONObject) ((JSONArray) json2.get(key)).get(i)).get("resultado");

								if (resultado.equals("CONFIRMADO")) {
									listaSubstancias.add(descricaoSubstancia);
								}

							}
						}

						for (String str : listaSubstancias) {
							s += (str + ", ");
						}

						c1 = c1.replace("[:tipoSubstanciaConfirmatorio]", s);
						c1 = c1.replace(", .", ".");
					}

					if (pericia.isTriagem() || pericia.isConfirmatorio() && (laudoModeloConclusaoSelecionada.getConclusao().getApelido().equals("DETECTADO")
							|| laudoModeloConclusaoSelecionada.getConclusao().getApelido().equals("DETECTADO / NÃO CONFIRMADO")
							|| laudoModeloConclusaoSelecionada.getConclusao().getApelido().equals("DETECTADO / NÃO REALIZADO")
							|| laudoModeloConclusaoSelecionada.getConclusao().getApelido().equals("NÃO CONFIRMADO"))) {
						JSONObject json = new JSONObject(pericia.getSolicitacaoResultadoKit());

						String p = "";
						String q = "";
						String r = "";
						String t = "";
						List<String> listaSubstanciasTriagem = new ArrayList<String>();
						List<String> listaSubstanciasDetectadasTriagem = new ArrayList<String>();
						List<String> listaSubstanciasNaoDetectadasTriagem = new ArrayList<String>();
						List<String> listaSubstanciasInconclusivasTriagem = new ArrayList<String>();

						@SuppressWarnings("unchecked")
						Iterator<String> iter = json.keys();

						if (iter != null && iter.hasNext()) {
							String key = iter.next();
							for (int i = 0; i < ((JSONArray) json.get(key)).length(); ++i) {
								String descricaoSubstancia = "";
								String resultado = "";

								descricaoSubstancia += ((JSONObject) ((JSONArray) json.get(key)).get(i)).get("descricaoClasseSubstancia");
								resultado += ((JSONObject) ((JSONArray) json.get(key)).get(i)).get("resultado");

								if (resultado.equals("NÃO DETECTADO")) {
									listaSubstanciasNaoDetectadasTriagem.add(descricaoSubstancia);
								}

								if (resultado.equals("DETECTADO")) {
									listaSubstanciasDetectadasTriagem.add(descricaoSubstancia);
								}

								if (resultado.equals("INCONCLUSIVO")) {
									listaSubstanciasInconclusivasTriagem.add(descricaoSubstancia);
								}

								listaSubstanciasTriagem.add(descricaoSubstancia);

							}

							for (String str : listaSubstanciasTriagem) {
								p += (str + ", ");
							}

							for (String str : listaSubstanciasDetectadasTriagem) {
								q += (str + ", ");
							}

							for (String str : listaSubstanciasNaoDetectadasTriagem) {
								r += (str + ", ");
							}

							for (String str : listaSubstanciasInconclusivasTriagem) {
								t += (str + ", ");
							}

							c1 = c1.replace("[:tipoSubstanciaTriagem]", p);
							c1 = c1.replace(", .", ".");

							c1 = c1.replace("[:tipoSubstanciasDetectadasTriagem]", q);
							c1 = c1.replace(", .", ".");

							c1 = c1.replace("[:tipoSubstanciaNaoDetectadaTriagem]", r);
							c1 = c1.replace(", .", ".");

							c1 = c1.replace("[:tipoSubstanciaInconclusivaTriagem]", t);
							c1 = c1.replace(", .", ".");

							descricaoConclusaoLaudo = descricaoConclusaoLaudo + c1;
						}
					}

					laudoConclusao.getConclusao().setDescricao(descricaoConclusaoLaudo);
					laudoConclusao.getConclusao().setDescricao(laudoModeloConclusaoSelecionada.getConclusao().getDescricao());
				}

				if (laudo.getListaLaudoConclusao() == null)
					laudo.setListaLaudoConclusao(new ArrayList<LaudoConclusao>());
				laudo.getListaLaudoConclusao().add(laudoConclusao);

				// Remover LaudoConclusao do select de escolha
				listaLaudoModeloConclusao.remove(laudoModeloConclusaoSelecionada);

				laudoModeloConclusaoSelecionada = null;
			}
		} catch (DadosInvalidosException e) {
			FacesUtils.addErrorMessage("Não foi lançado nenhum resultado para o Laudo!");
		}
	}

	public void adicionarNotas() {
		if (laudoModeloNotasSelecionadas != null) {
			LaudoNotas laudoNotas = new LaudoNotas();
			laudoNotas.setLaudo(laudo);
			laudoNotas.setUsuario(getUsuarioAutenticado());
			laudoNotas.setNotas(laudoModeloNotasSelecionadas.getNotas());
//			if (laudoModeloNotasSelecionadas.getNotas().isPermiteEdicao()) {
//				laudoNotas.setDescricao(laudoModeloNotasSelecionadas.getNotas().getDescricao());
//			}

			if (laudo.getListaLaudoNotas() == null)
				laudo.setListaLaudoNotas(new ArrayList<LaudoNotas>());
			laudo.getListaLaudoNotas().add(laudoNotas);

			// Remover Laudonotas do select de escolha
			listaLaudoModeloNotas.remove(laudoModeloNotasSelecionadas);

			laudoModeloNotasSelecionadas = null;
		}
	}

	public void excluirConclusao(LaudoConclusao laudoConclusao) {
		laudo.getListaLaudoConclusao().remove(laudoConclusao);
		listaLaudoModeloConclusao.add(laudoModeloConclusaoDao.buscarPorLaudoConclusao(laudoConclusao, laudo.getLaudoModeloTipoExameSetor()));
	}

	public void excluirNotas(LaudoNotas laudoNotas) {
		laudo.getListaLaudoNotas().remove(laudoNotas);
		listaLaudoModeloNotas.add(laudoModeloNotasDao.buscarPorLaudoNotas(laudoNotas, laudo.getLaudoModeloTipoExameSetor()));
	}

	public void uploadLaudoDocumento(FileUploadEvent event) {
		this.laudoUpload = event.getFile();
		this.laudoUpload.getContents();
	}

	private void validarDocumento() throws DocumentoInvalidoException, LaudoNaoAnexadoException, Exception {

		byte[] modeloDocumento = null;

		if (this.laudoUpload != null)
			modeloDocumento = this.laudoUpload.getContents();

		if ((modeloDocumento == null || modeloDocumento.length == 0))
			throw new LaudoNaoAnexadoException();

		InputStream input = new ByteArrayInputStream(modeloDocumento);
		TextDocument laudoDocumento;
		laudoDocumento = TextDocument.loadDocument(input);
		VariableField _numeroLaudo = laudoDocumento.getVariableFieldByName("numero");

		Calendar c = Calendar.getInstance();
		c.setTime(laudo.getDataInclusao());

		if (_numeroLaudo == null || !(c.get(Calendar.YEAR) + "." + StrUtil.lpad(Integer.toString(laudo.getNumero()), 7, '0')).equals(_numeroLaudo.getOdfElement().getAttribute("office:string-value")))
			throw new DocumentoInvalidoException();

		if (laudo.getListaLaudoAnexo() == null)
			laudo.setListaLaudoAnexo(new ArrayList<LaudoAnexo>());
		laudo.getListaLaudoAnexo().clear();

		LaudoAnexo laudoAnexo = new LaudoAnexo();
		laudoAnexo.setUsuario(getUsuarioAutenticado());
		laudoAnexo.setLaudo(laudo);
		laudoAnexo.setArquivoAnexado(modeloDocumento);
		laudoAnexo.setTipoDocumentoAnexo(TipoDocumentoAnexoEnum.LAUDO_ODT);
		laudo.getListaLaudoAnexo().add(laudoAnexo);
	}

	public DefaultStreamedContent downloadLaudoModelo() {
		try {

			byte[] arquivo = gerarLaudoDocumento();
			InputStream in = new ByteArrayInputStream(arquivo);
			setUpdating(true);
			return new DefaultStreamedContent(in, StrUtil.CONTENT_TYPE_OPENDOCUMENT_FILE, nomeArquivo);
			// } catch (LaudoNaoAnexoException e) {
			// FacesUtils.addErrorMessage("O documento não foi anexado ao laudo!");
			// e.printStackTrace();
		} catch (Exception e) {
			FacesUtils.addErrorMessage(StrUtil.MSG_ERRO_EXCEPTION + " " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public DefaultStreamedContent downloadLaudoDocumento() {
		try {
			validarLaudo();
			this.laudo = this.laudoDao.salvarLaudo(this.periciaLaudo, tipoLaudo);
			byte[] arquivo = gerarLaudoDocumento();
			InputStream in = new ByteArrayInputStream(arquivo);
			setUpdating(true);
			return new DefaultStreamedContent(in, StrUtil.CONTENT_TYPE_OPENDOCUMENT_FILE, nomeArquivo);
		} catch (DocumentoInvalidoException e) {
			FacesUtils.addErrorMessage("O anexado não pertence ao laudo!");
			e.printStackTrace();
		} catch (LaudoNaoAnexadoException e) {
			FacesUtils.addErrorMessage("O documento não foi anexado ao laudo!");
			e.printStackTrace();
		} catch (LaudoSemMetodologiaException e) {
			FacesUtils.addErrorMessage("Não foram incluídas metodologias ao laudo!");
			e.printStackTrace();
		} catch (LaudoSemConclusaoException e) {
			FacesUtils.addErrorMessage("Não incluída conclusão laudo!");
			e.printStackTrace();
		} catch (LaudoSemQuesitosException e) {
			FacesUtils.addErrorMessage("Não foram incluídos quesitos ao laudo!");
			e.printStackTrace();
		} catch (LaudoSemDatasException e) {
			FacesUtils.addErrorMessage("Existem dados no relatório da ocorrência que não foi preenchido!");
		} catch (Exception e) {
			FacesUtils.addErrorMessage(StrUtil.MSG_ERRO_EXCEPTION);
			e.printStackTrace();
		}
		return null;
	}

	public void downloadLaudoDocumentoAtual() {
		try {

			byte[] arquivo = gerarLaudoDocumento();
			InputStream in = new ByteArrayInputStream(arquivo);
			setUpdating(true);
			downloadLaudoModelo = new DefaultStreamedContent(in, StrUtil.CONTENT_TYPE_OPENDOCUMENT_FILE, nomeArquivo);

			/*
			 * EXECUTA UM CLICK EM BOTÃO NA PÁGINA DE MODALPERICIALAUDO PARA EFETUAR O DOWNLOAD DO ARQUIVO.
			 *
			 */
			RequestContext.getCurrentInstance().execute("$('#downloadLaudo').click();");

		} catch (Exception e) {
			FacesUtils.addErrorMessage(StrUtil.MSG_ERRO_EXCEPTION);
			e.printStackTrace();
		}
	}

	public DefaultStreamedContent downloadLaudoAnexo(PericiaLaudo periciaLaudo) {
		try {

			if (periciaLaudo != null) {
				LaudoAnexo laudoAnexo = laudoAnexoDao.buscarUltimoLaudoAnexoPorLaudo(periciaLaudo.getLaudo());

				if (laudoAnexo != null) {
					Calendar dataLaudo = Calendar.getInstance();
					dataLaudo.setTime(laudoAnexo.getDataInclusao());
					String logMensagem = "O usuário " + getUsuarioAutenticado().getNome() + " com cpf " + getUsuarioAutenticado().getCpf() + ", visualizou o Laudo de número "
							+ dataLaudo.get(Calendar.YEAR) + "." + StrUtil.lpad(Integer.toString(periciaLaudo.getLaudo().getNumero()), 7, '0') + ". O arquivo visualizado era do tipo: ";
					String sufixo = null;
					String content = StrUtil.CONTENT_TYPE_OPENDOCUMENT_FILE;
					switch (laudoAnexo.getTipoDocumentoAnexo()) {
					case LAUDO_ODT:
						sufixo = ".odt";
						break;
					case LAUDO_PDF:
					case LAUDO_ASSINADO:
					case LAUDO_ASSINADO_COM_REVISOR:
					case LAUDO_ASSINADO_ELETRONICAMENTE:
						sufixo = ".pdf";
						content = StrUtil.CONTENT_TYPE_PDF_FILE;
						break;
					default:
						break;
					}
					auditoriaLogDao.criarAuditoria(TipoAcaoEnum.VISUALIZACAO_LAUDO, getUsuarioAutenticado(), logMensagem + laudoAnexo.getTipoDocumentoAnexo().getDescricao());
					InputStream in = new ByteArrayInputStream(laudoAnexo.getArquivoAnexado());
					return new DefaultStreamedContent(in, content, laudoAnexo.getLaudo().getTipoLaudo().getDescricao() + " - " + periciaLaudo.getLaudo().getNumero() + sufixo);
				} else {
					FacesUtils.addErrorMessage("O laudo não possui Anexo!");
					return null;
				}
			}
		} catch (Exception e) {
			FacesUtils.addErrorMessage("Ocorreu um erro ao visualizar o laudo!");
			e.printStackTrace();
		}
		return null;
	}

	public DefaultStreamedContent downloadLaudoAnexoAssinado(PericiaLaudo periciaLaudo) {
		try {
			if (periciaLaudo != null) {
				LaudoAnexo laudoAnexo = laudoAnexoDao.buscarUltimoLaudoAnexoPorLaudo(periciaLaudo.getLaudo());
				if (laudoAnexo != null && (laudoAnexo.getTipoDocumentoAnexo().equals(TipoDocumentoAnexoEnum.LAUDO_ASSINADO)
						|| laudoAnexo.getTipoDocumentoAnexo().equals(TipoDocumentoAnexoEnum.LAUDO_ASSINADO_COM_REVISOR))) {
					Calendar dataLaudo = Calendar.getInstance();
					dataLaudo.setTime(laudoAnexo.getDataInclusao());
					String logMensagem = "O usuário " + getUsuarioAutenticado().getNome() + " com cpf " + getUsuarioAutenticado().getCpf() + ", visualizou o Laudo de número "
							+ dataLaudo.get(Calendar.YEAR) + "." + StrUtil.lpad(Integer.toString(periciaLaudo.getLaudo().getNumero()), 7, '0') + ". O arquivo visualizado era do tipo: ";
					String sufixo = null;
					String content = StrUtil.CONTENT_TYPE_OPENDOCUMENT_FILE;
					sufixo = ".pdf";
					content = StrUtil.CONTENT_TYPE_PDF_FILE;
					auditoriaLogDao.criarAuditoria(TipoAcaoEnum.VISUALIZACAO_LAUDO, getUsuarioAutenticado(), logMensagem + laudoAnexo.getTipoDocumentoAnexo().getDescricao());
					InputStream in = new ByteArrayInputStream(laudoAnexo.getArquivoAnexado());
					return new DefaultStreamedContent(in, content, "Laudo-" + periciaLaudo.getLaudo().getNumero() + sufixo);
				} else {
					FacesUtils.addErrorMessage("O laudo ainda não foi assinado digitalmente!");
				}
			} else {
				FacesUtils.addErrorMessage("O laudo não possui Anexo!");
			}

		} catch (Exception e) {
			FacesUtils.addErrorMessage("Ocorreu um erro ao visualizar o laudo!");
			e.printStackTrace();
		}
		return null;
	}

	public DefaultStreamedContent downloadInformativoTecnicoAnexoAssinado(PericiaLaudo periciaLaudo) {
		try {
			if (periciaLaudo != null) {
				LaudoAnexo laudoAnexo = laudoAnexoDao.buscarUltimoLaudoAnexoPorLaudo(periciaLaudo.getLaudo());
				if (laudoAnexo != null && laudoAnexo.getTipoDocumentoAnexo().equals(TipoDocumentoAnexoEnum.LAUDO_ASSINADO_ELETRONICAMENTE)) {
					Calendar dataLaudo = Calendar.getInstance();
					dataLaudo.setTime(laudoAnexo.getDataInclusao());
					String logMensagem = "O usuário " + getUsuarioAutenticado().getNome() + " com cpf " + getUsuarioAutenticado().getCpf() + ", visualizou o Informativo técnico de número "
							+ dataLaudo.get(Calendar.YEAR) + "." + StrUtil.lpad(Integer.toString(periciaLaudo.getLaudo().getNumero()), 7, '0') + ". O arquivo visualizado era do tipo: ";
					String sufixo = null;
					String content = StrUtil.CONTENT_TYPE_OPENDOCUMENT_FILE;
					sufixo = ".pdf";
					content = StrUtil.CONTENT_TYPE_PDF_FILE;
					auditoriaLogDao.criarAuditoria(TipoAcaoEnum.VISUALIZACAO_LAUDO, getUsuarioAutenticado(), logMensagem + laudoAnexo.getTipoDocumentoAnexo().getDescricao());
					InputStream in = new ByteArrayInputStream(laudoAnexo.getArquivoAnexado());
					return new DefaultStreamedContent(in, content,
							"Informativo Técnico - " + periciaLaudo.getLaudo().getNumero() + "-" + StrUtil.anoData(periciaLaudo.getLaudo().getDataInclusao()) + sufixo);
				} else {
					FacesUtils.addErrorMessage("O informativo técnico ainda não foi assinado eletronicamente!");
				}
			} else {
				FacesUtils.addErrorMessage("O informativo técnico não possui Anexo!");
			}

		} catch (Exception e) {
			FacesUtils.addErrorMessage("Ocorreu um erro ao visualizar o informativo técnico!");
			e.printStackTrace();
		}
		return null;
	}

	public void salvarLaudoRapido() {
		try {
			validarLaudo();
			// SALVA O CODIGO PARA CONSULTA DE DPVAT
			if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.DPVAT.getId())) {
				String codigoValidacao = DigestUtils.md5Hex(laudo.getNumero().toString() + StrUtil.dataFormatada(laudo.getDataInclusao()));
				laudo.setCodigoValidacao(codigoValidacao.substring(0, 12));
			}

			laudo = laudoDao.salvarLaudo(periciaLaudo, tipoLaudo);
			byte[] arquivo = gerarLaudoDocumento();
			if (laudo.getListaLaudoAnexo() == null)
				laudo.setListaLaudoAnexo(new ArrayList<LaudoAnexo>());
			laudo.getListaLaudoAnexo().clear();

			LaudoAnexo laudoAnexo = new LaudoAnexo();
			laudoAnexo.setUsuario(getUsuarioAutenticado());
			laudoAnexo.setLaudo(laudo);
			laudoAnexo.setArquivoAnexado(arquivo);
			laudoAnexo.setTipoDocumentoAnexo(TipoDocumentoAnexoEnum.LAUDO_ODT);
			laudo.getListaLaudoAnexo().add(laudoAnexo);

			laudo = laudoDao.salvarLaudo(periciaLaudo, tipoLaudo);

			this.laudo.setListaLaudoMetodologia(null);
			this.laudo.setListaLaudoQuesito(null);
			this.laudo.setListaLaudoConclusao(null);
			this.laudo.setListaLaudoFoto(null);
			this.laudo.setListaLaudoParecer(null);
			this.laudo.setListaLaudoAnexo(null);
			this.laudo.setListPericiaLaudo(null);
			if (this.pericia.getListPericiaLaudo() == null)
				this.pericia.setListPericiaLaudo(new ArrayList<PericiaLaudo>());
			this.pericia.getListPericiaLaudo().add(this.periciaLaudo);
			setBrowsing(true);
			if (solicitacaoProcedimentoPericial.getSetor().isNecropapiloscopia()) {
				FacesUtils.addInfoMessage(StrUtil.MSG_SUCESSO_INCLUIR_INFORMATIVO);
			} else {
				FacesUtils.addInfoMessage(StrUtil.MSG_SUCESSO_INCLUIR_LAUDO);
			}

		} catch (LaudoSemMetodologiaException e) {
			FacesUtils.addErrorMessage("Não foi incluída nenhuma metodologias ao laudo!");
			e.printStackTrace();
		} catch (LaudoSemConclusaoException e) {
			FacesUtils.addErrorMessage("Não foi incluída nenhuma conclusão ao laudo!");
			e.printStackTrace();
		} catch (LaudoSemQuesitosException e) {
			FacesUtils.addErrorMessage("Não foi incluído nenhum quesito ao laudo!");
			e.printStackTrace();
		} catch (Exception e) {
			FacesUtils.addErrorMessage(StrUtil.MSG_ERRO_EXCEPTION);
			e.printStackTrace();
		}
	}

	public void gerarLaudo() {

		try {
			if (periciaLaudoDao.existePericiaLaudo(this.pericia))
				throw new LaudoJaExisteException();

			validarLaudo();
			laudo = laudoDao.salvarLaudo(periciaLaudo, tipoLaudo);
			setUpdating(true);
			if (solicitacaoProcedimentoPericial.getSetor().isNecropapiloscopia()) {
				FacesUtils.addInfoMessage(StrUtil.MSG_SUCESSO_INCLUIR_INFORMATIVO);
			} else {
				FacesUtils.addInfoMessage(StrUtil.MSG_SUCESSO_INCLUIR_LAUDO);
			}

			byte[] arquivo = gerarLaudoDocumento();
			InputStream in = new ByteArrayInputStream(arquivo);
			downloadLaudoModelo = new DefaultStreamedContent(in, StrUtil.CONTENT_TYPE_OPENDOCUMENT_FILE, nomeArquivo);

			// Executa um click em botão na página de modalPericiaLaudo para
			// efetuar o Download do arquivo.
			RequestContext.getCurrentInstance().execute("$('#downloadLaudo').click();");
		} catch (LaudoSemMetodologiaException e) {
			FacesUtils.addErrorMessage("Não foi incluída nenhuma metodologias ao laudo!");
			e.printStackTrace();
		} catch (LaudoSemConclusaoException e) {
			FacesUtils.addErrorMessage("Não foi incluída nenhuma conclusão ao laudo!");
			e.printStackTrace();
		} catch (LaudoSemQuesitosException e) {
			FacesUtils.addErrorMessage("Não foi incluído nenhum quesito ao laudo!");
		} catch (LaudoSemDatasException e) {
			FacesUtils.addErrorMessage("Existem dados no relatório da ocorrência que não foi preenchido!");
			e.printStackTrace();
		} catch (Exception e) {
			FacesUtils.addErrorMessage(StrUtil.MSG_ERRO_EXCEPTION);
			e.printStackTrace();
		}

	}

	public void verificaSolicitacaoNaComel(SolicitacaoProcedimentoPericial solicitacaoDaComel) {
		this.solicitacaoProcedimentoPericial = solicitacaoDaComel;
		this.solicitacaoProcedimentoPericial = solicitacaoProcedimentoPericialDao.find(solicitacaoProcedimentoPericial.getId());

	}

	public byte[] gerarLaudoDocumento() {
		try {
			solicitacaoProcedimentoPericial.setSetor(setorDao.find(solicitacaoProcedimentoPericial.getSetor().getId()));

			byte[] modeloDocumento = null;
			if (laudoUpload != null)
				modeloDocumento = laudoUpload.getContents();
			else if ((laudo.getUltimoLaudoAnexo() != null && laudo.getUltimoLaudoAnexo().getDataFinalizacao() == null && laudo.getTipoLaudo().equals(TipoLaudoEnum.ORIGINAL))
					|| (laudo.getUltimoLaudoAnexo() != null && laudo.getTipoLaudo().equals(TipoLaudoEnum.ADITAMENTO)))
				modeloDocumento = laudo.getUltimoLaudoAnexo().getArquivoAnexado();
			else
				modeloDocumento = laudo.getUltimoLaudoModeloAnexo().getModeloAnexo();

			solicitacaoProcedimentoPericial.setDocumentoProcedimentoPericial(documentoProcedimentoPericialDao.find(solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getId()));
			String numero = Integer.toString(laudo.getNumero());

			if (solicitacaoProcedimentoPericial.getSetor().isNecropapiloscopia())
				nomeArquivo = "Informativo Técnico - " + numero + ".odt";
			nomeArquivo = "Laudo Pericial -" + numero + ".odt";

			InputStream input = new ByteArrayInputStream(modeloDocumento);

			// Carrega o modelo para inserção dos valores nos campos.
			TextDocument laudoDocumento;
			laudoDocumento = TextDocument.loadDocument(input);

			// Coloca uma mensagem com código de validação para consulta do DPVAT
			if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.DPVAT.getId())) {
				String codigoValidacao = null;
				if (laudo.getCodigoValidacao() == null || laudo.getCodigoValidacao().isEmpty()) {
					codigoValidacao = DigestUtils.md5Hex(laudo.getNumero().toString() + StrUtil.dataFormatada(laudo.getDataInclusao()));
					laudo.setCodigoValidacao(codigoValidacao.substring(0, 12));
				} else {
					codigoValidacao = laudo.getCodigoValidacao();
				}
				Paragraph paragrafo = laudoDocumento.getParagraphByIndex(0, false);
				Textbox box = paragrafo.addTextbox(new FrameRectangle(0, 19.5, 18, 0.5, SupportedLinearMeasure.CM));
				box.clearContent();

				Paragraph paragrafoDae = box
						.addParagraph("Para verificar a autenticidade do documento acesse o endereço http://dpvat.pefoce.ce.gov.br e informe o código: GAL" + codigoValidacao.substring(0, 12));
				paragrafoDae.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
				paragrafoDae.setFont(new Font("Arial", FontStyle.BOLDITALIC, 7.5));
			}

			// OdfDocument doc = OdfDocument.loadDocument(in);
			// doc.getContentDom().getAttributes().

			Calendar dataLaudo = Calendar.getInstance();

//			if (laudo.getUltimoLaudoAnexo() != null)
//				dataLaudo.setTime(laudo.getUltimoLaudoAnexo().getDataInclusao());
//			else
			dataLaudo.setTime(laudo.getDataInclusao());

			// Insere o número do documento na variável número do documento.
			VariableField _numeroLaudo = laudoDocumento.getVariableFieldByName("numero");
			if (_numeroLaudo != null) {
				_numeroLaudo.updateField(dataLaudo.get(Calendar.YEAR) + "." + StrUtil.lpad(Integer.toString(laudo.getNumero()), 7, '0'), null);
			}

			Font fontParagrafoBold = new Font("Helvetica", FontStyle.BOLD, 11);
			Font fontParagrafoPBold = new Font("Helvetica", FontStyle.BOLD, 10.5);
			Font fontParagrafoPNormal = new Font("Helvetica", FontStyle.REGULAR, 10.5);
			Font fontListaBlack = new Font("Helvetica", FontStyle.BOLD, 10.5);
			Font fontLista = new Font("Helvetica", FontStyle.ITALIC, 10.5);
			Font fontTableHeader = new Font("Helvetica", FontStyle.BOLD, 10);
			Font fontTableDetail = new Font("Helvetica", FontStyle.REGULAR, 9.5);
			Font fontParagrafoNotas = new Font("Helvetica", FontStyle.REGULAR, 10);
			Font fontRed = new Font("Helvetica", FontStyle.BOLD, 9.5, Color.RED);
			Font fontLegendTable = new Font("Helvetica", FontStyle.REGULAR, 9.0);

			// Adiciona estilo ao paragrafo (barra cinza, fonte helvetica e tamanho 10.5pt) para ser usado nas seções do laudo.
			OdfOfficeAutomaticStyles styles = laudoDocumento.getContentDom().getAutomaticStyles();
			StyleStyleElement style = styles.newStyleStyleElement(OdfStyleFamily.Paragraph.getName(), "backgroud_text");
			style.newStyleParagraphPropertiesElement().setFoBackgroundColorAttribute("#B2B2B2");
			style.setStyleNameAttribute("teste_de_estilo");
			StyleTextPropertiesElement textStyle = style.newStyleTextPropertiesElement(null);
			textStyle.setFoBackgroundColorAttribute("#B2B2B2");
			textStyle.setFoFontWeightAttribute("bold");
			textStyle.setFoFontFamilyAttribute("Helvetica");
			textStyle.setFoFontSizeAttribute("10.5pt");

			VariableField _campoCoordenadoria = laudoDocumento.getVariableFieldByName("campoCoordenadoria");
			if (_campoCoordenadoria != null) {
				_campoCoordenadoria.updateField((getSetorUsuarioAutenticado().getSetorPrincipal() != null ? getSetorUsuarioAutenticado().getSetorPrincipal().getDescricaoCompleta() : ""), null);
			}

			VariableField _campoNucleo = laudoDocumento.getVariableFieldByName("campoNucleo");
			if (_campoNucleo != null) {
				_campoNucleo.updateField((laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getSetor().getSetorPrincipal() != null
						? laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getSetor().getDescricaoCompleta()
						: ""), null);
			}

			// Section secaoCabecalhoDocumento = laudoDocumento.getSectionByName("secaoCabecalhoDocumento");
			// if (secaoCabecalhoDocumento != null) {
			// limparSecao(secaoCabecalhoDocumento);
			// Paragraph pOrg = secaoCabecalhoDocumento.addParagraph("Perícia Forense do Estado do Ceará - PEFOCE");
			// pOrg.setFont(fontParagrafoBold);
			// Paragraph pCoord = secaoCabecalhoDocumento.addParagraph((getSetorUsuarioAutenticado().getSetorPrincipal() != null ?
			// getSetorUsuarioAutenticado().getSetorPrincipal().getDescricaoCompleta() : ""));
			// pCoord.setFont(fontParagrafoPBold);
			// Paragraph pNucleo = secaoCabecalhoDocumento.addParagraph(getSetorUsuarioAutenticado().getDescricaoCompleta());
			// pNucleo.setFont(fontParagrafoPNormal);
			//
			// }

			// Informa o tipo de documento
			VariableField _campoTipoDocumento = laudoDocumento.getVariableFieldByName("campoTipoDocumento");
			if (_campoTipoDocumento != null) {
				_campoTipoDocumento.updateField(this.tipoLaudo.getDescricao(), null);
			}

			// Section secaoTipoDocumento = laudoDocumento.getSectionByName("secaoTipoDocumento");
			// if (secaoTipoDocumento != null) {
			// limparSecao(secaoTipoDocumento);
			// Paragraph pTipoDoc = secaoTipoDocumento.addParagraph("LAUDO PERICIAL");
			// pTipoDoc.setFont(fontParagrafoBold);
			// }

			// Section secaoNumero = laudoDocumento.getSectionByName("secaoNumero");
			// if (secaoNumero != null) {
			// limparSecao(secaoNumero);
			// Paragraph pTipoDoc = secaoNumero.addParagraph("Número: " + Calendar.getInstance().get(Calendar.YEAR) + "." +
			// StrUtil.lpad(Integer.toString(laudo.getNumero()), 7, '0'));
			// pTipoDoc.setFont(fontParagrafoBold);
			// }

			// INFORMA O TIPO DE EXAME DO LAUDO.
			VariableField _campoTipoExame = laudoDocumento.getVariableFieldByName("campoTipoExame");
			if (_campoTipoExame != null) {
				_campoTipoExame.updateField(laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getDescricao(), null);
			}

			// Section secaoTipoExame = laudoDocumento.getSectionByName("secaoTipoExame");
			// if (secaoTipoExame != null) {
			// limparSecao(secaoTipoExame);
			// Paragraph pTipoExame =
			// secaoTipoExame.addParagraph(laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getDescricao());
			// pTipoExame.setFont(fontParagrafoBold);
			// }

			// ÓRGÃO QUE SOLICITOU O LAUDO
			VariableField _campoOrgaoSolicitante = laudoDocumento.getVariableFieldByName("campoOrgaoSolicitante");
			if (_campoOrgaoSolicitante != null) {
				_campoOrgaoSolicitante.updateField(solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getSetor().getDescricaoCompleta(), null);
			}

			String strNumeroDocumento = solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getTipoDocumento().getDescricao() + " - "
					+ solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getNumero().toString() + "/" + solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getAno();

			// NÚMERO DO DOCUMENTO QUE DEU ORIGEM A SOLICITAÇÃO
			VariableField _campoNumeroDocumento = laudoDocumento.getVariableFieldByName("campoDocumentoSolicitante");
			if (_campoNumeroDocumento != null) {
				_campoNumeroDocumento.updateField(strNumeroDocumento, null);
			}
			// ENDEREÇO DO SETOR QUE REALIZARÁ O LAUDO
			VariableField _campoEndereco = laudoDocumento.getVariableFieldByName("campoEndereco");
			if (_campoEndereco != null) {
				_campoEndereco.updateField(solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getSetor().getEnderecoSetor(), null);
			}
			
			//Preenche campos no texto dos novos modelos de informativos técnicos: positivo, negativo e inconclusivo
			if(laudo.getTipoLaudo().getId() == TipoLaudoEnum.INFORMATIVO_TECNICO.getId() && laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.IDENTIFICACAO_PAPILOSCOPICA.getId()) {
				if (pericia.getListPericiaEvidencia() != null && !pericia.getListPericiaEvidencia().isEmpty()) {
					for (PericiaEvidencia periciaEvidencia : pericia.getListPericiaEvidencia()) {
						if (periciaEvidencia.getEvidencia().getTipoEvidencia().getId().equals(TipoEvidenciaEnum.PESSOA_ENVOLVIDA.getId())) {
							EvidenciaEnvolvidoPessoa cadaver = evidenciaEnvolvidoPessoaDao.buscarPessoaPorEvidencia(periciaEvidencia.getEvidencia());
					
							VariableField _campoPulseira = laudoDocumento.getVariableFieldByName("campoPulseira");
							if(cadaver.getRegistroCadaver() != null) {
								if(_campoPulseira != null)
									_campoPulseira.updateField(cadaver.getRegistroCadaver().toString(), null);
							}else
								if(_campoPulseira != null)
									_campoPulseira.updateField("_______________",null);
							
							VariableField _campoNome = laudoDocumento.getVariableFieldByName("campoNome");
							if(cadaver.getNome() != null && !cadaver.getNome().isEmpty()) {
								if(_campoNome != null)
									_campoNome.updateField(cadaver.getNome(), null);
							}else
								if(_campoNome != null)
									_campoNome.updateField("________________________________________", null);
						
							VariableField _campoNomePai = laudoDocumento.getVariableFieldByName("campoNomePai");
							if(cadaver.getNomePai() != null && !cadaver.getNomePai().isEmpty()) {
								if(_campoNomePai != null)
									_campoNomePai.updateField(cadaver.getNomePai(), null);
							}else
								if(_campoNomePai != null)
									_campoNomePai.updateField("________________________________________", null);
							
							VariableField _campoNomeMae = laudoDocumento.getVariableFieldByName("campoNomeMae");
							if(cadaver.getNomeMae() != null && !cadaver.getNomeMae().isEmpty()) {
								if(_campoNomeMae != null)
									_campoNomeMae.updateField(cadaver.getNomeMae(), null);
							}else
								if(_campoNomeMae != null)
									_campoNomeMae.updateField("________________________________________", null);
						
							VariableField _campoDataNascimento = laudoDocumento.getVariableFieldByName("campoNascimento");
							if(cadaver.getDataNascimento() != null) {
								if(_campoDataNascimento != null) {
									SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
									String dataNascimento = formatador.format(cadaver.getDataNascimento());
									_campoDataNascimento.updateField(dataNascimento, null);
								}	
							}else
								if(_campoDataNascimento != null)
									_campoDataNascimento.updateField("_______________", null);
							
							VariableField _campoMotivo = laudoDocumento.getVariableFieldByName("campoMotivo");
							if(cadaver.getMotivoNaoIdentificado() != null) {
								if(_campoMotivo != null)
									_campoMotivo.updateField(cadaver.getMotivoNaoIdentificado().getDescricao(),null);
							}
						}
					}
				}
			}

			Section secaoPreambulo = laudoDocumento.getSectionByName("secaoPreambulo");
			if (secaoPreambulo != null) {
				limparSecao(secaoPreambulo);

				if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.POTENCIALIDADE_LESIVA.getId()
						|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_PATRIMONIO_VISTORIA.getId()
						|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_VIDA_HOMICIDIO.getId()) {
					Paragraph pPreambulo = secaoPreambulo.addParagraph("PREÂMBULO\n");
					pPreambulo.setFont(fontParagrafoPBold);
				}

				SolicitacaoTramitacao recebimentoSolicitacaoCalf = solicitacaoTramitacaoDao.buscarPrimeiraSolicitacaoRecebida(solicitacaoProcedimentoPericial);

				SolicitacaoTramitacao criacaoSolicitacaoCalf = solicitacaoTramitacaoDao.buscarCriacaoSolicitacao(solicitacaoProcedimentoPericial);

				String preambulo = "";

				if (solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal().getId() == SetorEnum.CALF.getId()) {

					preambulo = "No dia " + ((recebimentoSolicitacaoCalf != null ? StrUtil.dataPorExtenso(recebimentoSolicitacaoCalf.getDataInclusao())
							: (criacaoSolicitacaoCalf != null ? StrUtil.dataPorExtenso(criacaoSolicitacaoCalf.getDataInclusao())
									: StrUtil.dataPorExtenso(solicitacaoProcedimentoPericial.getDataInclusao())))
							+ ", no " + laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getSetor().getDescricaoCompleta() + " da "
							+ (getSetorUsuarioAutenticado().getSetorPrincipal() != null ? laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getSetor().getSetorPrincipal().getDescricaoCompleta()
									: "")
							+ " da Perícia Forense do Estado do Ceará, em " + laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getSetor().getUnidade().getDescricao()
							+ "-CE, deu entrada amostra destinada a exame de " + laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getDescricao() + " referente a(ao) "
							+ solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getTipoDocumento().getDescricao() + " nº. "
							+ solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getNumero().toString() + "/"
							+ solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getAno() + " do(a) "
							+ solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getSetor().getDescricaoCompleta() + ", pelo(a) Coordenador(a) "
							+ (solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal().getGestor().getRegistroFuncional().getTratamento() != null
									? solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal().getGestor().getRegistroFuncional().getTratamento().getAbreviacao() + " "
									: "")
							+ solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal().getGestor().getNome() + ", foi designado como perito(a) relator(a) "
							+ getUsuarioAutenticado().getRegistroFuncional().getTratamento().getAbreviacao() + " " + getUsuarioAutenticado().getNome()
							+ " para proceder ao exame, devendo o(as) perito(as) descrever com a verdade todas as circunstâncias que encontrar, descobrir ou observar e responder ao(s) quesito(s) formulado(s).");
				}

				else if (solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal().getId() == SetorEnum.COPEC.getId()) {

					ProcedimentoSolicitacaoOcorrencia ocorrencia = procedimentoSolicitacaoOcorrenciaDao.buscarUltimaOcorrenciaPorSolicitacao(solicitacaoProcedimentoPericial);

					preambulo = "No dia " + ((laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_VIDA_HOMICIDIO.getId()
							? StrUtil.dataPorExtenso(ocorrencia.getHoraChamada())
							: StrUtil.dataPorExtenso(pericia.getDataInclusao()))
							+ ", no " + laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getSetor().getDescricaoCompleta() + " da "
							+ (solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal() != null
									? laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getSetor().getSetorPrincipal().getDescricaoCompleta()
									: "")
							+ " da Perícia Forense do Estado do Ceará, em " + laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getSetor().getUnidade().getDescricao()
							+ "-CE, pelo Coordenador(a) "
							+ (solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal().getGestor().getRegistroFuncional().getTratamento() != null
									? solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal().getGestor().getRegistroFuncional().getTratamento().getAbreviacao()
									: "")
							+ " " + solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal().getGestor().getNome() + ", foi designado o(a) perito(a) "
							+ getUsuarioAutenticado().getRegistroFuncional().getTratamento().getAbreviacao() + " " + getUsuarioAutenticado().getNome() + " para proceder exame "
							+ laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getDescricao() + ", a fim de atender à solicitação do(a) "
							+ solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getSetor().getDescricaoCompleta() + " de acordo com o(a) "
							+ solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getTipoDocumento().getDescricao() + " de número "
							+ (solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getTipoDocumento().getId() == TipoDocumentoPericialEnum.INCIDENCIA.getId()
									? "I" + solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getAno()
											+ StrUtil.lpad(solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getNumero().toString(), 7, '0')
									: solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getNumero().toString() + "/"
											+ solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getAno())
							+ ", descrevendo com a verdade todas as circunstâncias que encontrar"
							+ (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.POTENCIALIDADE_LESIVA.getId()
									|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_PATRIMONIO_VISTORIA.getId()
											? ", bem assim, esclarecer tudo quanto interessar possa."
											: ", sistematicamente, à luz de princípios técnico-legais do Sistema Criminalístico."));

				} else {

					preambulo = "No dia "
							+ (solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal().getId() == SetorEnum.COMEL.getId()
									? StrUtil.dataPorExtenso(solicitacaoProcedimentoPericial.getDataInclusao())
									: StrUtil.dataPorExtenso(pericia.getDataInclusao()))
							+ ", no " + laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getSetor().getDescricaoCompleta() + " da "
							+ (solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal() != null
									? laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getSetor().getSetorPrincipal().getDescricaoCompleta()
									: "")
							+ " da Perícia Forense do Estado do Ceará, em " + laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getSetor().getUnidade().getDescricao()
							+ "-CE, pelo Coordenador(a) "
							+ (solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal().getGestor().getRegistroFuncional().getTratamento() != null
									? solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal().getGestor().getRegistroFuncional().getTratamento().getAbreviacao()
									: "")
							+ " " + solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal().getGestor().getNome() + ", foi designado o(a) perito(a) "
							+ getUsuarioAutenticado().getRegistroFuncional().getTratamento().getAbreviacao() + " " + getUsuarioAutenticado().getNome() + " para proceder exame "
							+ laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getDescricao() + ", a fim de atender à solicitação do(a) "
							+ solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getSetor().getDescricaoCompleta() + " de acordo com o(a) "
							+ solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getTipoDocumento().getDescricao() + " de número "
							+ solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getNumero().toString() + "/"
							+ solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getAno()
							+ ", descrevendo com a verdade todas as circunstâncias que encontrar, descobrir ou observar, e responder o(s) quesito(s) formulado(s).";

				}
				
				if(laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.TORTURA.getId())
						|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.TORTURA_EM_FLAGRANTE.getId())) {
					Paragraph pPreambulo = secaoPreambulo.addParagraph("PREÂMBULO\n");
					pPreambulo.setFont(fontParagrafoPBold);
					
					preambulo = "Exame de corpo de delito realizado em observância aos requisitos dispostos no Protocolo de Istambul, em conformidade com o Art. 4º da Resolução "
							  + "Nº 414/2021 do Conselho Nacional de Justiça, em atendimento à solicitação judicial cuja numeração consta da capa do presente Laudo Pericial.";
				}
				
				Paragraph pPream = secaoPreambulo.addParagraph(preambulo);
				pPream.setFont(fontParagrafoPNormal);
				pPream.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

			}

			Section secaoHistorico = laudoDocumento.getSectionByName("secaoHistorico");
			if (secaoHistorico != null) {
				limparSecao(secaoHistorico);

				Paragraph pHistorico = secaoHistorico.addParagraph("HISTÓRICO\n");
				pHistorico.setFont(fontParagrafoPBold);

				if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_PATRIMONIO_VISTORIA.getId()) {
					for (PericiaEvidencia periciaEvidencia : pericia.getListPericiaEvidencia()) {
						if (periciaEvidencia.isUtilizaNoLaudo()) {

							String historico = "";

							historico = "Atendendo a solicitação supracitada por volta das " + StrUtil.horaFormatada(solicitacaoProcedimentoPericial.getDataInclusao()) + "hs " + "do dia "
									+ StrUtil.dataPorExtenso(solicitacaoProcedimentoPericial.getDataInclusao())
									+ ", esta equipe pericial composta pelo técnico citado, compareceu no pátio do estacionamento da "
									+ periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getLocalVistoriaVeiculo().getDescricao()
									+ ", onde deu início aos trabalhos técnicos periciais, que se faziam mister.";

							Paragraph pHist = secaoHistorico.addParagraph(historico);
							pHist.setFont(fontParagrafoPNormal);
							pHist.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
						}
					}
				}

				if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_VIDA_HOMICIDIO.getId()) {
					ProcedimentoSolicitacaoOcorrencia ocorrencia = procedimentoSolicitacaoOcorrenciaDao.buscarUltimaOcorrenciaPorSolicitacao(solicitacaoProcedimentoPericial);

					Paragraph pDescricaoHistorico;
					String sHistorico = "";
					sHistorico = sHistorico + ("Por volta das " + (ocorrencia.getHoraChamada() != null ? StrUtil.horaDataFormatada(ocorrencia.getHoraChamada()) : " "));

					sHistorico = sHistorico + (", a equipe pericial foi acionada para atender a solicitação da CIOPS de nº de incidência "
							+ (solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getNumero() != null
									? "I" + solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getAno()
											+ StrUtil.lpad(solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial().getNumero().toString(), 7, '0')
									: " "));

					sHistorico = sHistorico + (", e, logo que possível, iniciou deslocamento, para a " + (ocorrencia.getDescricaoLogradouro() != null ? ocorrencia.getDescricaoLogradouro() : " "));

					sHistorico = sHistorico + (", nas proximidades do imóvel de nº " + (ocorrencia.getNumeroLogradouro() != null ? ocorrencia.getNumeroLogradouro() : " "));

					sHistorico = sHistorico + (", no bairro " + (ocorrencia.getBairro() != null ? StrUtil.toCamelCase(ocorrencia.getBairro().getDescricao()) : " "));

					sHistorico = sHistorico + (", no município " + (ocorrencia.getMunicipio() != null ? StrUtil.toCamelCase(ocorrencia.getMunicipio().getDescricao()) : " "));

					sHistorico = sHistorico + (", onde, segundo informações da CIOPS teria ocorrido uma morte violenta.\n\n");

					sHistorico = sHistorico + ("Chegando ao local, por volta de " + (ocorrencia.getHoraAtendimento() != null ? StrUtil.horaDataFormatada(ocorrencia.getHoraAtendimento()) : " "));

					for (PericiaEvidencia envolvidoPessoa : pericia.getListPericiaEvidencia()) {
						if (envolvidoPessoa.getEvidencia().getTipoEvidencia().getId().equals(TipoEvidenciaEnum.PESSOA_ENVOLVIDA.getId())) {

							if (envolvidoPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getSexo() != null && envolvidoPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getSexo().equals("M")) {

								sHistorico = sHistorico + ", fora constatada a presença de um cadáver do sexo masculino";

								sHistorico = sHistorico
										+ ((envolvidoPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoInstrumento() != null ? ", com lesões compatíveis com as produzidas por instrumento "
												+ envolvidoPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoInstrumento().getDescricao().toLowerCase() : ""));
							}

							if (envolvidoPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getSexo() != null && envolvidoPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getSexo().equals("F")) {
								sHistorico = sHistorico + ", fora constatada a presença de um cadáver do sexo feminino";

								sHistorico = sHistorico
										+ ((envolvidoPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoInstrumento() != null ? ", com lesões compatíveis com as produzidas por instrumento "
												+ envolvidoPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoInstrumento().getDescricao().toLowerCase() : ""));
							}

							if (envolvidoPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getSexo() != null && envolvidoPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getSexo().equals("I")) {
								sHistorico = sHistorico + ", fora constatada a presença de um cadáver do sexo indefinido";

								sHistorico = sHistorico
										+ ((envolvidoPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoInstrumento() != null ? ", com lesões compatíveis com as produzidas por instrumento "
												+ envolvidoPessoa.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoInstrumento().getDescricao().toLowerCase() : ""));
							}
						}
					}

					sHistorico = sHistorico + (". Segundo informação prestada pelos policiais militares responsáveis pelo isolamento do local a ocorrência teria se dado por volta de "
							+ (ocorrencia.getDataHoraOcorrencia() != null ? StrUtil.horaDataFormatada(ocorrencia.getDataHoraOcorrencia()) + ".\n" : " "));

					sHistorico = sHistorico + ("\nEm seguida, este perito deu início aos trabalhos periciais através do exame da vítima, da natureza de suas lesões e dos demais vestígios, "
							+ "bem como dos levantamentos periciais que se faziam necessários, os quais passam a ser definidos nos termos do presente laudo.");

					pDescricaoHistorico = secaoHistorico.addParagraph(sHistorico);

					pDescricaoHistorico.setFont(fontParagrafoPNormal);
					pDescricaoHistorico.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

				}
			}

			Section secaoDadosLocal = laudoDocumento.getSectionByName("secaoDadosLocal");
			if (secaoDadosLocal != null) {
				limparSecao(secaoDadosLocal);

				Paragraph tituloDadosLocal = secaoDadosLocal.addParagraph("ISOLAMENTO E PRESERVAÇÃO DO LOCAL\n");
				tituloDadosLocal.setFont(fontParagrafoPBold);

				Paragraph pDadosIsolamentoLocal;
				String sDadosIsolamentoLocal = "";

				ProcedimentoSolicitacaoOcorrencia ocorrencia = procedimentoSolicitacaoOcorrenciaDao.buscarUltimaOcorrenciaPorSolicitacao(solicitacaoProcedimentoPericial);

				sDadosIsolamentoLocal = sDadosIsolamentoLocal
						+ ("Quando da chegada da perícia, o local encontrava-se " + (ocorrencia.getTipoIsolamentoLocal() != null ? ocorrencia.getTipoIsolamentoLocal().getDescricao() : " "));

				sDadosIsolamentoLocal = sDadosIsolamentoLocal + (" e " + (ocorrencia.getTipoPreservacaoLocal() != null ? ocorrencia.getTipoPreservacaoLocal().getDescricao() : " "));

				sDadosIsolamentoLocal = sDadosIsolamentoLocal
						+ (", estando guarnecido pelos policiais militares comandados pelo " + (ocorrencia.getPolicialResponsavel() != null ? ocorrencia.getPolicialResponsavel() : " "));

				sDadosIsolamentoLocal = sDadosIsolamentoLocal + (", na viatura de prefixo " + (ocorrencia.getPrefixoViatura() != null ? ocorrencia.getPrefixoViatura() + ". " : " "));

				sDadosIsolamentoLocal = sDadosIsolamentoLocal + ("Os policiais militares permaneceram no local até o término dos exames periciais.");

				pDadosIsolamentoLocal = secaoDadosLocal.addParagraph(sDadosIsolamentoLocal);

				pDadosIsolamentoLocal.setFont(fontParagrafoPNormal);
				pDadosIsolamentoLocal.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

				Paragraph tituloDoLocal = secaoDadosLocal.addParagraph("\nDO LOCAL\n");
				tituloDoLocal.setFont(fontParagrafoPBold);

				Paragraph pDadosLocal;
				String sDadosLocal = "";

				sDadosLocal = sDadosLocal + ("O local imediato trata-se de local " + (ocorrencia.getTipoLocal() != null ? ocorrencia.getTipoLocal().getDescricao().toLowerCase() + ", " : " "));

				sDadosLocal = sDadosLocal + (ocorrencia.getDescricaoLocal() != null && !ocorrencia.getDescricaoLocal().isEmpty() ? ocorrencia.getDescricaoLocal().toLowerCase() + ". " : " ");

				sDadosLocal = sDadosLocal + ("A posição geográfica do local dos exames foi determinada através do uso do GPS (Global Positioning System), o qual indicou as coordenadas: "
						+ (ocorrencia.getLatitude() != null || ocorrencia.getLatitude().isEmpty() && ocorrencia.getLongitude() != null || ocorrencia.getLongitude().isEmpty()
								? ocorrencia.getLatitude() + "S " + ocorrencia.getLongitude() + "W. "
								: " "));

				sDadosLocal = sDadosLocal
						+ ("O registro do referido ponto perpetua o posicionamento geográfico do local em questão, independentemente de alterações que porventura venham a ocorrer naquela área. ");

				pDadosLocal = secaoDadosLocal.addParagraph(sDadosLocal);

				pDadosLocal.setFont(fontParagrafoPNormal);
				pDadosLocal.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

			}

			int numeroCadaver = 0;

			Section secaoDadosCadaver = laudoDocumento.getSectionByName("secaoDadosCadaver");
			if (secaoDadosCadaver != null) {
				limparSecao(secaoDadosCadaver);

				Paragraph tituloExames = secaoDadosCadaver.addParagraph("EXAMES");
				tituloExames.setFont(fontParagrafoPBold);

				for (PericiaEvidencia evidenciaEnvolvido : pericia.getListPericiaEvidencia()) {
					if (evidenciaEnvolvido.isUtilizaNoLaudo()) {
						if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_VIDA_HOMICIDIO.getId()) {
							if (evidenciaEnvolvido.getEvidencia().getTipoEvidencia().getId().equals(TipoEvidenciaEnum.PESSOA_ENVOLVIDA.getId())) {

								Paragraph pC = secaoDadosCadaver.addParagraph("\nCADÁVER " + (StrUtil.lpad(Integer.toString(++numeroCadaver), 2, '0') + ""));
								pC.setFont(fontParagrafoBold);
								pC.setHorizontalAlignment(HorizontalAlignmentType.LEFT);

								Paragraph tituloDadosCadaver = secaoDadosCadaver.addParagraph("LOCALIZAÇÃO E POSIÇÃO\n");
								tituloDadosCadaver.setFont(fontParagrafoPBold);

								Paragraph pDadosLocPosicao;
								String sDadosLocPosicao = "";

								sDadosLocPosicao = sDadosLocPosicao + ("O cadáver encontrava-se " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoPosicaoCorpo() != null
										? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoPosicaoCorpo().getDescricao()
										: " "));

								sDadosLocPosicao = sDadosLocPosicao + (", na posição mostrada nas fotografias, sendo o entorno do corpo e suas adjacências os locais de maior interesse pericial.\n");

								pDadosLocPosicao = secaoDadosCadaver.addParagraph(sDadosLocPosicao);

								pDadosLocPosicao.setFont(fontParagrafoPNormal);
								pDadosLocPosicao.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

								Paragraph tituloIdentificacaoCadaver = secaoDadosCadaver.addParagraph("IDENTIFICAÇÃO\n");
								tituloIdentificacaoCadaver.setFont(fontParagrafoPBold);

								Paragraph pDadosCadaver;
								String sDadosCadaver = "";

								sDadosCadaver = sDadosCadaver + ("O cadáver tinha aparência de " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoFaixaEtaria() != null
										? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoFaixaEtaria().getDescricao()
										: " "));

								if (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getSexo() != null
										&& evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getSexo().equals("M")) {

									sDadosCadaver = sDadosCadaver + ", do sexo masculino";

								}

								if (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getSexo() != null
										&& evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getSexo().equals("F")) {
									sDadosCadaver = sDadosCadaver + ", do sexo feminino";

								}

								if (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getSexo() != null
										&& evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getSexo().equals("I")) {
									sDadosCadaver = sDadosCadaver + ", do sexo indefinido";

								}

								sDadosCadaver = sDadosCadaver + (", de cor " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoCorPele() != null
										? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoCorPele().getDescricao()
										: " "));

								sDadosCadaver = sDadosCadaver + (", apresentando estatura " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoEstatura() != null
										? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoEstatura().getDescricao()
										: " "));

								sDadosCadaver = sDadosCadaver + (", e biotipo " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoBiotipo() != null
										? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoBiotipo().getDescricao()
										: " "));

								sDadosCadaver = sDadosCadaver + (", com cabelo " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoCorCabelo() != null
										? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoCorCabelo().getDescricao()
										: " "));

								sDadosCadaver = sDadosCadaver + (", " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoComprimentoCabelo() != null
										? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoComprimentoCabelo().getDescricao()
										: " "));

								sDadosCadaver = sDadosCadaver + (", " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoCabelo() != null
										? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoCabelo().getDescricao() + ".\n"
										: " "));

								// SE A VITIMA ENVOLVIDA FOR IDENTIFICADA E DADOS FOREM PREENCHIDOS NO ACOLHIMENTO
								if (!evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().isDesconhecido()) {
									if (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getListaPessoaDocumento() != null
											&& !evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getListaPessoaDocumento().isEmpty()) {

										for (PessoaDocumento evidenciaPessoaDocumento : evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getListaPessoaDocumento()) {

											sDadosCadaver = sDadosCadaver + ("A vítima foi identificada como " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNome() != null
													? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNome().toLowerCase()
													: " "));

											sDadosCadaver = sDadosCadaver + (", portador do documento "
													+ (evidenciaPessoaDocumento.getPessoaTipoDocumento() != null ? evidenciaPessoaDocumento.getPessoaTipoDocumento().getDescricao() : " "));

											sDadosCadaver = sDadosCadaver + (", de nº " + (evidenciaPessoaDocumento.getNumero() != null ? evidenciaPessoaDocumento.getNumero() : " "));

										}

										// Se Documento contem nome de pai e mãe
										if (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNomePai() != null
												&& !evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNomePai().isEmpty()
												|| evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNomeMae() != null
														&& !evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNomeMae().isEmpty()) {

											sDadosCadaver = sDadosCadaver + (", filho de " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNomePai() + " e "
													+ evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNomeMae()));

											// Se conter apenas nome de pai
										} else if (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNomePai() != null
												&& !evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNomePai().isEmpty()
												|| evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNomeMae() == null
														&& evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNomeMae().isEmpty()) {

											sDadosCadaver = sDadosCadaver + (", filho de " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNomePai()));

											// Se conter apenas nome de mae
										} else if (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNomeMae() != null
												&& !evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNomeMae().isEmpty()
												|| evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNomePai() == null
														&& evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNomePai().isEmpty()) {

											sDadosCadaver = sDadosCadaver + (", filho de " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNomeMae()));
										}

										sDadosCadaver = sDadosCadaver + (", nascido em " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getDataNascimento() != null
												? StrUtil.dataFormatada(evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getDataNascimento())
												: " "));

										sDadosCadaver = sDadosCadaver + ((evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNaturalidade() != null
												? ", no município de " + evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getNaturalidade().getDescricao() + ". "
												: " "));

										sDadosCadaver = sDadosCadaver + ("\nO corpo da vítima deu entrada na Coordenadoria de Medicina Legal com o registro de cadáver nº "
												+ (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getRegistroCadaver() != null
														? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getRegistroCadaver() + ".\n"
														: " "));
									}
								}
								// SE O ENVOLVIDO NÃO FOR IDENTIFICADO
								if (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().isDesconhecido()) {

									sDadosCadaver = sDadosCadaver + ("A vítima não portava consigo documento que possibilitasse sua identificação, dando entrada na COMEL como DESCONHECIDO DO SEXO "
											+ (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getSexo() != null
													? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getSexo()
													: " "));

									sDadosCadaver = sDadosCadaver + ("com o registro de cadáver nº " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getRegistroCadaver() != null
											? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getRegistroCadaver() + ".\n"
											: " "));

								}

								pDadosCadaver = secaoDadosCadaver.addParagraph(sDadosCadaver);

								pDadosCadaver.setFont(fontParagrafoPNormal);
								pDadosCadaver.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

								if (evidenciaEnvolvido.isUtilizaNoLaudo()) {

									Paragraph pTituloVestes = secaoDadosCadaver.addParagraph("VESTES\n");
									pTituloVestes.setFont(fontParagrafoPBold);

									Paragraph pDescricaoVestes;
									String sVestes = "";

									sVestes = sVestes + ("A vítima trajava " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getVesteSuperior() != null
											? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getVesteSuperior().toLowerCase()
											: " "));

									sVestes = sVestes + (", de cor predominante " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getCorVesteSuperior() != null
											? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getCorVesteSuperior().toLowerCase() + ". "
											: " "));

									sVestes = sVestes + ((evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getVesteInferior() != null
											? StrUtil.toCamelCase(evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getVesteInferior())
											: " "));

									sVestes = sVestes + (", de cor predominante " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getCorVesteInferior() != null
											? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getCorVesteInferior().toLowerCase() + ". "
											: " "));

									// SE O ENVOLVIDO ESTAVA CALÇADO
									if (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().isCalcado()) {

										sVestes = sVestes + ("Calçava " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getDescricaoCalcado() != null
												? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getDescricaoCalcado().toLowerCase()
												: " "));

										sVestes = sVestes + (", de cor " + (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getCorCalcado() != null
												? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getCorCalcado().toLowerCase()
												: " "));

										sVestes = sVestes + (", como mostram as fotografias.");

									} else {

										sVestes = sVestes + (" e tinha os pés descalços como mostram as fotografias.\n ");

									}

									pDescricaoVestes = secaoDadosCadaver.addParagraph(sVestes);

									pDescricaoVestes.setFont(fontParagrafoPNormal);
									pDescricaoVestes.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

									Paragraph pTituloBuscas = secaoDadosCadaver.addParagraph("\nBUSCAS\n");
									pTituloBuscas.setFont(fontParagrafoPBold);

									Paragraph pDescricaoBuscas;
									String sBuscas = "";

									if (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getPertences() != null) {

										sBuscas = sBuscas + ("Durante as buscas na superfície corporal e nas vestes da vítima, foram encontrados os seguintes pertences: "
												+ (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getPertences() + ". "));

										sBuscas = sBuscas + ("Após liberação do local e dos pertences da vítima que não possuíam interesse pericial, os mesmos foram destinados a "
												+ (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getDestinoPertences() != null
														? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getDestinoPertences().getDescricao() + "."
														: " "));

									} else {

										sBuscas = sBuscas + ("Durante as buscas na superfície corporal e nas vestes da vítima, não foram encontrados pertences de interesse pericial.\n ");

									}

									pDescricaoBuscas = secaoDadosCadaver.addParagraph(sBuscas);

									pDescricaoBuscas.setFont(fontParagrafoPNormal);
									pDescricaoBuscas.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

								}

								if (evidenciaEnvolvido.isUtilizaNoLaudo()) {

									Paragraph pTituloExamePerinecroscopico = secaoDadosCadaver.addParagraph("\nEXAME PERINECROSCÓPICO\n");
									pTituloExamePerinecroscopico.setFont(fontParagrafoPBold);

									Paragraph pDescricaoExamePerinecroscopico;
									String sExamePerinecoscopico = "";

									sExamePerinecoscopico = sExamePerinecoscopico
											+ ("Examinando a superfície corporal do cadáver acima descrito e de acordo com as condições existentes no local de crime, constatou-se:");

									sExamePerinecoscopico = sExamePerinecoscopico + ("\nAusência de sinais vitais; ");

									sExamePerinecoscopico = sExamePerinecoscopico + ("\nTemperatura corporal baixa; ");

									sExamePerinecoscopico = sExamePerinecoscopico + "\nRigidez cadavérica "
											+ (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoRigidezCadaverica() != null
													? evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getTipoRigidezCadaverica().getDescricao().toLowerCase() + ";"
													: " ");

									sExamePerinecoscopico = sExamePerinecoscopico + ("\nMarcas de hipóstases nas regiões declivosas do cadáver;\n");

									pDescricaoExamePerinecroscopico = secaoDadosCadaver.addParagraph(sExamePerinecoscopico);

									pDescricaoExamePerinecroscopico.setFont(fontParagrafoPNormal);
									pDescricaoExamePerinecroscopico.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
								}

								boolean paragrafoLesaoGerado = false;

								Table tableLesao;

								tableLesao = laudoDocumento.getTableByName("tabelaLesao" + evidenciaEnvolvido.getEvidencia().getId().toString());

								if (!paragrafoLesaoGerado) {
									Paragraph pL = secaoDadosCadaver.addParagraph("TABELA LESÕES ENVOLVIDO\n");
									pL.setFont(fontParagrafoBold);
									pL.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
									laudoDocumento.addPageBreak();
									paragrafoLesaoGerado = true;
								}

								evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido()
										.setListaPessoaLesao(pessoaLesaoDao.buscarPessoaLesaoPorEnvolvidoPessoa(evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido()));

								if (evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getListaPessoaLesao() != null
										|| !evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getListaPessoaLesao().isEmpty()) {

									for (PessoaLesao evidenciaLesao : evidenciaEnvolvido.getEvidencia().getUltimaEvidenciaEnvolvido().getListaPessoaLesao()) {
										if (evidenciaLesao != null) {
											if (secaoDadosCadaver != null) {
												tableLesao = secaoDadosCadaver.getTableByName("tabelaLesao" + evidenciaEnvolvido.getEvidencia().getId().toString());

												if (tableLesao == null) {
													// Montando cabeçalho da tabela de Lesoes
													tableLesao = secaoDadosCadaver.addTable(1, 5);
													tableLesao.setTableName("tabelaLesao" + evidenciaEnvolvido.getEvidencia().getId().toString());

													// Montando colunas da tabela de lesões
													Cell cellLesao0 = tableLesao.getCellByPosition(0, 0);
													cellLesao0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellLesao0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellLesao0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellLesao0.addParagraph("QTDE.").setFont(fontTableHeader);

													Cell cellLesao1 = tableLesao.getCellByPosition(1, 0);
													cellLesao1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellLesao1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellLesao1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellLesao1.addParagraph("TIPO LESÃO").setFont(fontTableHeader);

													Cell cellLesao2 = tableLesao.getCellByPosition(2, 0);
													cellLesao2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellLesao2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellLesao2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellLesao2.addParagraph("LOC. LESÃO").setFont(fontTableHeader);

													Cell cellLesao3 = tableLesao.getCellByPosition(3, 0);
													cellLesao3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellLesao3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellLesao3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellLesao3.addParagraph("EFEITO SECUNDÁRIO").setFont(fontTableHeader);

													Cell cellLesao4 = tableLesao.getCellByPosition(4, 0);
													cellLesao4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellLesao4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellLesao4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellLesao4.addParagraph("LOC. EFEITO SECUNDÁRIO").setFont(fontTableHeader);
												}

												if (tableLesao != null) {

													int linhaTabela = tableLesao.getRowCount();

													// QUANTIDADE
													Cell cellLesao0 = tableLesao.getCellByPosition(0, linhaTabela);
													cellLesao0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellLesao0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellLesao0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellLesao0.addParagraph(evidenciaLesao != null ? evidenciaLesao.getQuantidade() : " ").setFont(fontTableDetail);

													// TIPO LESÃO
													Cell cellLesao1 = tableLesao.getCellByPosition(1, linhaTabela);
													cellLesao1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellLesao1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellLesao1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellLesao1.addParagraph(evidenciaLesao.getTipoLesao() != null ? evidenciaLesao.getTipoLesao().getApelido() : " ").setFont(fontTableDetail);

													// LOCALIZACAO
													Cell cellLesao2 = tableLesao.getCellByPosition(2, linhaTabela);
													cellLesao2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellLesao2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellLesao2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellLesao2.setFont(fontTableDetail);
													cellLesao2.addParagraph(evidenciaLesao.getLocalizacaoLesao() != null ? evidenciaLesao.getLocalizacaoLesao().getDescricao() : " ");

													if (evidenciaLesao.getEfeitoSecundarioLesaoEnvolvido() != null) {
														// EFEITO SECUNDARIO
														Cell cellLesao3 = tableLesao.getCellByPosition(3, linhaTabela);
														cellLesao3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
														cellLesao3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
														cellLesao3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
														cellLesao3.setFont(fontTableDetail);
														cellLesao3.addParagraph(
																evidenciaLesao.getEfeitoSecundarioLesaoEnvolvido() != null ? evidenciaLesao.getEfeitoSecundarioLesaoEnvolvido().getApelido() : " ");

														// LOCALIZACAO EFEITO SECUNDARIO
														Cell cellLesao4 = tableLesao.getCellByPosition(4, linhaTabela);
														cellLesao4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
														cellLesao4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
														cellLesao4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
														cellLesao4.setFont(fontTableDetail);
														cellLesao4.addParagraph(
																evidenciaLesao.getLocalizacaoLesaoSecundariaEnvolvido() != null ? evidenciaLesao.getLocalizacaoLesaoSecundariaEnvolvido().getDescricao()
																		: " ");
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}

			Section secaoDosExames = laudoDocumento.getSectionByName("secaoDosExames");
			if (secaoDosExames != null) {
				limparSecao(secaoDosExames);

				for (PericiaEvidencia periciaEvidencia : pericia.getListPericiaEvidencia()) {
					if (periciaEvidencia.isUtilizaNoLaudo()) {
						if (periciaEvidencia.getEvidencia().getTipoEvidencia().getId().equals(TipoEvidenciaEnum.PESSOA_ENVOLVIDA.getId())) {

							if (periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().isVeiculoComAvarias()) {
								if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_PATRIMONIO_VISTORIA.getId()) {
									Paragraph pDosExames = secaoDosExames.addParagraph("DOS EXAMES:\n");
									pDosExames.setFont(fontParagrafoPBold);
								}

								Paragraph pDescricaoAosExames;
								String dosExames = "";

								if (periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getListaTipolocalVistoriaTerco() != null
										&& periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getListaTipolocalVistoriaTerco().length > 0) {
									dosExames = dosExames + "Prosseguindo os exames de praxe para fatos desta natureza, este técnico constatou que o veículo examinado apresentava dano(s) ";
									for (int descricaoLocalVistoriaTerco : periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getListaTipolocalVistoriaTerco()) {
										for (TipoRespostaLocalVistoriaTercoEnum localVistoriaTerco : TipoRespostaLocalVistoriaTercoEnum.values()) {
											if (localVistoriaTerco.getId() == descricaoLocalVistoriaTerco)
												dosExames = dosExames + (localVistoriaTerco.getDescricao() + ", ");
										}
									}
								}

								if (periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getListaTipolocalVistoriaLateralVeiculo() != null
										&& periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getListaTipolocalVistoriaLateralVeiculo().length > 0) {
									dosExames = dosExames + "no setor(es) ";
									for (int descricaoLocalVistoriaLateral : periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getListaTipolocalVistoriaLateralVeiculo()) {
										for (TipoRespostaLocalVistoriaLateralVeiculoEnum localVistoriaLateral : TipoRespostaLocalVistoriaLateralVeiculoEnum.values()) {
											if (localVistoriaLateral.getId() == descricaoLocalVistoriaLateral)
												dosExames = dosExames + (localVistoriaLateral.getDescricao() + ", ");
										}
									}
								}

								dosExames = dosExames + (periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getTipoCaracteristicaProduzidaInstrumento() != null
										? "com características produzidas por instrumento "
												+ periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getTipoCaracteristicaProduzidaInstrumento().getDescricao().toLowerCase()
										: " ");

								dosExames = dosExames + (".");

								pDescricaoAosExames = secaoDosExames.addParagraph(dosExames);

								pDescricaoAosExames.setFont(fontParagrafoPNormal);
								pDescricaoAosExames.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

							}
						}
					}
				}
			}

			Section secaoIdentificacaoVeiculo = laudoDocumento.getSectionByName("secaoIdentificacaoVeiculo");
			if (secaoIdentificacaoVeiculo != null) {
				limparSecao(secaoIdentificacaoVeiculo);

				for (PericiaEvidencia periciaEvidencia : pericia.getListPericiaEvidencia()) {
					if (periciaEvidencia.isUtilizaNoLaudo()) {

						if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_PATRIMONIO_VISTORIA.getId()) {
							Paragraph pIdentificacaoVeiculo = secaoIdentificacaoVeiculo.addParagraph("IDENTIFICAÇÃO DO VEÍCULO EXAMINADO:\n");
							pIdentificacaoVeiculo.setFont(fontParagrafoPBold);
						}

						Paragraph pDescricaoIdentificacaoVeiculo;
						String sIdentificacaoVeiculo = "";

						sIdentificacaoVeiculo = sIdentificacaoVeiculo + (periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getEvidenciaVeiculoMarca() != null
								? "Marca: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getEvidenciaVeiculoMarca().getDescricao()
								: " ");

						sIdentificacaoVeiculo = sIdentificacaoVeiculo + (periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getEvidenciaVeiculoTipo() != null
								? "\nTipo: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getEvidenciaVeiculoTipo().getDescricao()
								: " ");

						sIdentificacaoVeiculo = sIdentificacaoVeiculo + (periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getEvidenciaVeiculoModelo() != null
								? "\nModelo: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getEvidenciaVeiculoModelo().getDescricao()
								: " ");

						sIdentificacaoVeiculo = sIdentificacaoVeiculo + (periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getPlacaOriginal() != null
								? "\nPlacas: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getPlacaOriginal()
								: " ");

						sIdentificacaoVeiculo = sIdentificacaoVeiculo + (periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getCor() != null
								? "\nCor: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getCor().getDescricao()
								: " ");

						sIdentificacaoVeiculo = sIdentificacaoVeiculo + (periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getProprietario() != null
								? "\nRegistrado em nome de: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getProprietario()
								: " ");

						sIdentificacaoVeiculo = sIdentificacaoVeiculo + (periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getCondutor() != null
								? "\nGuiado por: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getCondutor()
								: " ");

						sIdentificacaoVeiculo = sIdentificacaoVeiculo + (periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getCondutorCpf() != null
								? ", com CPF: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getCondutorCpf()
								: " ");

						sIdentificacaoVeiculo = sIdentificacaoVeiculo + (periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getCondutorRg() != null
								? ", com RG: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaVeiculo().getCondutorRg()
								: " ");

						sIdentificacaoVeiculo = sIdentificacaoVeiculo + (".");

						pDescricaoIdentificacaoVeiculo = secaoIdentificacaoVeiculo.addParagraph(sIdentificacaoVeiculo);

						pDescricaoIdentificacaoVeiculo.setFont(fontParagrafoPNormal);
						pDescricaoIdentificacaoVeiculo.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
					}
				}
			}

			SolicitacaoProcedimentoPericialFormulario ultimoFormulario = solicitacaoProcedimentoFormularioDao.buscarUltimoFormulario(solicitacaoProcedimentoPericial);
			if (ultimoFormulario == null && solicitacaoProcedimentoPericial.getSolicitacaoProcedimentoPericial() != null) {
				ultimoFormulario = solicitacaoProcedimentoFormularioDao.buscarUltimoFormulario(solicitacaoProcedimentoPericial.getSolicitacaoProcedimentoPericial());
			}

			if (solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal().getId() == SetorEnum.CALF.getId()) {

				for (PericiaEvidencia periciaEvidencia : pericia.getListPericiaEvidencia()) {
					if (periciaEvidencia.isUtilizaNoLaudo() && periciaEvidencia.getEvidencia().getTipoEvidencia().equals(TipoEvidenciaEnum.MATERIAL_QUIMICO_BIOLOGICO)) {

						Section secaoMaterialRecebido = laudoDocumento.getSectionByName("secaoMaterialRecebido");
						if (secaoMaterialRecebido != null) {
							limparSecao(secaoMaterialRecebido);

							if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.IDENTIFICACAO_DE_MACONHA_E_HAXIXE.getId()
									|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.IDENTIFICACAO_DE_COCAINA.getId()) {

								Paragraph pObjetivo = secaoMaterialRecebido.addParagraph("OBJETIVO");
								pObjetivo.setFont(fontParagrafoPBold);

								if (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoSubstancia().equals(TipoSubstanciaEnum.MACONHA)) {
									Paragraph pRespostaObjetivo = secaoMaterialRecebido.addParagraph("Identificação de maconha.\n");
									pRespostaObjetivo.setFont(fontParagrafoPNormal);
								}

								if (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoSubstancia().equals(TipoSubstanciaEnum.COCAINA)
										|| periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoSubstancia().equals(TipoSubstanciaEnum.CRACK)) {
									Paragraph pRespostaObjetivo = secaoMaterialRecebido.addParagraph("Identificação de cocaína.\n");
									pRespostaObjetivo.setFont(fontParagrafoPNormal);
								}
							}

							if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.IDENTIFICACAO_DE_MACONHA_E_HAXIXE.getId()
									|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.IDENTIFICACAO_DE_COCAINA.getId()) {

								if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.IDENTIFICACAO_DE_MACONHA_E_HAXIXE.getId()
										|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.IDENTIFICACAO_DE_COCAINA.getId()) {
									Paragraph pTituloMaterialRecebido = secaoMaterialRecebido.addParagraph("MATERIAL RECEBIDO");
									pTituloMaterialRecebido.setFont(fontParagrafoPBold);
								}
								Paragraph pDescricaoMaterialRecebido;
								String sMaterialRecebido = "";
								sMaterialRecebido = sMaterialRecebido + (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getQuantidadeEmbalagens() != null
										? "Apresentava em anexo " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getQuantidadeEmbalagens()
										: "");

								sMaterialRecebido = sMaterialRecebido + (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaMaterialApresentacao() != null
										? " " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaMaterialApresentacao().getDescricao().toLowerCase()
										: "");

								if (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaFechadoPor() != null
										&& periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaFechadoPor().equals(TipoFechamentoEnum.OUTRO)) {
									sMaterialRecebido = sMaterialRecebido + (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaFechadoPor() != null
											? " fechado(s) por " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getFechadoPorOutroTipo().toLowerCase()
											: "");
								} else {
									sMaterialRecebido = sMaterialRecebido + (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaFechadoPor() != null
											? " fechado(s) por " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaFechadoPor().getDescricao().toLowerCase()
											: "");
								}

								if (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaContendoEmbalagem() != null
										&& periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaContendoEmbalagem().equals(TipoEvidenciaContendoEmbalagemEnum.OUTRO)) {
									sMaterialRecebido = sMaterialRecebido + ", contendo " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getQuantidadeEmbalagensContendo() + " "
											+ periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaContendoOutroTipoEmbalagem();

								} else if (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaContendoEmbalagem() != null && periciaEvidencia.getEvidencia()
										.getUltimaEvidenciaMaterial().getEvidenciaContendoEmbalagem().equals(TipoEvidenciaContendoEmbalagemEnum.CIGARRO_TIPO_ARTESANAL)) {
									sMaterialRecebido = sMaterialRecebido + ", contendo " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getQuantidadeEmbalagensContendo() + " "
											+ periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaContendoEmbalagem().getDescricao().toLowerCase() + " confeccionado com "
											+ periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaCigarroConfeccionadoPor();

								} else if (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaContendoEmbalagem() != null) {
									sMaterialRecebido = sMaterialRecebido + ", contendo " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getQuantidadeEmbalagensContendo() + " "
											+ periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaContendoEmbalagem().getDescricao().toLowerCase();
								}

								if (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEmbalagemFechadaPor() != null
										&& periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEmbalagemFechadaPor() != (TipoFechamentoEnum.OUTRO)) {
									sMaterialRecebido = sMaterialRecebido + "fechada(s) por "
											+ periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEmbalagemFechadaPor().getDescricao().toLowerCase();

								} else if (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEmbalagemFechadaPorOutroTipo() != null) {
									sMaterialRecebido = sMaterialRecebido + "fechada(s) por "
											+ periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEmbalagemFechadaPorOutroTipo().toLowerCase();
								}

								sMaterialRecebido = sMaterialRecebido + (", acondicionando a(s) amostra(s).");

								pDescricaoMaterialRecebido = secaoMaterialRecebido.addParagraph(sMaterialRecebido);

								pDescricaoMaterialRecebido.setFont(fontParagrafoPNormal);
								pDescricaoMaterialRecebido.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

								Paragraph pTituloAoExame = secaoMaterialRecebido.addParagraph("\nAO EXAME:");
								pTituloAoExame.setFont(fontParagrafoPBold);

								Paragraph pDescricaoAoExame;
								String sAoExame = "";

								sAoExame = sAoExame + (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoAspectoSubstancia() != null
										? "Tratava-se de amostra de " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoAspectoSubstancia().getDescricao().toLowerCase()
										: "");

								sAoExame = sAoExame + (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getCor() != null
										? ", de cor " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getCor().getDescricao().toLowerCase()
										: "");

								if (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().isSubstanciaPrensada() == true)
									sAoExame = sAoExame + (", prensada");
								else
									sAoExame = sAoExame + ("");

								if (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().isSubstanciaFragmentada() == true)
									sAoExame = sAoExame + (", fragmentado");
								else
									sAoExame = sAoExame + ("");

								sAoExame = sAoExame + (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoConsistencia() != null
										? ", de consistência " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoConsistencia().getDescricao().toLowerCase()
										: "");

								sAoExame = sAoExame + (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoConstituido() != null
										? ", constituída por " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoConstituido().getDescricao().toLowerCase()
										: "");

								sAoExame = sAoExame + (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoPeso() != null
										? ". O peso " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getTipoPeso().getDescricao().toLowerCase()
										: "");

								sAoExame = sAoExame + (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getQuantidade() != null
										? " total foi de " + StrUtil.arredondarDuasCasasDecimais(periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getQuantidade()) + " "
										: "");

								sAoExame = sAoExame + (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getUnidadeMedida() != null
										? periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getUnidadeMedida().getDescricao().toLowerCase()
										: "");

								sAoExame = sAoExame + (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getQuantidadeUtilizada() != null
										? ", sendo subtraído " + StrUtil.arredondarDuasCasasDecimais(periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getQuantidadeUtilizada()) + " "
										: "");

								sAoExame = sAoExame + (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getUnidadeMedidaUtilizada() != null
										? periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getUnidadeMedidaUtilizada().getDescricao().toLowerCase() + " para análises"
										: "");

								sAoExame = sAoExame
										+ (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().isMaterialTotalmenteConsumido() ? ". A amostra foi totalmente consumida nas análises" : "");

								sAoExame = sAoExame + ".";

								pDescricaoAoExame = secaoMaterialRecebido.addParagraph(sAoExame);

								pDescricaoAoExame.setFont(fontParagrafoPNormal);
								pDescricaoAoExame.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
							}

							if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.ALCOOLEMIA.getId()
									|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_ETANOL_EM_AMOSTRA_BIOLOGICA.getId())
									|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_SANGUE.getId()
									|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_URINA.getId()) {

//								Paragraph pTituloMaterialRecebido = secaoMaterialRecebido.addParagraph("MATERIAL RECEBIDO");
//								pTituloMaterialRecebido.setFont(fontParagrafoPBold);

								Paragraph pDescricaoMaterialRecebido;
								String sMaterialRecebido = "";
								if (!periciaEvidencia.getEvidencia().getTipoEvidencia().getId().equals(TipoEvidenciaEnum.PESSOA_ENVOLVIDA.getId())) {

									sMaterialRecebido = sMaterialRecebido + (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaMaterialApresentacao() != null
											? "Amostra acondicionada em " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaMaterialApresentacao().getDescricao().toLowerCase()
											: "");

									sMaterialRecebido = sMaterialRecebido + (", identificado(s) por etiqueta ID ");

									for (PericiaEvidencia evidencia : pericia.getListPericiaEvidencia()) {
										if (!evidencia.getEvidencia().getTipoEvidencia().getId().equals(TipoEvidenciaEnum.PESSOA_ENVOLVIDA.getId())) {
											if (pericia.getListPericiaEvidencia().size() > 1) {
												sMaterialRecebido = sMaterialRecebido + (evidencia.getEvidencia().getId() + ", ");
											} else {
												sMaterialRecebido = sMaterialRecebido + (evidencia.getEvidencia().getId());
											}
										}
									}

									if (ultimoFormulario != null) {
										if (ultimoFormulario.getListaTipolocalDaColeta() != null && ultimoFormulario.getListaTipolocalDaColeta().length > 0) {
											sMaterialRecebido = sMaterialRecebido + ", coletada do(as) ";
											for (int descricaoLocalColeta : ultimoFormulario.getListaTipolocalDaColeta()) {
												for (TipoRespostaLocalDaColetaEnum localColeta : TipoRespostaLocalDaColetaEnum.values()) {
													if (ultimoFormulario.getListaTipolocalDaColeta().length > 1) {
														if (localColeta.getId() == descricaoLocalColeta)
															sMaterialRecebido = sMaterialRecebido + (localColeta.getDescricao() + "/");
													} else {
														if (localColeta.getId() == descricaoLocalColeta) {
															sMaterialRecebido = sMaterialRecebido + (localColeta.getDescricao());
														}
													}
												}
											}
										}
									}

									sMaterialRecebido = sMaterialRecebido + (".");

									sMaterialRecebido = sMaterialRecebido.replace(", .", ".");

									sMaterialRecebido = sMaterialRecebido.replace(", ,", ",");

									pDescricaoMaterialRecebido = secaoMaterialRecebido.addParagraph(sMaterialRecebido);

									pDescricaoMaterialRecebido.setFont(fontParagrafoPNormal);
									pDescricaoMaterialRecebido.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
								}
							}
						}
					}
				}
			}

			if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.POTENCIALIDADE_LESIVA.getId())) {
				for (PericiaEvidencia periciaEvidencia : pericia.getListPericiaEvidencia()) {
					if (periciaEvidencia.isUtilizaNoLaudo()) {

						Section secaoObjetoRecebido = laudoDocumento.getSectionByName("secaoMaterialRecebido");
						if (secaoObjetoRecebido != null) {
							limparSecao(secaoObjetoRecebido);

							Paragraph pObjetivo = secaoObjetoRecebido.addParagraph("OBJETIVO:\n");
							pObjetivo.setFont(fontParagrafoPBold);

							Paragraph pRespostaObjetivo = secaoObjetoRecebido.addParagraph("A perícia tem por objetivo proceder ao exame em "
									+ (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaTipoArmaBranca().getDescricao().toLowerCase())
									+ ", a fim de que seja atendida a solicitação constante no ofício supramencionado, parcialmente transcrita na sequência abaixo:\n ");

							Paragraph pCitacaoObjetivo = secaoObjetoRecebido.addParagraph("[...] que seja informado o seguinte... [...].\n");

							pRespostaObjetivo.setFont(fontParagrafoPNormal);
							pRespostaObjetivo.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
							pCitacaoObjetivo.setFont(fontParagrafoPNormal);

							Paragraph pTituloMaterialRecebido = secaoObjetoRecebido.addParagraph("MATERIAL RECEBIDO:\n");
							pTituloMaterialRecebido.setFont(fontParagrafoPBold);

							Paragraph pDescricaoObjetoRecebido;
							String sObjetoRecebido = "";

							sObjetoRecebido = sObjetoRecebido
									+ (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaObjetoApresentacao() != null ? "O material foi recebido com embalagem "
											+ periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaObjetoApresentacao().getDescricao().toLowerCase() : "");

							if (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaObjetoFechadoPor().equals(TipoFechamentoEnum.OUTRO)) {
								sObjetoRecebido = sObjetoRecebido + (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaObjetoFechadoPor() != null
										? " fechado(a) por " + periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaObjetoFechadoPorOutroTipo().toLowerCase()
										: "");
							} else {
								sObjetoRecebido = sObjetoRecebido + (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaObjetoFechadoPor() != null
										? " fechado(a) por " + periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaObjetoFechadoPor().getDescricao().toLowerCase()
										: "");
							}

							sObjetoRecebido = sObjetoRecebido + (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getNumeroLacre() != null && !periciaEvidencia.getEvidencia()
									.getUltimaEvidenciaObjeto().getNumeroLacre().isEmpty() ? " com lacre nº " + periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getNumeroLacre()
											+ ", autocolante e destrutivo. O qual foi realizado um recorte abaixo do lacre a fim de evitar a destruição e inserir dentro da nova embalagem seguindo o rito da cadeia de custódia vigente.\n"
											: "");

							if (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getNumeroLacre() == null
									|| periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getNumeroLacre().isEmpty())
								sObjetoRecebido = sObjetoRecebido + (" sem a presença do lacre. Após o cadastro do(a) ")
										+ periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaTipoArmaBranca().getDescricao().toLowerCase()
										+ (" no sistema galileu, foi gerado uma etiqueta de identificação de nº ") + periciaEvidencia.getEvidencia().getId() + ".\n";

							sObjetoRecebido = sObjetoRecebido + ("");

							pDescricaoObjetoRecebido = secaoObjetoRecebido.addParagraph(sObjetoRecebido);

							pDescricaoObjetoRecebido.setFont(fontParagrafoPNormal);
							pDescricaoObjetoRecebido.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

							Paragraph pTituloDoExameDeConstatacao = secaoObjetoRecebido.addParagraph("DO EXAME DE CONSTATAÇÃO:\n");
							pTituloDoExameDeConstatacao.setFont(fontParagrafoPBold);

							Paragraph pDescricaoExameDeConstatacao;
							String sObjetoExameDeConstatacao = "";

							sObjetoExameDeConstatacao = sObjetoExameDeConstatacao + (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaTipoArmaBranca() != null
									? "Trata-se de um(a) " + periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaTipoArmaBranca().getDescricao().toLowerCase()
									: "");

							sObjetoExameDeConstatacao = sObjetoExameDeConstatacao + (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getTamanhoTotalObjeto() != null
									? " de tamanho total " + periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getTamanhoTotalObjeto() + " centímetros de comprimento"
									: "");

							if (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getDescricaoMarca() != null
									&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getDescricaoMarca().isEmpty())
								sObjetoExameDeConstatacao = sObjetoExameDeConstatacao + (", de marca " + periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getDescricaoMarca().toLowerCase());

							sObjetoExameDeConstatacao = sObjetoExameDeConstatacao + (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getTipoCaboArmaBranca() != null
									? ", cabo de " + periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getTipoCaboArmaBranca().getDescricao().toLowerCase()
									: "");

							sObjetoExameDeConstatacao = sObjetoExameDeConstatacao + (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getTamanhoTotalCabo() != null
									? ", medindo " + periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getTamanhoTotalCabo() + " centímetros de comprimento"
									: "");

							sObjetoExameDeConstatacao = sObjetoExameDeConstatacao + (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getCor() != null
									? ", de cor " + periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getCor().getDescricao().toLowerCase()
									: "");

							sObjetoExameDeConstatacao = sObjetoExameDeConstatacao + (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getTipoFixacaoCaboELmaniaArmaBranca() != null
									? ", fixação por " + periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getTipoFixacaoCaboELmaniaArmaBranca().getDescricao().toLowerCase()
									: "");

							sObjetoExameDeConstatacao = sObjetoExameDeConstatacao + (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaTipoLamina() != null
									? " na lâmina de " + periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getEvidenciaTipoLamina().getDescricao().toLowerCase()
									: "");

							sObjetoExameDeConstatacao = sObjetoExameDeConstatacao + (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getTamanhoTotalLamina() != null
									? ", medindo " + periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getTamanhoTotalLamina() + " centímetros de comprimento"
									: "");

							if (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().isAcompanhaBainha()) {
								sObjetoExameDeConstatacao = sObjetoExameDeConstatacao + (", acompanhado de uma bainha.\n");
							} else {
								sObjetoExameDeConstatacao = sObjetoExameDeConstatacao + (".\n");
							}

							pDescricaoExameDeConstatacao = secaoObjetoRecebido.addParagraph(sObjetoExameDeConstatacao);

							pDescricaoExameDeConstatacao.setFont(fontParagrafoPNormal);
							pDescricaoExameDeConstatacao.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

							Paragraph pTituloDasConsideracoesTecnicas = secaoObjetoRecebido.addParagraph("DAS CONSIDERAÇÕES TÉCNICAS:\n");
							pTituloDasConsideracoesTecnicas.setFont(fontParagrafoPBold);

							Paragraph pDescricaoConsideracoesTecnicas;
							String sObjetoConsideracoesTecnicas = "";

							sObjetoConsideracoesTecnicas = sObjetoConsideracoesTecnicas + (periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getEmbaladoEm() != null
									? "O material analisado foi embalado em " + periciaEvidencia.getEvidencia().getUltimaEvidenciaObjeto().getEmbaladoEm().getDescricao().toLowerCase()
									: "");

							if (periciaEvidencia.getEvidencia().getUltimoLacre() != null)
								sObjetoConsideracoesTecnicas = sObjetoConsideracoesTecnicas + (periciaEvidencia.getEvidencia().getUltimoUsuarioLacreCustodia().getLacre() != null
										? ", com lacre nº " + periciaEvidencia.getEvidencia().getUltimoUsuarioLacreCustodia().getLacre().getNumero()
										: "");

							sObjetoConsideracoesTecnicas = sObjetoConsideracoesTecnicas
									+ (" e entregue no Núcleo de Controle Cartorial e Expediente (NUCCE) da Coordenadoria de Perícia Criminal (COPEC) na Perícia Forense do Estado do Ceará (PEFOCE).\n"
											+ "A autoridade solicitante deverá realizar o resgate do material citado.");

							pDescricaoConsideracoesTecnicas = secaoObjetoRecebido.addParagraph(sObjetoConsideracoesTecnicas);

							pDescricaoConsideracoesTecnicas.setFont(fontParagrafoPNormal);
							pDescricaoConsideracoesTecnicas.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

						}
					}
				}
			}
			boolean isCadaver = false;
			Section secaoEnvolvidos = laudoDocumento.getSectionByName("secaoEnvolvidos");
			if (secaoEnvolvidos != null) {
				limparSecao(secaoEnvolvidos);

				if (pericia.getListPericiaEvidencia() != null && !pericia.getListPericiaEvidencia().isEmpty()) {
					for (PericiaEvidencia periciaEvidencia : pericia.getListPericiaEvidencia()) {
						if (periciaEvidencia.getEvidencia().getTipoEvidencia().getId().equals(TipoEvidenciaEnum.PESSOA_ENVOLVIDA.getId())) {
							EvidenciaEnvolvidoPessoa pessoa = evidenciaEnvolvidoPessoaDao.buscarPessoaPorEvidencia(periciaEvidencia.getEvidencia());
							Paragraph pEnvolvido;
							if(pessoa.getRegistroCadaver() != null) {
								pEnvolvido = secaoEnvolvidos.addParagraph(pessoa.getNome() != null ? "Envolvidos(s): " + pessoa.getNome().toUpperCase() + " - Registro Cadáver:" + pessoa.getRegistroCadaver() : "");
								isCadaver = true;
							}else
								pEnvolvido = secaoEnvolvidos.addParagraph(pessoa.getNome() != null ? "Envolvidos(s): " + pessoa.getNome().toUpperCase() : "");
							pEnvolvido.setFont(fontParagrafoPNormal);
						}

						if (periciaEvidencia.isUtilizaNoLaudo()) {

							if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.ALCOOLEMIA.getId()
									|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_ETANOL_EM_AMOSTRA_BIOLOGICA.getId())
									|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_SEMEN_EM_AMOSTRA_BIOLOGICA.getId()
									|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.TRICHOMONAS_VAGINALLIS.getId()
									|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.DETECCAO_DE_HORMONIO_EM_AMOSTRA_BIOLOGICA.getId()
									|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_SANGUE.getId()
									|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_URINA.getId()) {
								if (periciaEvidencia.getEvidencia().getTipoEvidencia().getId().equals(TipoEvidenciaEnum.MATERIAL_QUIMICO_BIOLOGICO.getId())) {

									Paragraph pNumeroCaso = secaoEnvolvidos.addParagraph("Caso: " + solicitacaoProcedimentoPericial.getProcedimentoPericial().getNumero());
									pNumeroCaso.setFont(fontParagrafoPNormal);

									if (solicitacaoProcedimentoPericial != null) {
										if (solicitacaoProcedimentoPericial.getSolicitacaoProcedimentoPericial() != null && solicitacaoProcedimentoPericial.getSolicitacaoProcedimentoPericial().getId() != null) {
											Paragraph pNumeroSolicitacaoComel = secaoEnvolvidos
													.addParagraph("Solicitação COMEL: " + solicitacaoProcedimentoPericial.getSolicitacaoProcedimentoPericial().getId());
											pNumeroSolicitacaoComel.setFont(fontParagrafoPNormal);
											Paragraph pSolicitadaPor = secaoEnvolvidos.addParagraph("Exame solicitado por (perito): " + solicitacaoProcedimentoPericial.getUsuario().getNome());
											pSolicitadaPor.setFont(fontParagrafoPNormal);
										}

										Paragraph pNumeroSolicitacaoCalf = secaoEnvolvidos.addParagraph("Solicitação CALF: " + solicitacaoProcedimentoPericial.getId().toString());
										pNumeroSolicitacaoCalf.setFont(fontParagrafoPNormal);

										// SE A COLETA FOR FEITA NA CALF
										if ((solicitacaoProcedimentoPericial.getSolicitacaoProcedimentoPericial() == null
												|| solicitacaoProcedimentoPericial.getSolicitacaoProcedimentoPericial().getId() == null)
												&& solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal().getId().equals(SetorEnum.CALF.getId())) {
											Paragraph pColetaSupervisionadaPor = secaoEnvolvidos
													.addParagraph("Coleta supervisionada por: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getSupervisorDaColeta());
											pColetaSupervisionadaPor.setFont(fontParagrafoPNormal);

											Paragraph pDataDaColeta = secaoEnvolvidos
													.addParagraph("Data da coleta: " + StrUtil.dataFormatada(periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getDataColeta()));
											pDataDaColeta.setFont(fontParagrafoPNormal);
										}

										if (ultimoFormulario != null) {

											// SE A COLETA FOR FEITA NA COMEL
											if (solicitacaoProcedimentoPericial.getSolicitacaoProcedimentoPericial() != null
													&& solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal().getId().equals(SetorEnum.CALF.getId())) {
												Paragraph pDataDaColeta = secaoEnvolvidos.addParagraph("Data da coleta: " + StrUtil.dataFormatada(ultimoFormulario.getDataDaColeta()));
												pDataDaColeta.setFont(fontParagrafoPNormal);
											}

											// SE A COLETA FOR FEITA NA COMEL
											if (ultimoFormulario.getDataDaOcorrencia() != null) {
												Paragraph pDataDaOcorencia = secaoEnvolvidos.addParagraph("Data da ocorrência: " + StrUtil.dataFormatada(ultimoFormulario.getDataDaOcorrencia()));
												pDataDaOcorencia.setFont(fontParagrafoPNormal);
											}
										}

										// SE O RESULTADO FOR PREENCHIDO PELO PERITO
										if ((laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.ALCOOLEMIA.getId()
												|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_ETANOL_EM_AMOSTRA_BIOLOGICA.getId())
												|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_SANGUE.getId()
												|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_URINA.getId()) && (pericia.getDataAnalise() != null && pericia.getSequencia() != null)) {
											Paragraph pDataDaColeta = secaoEnvolvidos
														.addParagraph("Análise iniciada em: " + StrUtil.dataFormatada(pericia.getDataAnalise()) + "   Sequência: " + pericia.getSequencia().toString());
												pDataDaColeta.setFont(fontParagrafoPNormal);
										} else if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_SEMEN_EM_AMOSTRA_BIOLOGICA.getId()
												|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.DETECCAO_DE_HORMONIO_EM_AMOSTRA_BIOLOGICA.getId()
												|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.TRICHOMONAS_VAGINALLIS.getId()) {

											Paragraph pDataDaColeta = secaoEnvolvidos
													.addParagraph(pericia.getDataAnalise() != null ? "Análise iniciada em: " + (StrUtil.dataFormatada(pericia.getDataAnalise())) : "");
											pDataDaColeta.setFont(fontParagrafoPNormal);
										}

										HistoricoProcedimentoSIP historicoSuspeita = historicoProcedimentoSIPDao
												.buscarUltimoHistoricoProcedimento(getSolicitacaoProcedimentoPericial().getProcedimentoPericial());
										if (historicoSuspeita != null) {
											if (historicoSuspeita.getNaturezaOcorrencia() != null) {
												Paragraph pHistoricoSuspeita = secaoEnvolvidos.addParagraph("Natureza do procedimento: " + historicoSuspeita.getNaturezaOcorrencia().charAt(0)
														+ historicoSuspeita.getNaturezaOcorrencia().substring(1, historicoSuspeita.getNaturezaOcorrencia().length()).toLowerCase());
												pHistoricoSuspeita.setFont(fontParagrafoPNormal);
											}
										}
										break;
									} else {

										Paragraph pNumeroSolicitacaoCalf = secaoEnvolvidos.addParagraph("Solicitação CALF: " + solicitacaoProcedimentoPericial.getId().toString());
										pNumeroSolicitacaoCalf.setFont(fontParagrafoPNormal);

										// SE A COLETA FOR FEITA NA CALF
										if (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getDataColeta() != null) {
											Paragraph pDataDaColeta = secaoEnvolvidos
													.addParagraph("Data da coleta: " + StrUtil.dataFormatada(periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getDataColeta()));
											pDataDaColeta.setFont(fontParagrafoPNormal);
										}

										if (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getSupervisorDaColeta() != null) {
											Paragraph pColetaSupervisionadaPor = secaoEnvolvidos
													.addParagraph("Coleta supervisionada por: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getSupervisorDaColeta());
											pColetaSupervisionadaPor.setFont(fontParagrafoPNormal);
										}

										if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.ALCOOLEMIA.getId()
												|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_ETANOL_EM_AMOSTRA_BIOLOGICA.getId())
												|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_SANGUE.getId()
												|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_URINA.getId()) {
											Paragraph pDataDaColeta = secaoEnvolvidos
													.addParagraph("Data da análise: " + StrUtil.dataFormatada(pericia.getDataAnalise()) + "   Sequência: " + pericia.getSequencia().toString());
											pDataDaColeta.setFont(fontParagrafoPNormal);

										} else if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_SEMEN_EM_AMOSTRA_BIOLOGICA.getId()) {
											Paragraph pDataDaColeta = secaoEnvolvidos
													.addParagraph(pericia.getDataAnalise() != null ? "Data da análise: " + (StrUtil.dataFormatada(pericia.getDataAnalise())) : "");
											pDataDaColeta.setFont(fontParagrafoPNormal);
										}

										HistoricoProcedimentoSIP historicoSuspeita = historicoProcedimentoSIPDao
												.buscarUltimoHistoricoProcedimento(getSolicitacaoProcedimentoPericial().getProcedimentoPericial());

										if (historicoSuspeita != null) {
											if (historicoSuspeita.getNaturezaOcorrencia() != null) {
												Paragraph pHistoricoSuspeita = secaoEnvolvidos.addParagraph("Natureza do procedimento: " + historicoSuspeita.getNaturezaOcorrencia().charAt(0)
														+ historicoSuspeita.getNaturezaOcorrencia().substring(1, historicoSuspeita.getNaturezaOcorrencia().length()).toLowerCase());
												pHistoricoSuspeita.setFont(fontParagrafoPNormal);
											}
										}
										break;
									}
								}
							}
						}
					}
				}
			}
			if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.ALCOOLEMIA.getId() 
					|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_ETANOL_EM_AMOSTRA_BIOLOGICA.getId())) {
				boolean paragraResultadosGerado = false;

				Table tableResultados;
				Section secaoResultados = laudoDocumento.getSectionByName("secaoResultados");
				if (secaoResultados != null) {
					limparSecao(secaoResultados);
					tableResultados = secaoResultados.getTableByName("tabelaResultados");
					if (tableResultados != null)
						tableResultados.remove();
				}

				if (!paragraResultadosGerado) {
					paragraResultadosGerado = true;
				}

				if (secaoResultados != null) {
					tableResultados = secaoResultados.getTableByName("tabelaResultados");

					if (tableResultados == null) {
						// MONTANDO CABEÇALHO DA TABELA DE CARTUCHOS
						tableResultados = secaoResultados.addTable(1, 4);
						tableResultados.setTableName("tabelaResultados");

						// MONTANDO COLUNAS DA TABELA DE CARTUCHOS
						Cell cellResultado0 = tableResultados.getCellByPosition(0, 0);
						cellResultado0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
						cellResultado0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
						cellResultado0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
						cellResultado0.addParagraph("SUBSTÂNCIAS").setFont(fontTableHeader);

						Cell cellResultado1 = tableResultados.getCellByPosition(1, 0);
						cellResultado1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
						cellResultado1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
						cellResultado1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
						cellResultado1.addParagraph("LIMITE DE DETECÇÃO").setFont(fontTableHeader);

						Cell cellResultado2 = tableResultados.getCellByPosition(2, 0);
						cellResultado2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
						cellResultado2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
						cellResultado2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
						cellResultado2.addParagraph("RESULTADO").setFont(fontTableHeader);

						Cell cellResultado3 = tableResultados.getCellByPosition(3, 0);
						cellResultado3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
						cellResultado3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
						cellResultado3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
						cellResultado3.addParagraph("VALOR").setFont(fontTableHeader);
					}

					if (tableResultados != null) {

						int linhaTabela = tableResultados.getRowCount();

						// SUBSTANCIAS
						Cell cellResultado0 = tableResultados.getCellByPosition(0, linhaTabela);
						cellResultado0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
						cellResultado0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
						cellResultado0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
						cellResultado0.addParagraph("ETANOL").setFont(fontTableDetail);

						// LIMITE DE DETECÇÃO
						Cell cellResultado1 = tableResultados.getCellByPosition(1, linhaTabela);
						cellResultado1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
						cellResultado1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
						cellResultado1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
						if(isCadaver)
							cellResultado1.addParagraph("Maior ou igual a 2,0 dg/L ").setFont(fontTableDetail);
						else
							cellResultado1.addParagraph("Maior ou igual a 1,0 dg/L ").setFont(fontTableDetail);
						
						// SE O RESULTADO FOR PREENCHIDO PELO PERITO
						// RESULTADO
						Cell cellResultado2 = tableResultados.getCellByPosition(2, linhaTabela);
						cellResultado2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
						cellResultado2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
						cellResultado2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
						cellResultado2.setFont(fontRed);
						cellResultado2.addParagraph(pericia.getTipoResultadoAlcoolemia() != null ? pericia.getTipoResultadoAlcoolemia().getDescricao() : " ");

						// VALOR
						Cell cellResultado3 = tableResultados.getCellByPosition(3, linhaTabela);
						cellResultado3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
						cellResultado3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
						cellResultado3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));

						if (pericia.getTipoResultadoAlcoolemia().getId().equals(TipoResultadoAlcoolemiaEnum.NAO_DETECTADO.getId())) {
							if(isCadaver)
								cellResultado3.addParagraph("Menor que 2,0 dg/L").setFont(fontTableDetail);
							else
								cellResultado3.addParagraph("Menor que 1,0 dg/L").setFont(fontTableDetail);
						}
						if (pericia.getTipoResultadoAlcoolemia().getId().equals(TipoResultadoAlcoolemiaEnum.DETECTADO.getId())) {
							cellResultado3.addParagraph(!pericia.getValorDetectado().isEmpty() && pericia.getValorDetectado() != null ? pericia.getValorDetectado() + " dg/L" : " ")
									.setFont(fontTableDetail);
						}
					}
				}
			} else if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_SEMEN_EM_AMOSTRA_BIOLOGICA.getId()
					|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.TRICHOMONAS_VAGINALLIS.getId()
					|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.DETECCAO_DE_HORMONIO_EM_AMOSTRA_BIOLOGICA.getId()) {

				Section secaoResultados = laudoDocumento.getSectionByName("secaoResultados");
				if (secaoResultados != null) {
					limparSecao(secaoResultados);

					Paragraph pRespostaResultados;
					String sRespostaResultados = "";
					if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_SEMEN_EM_AMOSTRA_BIOLOGICA.getId()
							|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.TRICHOMONAS_VAGINALLIS.getId()) {
						sRespostaResultados = sRespostaResultados + "Microscopia:\n";
					}

					if (pericia.isMicroscopiaVisualizado()) {
						if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_SEMEN_EM_AMOSTRA_BIOLOGICA.getId()) {
							sRespostaResultados = sRespostaResultados + (" - VISUALIZADO espermatozoide na(s) amostra(s) analisada(s) (conteúdo ");
						} else if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.TRICHOMONAS_VAGINALLIS.getId()) {
							sRespostaResultados = sRespostaResultados + (" - VISUALIZADO Trichomonas vaginallis na(s) amostra(s) analisada(s) (conteúdo ");
						}
						if (pericia.getTipoAmostraVisualizadaResultadoMicroscopia() != null && !pericia.getTipoAmostraVisualizadaResultadoMicroscopia().equals(TipoAmostraEnum.OUTRO)) {
							sRespostaResultados = sRespostaResultados + (pericia.getTipoAmostraVisualizadaResultadoMicroscopia().getDescricao().toLowerCase() + ").\n");
						} else {
							sRespostaResultados = sRespostaResultados + (pericia.getRespostaOutraAmostraVisualizadaMicroscopia().toLowerCase() + ").\n");
						}
					}

					if (pericia.isMicroscopiaNaoVisualizado()) {
						if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_SEMEN_EM_AMOSTRA_BIOLOGICA.getId()) {
							sRespostaResultados = sRespostaResultados + (" - NÃO VISUALIZADO espermatozoide na(s) amostra(s) analisada(s) (conteúdo ");
						} else if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.TRICHOMONAS_VAGINALLIS.getId()) {
							sRespostaResultados = sRespostaResultados + (" - NÃO VISUALIZADO Trichomonas vaginallis na(s) amostra(s) analisada(s) (conteúdo ");
						}

						if (pericia.getTipoAmostraNaoVisualizadaResultadoMicroscopia() != null && !pericia.getTipoAmostraNaoVisualizadaResultadoMicroscopia().equals(TipoAmostraEnum.OUTRO)) {
							sRespostaResultados = sRespostaResultados + (pericia.getTipoAmostraNaoVisualizadaResultadoMicroscopia().getDescricao().toLowerCase() + ").\n");
						} else {
							sRespostaResultados = sRespostaResultados + (pericia.getRespostaOutraAmostraNaoVisualizadaMicroscopia().toLowerCase() + ").\n");
						}
					}

					if (pericia.isMicroscopiaNaoRealizado()) {
						if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_SEMEN_EM_AMOSTRA_BIOLOGICA.getId()) {
							sRespostaResultados = sRespostaResultados + (" - NÃO REALIZADO (conteúdo ");
						} else if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.TRICHOMONAS_VAGINALLIS.getId()) {
							sRespostaResultados = sRespostaResultados + (" - NÃO REALIZADO (conteúdo ");
						}

						if (pericia.getTipoAmostraNaoRealizadaResultadoMicroscopia() != null && !pericia.getTipoAmostraNaoRealizadaResultadoMicroscopia().equals(TipoAmostraEnum.OUTRO)) {
							sRespostaResultados = sRespostaResultados + (pericia.getTipoAmostraNaoRealizadaResultadoMicroscopia().getDescricao().toLowerCase() + ").\n");
						} else {
							sRespostaResultados = sRespostaResultados + (pericia.getRespostaOutraAmostraNaoRealizadaMicroscopia().toLowerCase() + ").\n");
						}
					}

					if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_SEMEN_EM_AMOSTRA_BIOLOGICA.getId()) {
						sRespostaResultados = sRespostaResultados + "Detecção de PSA:\n";

						if (pericia.isPsaReagente()) {
							sRespostaResultados = sRespostaResultados + (" - REAGENTE (conteúdo ");
							if (pericia.getTipoAmostraReagenteResultadoPSA() != null && !pericia.getTipoAmostraReagenteResultadoPSA().equals(TipoAmostraEnum.OUTRO)) {
								sRespostaResultados = sRespostaResultados + (pericia.getTipoAmostraReagenteResultadoPSA().getDescricao().toLowerCase() + ").\n");
							} else {
								sRespostaResultados = sRespostaResultados + (pericia.getRespostaOutraAmostraReagentePSA().toLowerCase() + ").\n");
							}
						}

						if (pericia.isPsaNaoReagente()) {
							sRespostaResultados = sRespostaResultados + (" - NÃO REAGENTE (conteúdo ");
							if (pericia.getTipoAmostraNaoReagenteResultadoPSA() != null && !pericia.getTipoAmostraNaoReagenteResultadoPSA().equals(TipoAmostraEnum.OUTRO)) {
								sRespostaResultados = sRespostaResultados + (pericia.getTipoAmostraNaoReagenteResultadoPSA().getDescricao().toLowerCase() + ").\n");
							} else {
								sRespostaResultados = sRespostaResultados + (pericia.getRespostaOutraAmostraNaoReagentePSA().toLowerCase() + ").\n");
							}
						}
						if (pericia.isPsaNaoRealizado()) {
							sRespostaResultados = sRespostaResultados + (" - NÃO REALIZADO (conteúdo ");
							if (pericia.getTipoAmostraNaoRealizadaResultadoPSA() != null && !pericia.getTipoAmostraNaoRealizadaResultadoPSA().equals(TipoAmostraEnum.OUTRO)) {
								sRespostaResultados = sRespostaResultados + (pericia.getTipoAmostraNaoRealizadaResultadoPSA().getDescricao().toLowerCase() + ").\n");
							} else {
								sRespostaResultados = sRespostaResultados + (pericia.getRespostaOutraAmostraNaoRealizadaPSA().toLowerCase() + ").\n");
							}
						}

					} else if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.DETECCAO_DE_HORMONIO_EM_AMOSTRA_BIOLOGICA.getId()) {
						sRespostaResultados = sRespostaResultados + "Detecção de hCG:\n";

						if (pericia.isPsaReagente()) {
							sRespostaResultados = sRespostaResultados + (" - REAGENTE");
						}

						if (pericia.isPsaNaoReagente()) {
							sRespostaResultados = sRespostaResultados + (" - NÃO REAGENTE");
						}

						if (pericia.isPsaNaoRealizado()) {
							sRespostaResultados = sRespostaResultados + (" - NÃO REALIZADO");
						}
					}

					pRespostaResultados = secaoResultados.addParagraph(sRespostaResultados);

					pRespostaResultados.setFont(fontParagrafoPNormal);
					pRespostaResultados.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
				}
			}

			if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_SANGUE.getId()
					|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_URINA.getId()) {
				boolean paragraResultadosTriagemGerado = false;

				Table tableResultadosTriagem;
				Section secaoResultadosTriagem = laudoDocumento.getSectionByName("secaoResultadosTriagem");
				if (secaoResultadosTriagem != null) {
					limparSecao(secaoResultadosTriagem);
					tableResultadosTriagem = secaoResultadosTriagem.getTableByName("tabelaResultadosTriagem");
					if (tableResultadosTriagem != null)
						tableResultadosTriagem.remove();
				}

				if (!paragraResultadosTriagemGerado) {
					paragraResultadosTriagemGerado = true;
				}

				if (pericia.isKitTreze() || pericia.isKitVinte() || pericia.isKitVinteUm() || pericia.isKitDoze()) {

					if (secaoResultadosTriagem != null) {
						tableResultadosTriagem = secaoResultadosTriagem.getTableByName("tabelaResultadosTriagem");
						Paragraph pObjetivoTriagem = secaoResultadosTriagem.addParagraph("RESULTADOS");
						pObjetivoTriagem.getOdfElement().setStyleName(style.getStyleNameAttribute());

						if (tableResultadosTriagem == null) {
							// MONTANDO CABEÇALHO DA TABELA DE SUBSTÂNCIAS
							tableResultadosTriagem = secaoResultadosTriagem.addTable(1, 4);
							tableResultadosTriagem.setTableName("tabelaResultadosTriagem");

							// MONTANDO COLUNAS DA TABELA DE SUBSTÂNCIAS
							Cell cellResultado0 = tableResultadosTriagem.getCellByPosition(0, 0);
							cellResultado0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
							cellResultado0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
							cellResultado0.addParagraph("Classe de Substâncias").setFont(fontTableHeader);
							cellResultado0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
							cellResultado0.getTableColumn().setWidth(65);

							Cell cellResultado1 = tableResultadosTriagem.getCellByPosition(1, 0);
							cellResultado1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
							cellResultado1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
							cellResultado1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
							cellResultado1.addParagraph("Limite de detecção (cut off)").setFont(fontTableHeader);
							cellResultado1.getTableColumn().setWidth(40);

							Cell cellResultado2 = tableResultadosTriagem.getCellByPosition(2, 0);
							cellResultado2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
							cellResultado2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
							cellResultado2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
							cellResultado2.addParagraph("Resultado").setFont(fontTableHeader);
							cellResultado2.getTableColumn().setWidth(30);

							Cell cellResultado3 = tableResultadosTriagem.getCellByPosition(3, 0);
							cellResultado3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
							cellResultado3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
							cellResultado3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
							cellResultado3.addParagraph("Valor** (ng/ml)").setFont(fontTableHeader);
							cellResultado3.getTableColumn().setWidth(25);

						}

						if (tableResultadosTriagem != null) {

							int linhaTabela = tableResultadosTriagem.getRowCount();
							JSONObject json2 = new JSONObject(pericia.getSolicitacaoResultadoKit());

							@SuppressWarnings("unchecked")
							Iterator<String> iter2 = json2.keys();

							if (iter2 != null && iter2.hasNext()) {
								String key = iter2.next();
								for (int i = 0; i < ((JSONArray) json2.get(key)).length(); ++i) {
									String descricaoSubstancia = "";
									String resultado = "";
									String valor = "";
									String limiteDeteccao = "";

									descricaoSubstancia += ((JSONObject) ((JSONArray) json2.get(key)).get(i)).get("descricaoClasseSubstancia");
									resultado += ((JSONObject) ((JSONArray) json2.get(key)).get(i)).get("resultado");
									valor += ((JSONObject) ((JSONArray) json2.get(key)).get(i)).get("valor");
									limiteDeteccao += ((JSONObject) ((JSONArray) json2.get(key)).get(i)).get("limiteDeteccao");

									// SUBSTANCIAS
									Cell cellResultado0 = tableResultadosTriagem.getCellByPosition(0, linhaTabela);
									cellResultado0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
									cellResultado0.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
									cellResultado0.addParagraph(" " + descricaoSubstancia).setFont(fontTableDetail);
									cellResultado0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));

									// LIMITE DE DETECÇÃO
									Cell cellResultado1 = tableResultadosTriagem.getCellByPosition(1, linhaTabela);
									cellResultado1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
									cellResultado1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
									cellResultado1.addParagraph(limiteDeteccao).setFont(fontTableDetail);
									cellResultado1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));

									// RESULTADO
									Cell cellResultado2 = tableResultadosTriagem.getCellByPosition(2, linhaTabela);
									cellResultado2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
									cellResultado2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
									cellResultado2.addParagraph(resultado).setFont(fontTableDetail);
									cellResultado2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));

									// VALOR
									Cell cellResultado3 = tableResultadosTriagem.getCellByPosition(3, linhaTabela);
									cellResultado3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
									cellResultado3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
									cellResultado3.addParagraph(valor).setFont(fontTableDetail);
									cellResultado3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
								}
							}
						}

						Paragraph pLegendaTabela;
						String sLegendaTabela = "";
						sLegendaTabela = sLegendaTabela + ("Diferenças individuais no metabolismo, excreção e a redistribuição pos-mortem das drogas e/ou seus metabólitos podem afetar "
								+ "especificamente a concentração e o tempo de detecção. *Os principais metabólitos da cocaína incluem a benzoilecgonina e a metilecgonina."
								+ "**Valor semi-quantitativo.");

						pLegendaTabela = secaoResultadosTriagem.addParagraph(sLegendaTabela);

						pLegendaTabela.setFont(fontLegendTable);
						pLegendaTabela.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
					}
				}
			}

			if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_SANGUE.getId()
					|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_URINA.getId()) {
				boolean paragraResultadosConfirmatorioGerado = false;

				Table tableResultadosConfirmatorio;
				Section secaoResultadosConfirmatorio = laudoDocumento.getSectionByName("secaoResultadosConfirmatorio");
				if (secaoResultadosConfirmatorio != null) {
					limparSecao(secaoResultadosConfirmatorio);
					tableResultadosConfirmatorio = secaoResultadosConfirmatorio.getTableByName("tabelaResultadosConfirmatorio");
					if (tableResultadosConfirmatorio != null)
						tableResultadosConfirmatorio.remove();
				}

				if (!paragraResultadosConfirmatorioGerado) {
					paragraResultadosConfirmatorioGerado = true;
				}

				if (pericia.isConfirmatorio()) {

					if (secaoResultadosConfirmatorio != null) {
						tableResultadosConfirmatorio = secaoResultadosConfirmatorio.getTableByName("tabelaResultadosConfirmatorio");
						Paragraph pObjetivoTriagem = secaoResultadosConfirmatorio.addParagraph("RESULTADOS");
						pObjetivoTriagem.getOdfElement().setStyleName(style.getStyleNameAttribute());

						if (tableResultadosConfirmatorio == null) {
							// MONTANDO CABEÇALHO DA TABELA DE SUBSTÂNCIAS
							tableResultadosConfirmatorio = secaoResultadosConfirmatorio.addTable(1, 3);
							tableResultadosConfirmatorio.setTableName("tabelaResultadosConfirmatorio");

							// MONTANDO COLUNAS DA TABELA DE SUBSTÂNCIAS
							Cell cellResultado0 = tableResultadosConfirmatorio.getCellByPosition(0, 0);
							cellResultado0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
							cellResultado0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
							cellResultado0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
							cellResultado0.addParagraph("SUBSTÂNCIA(S)").setFont(fontTableHeader);
							cellResultado0.getTableColumn().setWidth(110);

							Cell cellResultado1 = tableResultadosConfirmatorio.getCellByPosition(1, 0);
							cellResultado1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
							cellResultado1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
							cellResultado1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
							cellResultado1.addParagraph("RESULTADO").setFont(fontTableHeader);

							Cell cellResultado2 = tableResultadosConfirmatorio.getCellByPosition(2, 0);
							cellResultado2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
							cellResultado2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
							cellResultado2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
							cellResultado2.addParagraph("VALOR (ng/ml)").setFont(fontTableHeader);
							cellResultado2.getTableColumn().setWidth(30);

						}

						if (tableResultadosConfirmatorio != null) {

							int linhaTabela = tableResultadosConfirmatorio.getRowCount();
							JSONObject json2 = new JSONObject(pericia.getSolicitacaoResultadoConfirmatorio());

							@SuppressWarnings("unchecked")
							Iterator<String> iter2 = json2.keys();

							if (iter2 != null && iter2.hasNext()) {
								String key = iter2.next();
								for (int i = 0; i < ((JSONArray) json2.get(key)).length(); ++i) {
									String descricaoSubstancia = "";
									String resultado = "";
									String valor = "";

									descricaoSubstancia += ((JSONObject) ((JSONArray) json2.get(key)).get(i)).get("descricaoClasseSubstancia");
									resultado += ((JSONObject) ((JSONArray) json2.get(key)).get(i)).get("resultado");
									valor += ((JSONObject) ((JSONArray) json2.get(key)).get(i)).get("valor");

									// SUBSTANCIAS
									Cell cellResultado0 = tableResultadosConfirmatorio.getCellByPosition(0, linhaTabela);
									cellResultado0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
									cellResultado0.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
									cellResultado0.addParagraph(" " + descricaoSubstancia).setFont(fontTableDetail);
									cellResultado0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));

									// RESULTADO
									Cell cellResultado1 = tableResultadosConfirmatorio.getCellByPosition(1, linhaTabela);
									cellResultado1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
									cellResultado1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
									cellResultado1.addParagraph(resultado).setFont(fontTableDetail);
									cellResultado1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));

									// VALOR
									Cell cellResultado2 = tableResultadosConfirmatorio.getCellByPosition(2, linhaTabela);
									cellResultado2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
									cellResultado2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
									cellResultado2.addParagraph(valor).setFont(fontTableDetail);
									cellResultado2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));

								}
							}
						}

						Paragraph pLegendaTabela;
						String sLegendaTabela = "";
						sLegendaTabela = sLegendaTabela + ("Legenda: NQ  - Não quantificado neste método.");
						pLegendaTabela = secaoResultadosConfirmatorio.addParagraph(sLegendaTabela);

						pLegendaTabela.setFont(fontLegendTable);
						pLegendaTabela.setHorizontalAlignment(HorizontalAlignmentType.LEFT);

						Paragraph pLegendaAuxiliarTabela;
						String sLegendaAuxiliarTabela = "";
						sLegendaAuxiliarTabela = sLegendaAuxiliarTabela + ("Obs.: Poderá ser confirmada a presença de outras substâncias que não estão presentes no rol do exame de triagem.");

						pLegendaAuxiliarTabela = secaoResultadosConfirmatorio.addParagraph(sLegendaAuxiliarTabela);

						pLegendaAuxiliarTabela.setFont(fontLegendTable);
						pLegendaAuxiliarTabela.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

					}
				}
			}

			Section secaoAmostra = laudoDocumento.getSectionByName("secaoAmostra");
			if (secaoAmostra != null) {
				limparSecao(secaoAmostra);

				Paragraph pAmostra;
				String sAmostra = "";

				if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_SEMEN_EM_AMOSTRA_BIOLOGICA.getId()
						|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.TRICHOMONAS_VAGINALLIS.getId()) {

					if (pericia.isMicroscopiaVisualizado()) {
						if (!pericia.getTipoAmostraVisualizadaResultadoMicroscopia().equals(TipoAmostraEnum.OUTRO)) {
							sAmostra = sAmostra + (pericia.getTipoAmostraVisualizadaResultadoMicroscopia().getDescricao().toLowerCase());
						} else if (pericia.getTipoAmostraVisualizadaResultadoMicroscopia().equals(TipoAmostraEnum.OUTRO)) {
							sAmostra = sAmostra + (pericia.getRespostaOutraAmostraVisualizadaMicroscopia().toLowerCase());
						}
					}

					if (pericia.isMicroscopiaNaoVisualizado()) {
						if (!sAmostra.isEmpty())
							sAmostra = sAmostra + "; ";
						if (!pericia.getTipoAmostraNaoVisualizadaResultadoMicroscopia().equals(TipoAmostraEnum.OUTRO)) {
							sAmostra = sAmostra + (pericia.getTipoAmostraNaoVisualizadaResultadoMicroscopia().getDescricao().toLowerCase());
						} else if (pericia.getTipoAmostraNaoVisualizadaResultadoMicroscopia().equals(TipoAmostraEnum.OUTRO)) {
							sAmostra = sAmostra + (pericia.getRespostaOutraAmostraNaoVisualizadaMicroscopia().toLowerCase());
						}

					}

					if (pericia.isMicroscopiaNaoRealizado()) {
						if (!sAmostra.isEmpty())
							sAmostra = sAmostra + "; ";

						if (!pericia.getTipoAmostraNaoRealizadaResultadoMicroscopia().equals(TipoAmostraEnum.OUTRO)) {
							sAmostra = sAmostra + (pericia.getTipoAmostraNaoRealizadaResultadoMicroscopia().getDescricao().toLowerCase());
						} else if (pericia.getTipoAmostraNaoRealizadaResultadoMicroscopia().equals(TipoAmostraEnum.OUTRO)) {
							sAmostra = sAmostra + (pericia.getRespostaOutraAmostraNaoRealizadaMicroscopia().toLowerCase());
						}
					}

				} else if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.DETECCAO_DE_HORMONIO_EM_AMOSTRA_BIOLOGICA.getId()) {
					sAmostra = sAmostra + "Referida como ";
					for (PericiaEvidencia periciaEvidencia : pericia.getListPericiaEvidencia()) {
						if (periciaEvidencia.isUtilizaNoLaudo()) {
							if (periciaEvidencia.getEvidencia().getTipoEvidencia().getId().equals(TipoEvidenciaEnum.MATERIAL_QUIMICO_BIOLOGICO.getId())) {
								sAmostra = sAmostra + (periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaMaterialTipo().getDescricao().toLowerCase());
							}
						}
					}
				}

				if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() != TipoExameEnum.DETECCAO_DE_HORMONIO_EM_AMOSTRA_BIOLOGICA.getId()
						&& laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() != TipoExameEnum.PESQUISA_DE_DROGAS_EM_SANGUE.getId()) {
					sAmostra = "Swab(s) " + sAmostra + ".";
				} else {
					sAmostra = sAmostra + ".";
				}

				pAmostra = secaoAmostra.addParagraph(sAmostra);
				pAmostra.setFont(fontParagrafoPNormal);
				pAmostra.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
			}

			Section secaoMetodologiaTriagem = laudoDocumento.getSectionByName("secaoMetodologiaTriagem");
			Section secaoTabelaMetodologiaTriagemKitDoze = laudoDocumento.getSectionByName("secaoTabelaMetodologiaTriagemKitDoze");
			Section secaoTabelaMetodologiaTriagemKitTreze = laudoDocumento.getSectionByName("secaoTabelaMetodologiaTriagemKitTreze");
			Section secaoTabelaMetodologiaTriagemKitVinte = laudoDocumento.getSectionByName("secaoTabelaMetodologiaTriagemKitVinte");
			Section secaoTabelaMetodologiaTriagemKitVinteUm = laudoDocumento.getSectionByName("secaoTabelaMetodologiaTriagemKitVinteUm");

			if (secaoMetodologiaTriagem != null) {
				limparSecao(secaoMetodologiaTriagem);

				Paragraph pMetodologiaTriagem;
				String sMetodologiaTriagem = "";

				if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_SANGUE.getId()
						|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_URINA.getId()) {

					if (pericia.isTriagem()) {

						Paragraph pTituloTriagem = secaoMetodologiaTriagem.addParagraph("EXAME TOXICOLÓGICO DE TRIAGEM (SCREENING):");
						pTituloTriagem.getOdfElement().setStyleName(style.getStyleNameAttribute());

						secaoMetodologiaTriagem.addParagraph("");

						Paragraph pObjetivoTriagem = secaoMetodologiaTriagem.addParagraph("OBJETIVO");
						pObjetivoTriagem.getOdfElement().setStyleName(style.getStyleNameAttribute());

						Paragraph pDescricaoObjetivoTriagem;
						String sDescricaoObjetivoTriagem = "";
						sDescricaoObjetivoTriagem = sDescricaoObjetivoTriagem + ("Realizar pesquisa de substâncias ilícitas e/ou controladas na amostra analisada.\n");

						pDescricaoObjetivoTriagem = secaoMetodologiaTriagem.addParagraph(sDescricaoObjetivoTriagem);

						pDescricaoObjetivoTriagem.setFont(fontParagrafoPNormal);
						pDescricaoObjetivoTriagem.setHorizontalAlignment(HorizontalAlignmentType.LEFT);

						for (LaudoMetodologia laudoMetodologia : laudo.getListaLaudoMetodologia()) {
							String conteudo;
							if (laudoMetodologia.getMetodologia().getDescricao() != null && !laudoMetodologia.getMetodologia().getDescricao().isEmpty()) {
								if ((laudoMetodologia.getMetodologia().getPertenceAoKit() != null)
										&& (pericia.isKitTreze() && laudoMetodologia.getMetodologia().getPertenceAoKit().getId() == TipoKitEnum.KIT_13.getId()
												|| pericia.isKitVinte() && laudoMetodologia.getMetodologia().getPertenceAoKit().getId() == TipoKitEnum.KIT_20.getId()
												|| pericia.isKitVinteUm() && laudoMetodologia.getMetodologia().getPertenceAoKit().getId() == TipoKitEnum.KIT_21.getId()
												|| pericia.isKitDoze() && laudoMetodologia.getMetodologia().getPertenceAoKit().getId() == TipoKitEnum.KIT_12.getId())) {

									Paragraph p = secaoMetodologiaTriagem.addParagraph("METODOLOGIA");
									p.getOdfElement().setStyleName(style.getStyleNameAttribute());

									conteudo = laudoMetodologia.getMetodologia().getDescricao();

									Paragraph pm = secaoMetodologiaTriagem.addParagraph(conteudo);
									pm.setFont(fontParagrafoPNormal);
									pm.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

								}
							}
						}
					}
					// APAGA AS TABELAS DAS METODOLOGIAS EM SUAS DEVIDAS SEÇÕES SE HOUVER
					if (!pericia.isKitTreze()) {
						apagarSecao(secaoTabelaMetodologiaTriagemKitTreze);
					}
					if (!pericia.isKitVinte()) {
						apagarSecao(secaoTabelaMetodologiaTriagemKitVinte);
					}
					if (!pericia.isKitVinteUm()) {
						apagarSecao(secaoTabelaMetodologiaTriagemKitVinteUm);
					}
					if (!pericia.isKitDoze()) {
						apagarSecao(secaoTabelaMetodologiaTriagemKitDoze);
					}
				}

				pMetodologiaTriagem = secaoMetodologiaTriagem.addParagraph(sMetodologiaTriagem);
				pMetodologiaTriagem.setFont(fontParagrafoPNormal);
				pMetodologiaTriagem.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
			}

			Section secaoMetodologiaConfirmatorio = laudoDocumento.getSectionByName("secaoMetodologiaConfirmatorio");
			if (secaoMetodologiaConfirmatorio != null) {
				limparSecao(secaoMetodologiaConfirmatorio);

				Paragraph pMetodologiaConfirmatorio;
				String sMetodologiaConfirmatorio = "";

				if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_SANGUE.getId()
						|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_URINA.getId()) {

//					SolicitacaoResultado solicitacaoResultado = solicitacaoResultadoDao.buscarSolicitacaoResultadoPorSolicitacao(this.solicitacaoProcedimentoPericial);

					if (pericia.isConfirmatorio()) {

						Paragraph pRespostaObjetivo = secaoMetodologiaConfirmatorio.addParagraph("EXAME TOXICOLÓGICO CONFIRMATÓRIO: ");
						pRespostaObjetivo.getOdfElement().setStyleName(style.getStyleNameAttribute());

						secaoMetodologiaConfirmatorio.addParagraph("");

						Paragraph pObjetivoTriagem = secaoMetodologiaConfirmatorio.addParagraph("OBJETIVO");
						pObjetivoTriagem.getOdfElement().setStyleName(style.getStyleNameAttribute());

						Paragraph pDescricaoObjetivoConfirmatorio;
						String sDescricaoObjetivoConfirmatorio = "";
						sDescricaoObjetivoConfirmatorio = sDescricaoObjetivoConfirmatorio + ("Confirmar a presença das drogas e/ou metabólitos detectados na amostra analisada.\n");

						pDescricaoObjetivoConfirmatorio = secaoMetodologiaConfirmatorio.addParagraph(sDescricaoObjetivoConfirmatorio);

						pDescricaoObjetivoConfirmatorio.setFont(fontParagrafoPNormal);
						pDescricaoObjetivoConfirmatorio.setHorizontalAlignment(HorizontalAlignmentType.LEFT);

						for (LaudoMetodologia laudoMetodologia : laudo.getListaLaudoMetodologia()) {
							String conteudo;
							if (laudoMetodologia.getMetodologia().getDescricao() != null && !laudoMetodologia.getMetodologia().getDescricao().isEmpty()) {
								if (laudoMetodologia.getMetodologia().getPertenceAoKit() == null) {
									Paragraph p = secaoMetodologiaConfirmatorio.addParagraph("METODOLOGIA");
									p.getOdfElement().setStyleName(style.getStyleNameAttribute());

									conteudo = laudoMetodologia.getMetodologia().getDescricao();
									Paragraph pm = secaoMetodologiaConfirmatorio.addParagraph(conteudo);
									pm.setFont(fontParagrafoPNormal);
									pm.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
								}
							}
						}

						Paragraph pDescricaoMetodoAnalitico;
						String sDescricaoMetodoAnalitico = "";
						sDescricaoMetodoAnalitico = sDescricaoMetodoAnalitico + ("Condições cromatográficas aplicada de acordo com o método: ");

						if (pericia.getListaTipoMetodoAnalitico() != null && pericia.getListaTipoMetodoAnalitico().length > 0) {
							for (int e : pericia.getListaTipoMetodoAnalitico()) {
								for (TipoMetodoAnaliticoEnum metodo : TipoMetodoAnaliticoEnum.values()) {
									if (pericia.getListaTipoMetodoAnalitico().length > 1) {
										if (metodo.getId() == e)
											sDescricaoMetodoAnalitico = sDescricaoMetodoAnalitico + (metodo.getDescricao() + ", ");
									} else {
										if (metodo.getId() == e) {
											sDescricaoMetodoAnalitico = sDescricaoMetodoAnalitico + (metodo.getDescricao());
										}
									}
								}
							}
						}

						sDescricaoMetodoAnalitico = sDescricaoMetodoAnalitico + (".");
						sDescricaoMetodoAnalitico = sDescricaoMetodoAnalitico.replace(", .", ".");

						pDescricaoMetodoAnalitico = secaoMetodologiaConfirmatorio.addParagraph(sDescricaoMetodoAnalitico);

						pDescricaoMetodoAnalitico.setFont(fontParagrafoPNormal);
						pDescricaoMetodoAnalitico.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

					}
				}

				pMetodologiaConfirmatorio = secaoMetodologiaConfirmatorio.addParagraph(sMetodologiaConfirmatorio);
				pMetodologiaConfirmatorio.setFont(fontParagrafoPNormal);
				pMetodologiaConfirmatorio.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
			}

			Section secaoDocumentoSolicitacao = laudoDocumento.getSectionByName("secaoDocumentoSolicitacao");
			if (secaoDocumentoSolicitacao != null) {
				limparSecao(secaoDocumentoSolicitacao);
				DocumentoProcedimentoPericial docP = solicitacaoProcedimentoPericial.getDocumentoProcedimentoPericial();
				String numDoc = docP.getTipoDocumento().getDescricao() + ": " + docP.getSetor().getIdUnidadeSIP() + "-" + docP.getNumero() + "/" + docP.getAno();
				Paragraph pDocSol = secaoDocumentoSolicitacao.addParagraph(numDoc);
				pDocSol.setFont(fontParagrafoPNormal);
			}

			HistoricoProcedimentoSIP historicoSIP = historicoProcedimentoSIPDao.buscarUltimoHistoricoProcedimento(solicitacaoProcedimentoPericial.getProcedimentoPericial());
			VariableField _campoTipoProcedimento = laudoDocumento.getVariableFieldByName("campoTipoProcedimento");
			VariableField _campoNumeroProcedimento = laudoDocumento.getVariableFieldByName("campoNumeroProcedimento");

			if (historicoSIP != null) {
				if (_campoTipoProcedimento != null) {
					_campoTipoProcedimento.updateField(historicoSIP.getTipoProcedimento(), null);
				}

				if (_campoTipoProcedimento != null) {
					String strCampoProcedimento = historicoSIP.getTipoProcedimento() + ": ";
					_campoTipoProcedimento.updateField(strCampoProcedimento, null);
				}
				if (_campoNumeroProcedimento != null) {
					_campoNumeroProcedimento.updateField(historicoSIP.getSetor().getIdUnidadeSIP() + "-" + historicoSIP.getNumero() + "/"
							+ (historicoSIP.getDataInstauracao() != null ? StrUtil.anoData(historicoSIP.getDataInstauracao()) : historicoSIP.getAno()), null);
				}
			}

			Section secaoProcedimentoPolicial = laudoDocumento.getSectionByName("secaoProcedimentoPolicial");
			if (secaoProcedimentoPolicial != null && historicoSIP != null) {
				limparSecao(secaoProcedimentoPolicial);
				String numProc = historicoSIP.getTipoProcedimento() + ": " + historicoSIP.getSetor().getIdUnidadeSIP() + "-" + historicoSIP.getNumero() + "/"
						+ (historicoSIP.getDataInstauracao() != null ? StrUtil.anoData(historicoSIP.getDataInstauracao()) : historicoSIP.getAno());
				Paragraph pProcPol = secaoProcedimentoPolicial.addParagraph(numProc);
				pProcPol.setFont(fontParagrafoPNormal);
			}

			Section secaoDelegaciaOrigem = laudoDocumento.getSectionByName("secaoDelegaciaOrigem");
			if (secaoDelegaciaOrigem != null && historicoSIP != null) {
				limparSecao(secaoDelegaciaOrigem);
				Paragraph pDelOri = secaoDelegaciaOrigem.addParagraph(historicoSIP.getSetor().getDescricaoCompleta());
				pDelOri.setFont(fontParagrafoPNormal);
			}

			Section secaoDataEntradaExame = laudoDocumento.getSectionByName("secaoDataEntradaExame");
			if (secaoDataEntradaExame != null) {
				limparSecao(secaoDataEntradaExame);
				Paragraph pDatEntExa = secaoDataEntradaExame.addParagraph(StrUtil.dataFormatada(solicitacaoProcedimentoPericial.getDataInclusao()));
				pDatEntExa.setFont(fontParagrafoPNormal);
			}

			Section secaoDataDocumento = laudoDocumento.getSectionByName("secaoDataDocumento");
			if (secaoDataDocumento != null) {
				limparSecao(secaoDataDocumento);
				Paragraph pDataDocumento = secaoDataDocumento.addParagraph(StrUtil.dataPorExtenso(new Date()));
				pDataDocumento.setFont(fontParagrafoPNormal);
				pDataDocumento.setHorizontalAlignment(HorizontalAlignmentType.RIGHT);
			}

			Section secaoAssinatura = laudoDocumento.getSectionByName("secaoAssinatura");
			if (secaoAssinatura != null) {
				limparSecao(secaoAssinatura);
				Paragraph pNomeAssinante = secaoAssinatura.addParagraph(pericia.getUsuario().getRegistroFuncional().getTratamento().getAbreviacao() + " " + pericia.getUsuario().getNome());
				pNomeAssinante.setFont(fontParagrafoPBold);
				pNomeAssinante.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
				pericia.getUsuario().setUsuarioHistorico(usuarioDao.findLastUsuarioHistorico(pericia.getUsuario()));
				Paragraph pCargoAssinante = secaoAssinatura.addParagraph(
						pericia.getUsuario().getUsuarioHistorico().getCorporacaoCargo().getCargo().getDescricao() + " Matrícula: " + pericia.getUsuario().getRegistroFuncional().getMatricula() + " "
								+ (pericia.getUsuario().getRegistroFuncional().getOrgaoRegulador() != null && pericia.getUsuario().getRegistroFuncional().getInscricaoOrgaoRegulador() != null
										? (pericia.getUsuario().getRegistroFuncional().getOrgaoRegulador() + " " + pericia.getUsuario().getRegistroFuncional().getInscricaoOrgaoRegulador())
										: ""));
				;
				pCargoAssinante.setFont(fontParagrafoPNormal);
				pCargoAssinante.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
			}

			// METODOLOGIA
			if (laudo.getLaudoModeloTipoExameSetor().getLaudoModelo().isPossuiMetodologia()) {
				Section secaoMetodologia = laudoDocumento.getSectionByName("secaoMetodologia");

				if (secaoMetodologia != null) {
					limparSecao(secaoMetodologia);

					if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() != TipoExameEnum.ALCOOLEMIA.getId()
							&& !laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_ETANOL_EM_AMOSTRA_BIOLOGICA.getId())
							&& laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() != TipoExameEnum.PESQUISA_DE_SEMEN_EM_AMOSTRA_BIOLOGICA.getId()
							&& laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() != TipoExameEnum.TRICHOMONAS_VAGINALLIS.getId()
							&& laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() != TipoExameEnum.DETECCAO_DE_HORMONIO_EM_AMOSTRA_BIOLOGICA.getId()) {
						Paragraph p = secaoMetodologia.addParagraph("MÉTODO");
						p.setFont(fontParagrafoPBold);
					}

					int i = 1;
					for (LaudoMetodologia laudoMetodologia : laudo.getListaLaudoMetodologia()) {
						String conteudo;
						if (laudoMetodologia.getDescricao() != null && !laudoMetodologia.getDescricao().isEmpty()) {
							conteudo = i++ + ") " + laudoMetodologia.getDescricao();
						} else {
							conteudo = i++ + ") " + laudoMetodologia.getMetodologia().getDescricao();
						}
						Paragraph pm = secaoMetodologia.addParagraph(conteudo);
						pm.setFont(fontParagrafoPNormal);
						pm.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
					}
				}
			}

			// QUESITOS
			if (laudo.getLaudoModeloTipoExameSetor().getLaudoModelo().isPossuiQuesito()) {
				if(laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.TORTURA.getId())
					|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.TORTURA_EM_FLAGRANTE.getId())) {
					
					Section secaoQuesitos = laudoDocumento.getSectionByName("secaoQuesitos");
					if (secaoQuesitos != null)
						limparSecao(secaoQuesitos);
						
					int contPergunta = 1;
					Bloco blcAnterior = new Bloco();
					Quesito quesitoBlocoCinco = quesitoDao.find(108);
					
					for (LaudoQuesito laudoQuesito : laudo.getListaLaudoQuesito()) {
						if(!blcAnterior.equals(laudoQuesito.getQuesito().getBloco())) {
							blcAnterior = laudoQuesito.getQuesito().getBloco();
							contPergunta = 1;
							
							Paragraph pBloco = secaoQuesitos.addParagraph(laudoQuesito.getQuesito().getBloco().getDescricao());
							pBloco.setFont(fontParagrafoPBold);
						}
						if(!laudoQuesito.getQuesito().getId().equals(quesitoBlocoCinco.getId())) {
							Paragraph pPergunta;
							pPergunta = secaoQuesitos.addParagraph(contPergunta + ") " + laudoQuesito.getQuesito().getDescricao());
							pPergunta.setFont(fontListaBlack);
							pPergunta.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
						}
						Paragraph pResposta;
						pResposta = secaoQuesitos.addParagraph("Resposta: " + laudoQuesito.getResposta());
						pResposta.setFont(fontParagrafoPNormal);
						pResposta.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
							
						contPergunta++;
					}
				}else {	
					Section secaoQuesitos = laudoDocumento.getSectionByName("secaoQuesitos");
					Section secaoRespostaQuesitos = laudoDocumento.getSectionByName("secaoRespostaQuesito");

					if (secaoQuesitos != null && secaoRespostaQuesitos != null) {
						limparSecao(secaoQuesitos);
						limparSecao(secaoRespostaQuesitos);

						Paragraph pQuesito = secaoQuesitos.addParagraph("QUESITOS:");
						pQuesito.setFont(fontParagrafoPBold);

						Paragraph pRespostaQuesitos = secaoRespostaQuesitos.addParagraph("RESPOSTAS AOS QUESITOS:");
						pRespostaQuesitos.setFont(fontParagrafoPBold);

						int i = 1;
						for (LaudoQuesito laudoQuesito : laudo.getListaLaudoQuesito()) {
							Paragraph pPergunta;
							pPergunta = secaoQuesitos.addParagraph(i + ") " + laudoQuesito.getQuesito().getDescricao());
							pPergunta.setFont(fontListaBlack);
							pPergunta.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

							Paragraph pResposta;
							pResposta = secaoRespostaQuesitos.addParagraph("Resposta ao " + i++ + "º) " + laudoQuesito.getResposta());
							pResposta.setFont(fontParagrafoPNormal);
							pResposta.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
						}
					}

					if (secaoRespostaQuesitos != null) {
						if (solicitacaoProcedimentoPericial.getUltimoExameSolicitacao().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.LESAO_CORPORAL_FLAGRANTE.getId())
								|| solicitacaoProcedimentoPericial.getUltimoExameSolicitacao().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.LESAO_CORPORAL_FLAGRANTE_NUAC.getId())
								|| solicitacaoProcedimentoPericial.getUltimoExameSolicitacao().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.EMBRIAGUEZ.getId())
								|| solicitacaoProcedimentoPericial.getUltimoExameSolicitacao().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.CRIME_SEXUAL_FLAGRANTE.getId())) {

							secaoRespostaQuesitos.addParagraph("");
							Paragraph pRespostaCNJ = secaoRespostaQuesitos.addParagraph("RESPOSTAS AO CNJ");
							pRespostaCNJ.setFont(fontParagrafoBold);
							secaoRespostaQuesitos.addParagraph("");
	
							// REPOSTA COVID19
							Paragraph pPerguntaCOVID19 = secaoRespostaQuesitos.addParagraph(getPerguntaCOVID19());
							pPerguntaCOVID19.setFont(fontListaBlack);
							pPerguntaCOVID19.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

							Paragraph pRespostaCOVID19 = secaoRespostaQuesitos.addParagraph(laudo.getRespostaCOVID19().getDescricao() + ".");
							pRespostaCOVID19.setFont(fontLista);
							pRespostaCOVID19.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
							secaoRespostaQuesitos.addParagraph("");
	
							if (laudo.getComplementoCOVID19() != null && !laudo.getComplementoCOVID19().trim().isEmpty()) {
								Paragraph p = secaoRespostaQuesitos.addParagraph("Informações Complementares:");
								p.setFont(fontListaBlack);
								p.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
	
								Paragraph pComplementoCOVID19 = secaoRespostaQuesitos.addParagraph(laudo.getComplementoCOVID19());
								pComplementoCOVID19.setFont(fontLista);
								pComplementoCOVID19.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
	
								secaoRespostaQuesitos.addParagraph("");
							}
	
							// RESPOSTA PATOLOGIA
							Paragraph pPerguntaPatologia = secaoRespostaQuesitos.addParagraph(getPerguntaPatologias());
							pPerguntaPatologia.setFont(fontListaBlack);
							pPerguntaPatologia.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
	
							Paragraph pRespostaPatologia = secaoRespostaQuesitos.addParagraph(laudo.getRespostaPatologias().getDescricao() + ".");
							pRespostaPatologia.setFont(fontLista);
							pRespostaPatologia.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
							secaoRespostaQuesitos.addParagraph("");
	
							if (laudo.getComplementoPatologias() != null && !laudo.getComplementoPatologias().trim().isEmpty()) {
								Paragraph p = secaoRespostaQuesitos.addParagraph("Informações Complementares:");
								p.setFont(fontListaBlack);
								p.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
	
								Paragraph pComplementoPatologias = secaoRespostaQuesitos.addParagraph(laudo.getComplementoPatologias());
								pComplementoPatologias.setFont(fontLista);
								pComplementoPatologias.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
	
								secaoRespostaQuesitos.addParagraph("");
							}
	
							// RESPOSTA DOCUMENTO MEDICO
							Paragraph pPerguntaDocumentoMedico = secaoRespostaQuesitos.addParagraph(getPerguntaDocumentoMedico());
							pPerguntaDocumentoMedico.setFont(fontListaBlack);
							pPerguntaDocumentoMedico.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
	
							Paragraph pRespostaDocumentoMedico = secaoRespostaQuesitos.addParagraph(laudo.getRespostaDocumentoMedico().getDescricao() + ".");
							pRespostaDocumentoMedico.setFont(fontLista);
							pRespostaDocumentoMedico.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
							secaoRespostaQuesitos.addParagraph("");
	
							if (laudo.getComplementoDocumentoMedico() != null && !laudo.getComplementoDocumentoMedico().trim().isEmpty()) {
								Paragraph p = secaoRespostaQuesitos.addParagraph("Informações Complementares:");
								p.setFont(fontListaBlack);
								p.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
	
								Paragraph pComplementoPatologias = secaoRespostaQuesitos.addParagraph(laudo.getComplementoDocumentoMedico());
								pComplementoPatologias.setFont(fontLista);
								pComplementoPatologias.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
	
								secaoRespostaQuesitos.addParagraph("");
							}
	
							// RESPOSTA DOCUMENTO MEDICO
							Paragraph pEsclarecimento = secaoRespostaQuesitos.addParagraph("ESCLARECIMENTOS ADICIONAIS:");
							pEsclarecimento.setFont(fontListaBlack);
							pEsclarecimento.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
	
							secaoRespostaQuesitos.addParagraph("1) As informações aqui registradas foram colhidas durante Perícia Médico Legal, portanto sem natureza assistencial.");
							secaoRespostaQuesitos.addParagraph(
									"2) Considerando as variáveis de período de incubação, possibilidade de infecção assintomática, bem como a inespecificidade dos sintomas de síndrome gripal, não é possível afirmar ou negar a possibilidade de Síndrome Infecciosa de COVID19");
							secaoRespostaQuesitos.addParagraph("3) Diagnóstico de patologias carece de investigação clínica e de exames complementares prévios executados em unidade de saúde.");
							secaoRespostaQuesitos.addParagraph(
									"4) Havendo necessidade de atendimento de natureza assistencial ou de orientação terapêutica, convém o encaminhamento do periciando para uma Unidade de Pronto Atendimento.");
							secaoRespostaQuesitos.addParagraph("");
	
						}
					}
	
				}
			}
			// CONCLUSÃO
			if (laudo.getLaudoModeloTipoExameSetor().getLaudoModelo().isPossuiConclusao()) {
				Section secaoConclusao = laudoDocumento.getSectionByName("secaoConclusao");

				if (secaoConclusao != null) {
					limparSecao(secaoConclusao);
					if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() != TipoExameEnum.ALCOOLEMIA.getId()
							&& !laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_ETANOL_EM_AMOSTRA_BIOLOGICA.getId())) {
						if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_SANGUE.getId()
								|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_URINA.getId()) {
							Paragraph p = secaoConclusao.addParagraph("CONCLUSÃO");
							p.getOdfElement().setStyleName(style.getStyleNameAttribute());
						} else {
							Paragraph p = secaoConclusao.addParagraph("CONCLUSÃO");
							p.setFont(fontParagrafoPBold);
						}
					}

					if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_DROGAS_EM_SANGUE.getId())
							|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_DROGAS_EM_URINA.getId())) {
//						SolicitacaoResultado solicitacaoResultado = solicitacaoResultadoDao.buscarSolicitacaoResultadoPorSolicitacao(this.solicitacaoProcedimentoPericial);

						if (pericia != null) {
							if (pericia.isConfirmatorio() || pericia.isTriagem()) {
								for (LaudoConclusao laudoConclusao : laudo.getListaLaudoConclusao()) {
									String conteudo;
									if (laudoConclusao.getConclusao().getDescricao() != null && !laudoConclusao.getConclusao().getDescricao().isEmpty()) {

										conteudo = laudoConclusao.getConclusao().getDescricao();
										Paragraph pm = secaoConclusao.addParagraph(conteudo);
										pm.setFont(fontParagrafoPNormal);
										pm.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

									}
								}
							}
						}
					} else {
						for (LaudoConclusao laudoConclusao : laudo.getListaLaudoConclusao()) {

							String conteudo;
							if (laudoConclusao.getDescricao() != null && !laudoConclusao.getDescricao().isEmpty()) {
								conteudo = laudoConclusao.getDescricao();
							} else {
								conteudo = laudoConclusao.getConclusao().getDescricao();
							}

							Paragraph pDescricao;
							pDescricao = secaoConclusao.addParagraph(conteudo);
							pDescricao.setFont(fontParagrafoPNormal);

							pDescricao.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

							if (solicitacaoProcedimentoPericial.getSetor().getSetorPrincipal().getId() == SetorEnum.CALF.getId()
									&& solicitacaoProcedimentoPericial.getUltimoExameSolicitacao().getTipoExameSetor().getTipoExame().getId()
											.equals(TipoExameEnum.IDENTIFICACAO_DE_MACONHA_E_HAXIXE.getId())
									&& solicitacaoProcedimentoPericial.getUltimoExameSolicitacao().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.IDENTIFICACAO_DE_COCAINA.getId())) {
								Paragraph pConclusaoPadrao;
								pConclusaoPadrao = secaoConclusao.addParagraph("As análises realizadas restringiram-se somente a quantidade da substância enviada, "
										+ "conforme o peso acima mensurado, não podemos afirmar se essa amostra corresponde ao total ou à parte do total da apreensão a que se vincula a guia supracitada.");

								pConclusaoPadrao.setFont(fontParagrafoPNormal);
								pConclusaoPadrao.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
							}
						}
					}
				}
			}

			// NOTAS
			if (laudo.getLaudoModeloTipoExameSetor().getLaudoModelo().isPossuiNotas()) {
				Section secaoNotas = laudoDocumento.getSectionByName("secaoNotas");

				if (secaoNotas != null) {
					limparSecao(secaoNotas);

					Paragraph p = secaoNotas.addParagraph("Notas:");
					p.setFont(fontParagrafoPBold);

					for (LaudoNotas laudoNotas : laudo.getListaLaudoNotas()) {

						String conteudo;
						if (laudoNotas.getNotas().getDescricao() != null && !laudoNotas.getNotas().getDescricao().isEmpty()) {
							conteudo = laudoNotas.getNotas().getDescricao();
						} else {
							conteudo = laudoNotas.getNotas().getDescricao();
						}

						Paragraph pDescricao;
						pDescricao = secaoNotas.addParagraph(conteudo);
						if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.ALCOOLEMIA.getId()
								|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_ETANOL_EM_AMOSTRA_BIOLOGICA.getId())) {
							pDescricao.setFont(fontParagrafoNotas);
						} else {
							pDescricao.setFont(fontParagrafoPNormal);

						}

						pDescricao.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
					}

					if (!pericia.isTriagem() && (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_SANGUE.getId()
							|| laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PESQUISA_DE_DROGAS_EM_URINA.getId())) {
						apagarSecao(secaoNotas);
					}
				}
			}

			// Parecer
			if (laudo.getLaudoModeloTipoExameSetor().getLaudoModelo().isPossuiParecer()) {
				Section secaoParecer = laudoDocumento.getSectionByName("secaoParecer");

				if (secaoParecer != null) {
					limparSecao(secaoParecer);

					Paragraph p = secaoParecer.addParagraph("PARECER:");
					p.setFont(fontParagrafoBold);

					Paragraph pDescricao;
					pDescricao = secaoParecer.addParagraph(laudo.getUltimoLaudoParecer().getDescricao());
					pDescricao.setFont(fontLista);
					pDescricao.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
				}
			}

			// Gerar tabela de vestigios
			if (pericia.getListPericiaEvidencia() != null && !pericia.getListPericiaEvidencia().isEmpty()) {
				Section secaoVestigios = laudoDocumento.getSectionByName("secaoVestigios");

				if (secaoVestigios != null) {

					limparSecao(secaoVestigios);

					Table tableBalistica = secaoVestigios.getTableByName("tabelaVestigiosBalistica");
					if (tableBalistica != null)
						tableBalistica.remove();

					Table tableDispositivo = secaoVestigios.getTableByName("tabelaVestigiosDispositivo");
					if (tableDispositivo != null)
						tableDispositivo.remove();

					Table tableDocumento = secaoVestigios.getTableByName("tabelaVestigiosDocumento");
					if (tableDocumento != null)
						tableDocumento.remove();

					Table tableMaterial = secaoVestigios.getTableByName("tabelaVestigiosMaterial");
					if (tableMaterial != null)
						tableMaterial.remove();

					Table tableVeiculo = secaoVestigios.getTableByName("tabelaVestigiosVeiculo");
					if (tableVeiculo != null)
						tableVeiculo.remove();

					Table tableObjeto = secaoVestigios.getTableByName("tabelaVestigiosObjeto");
					if (tableObjeto != null)
						tableObjeto.remove();

					if (solicitacaoProcedimentoPericial.getSetor().getId() != SetorEnum.NUBAF.getId() && solicitacaoProcedimentoPericial.getSetor().getId() != SetorEnum.NUPEX.getId()) {
						Paragraph p = secaoVestigios.addParagraph("MATERIAL EXAMINADO\n");
						p.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
						p.setFont(new Font("Helvetica", FontStyle.BOLD, 11));
					}

					if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_VIDA_HOMICIDIO.getId()) {
						Paragraph p = secaoVestigios.addParagraph("MATERIAL EXAMINADO\n");
						p.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
						p.setFont(new Font("Helvetica", FontStyle.BOLD, 11));
						laudoDocumento.addPageBreak();
					}

					boolean paragrafoBalisticaGerado = false;
					boolean paragrafoDispositivoGerado = false;
					boolean paragrafoDocumentoGerado = false;
					boolean paragrafoMaterialGerado = false;
					boolean paragrafoVeiculoGerado = false;
					boolean paragrafoObjetoGerado = false;
					boolean paragrafoCartuchoGerado = false;
					boolean paragrafoEstojoGerado = false;
					boolean paragrafoProjetilGerado = false;
					boolean paragrafoGeradoExameDeEficiencia = false;

					int numeroArma = 0;
					int numeroArmaEficiente = 0;

					Section secaoArma = laudoDocumento.getSectionByName("secaoArma");
					if (secaoArma != null) {
						limparSecao(secaoArma);
					}

					Table tableCartucho;
					Section secaoCartucho = laudoDocumento.getSectionByName("secaoCartucho");
					if (secaoCartucho != null) {
						limparSecao(secaoCartucho);
						tableCartucho = secaoCartucho.getTableByName("tabelaVestigiosCartucho");
						if (tableCartucho != null)
							tableCartucho.remove();
					}

					Table tableEstojo;
					Section secaoEstojo = laudoDocumento.getSectionByName("secaoEstojo");
					if (secaoEstojo != null) {
						limparSecao(secaoEstojo);
						tableEstojo = secaoEstojo.getTableByName("tabelaVestigiosEstojo");
						if (tableEstojo != null) {
							tableEstojo.remove();
						}
					}

					Table tableProjetil;
					Section secaoProjetil = laudoDocumento.getSectionByName("secaoProjetil");
					if (secaoProjetil != null) {
						limparSecao(secaoProjetil);
						tableProjetil = secaoProjetil.getTableByName("tabelaVestigiosProjetil");
						if (tableProjetil != null) {
							tableProjetil.remove();
						}
					}

					Section secaoExameDeEficiencia = laudoDocumento.getSectionByName("secaoExameDeEficiencia");
					if (secaoExameDeEficiencia != null) {
						limparSecao(secaoExameDeEficiencia);
					}

					for (PericiaEvidencia periciaEvidencia : pericia.getListPericiaEvidencia()) {
						if (periciaEvidencia.isUtilizaNoLaudo()) {

							periciaEvidencia.getEvidencia().setListEvidenciaFoto(evidenciaFotoDao.buscarFotoReconhecimentoPorEvidencia(periciaEvidencia.getEvidencia()));

							// SE O SETOR DA SOLICITACAO FOR NUBAF
							if (solicitacaoProcedimentoPericial.getSetor().getId() == SetorEnum.NUBAF.getId()) {
								switch (periciaEvidencia.getEvidencia().getTipoEvidencia()) {
								case MATERIAL_BALISTICO:
									switch (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoArma()) {
									case ARMA:

										if (secaoArma != null) {

											Paragraph pA = secaoArma.addParagraph("\nArma " + (StrUtil.lpad(Integer.toString(++numeroArma), 2, '0') + ""));
											pA.setFont(fontParagrafoBold);
											pA.setHorizontalAlignment(HorizontalAlignmentType.LEFT);

											// ADICIONANDO AS PROPRIEDADES DA ARMA EM UM PARAGRAFO SÓ“

											Paragraph paragrafoArma;
											String textoArma = "";

											textoArma = textoArma
													+ (periciaEvidencia.getEvidencia().getUltimoLacre() != null ? periciaEvidencia.getEvidencia().getUltimoLacre().getNumero() + "\n" : "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoArma().getDescricao() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoArma().getDescricao().isEmpty()
															? "Classificação: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoTipo().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoMarca() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoMarca().getDescricao().isEmpty()
															? "\nMarca: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoMarca().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoModelo() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoModelo().getDescricao().isEmpty()
															? "\nModelo: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoModelo().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getFabricacao() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getFabricacao().isEmpty()
															? "\nFabricação: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getFabricacao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAdulteracao() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAdulteracao().getDescricao().isEmpty()
															? "\nAdulteração da arma: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAdulteracao().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre().getDescricao().isEmpty()
															? "\nCalibre nominal: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getNumeroSerie() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getNumeroSerie().isEmpty()
															? "\nNúmero de série: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getNumeroSerie()
															: "");

											if (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getListaTipolocalNumeroDeSerie() != null
													&& periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getListaTipolocalNumeroDeSerie().length > 0) {
												textoArma = textoArma + "\nLocal do número de série: ";
												for (int descricaoLocalNumeroDeSerie : periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getListaTipolocalNumeroDeSerie()) {
													for (TipoRespostaLocalNumeroDeSerieEnum localNumeroSerie : TipoRespostaLocalNumeroDeSerieEnum.values()) {
														if (localNumeroSerie.getId() == descricaoLocalNumeroDeSerie)
															textoArma = textoArma + (localNumeroSerie.getDescricao() + "; ");
													}
												}
											}

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroLocalNumeroDeSerie() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroLocalNumeroDeSerie().isEmpty()
															? "\n   » Outro local do número de série: "
																	+ periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroLocalNumeroDeSerie()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().isRaspado() != false ? "\nRaspado: Sim" : "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoFuncionamentoArma() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoFuncionamentoArma().getDescricao().isEmpty()
															? "\nFuncionamento: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoFuncionamentoArma().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoSentidoRaia() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoSentidoRaia().getDescricao().isEmpty()
															? "\nSentido das raias: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoSentidoRaia().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getMedidaDoCano() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getMedidaDoCano().isEmpty()
															? "\nComprimento do cano: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getMedidaDoCano()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAlmaDoCano() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAlmaDoCano().getDescricao().isEmpty()
															? "\nAlma do cano: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAlmaDoCano().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getComprimentoTotalDaArma() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getComprimentoTotalDaArma().isEmpty()
															? "\nComprimento total da arma: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getComprimentoTotalDaArma()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getAlturaDaCoronha() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getAlturaDaCoronha().isEmpty()
															? "\nAltura da coronha: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getAlturaDaCoronha()
															: "");

											textoArma = textoArma + ("\nTipo de Acabamento/Armação " + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcabamentoArmacao() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcabamentoArmacao().getDescricao().isEmpty()
															? "\n   » Armação: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcabamentoArmacao().getDescricao()
															: ""));

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroAcabamentoArmacao() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroAcabamentoArmacao().isEmpty()
															? "\n   » Outro Tipo de acabamento armação: "
																	+ periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroAcabamentoArmacao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcabamentoFerrolho() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcabamentoFerrolho().getDescricao().isEmpty()
															? "\n   » Ferrolho: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcabamentoFerrolho().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroAcabamentoFerrolho() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroAcabamentoFerrolho().isEmpty()
															? "\n   ”» Outro Tipo de acabamento ferrolho: "
																	+ periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroAcabamentoFerrolho()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcabamentoCano() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcabamentoCano().getDescricao().isEmpty()
															? "\n   » Cano: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcabamentoCano().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroAcabamentoCano() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroAcabamentoCano().isEmpty()
															? "\n   » Outro Tipo de acabamento cano: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroAcabamentoCano()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoRespostaArmacaoArticulada() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoRespostaArmacaoArticulada().getDescricao().isEmpty()
															? "\n   » Armação articulada: "
																	+ periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoRespostaArmacaoArticulada().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoRespostaArmacaoRigida() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoRespostaArmacaoRigida().getDescricao().isEmpty()
															? "\n   » Armação rígida: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoRespostaArmacaoRigida().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAparelhoDePontariaAlca() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAparelhoDePontariaAlca().getDescricao().isEmpty()
															? "\n   » Aparelho de pontaria alça: "
																	+ periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAparelhoDePontariaAlca().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAparelhoDePontariaMassa() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAparelhoDePontariaMassa().getDescricao().isEmpty()
															? "\n   » Aparelho de pontaria massa: "
																	+ periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAparelhoDePontariaMassa().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroTipoAparelhoDePontariaMassa() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroTipoAparelhoDePontariaMassa().isEmpty()
															? "\n   » Outro aparelho de pontaria Massa: "
																	+ periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroTipoAparelhoDePontariaMassa()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoCoronha() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoCoronha().getDescricao().isEmpty()
															? "\nCoronha: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoCoronha().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getCor() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getCor().isEmpty()
															? "\nCor: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getCor()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroTipoDeCoronha() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroTipoDeCoronha().isEmpty()
															? "\n   » Outro tipo de coronha: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroTipoDeCoronha()
															: "");

											textoArma = textoArma
													+ (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().isLogomarcaDoFabricante() != false ? "\nLogomarca do fabricante: Sim" : "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getCapacidade() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getCapacidade().isEmpty()
															? "\nCapacidade: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getCapacidade()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcaoArma() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcaoArma().getDescricao().isEmpty()
															? "\nAção: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoAcaoArma().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoSistemaDePercussao() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoSistemaDePercussao().getDescricao().isEmpty()
															? "\nSistema de percussão: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoSistemaDePercussao().getDescricao()
															: "");

											if (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getListaTipoSistemaDeSegurancaDaArma() != null
													&& periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getListaTipoSistemaDeSegurancaDaArma().length > 0) {
												textoArma = textoArma + "\nSistema de segurança: ";
												for (int descricaoSistemaDeSeguranca : periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getListaTipoSistemaDeSegurancaDaArma()) {
													for (TipoSistemaDeSegurancaDaArmaEnum tipoSistemaDeSeguranca : TipoSistemaDeSegurancaDaArmaEnum.values()) {
														if (tipoSistemaDeSeguranca.getId() == descricaoSistemaDeSeguranca)
															textoArma = textoArma + (tipoSistemaDeSeguranca.getDescricao() + "; ");
													}
												}
											}

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getNumeroCamaras() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getNumeroCamaras().isEmpty()
															? "\nNº de câmaras: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getNumeroCamaras()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroTipoDeSistemaDeSegurancaDaArma() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroTipoDeSistemaDeSegurancaDaArma().isEmpty()
															? "\n   » Outro sistema de segurança: "
																	+ periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroTipoDeSistemaDeSegurancaDaArma()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoEstadoDeConservacaoDaArma() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoEstadoDeConservacaoDaArma().getDescricao().isEmpty()
															? "\nEstado geral de conservação: "
																	+ periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoEstadoDeConservacaoDaArma().getDescricao()
															: "");

											// Tipo carregador de espingarda
											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoCarregador() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoCarregador().getDescricao().isEmpty()
															? "\nCarregador(es): " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoCarregador().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().isRepublica() != false ? "\nBrasões da república: Sim" : "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getLocalBrasaoRepublica() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getLocalBrasaoRepublica().isEmpty()
															? "\nLocal brasões da república: " + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getLocalBrasaoRepublica())
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().isBrasoesEstado() != false ? "\nBrasões do estado: Sim" : "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getLocalBrasaoEstado() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getLocalBrasaoEstado().isEmpty()
															? "\nLocal brasões do estado: " + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getLocalBrasaoEstado())
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().isBrasoesPolicia() != false ? "\nBrasões da polícia: Sim " : "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getLocalBrasaoPolicia() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getLocalBrasaoPolicia().isEmpty()
															? "\nLocal brasões da polícia: " + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getLocalBrasaoPolicia())
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoPolicia() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoPolicia().getDescricao().isEmpty()
															? "\nPolicia " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoPolicia().getDescricao()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEstadoPolicia() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEstadoPolicia().isEmpty()
															? "; Estado: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEstadoPolicia()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getAbreviacoes() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getAbreviacoes().isEmpty()
															? "; Abreviações: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getAbreviacoes()
															: "");

											// Carregador de Pistola
											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getCarregadorPistola() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getCarregadorPistola().isEmpty()
															? "\nCarregador: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getCarregadorPistola()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getBandoleira() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getBandoleira().isEmpty()
															? "\nOutros: " + "\n   » Bandoleira: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getBandoleira()
															: "");

											textoArma = textoArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getSoleira() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getSoleira().isEmpty()
															? "\n   » Soleira: " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getSoleira()
															: "");

											paragrafoArma = secaoArma.addParagraph(textoArma);

											// CONFIGURAÇÃƒO DO PARAGRAFO PROPRIEDADES DA ARMA
											paragrafoArma.setFont(fontParagrafoPNormal);
											paragrafoArma.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
										}

										// PARAGRAFO EFICIÊNCIA DA ARMA
										if (secaoExameDeEficiencia != null) {

											if (!paragrafoGeradoExameDeEficiencia) {
												Paragraph pE = secaoExameDeEficiencia.addParagraph("\nEXAME DE EFICIÊNCIA");
												pE.setFont(fontParagrafoBold);
												pE.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
												paragrafoGeradoExameDeEficiencia = true;

											}

											Paragraph paragrafoEficienciaDaArma = secaoExameDeEficiencia.addParagraph("\nArma " + (StrUtil.lpad(Integer.toString(++numeroArmaEficiente), 2, '0') + ""));
											paragrafoEficienciaDaArma.setFont(fontParagrafoPNormal);
											paragrafoEficienciaDaArma.setHorizontalAlignment(HorizontalAlignmentType.LEFT);

											Paragraph paragrafoTestesDeEficiencia;
											String textoEficienciaDaArma = "";
											textoEficienciaDaArma = textoEficienciaDaArma + (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getArmaOperante() != null
													&& !periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getArmaOperante().getDescricao().isEmpty()
													&& periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getArmaOperante().getDescricao().equals(TipoRespostaEnum.SIM.getDescricao())
															? "Com a arma examinada foram efetuados tiros, e se observou que os mecanismos funcionaram normalmente, sem nenhuma deficiência assinalável."
															: " Não é eficiente.\n " + periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaMotivoDaInoperancia());

											paragrafoTestesDeEficiencia = secaoExameDeEficiencia.addParagraph(textoEficienciaDaArma);

											// CONFIGURAÇÃƒO DO PARAGRAFO PROPRIEDADES DA EFICIENCIA DA ARMA
											paragrafoTestesDeEficiencia.setFont(fontParagrafoPNormal);
											paragrafoTestesDeEficiencia.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
										}

										break;
									case CARTUCHO:
										if (secaoCartucho != null) {
											if (!paragrafoCartuchoGerado) {
												Paragraph pC = secaoCartucho.addParagraph("CARTUCHO(S)");
												pC.setFont(fontParagrafoBold);
												pC.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
												paragrafoCartuchoGerado = true;

												Paragraph paragrafoCartucho = secaoCartucho
														.addParagraph("Foram encaminhados XX (XXXX) cartuchos para arma de fogo, cujas características são apresentadas na tabela a seguir: \n");
												paragrafoCartucho.setFont(fontParagrafoPNormal);
												paragrafoCartucho.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
											}

											EvidenciaArmamento cartucho = evidenciaArmamentoDao.buscarUltimoArmamentoPorEvidencia(periciaEvidencia.getEvidencia());
											tableCartucho = secaoCartucho.getTableByName("tabelaVestigiosCartucho");

											if (tableCartucho == null) {
												// Montando cabeçalho da tabela de cartuchos
												tableCartucho = secaoCartucho.addTable(1, 9);
												tableCartucho.setTableName("tabelaVestigiosCartucho");

												// Montando colunas da tabela de cartuchos
												Cell cellCartucho0 = tableCartucho.getCellByPosition(0, 0);
												cellCartucho0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellCartucho0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellCartucho0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellCartucho0.addParagraph("Lacre").setFont(fontTableHeader);

												Cell cellCartucho1 = tableCartucho.getCellByPosition(1, 0);
												cellCartucho1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellCartucho1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellCartucho1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellCartucho1.addParagraph("Quant.").setFont(fontTableHeader);

												Cell cellCartucho2 = tableCartucho.getCellByPosition(2, 0);
												cellCartucho2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellCartucho2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellCartucho2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellCartucho2.addParagraph("Calibre nominal").setFont(fontTableHeader);

												Cell cellCartucho3 = tableCartucho.getCellByPosition(3, 0);
												cellCartucho3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellCartucho3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellCartucho3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellCartucho3.addParagraph("Marca/\nFabricação").setFont(fontTableHeader);

												Cell cellCartucho4 = tableCartucho.getCellByPosition(4, 0);
												cellCartucho4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellCartucho4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellCartucho4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellCartucho4.addParagraph("Projétil").setFont(fontTableHeader);

												Cell cellCartucho5 = tableCartucho.getCellByPosition(5, 0);
												cellCartucho5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellCartucho5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellCartucho5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellCartucho5.addParagraph("Estado").setFont(fontTableHeader);

												Cell cellCartucho6 = tableCartucho.getCellByPosition(6, 0);
												cellCartucho6.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellCartucho6.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellCartucho6.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellCartucho6.addParagraph("Número de lote").setFont(fontTableHeader);

												Cell cellCartucho7 = tableCartucho.getCellByPosition(7, 0);
												cellCartucho7.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellCartucho7.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellCartucho7.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellCartucho7.addParagraph("Qtde.\nutilizada").setFont(fontTableHeader);

												Cell cellCartucho8 = tableCartucho.getCellByPosition(8, 0);
												cellCartucho8.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellCartucho8.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellCartucho8.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellCartucho8.addParagraph("Qtde.\nremanescente").setFont(fontTableHeader);

											}
											// Alimentando tabela com os campos das evidências

											int linha = tableCartucho.getRowCount();

											// NUMER DO LACRE
											Cell cellCartucho0 = tableCartucho.getCellByPosition(0, linha);
											cellCartucho0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellCartucho0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellCartucho0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellCartucho0.addParagraph(periciaEvidencia.getEvidencia().getUltimoLacre() != null ? periciaEvidencia.getEvidencia().getUltimoLacre().getNumero() : " ")
													.setFont(fontTableDetail);

											// QUANTIDADE
											Cell cellCartucho1 = tableCartucho.getCellByPosition(1, linha);
											cellCartucho1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellCartucho1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellCartucho1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellCartucho1.addParagraph(cartucho.getQuantidade().toString()).setFont(fontTableDetail);

											// CALIBRE
											Cell cellCartucho2 = tableCartucho.getCellByPosition(2, linha);
											cellCartucho2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellCartucho2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellCartucho2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellCartucho2.addParagraph(cartucho.getEvidenciaArmamentoCalibre() != null ? cartucho.getEvidenciaArmamentoCalibre().getDescricao() : " ")
													.setFont(fontTableDetail);

											// MARCA
											Cell cellCartucho3 = tableCartucho.getCellByPosition(3, linha);
											cellCartucho3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellCartucho3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellCartucho3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellCartucho3.addParagraph(cartucho.getEvidenciaArmamentoMarca() != null ? cartucho.getEvidenciaArmamentoMarca().getDescricao() : " ")
													.setFont(fontTableDetail);

											// PROJETIL
											Cell cellCartucho4 = tableCartucho.getCellByPosition(4, linha);
											cellCartucho4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellCartucho4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellCartucho4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellCartucho4.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoProjetil() != null
													? periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoProjetil().getDescricao()
													: " ").setFont(fontTableDetail);

											// ESTADO
											Cell cellCartucho5 = tableCartucho.getCellByPosition(5, linha);
											cellCartucho5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellCartucho5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellCartucho5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellCartucho5.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoEstadoDeConservacaoCartuchoOuEstojo() != null
													? periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoEstadoDeConservacaoCartuchoOuEstojo().getDescricao()
													: " ").setFont(fontTableDetail);

											// NUMERO DE LOTE
											Cell cellCartucho6 = tableCartucho.getCellByPosition(6, linha);
											cellCartucho6.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellCartucho6.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellCartucho6.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellCartucho6.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getLote() != null
													? periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getLote()
													: " ").setFont(fontTableDetail);

											// QTDE UTILIZADA
											Cell cellCartucho7 = tableCartucho.getCellByPosition(7, linha);
											cellCartucho7.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellCartucho7.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellCartucho7.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellCartucho7.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getQuantidadeUtilizada() != null
													? periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getQuantidadeUtilizada()
													: " ").setFont(fontTableDetail);

											// QTDE REMANESCENTE
											Cell cellCartucho8 = tableCartucho.getCellByPosition(8, linha);
											cellCartucho8.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellCartucho8.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellCartucho8.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellCartucho8.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getQuantidadeRemanescente() != null
													? periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getQuantidadeRemanescente()
													: " ").setFont(fontTableDetail);
										}
										break;
									case ESTOJO:
										if (!paragrafoEstojoGerado) {
											Paragraph pE = secaoEstojo.addParagraph("ESTOJO(S)");
											pE.setFont(fontParagrafoBold);
											pE.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
											paragrafoEstojoGerado = true;

											Paragraph paragrafoEstojo = secaoEstojo
													.addParagraph("Foram encaminhados XX (XXXX) estojos para arma de fogo, cujas características são apresentadas na tabela a seguir:");
											paragrafoEstojo.setFont(fontParagrafoPNormal);
											paragrafoEstojo.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
											secaoEstojo.addParagraph(" ");
										}

										EvidenciaArmamento estojo = evidenciaArmamentoDao.buscarUltimoArmamentoPorEvidencia(periciaEvidencia.getEvidencia());

										tableEstojo = secaoEstojo.getTableByName("tabelaVestigiosEstojo");

										if (tableEstojo == null) {
											// Montando cabeçalho da tabela de estojos
											tableEstojo = secaoEstojo.addTable(1, 6);
											tableEstojo.setTableName("tabelaVestigiosEstojo");

											// Montando colunas da tabela de estojos
											Cell cellEstojo0 = tableEstojo.getCellByPosition(0, 0);
											cellEstojo0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellEstojo0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellEstojo0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellEstojo0.addParagraph("Lacre").setFont(fontTableHeader);

											Cell cellEstojo1 = tableEstojo.getCellByPosition(1, 0);
											cellEstojo1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellEstojo1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellEstojo1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellEstojo1.addParagraph("Quant.").setFont(fontTableHeader);

											Cell cellEstojo2 = tableEstojo.getCellByPosition(2, 0);
											cellEstojo2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellEstojo2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellEstojo2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellEstojo2.addParagraph("Calibre nominal").setFont(fontTableHeader);

											Cell cellEstojo3 = tableEstojo.getCellByPosition(3, 0);
											cellEstojo3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellEstojo3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellEstojo3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellEstojo3.addParagraph("Marca/\nFabricação").setFont(fontTableHeader);

											Cell cellEstojo4 = tableEstojo.getCellByPosition(4, 0);
											cellEstojo4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellEstojo4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellEstojo4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellEstojo4.addParagraph("Estado").setFont(fontTableHeader);

											Cell cellEstojo5 = tableEstojo.getCellByPosition(5, 0);
											cellEstojo5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellEstojo5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellEstojo5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellEstojo5.addParagraph("Número de lote").setFont(fontTableHeader);
										}

										// Alimentando tabela com os campos das evidÃªncias

										int linha = tableEstojo.getRowCount();
										// LACRE
										Cell cellEstojo0 = tableEstojo.getCellByPosition(0, linha);
										cellEstojo0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellEstojo0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellEstojo0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellEstojo0.addParagraph(periciaEvidencia.getEvidencia().getUltimoLacre() != null ? periciaEvidencia.getEvidencia().getUltimoLacre().getNumero() : " ")
												.setFont(fontTableDetail);

										// QUANTIDADE
										Cell cellEstojo1 = tableEstojo.getCellByPosition(1, linha);
										cellEstojo1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellEstojo1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellEstojo1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellEstojo1.addParagraph(estojo.getQuantidade() != null ? estojo.getQuantidade().toString() : " ").setFont(fontTableDetail);

										// CALIBRE NOMINAL
										Cell cellEstojo2 = tableEstojo.getCellByPosition(2, linha);
										cellEstojo2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellEstojo2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellEstojo2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellEstojo2.addParagraph(estojo.getEvidenciaArmamentoCalibre() != null ? estojo.getEvidenciaArmamentoCalibre().getDescricao() : " ").setFont(fontTableDetail);

										// MARCA/FABRICAÇÃƒO
										Cell cellEstojo3 = tableEstojo.getCellByPosition(3, linha);
										cellEstojo3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellEstojo3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellEstojo3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellEstojo3.addParagraph(estojo.getEvidenciaArmamentoMarca() != null ? estojo.getEvidenciaArmamentoMarca().getDescricao() : " ").setFont(fontTableDetail);

										// ESTADO
										Cell cellEstojo4 = tableEstojo.getCellByPosition(4, linha);
										cellEstojo4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellEstojo4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellEstojo4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellEstojo4.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoEstadoDeConservacaoCartuchoOuEstojo() != null
												? periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoEstadoDeConservacaoCartuchoOuEstojo().getDescricao()
												: " ").setFont(fontTableDetail);

										// NUMERO DE LOTE
										Cell cellEstojo5 = tableEstojo.getCellByPosition(5, linha);
										cellEstojo5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellEstojo5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellEstojo5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellEstojo5.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getLote() != null
												? periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getLote()
												: " ").setFont(fontTableDetail);

										break;
									case CARREGADOR:
										break;
									case PROJETIL:
										if (secaoProjetil != null) {
											if (!paragrafoProjetilGerado && secaoProjetil != null) {
												Paragraph pE = secaoProjetil.addParagraph("PROJÉTIL(EIS)");
												pE.setFont(fontParagrafoBold);
												pE.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
												paragrafoProjetilGerado = true;

												Paragraph paragrafoProjetil = secaoProjetil
														.addParagraph("Foram encaminhados XX (XXXX) projétil(eis) cujas características são apresentadas na tabela a seguir:");
												paragrafoProjetil.setFont(fontParagrafoPNormal);
												paragrafoProjetil.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
												secaoProjetil.addParagraph(" ");

												tableProjetil = secaoProjetil.getTableByName("tabelaVestigiosProjetil");

												if (tableProjetil == null) {
													// Montando cabeçalho da tabela de estojos
													tableProjetil = secaoProjetil.addTable(1, 6);
													tableProjetil.setTableName("tabelaVestigiosProjetil");

													// Montando colunas da tabela de estojos
													Cell cellProjetil0 = tableProjetil.getCellByPosition(0, 0);
													cellProjetil0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellProjetil0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellProjetil0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellProjetil0.addParagraph("Quant.").setFont(fontTableHeader);

													Cell cellProjetil1 = tableProjetil.getCellByPosition(1, 0);
													cellProjetil1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellProjetil1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellProjetil1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellProjetil1.addParagraph("Calibre").setFont(fontTableHeader);

													Cell cellProjetil2 = tableProjetil.getCellByPosition(2, 0);
													cellProjetil2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellProjetil2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellProjetil2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellProjetil2.addParagraph("Massa(G)").setFont(fontTableHeader);

													Cell cellProjetil3 = tableProjetil.getCellByPosition(3, 0);
													cellProjetil3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellProjetil3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellProjetil3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellProjetil3.addParagraph("Diâmetro Méd.").setFont(fontTableHeader);

													Cell cellProjetil4 = tableProjetil.getCellByPosition(4, 0);
													cellProjetil4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellProjetil4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellProjetil4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellProjetil4.addParagraph("Comp.").setFont(fontTableHeader);

													Cell cellProjetil5 = tableProjetil.getCellByPosition(5, 0);
													cellProjetil5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellProjetil5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellProjetil5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellProjetil5.addParagraph("Material").setFont(fontTableHeader);

													Cell cellProjetil6 = tableProjetil.getCellByPosition(6, 0);
													cellProjetil6.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellProjetil6.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellProjetil6.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellProjetil6.addParagraph("Deformação").setFont(fontTableHeader);
												}

												// Alimentando tabela com os campos das evidÃªncias

												linha = tableProjetil.getRowCount();
												// QUANTIDADE
												Cell cellProjetil0 = tableProjetil.getCellByPosition(0, linha);
												cellProjetil0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellProjetil0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellProjetil0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellProjetil0.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade() != null
														? periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade().toString()
														: " ").setFont(fontTableDetail);

												// CALIBRE
												Cell cellProjetil1 = tableProjetil.getCellByPosition(1, linha);
												cellProjetil1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellProjetil1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellProjetil1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellProjetil1.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre() != null
														? periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre().getDescricao()
														: " ").setFont(fontTableDetail);

												// MASSA
												Cell cellProjetil2 = tableProjetil.getCellByPosition(2, linha);
												cellProjetil2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellProjetil2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellProjetil2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellProjetil2.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getMassaProjetil() != null
														? periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getMassaProjetil().toString()
														: " ").setFont(fontTableDetail);

												// DIAMETRO MÉDIO
												Cell cellProjetil3 = tableProjetil.getCellByPosition(3, linha);
												cellProjetil3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellProjetil3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellProjetil3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellProjetil3.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getDiametroProjetil() != null
														? periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getDiametroProjetil().toString()
														: " ").setFont(fontTableDetail);

												// COMPRIMENTO
												Cell cellProjetil4 = tableProjetil.getCellByPosition(4, linha);
												cellProjetil4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellProjetil4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellProjetil4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellProjetil4.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getComprimentoProjetil() != null
														? periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getComprimentoProjetil().toString()
														: " ").setFont(fontTableDetail);

												// TIPO DE MATERIAL
												Cell cellProjetil5 = tableProjetil.getCellByPosition(5, linha);
												cellProjetil5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellProjetil5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellProjetil5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));

												if (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoMaterialProjetil() != null && !periciaEvidencia.getEvidencia()
														.getUltimaEvidenciaArmamento().getTipoMaterialProjetil().getId().equals(TipoRespostaMaterialProjetilEnum.OUTRO.getId())) {
													cellProjetil5.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoMaterialProjetil().getDescricao())
															.setFont(fontTableDetail);
												}

												if (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoMaterialProjetil() != null && periciaEvidencia.getEvidencia()
														.getUltimaEvidenciaArmamento().getTipoMaterialProjetil().getId().equals(TipoRespostaMaterialProjetilEnum.OUTRO.getId())) {
													cellProjetil5.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getRespostaOutroTipoDeMaterialProjetil())
															.setFont(fontTableDetail);
												}

												// DEFORMACAO
												Cell cellProjetil6 = tableProjetil.getCellByPosition(6, linha);
												cellProjetil6.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellProjetil6.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellProjetil6.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellProjetil6.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoDeformacaoProjetil() != null
														? periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoDeformacaoProjetil().getDescricao()
														: " ").setFont(fontTableDetail);
											}
										}

										break;
									case OUTROS:
										break;
									default:
										break;
									}
									break;
								case DISPOSITIVO_TECNOLOGICO:
									if (secaoVestigios != null) {
										if (!paragrafoDispositivoGerado) {
											Paragraph pb = secaoVestigios.addParagraph("\nDispositivos Tecnológicos\n");
											pb.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											pb.setFont(fontParagrafoBold);
											paragrafoDispositivoGerado = true;
										}

										EvidenciaDispositivoTecnologico dispositivo = evidenciaDispositivoTecnologicoDao.buscarUltimoDispositivoPorEvidencia(periciaEvidencia.getEvidencia());
										tableDispositivo = secaoVestigios.getTableByName("tabelaVestigiosDispositivo");
										if (tableDispositivo == null) {

											// Cria o cabeçalho da tabela.
											tableDispositivo = secaoVestigios.addTable(1, 7);
											tableDispositivo.setTableName("tabelaVestigiosDispositivo");

											Cell cellDispositivo0 = tableDispositivo.getCellByPosition(0, 0);
											cellDispositivo0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDispositivo0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDispositivo0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDispositivo0.addParagraph("Id").setFont(fontTableHeader);

											Cell cellDispositivo1 = tableDispositivo.getCellByPosition(1, 0);
											cellDispositivo1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDispositivo1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDispositivo1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDispositivo1.addParagraph("Tipo").setFont(fontTableHeader);

											Cell cellDispositivo2 = tableDispositivo.getCellByPosition(2, 0);
											cellDispositivo2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDispositivo2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDispositivo2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDispositivo2.addParagraph("Fabricante").setFont(fontTableHeader);

											Cell cellDispositivo3 = tableDispositivo.getCellByPosition(3, 0);
											cellDispositivo3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDispositivo3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDispositivo3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDispositivo3.addParagraph("Modelo").setFont(fontTableHeader);

											Cell cellDispositivo4 = tableDispositivo.getCellByPosition(4, 0);
											cellDispositivo4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDispositivo4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDispositivo4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDispositivo4.addParagraph("Nº Série").setFont(fontTableHeader);

											Cell cellDispositivo5 = tableDispositivo.getCellByPosition(5, 0);
											cellDispositivo5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDispositivo5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDispositivo5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDispositivo5.addParagraph("Capacidade").setFont(fontTableHeader);

											Cell cellDispositivo6 = tableDispositivo.getCellByPosition(6, 0);
											cellDispositivo6.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDispositivo6.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDispositivo6.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDispositivo6.addParagraph("IMEIs").setFont(fontTableHeader);
										}

										// Cria os dados da tabela para a evidência.
										int linha = tableDispositivo.getRowCount();
										Cell cellDispositivo0 = tableDispositivo.getCellByPosition(0, linha);
										cellDispositivo0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellDispositivo0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellDispositivo0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellDispositivo0.addParagraph(dispositivo.getEvidencia().getId().toString()).setFont(fontTableDetail);

										Cell cellDispositivo1 = tableDispositivo.getCellByPosition(1, linha);
										cellDispositivo1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellDispositivo1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellDispositivo1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellDispositivo1.addParagraph(dispositivo.getEvidenciaDispositivoTecnologicoTipo().getDescricao()).setFont(fontTableDetail);

										Cell cellDispositivo2 = tableDispositivo.getCellByPosition(2, linha);
										cellDispositivo2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellDispositivo2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellDispositivo2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellDispositivo2.addParagraph(
												dispositivo.getEvidenciaDispositivoTecnologicoFabricante() != null ? dispositivo.getEvidenciaDispositivoTecnologicoFabricante().getDescricao() : "")
												.setFont(fontTableDetail);

										Cell cellDispositivo3 = tableDispositivo.getCellByPosition(3, linha);
										cellDispositivo3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellDispositivo3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellDispositivo3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellDispositivo3
												.addParagraph(
														dispositivo.getEvidenciaDispositivoTecnologicoModelo() != null ? dispositivo.getEvidenciaDispositivoTecnologicoModelo().getDescricao() : "")
												.setFont(fontTableDetail);

										Cell cellDispositivo4 = tableDispositivo.getCellByPosition(4, linha);
										cellDispositivo4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellDispositivo4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellDispositivo4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellDispositivo4.addParagraph(dispositivo.getNumeroSerie()).setFont(fontTableDetail);

										Cell cellDispositivo5 = tableDispositivo.getCellByPosition(5, linha);
										cellDispositivo5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellDispositivo5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellDispositivo5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellDispositivo5.addParagraph((dispositivo.getCapacidade() != null ? dispositivo.getCapacidade().toString() : "") + " "
												+ (dispositivo.getCapacidadeUnidade() != null ? dispositivo.getCapacidadeUnidade().getSigla() : "")).setFont(fontTableDetail);

										Cell cellDispositivo6 = tableDispositivo.getCellByPosition(6, linha);
										cellDispositivo6.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellDispositivo6.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellDispositivo6.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										List<EvidenciaDispositivoTecnologicoImei> imeis = evidenciaDispositivoTecnologicoImeiDao.buscarPorDispositivo(dispositivo);

										for (EvidenciaDispositivoTecnologicoImei imei : imeis) {
											cellDispositivo6.addParagraph(imei.getNumero()).setFont(fontTableDetail);
										}
									}
									break;
								case DOCUMENTO:
									if (secaoVestigios != null) {
										if (!paragrafoDocumentoGerado) {
											Paragraph pb = secaoVestigios.addParagraph("\nDocumentos\n");
											pb.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											pb.setFont(fontParagrafoBold);
											paragrafoDocumentoGerado = true;
										}

										EvidenciaDocumento documento = evidenciaDocumentoDao.buscarUltimoDocumentoPorEvidencia(periciaEvidencia.getEvidencia());
										tableDocumento = secaoVestigios.getTableByName("tabelaVestigiosDocumento");
										if (tableDocumento == null) {

											// Cria o cabeÃ§alho para a tabela de documentos.
											tableDocumento = secaoVestigios.addTable(1, 5);
											tableDocumento.setTableName("tabelaVestigiosDocumento");

											Cell cellDocumento0 = tableDocumento.getCellByPosition(0, 0);
											cellDocumento0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDocumento0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDocumento0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDocumento0.addParagraph("Id").setFont(fontTableHeader);

											Cell cellDocumento1 = tableDocumento.getCellByPosition(1, 0);
											cellDocumento1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDocumento1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDocumento1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDocumento1.addParagraph("Tipo").setFont(fontTableHeader);

											Cell cellDocumento2 = tableDocumento.getCellByPosition(2, 0);
											cellDocumento2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDocumento2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDocumento2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDocumento2.addParagraph("Páginas").setFont(fontTableHeader);

											Cell cellDocumento3 = tableDocumento.getCellByPosition(3, 0);
											cellDocumento3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDocumento3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDocumento3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDocumento3.addParagraph("Folhas").setFont(fontTableHeader);

											Cell cellDocumento4 = tableDocumento.getCellByPosition(4, 0);
											cellDocumento4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDocumento4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDocumento4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDocumento4.addParagraph("Graf. Original").setFont(fontTableHeader);
										}

										// Gera os dados da tabela de documentos para a evidÃªncia
										int linha = tableDocumento.getRowCount();
										Cell cellDocumento0 = tableDocumento.getCellByPosition(0, linha);
										cellDocumento0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellDocumento0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellDocumento0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellDocumento0.addParagraph(documento.getEvidencia().getId().toString()).setFont(fontTableDetail);

										Cell cellDocumento1 = tableDocumento.getCellByPosition(1, linha);
										cellDocumento1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellDocumento1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellDocumento1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellDocumento1.addParagraph(documento.getEvidenciaDocumentoTipo().getDescricao()).setFont(fontTableDetail);

										Cell cellDocumento2 = tableDocumento.getCellByPosition(2, linha);
										cellDocumento2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellDocumento2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellDocumento2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellDocumento2.addParagraph(documento.getPaginas() != null ? documento.getPaginas().toString() : "").setFont(fontTableDetail);

										Cell cellDocumento3 = tableDocumento.getCellByPosition(3, linha);
										cellDocumento3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellDocumento3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellDocumento3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellDocumento3.addParagraph(documento.getFolhas() != null ? documento.getFolhas().toString() : "").setFont(fontTableDetail);

										Cell cellDocumento4 = tableDocumento.getCellByPosition(4, linha);
										cellDocumento4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellDocumento4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellDocumento4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellDocumento4.addParagraph(documento.getPaginasGrafismoOriginalPunho() != null ? documento.getPaginasGrafismoOriginalPunho().toString() : "")
												.setFont(fontTableDetail);
									}
									break;
								case MATERIAL_QUIMICO_BIOLOGICO:
									if (secaoVestigios != null) {
										if (!paragrafoMaterialGerado) {
											Paragraph pb = secaoVestigios.addParagraph("\nMateriais e Substâncias\n");
											pb.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											pb.setFont(fontParagrafoBold);
											paragrafoMaterialGerado = true;
										}

										EvidenciaMaterial material = evidenciaMaterialDao.buscarUltimoMaterialPorEvidencia(periciaEvidencia.getEvidencia());
										tableMaterial = secaoVestigios.getTableByName("tabelaVestigiosMaterial");
										if (tableMaterial == null) {

											// Cria cabeÃ§alho para a tabela de materiais
											tableMaterial = secaoVestigios.addTable(1, 6);
											tableMaterial.setTableName("tabelaVestigiosMaterial");

											Cell cellMaterial0 = tableMaterial.getCellByPosition(0, 0);
											cellMaterial0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellMaterial0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellMaterial0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellMaterial0.addParagraph("Id").setFont(fontTableHeader);

											Cell cellMaterial1 = tableMaterial.getCellByPosition(1, 0);
											cellMaterial1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellMaterial1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellMaterial1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellMaterial1.addParagraph("Tipo").setFont(fontTableHeader);

											Cell cellMaterial2 = tableMaterial.getCellByPosition(2, 0);
											cellMaterial2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellMaterial2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellMaterial2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellMaterial2.addParagraph("Apresentação").setFont(fontTableHeader);

											Cell cellMaterial3 = tableMaterial.getCellByPosition(3, 0);
											cellMaterial3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellMaterial3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellMaterial3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellMaterial3.addParagraph("Cor").setFont(fontTableHeader);

											Cell cellMaterial4 = tableMaterial.getCellByPosition(4, 0);
											cellMaterial4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellMaterial4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellMaterial4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellMaterial4.addParagraph("Qtd. Apresentada").setFont(fontTableHeader);

											Cell cellMaterial5 = tableMaterial.getCellByPosition(5, 0);
											cellMaterial5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellMaterial5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellMaterial5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellMaterial5.addParagraph("Qtd. Utilizada").setFont(fontTableHeader);
										}

										// Gera os dados para a tabela de materias da evidÃªncia.
										int linha = tableMaterial.getRowCount();
										Cell cellMaterial0 = tableMaterial.getCellByPosition(0, linha);
										cellMaterial0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellMaterial0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellMaterial0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellMaterial0.addParagraph(material.getEvidencia().getId().toString()).setFont(fontTableDetail);

										Cell cellMaterial1 = tableMaterial.getCellByPosition(1, linha);
										cellMaterial1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellMaterial1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellMaterial1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellMaterial1.addParagraph(material.getEvidenciaMaterialTipo().getDescricao()).setFont(fontTableDetail);

										Cell cellMaterial2 = tableMaterial.getCellByPosition(2, linha);
										cellMaterial2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellMaterial2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellMaterial2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellMaterial2.addParagraph(material.getEvidenciaMaterialApresentacao() != null ? material.getEvidenciaMaterialApresentacao().getDescricao() : "")
												.setFont(fontTableDetail);

										Cell cellMaterial3 = tableMaterial.getCellByPosition(3, linha);
										cellMaterial3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellMaterial3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellMaterial3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellMaterial3.addParagraph(material.getCor() != null ? material.getCor().getDescricao() : "").setFont(fontTableDetail);

										Cell cellMaterial4 = tableMaterial.getCellByPosition(4, linha);
										cellMaterial4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellMaterial4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellMaterial4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellMaterial4.addParagraph((material.getQuantidade() != null ? material.getQuantidade().toString() : "")
												+ (material.getUnidadeMedida() != null ? material.getUnidadeMedida().getSigla() : "")).setFont(fontTableDetail);

										Cell cellMaterial5 = tableMaterial.getCellByPosition(5, linha);
										cellMaterial5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellMaterial5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellMaterial5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellMaterial5.addParagraph((material.getQuantidadeUtilizada() != null ? material.getQuantidadeUtilizada().toString() : "")
												+ (material.getUnidadeMedidaUtilizada() != null ? material.getUnidadeMedidaUtilizada().getSigla() : "")).setFont(fontTableDetail);
									}
									break;
								case VEICULO:
									if (secaoVestigios != null) {
										if (!paragrafoVeiculoGerado) {
											Paragraph pb = secaoVestigios.addParagraph("\nVeículos\n");
											pb.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											pb.setFont(fontParagrafoBold);
											paragrafoVeiculoGerado = true;
										}

										EvidenciaVeiculo veiculo = evidenciaVeiculoDao.buscarUltimoVeiculoPorEvidencia(periciaEvidencia.getEvidencia());
										tableVeiculo = secaoVestigios.getTableByName("tabelaVestigiosVeiculo");
										if (tableVeiculo == null) {

											// Cria cabeçalho para a tabela de veículos
											tableVeiculo = secaoVestigios.addTable(1, 10);
											tableVeiculo.setTableName("tabelaVestigiosVeiculo");

											Cell cellVeiculo0 = tableVeiculo.getCellByPosition(0, 0);
											cellVeiculo0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo0.addParagraph("Id").setFont(fontTableHeader);

											Cell cellVeiculo1 = tableVeiculo.getCellByPosition(1, 0);
											cellVeiculo1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo1.addParagraph("Tipo").setFont(fontTableHeader);

											Cell cellVeiculo2 = tableVeiculo.getCellByPosition(2, 0);
											cellVeiculo2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo2.addParagraph("Marca").setFont(fontTableHeader);

											Cell cellVeiculo3 = tableVeiculo.getCellByPosition(3, 0);
											cellVeiculo3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo3.addParagraph("Modelo").setFont(fontTableHeader);

											Cell cellVeiculo4 = tableVeiculo.getCellByPosition(4, 0);
											cellVeiculo4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo4.addParagraph("Categoria").setFont(fontTableHeader);

											Cell cellVeiculo5 = tableVeiculo.getCellByPosition(5, 0);
											cellVeiculo5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo5.addParagraph("Cor").setFont(fontTableHeader);

											Cell cellVeiculo6 = tableVeiculo.getCellByPosition(6, 0);
											cellVeiculo6.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo6.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo6.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo6.addParagraph("Chassi").setFont(fontTableHeader);

											Cell cellVeiculo7 = tableVeiculo.getCellByPosition(7, 0);
											cellVeiculo7.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo7.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo7.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo7.addParagraph("Placas").setFont(fontTableHeader);

											Cell cellVeiculo8 = tableVeiculo.getCellByPosition(8, 0);
											cellVeiculo8.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo8.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo8.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo8.addParagraph("Placas / Adulterada").setFont(fontTableHeader);

											Cell cellVeiculo9 = tableVeiculo.getCellByPosition(9, 0);
											cellVeiculo9.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo9.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo9.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo9.addParagraph("MOTOR").setFont(fontTableHeader);
										}

										// Gera os dados para a tabela da evidencia
										int linha = tableVeiculo.getRowCount();
										Cell cellVeiculo0 = tableVeiculo.getCellByPosition(0, linha);
										cellVeiculo0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellVeiculo0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellVeiculo0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellVeiculo0.addParagraph(veiculo.getEvidencia().getId().toString()).setFont(fontTableDetail);

										Cell cellVeiculo1 = tableVeiculo.getCellByPosition(1, linha);
										cellVeiculo1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellVeiculo1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellVeiculo1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellVeiculo1.addParagraph(veiculo.getEvidenciaVeiculoTipo().getDescricao()).setFont(fontTableDetail);

										Cell cellVeiculo2 = tableVeiculo.getCellByPosition(2, linha);
										cellVeiculo2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellVeiculo2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellVeiculo2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellVeiculo2.addParagraph(veiculo.getEvidenciaVeiculoMarca() != null ? veiculo.getEvidenciaVeiculoMarca().getDescricao() : "").setFont(fontTableDetail);

										Cell cellVeiculo3 = tableVeiculo.getCellByPosition(3, linha);
										cellVeiculo3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellVeiculo3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellVeiculo3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellVeiculo3.addParagraph(veiculo.getEvidenciaVeiculoModelo() != null ? veiculo.getEvidenciaVeiculoModelo().getDescricao() : "").setFont(fontTableDetail);

										Cell cellVeiculo4 = tableVeiculo.getCellByPosition(4, linha);
										cellVeiculo4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellVeiculo4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellVeiculo4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellVeiculo4.addParagraph("----").setFont(fontTableDetail); // TODO ADICIONAR A
																									// CATEGORIA OU CRIAR UM
																									// ENUM DA MESMA NA
																									// TABELA
																									// EVIDENCIA_VEICULO

										Cell cellVeiculo5 = tableVeiculo.getCellByPosition(5, linha);
										cellVeiculo5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellVeiculo5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellVeiculo5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellVeiculo5.addParagraph(veiculo.getCor() != null ? veiculo.getCor().getDescricao() : "").setFont(fontTableDetail);

										Cell cellVeiculo6 = tableVeiculo.getCellByPosition(6, linha);
										cellVeiculo6.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellVeiculo6.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellVeiculo6.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellVeiculo6.addParagraph(veiculo.getChassi()).setFont(fontTableDetail);

										Cell cellVeiculo7 = tableVeiculo.getCellByPosition(7, linha);
										cellVeiculo7.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellVeiculo7.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellVeiculo7.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellVeiculo7.addParagraph(veiculo.getPlacaOriginal().toUpperCase()).setFont(fontTableDetail);

										Cell cellVeiculo8 = tableVeiculo.getCellByPosition(8, linha);
										cellVeiculo8.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellVeiculo8.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellVeiculo8.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellVeiculo8.addParagraph(veiculo.getPlaca().toUpperCase()).setFont(fontTableDetail);

										Cell cellVeiculo9 = tableVeiculo.getCellByPosition(9, linha);
										cellVeiculo9.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellVeiculo9.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellVeiculo9.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellVeiculo9.addParagraph(veiculo.getNumeroMotor()).setFont(fontTableDetail);
									}
									break;
								case OBJETO:
									if (secaoVestigios != null) {
										if (!paragrafoObjetoGerado) {
											Paragraph pb = secaoVestigios.addParagraph("\nObjetos\n");
											pb.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											pb.setFont(fontParagrafoBold);
											paragrafoObjetoGerado = true;
										}

										EvidenciaObjeto objeto = evidenciaObjetoDao.buscarUltimoObjetoPorEvidencia(periciaEvidencia.getEvidencia());
										tableObjeto = secaoVestigios.getTableByName("tabelaVestigiosObjeto");
										if (tableObjeto == null) {

											// Cria cabeÃ§alho para a tabela de veÃ­culos
											tableObjeto = secaoVestigios.addTable(1, 2);
											tableObjeto.setTableName("tabelaVestigiosObjeto");

											Cell cellObjeto0 = tableObjeto.getCellByPosition(0, 0);
											cellObjeto0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellObjeto0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellObjeto0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellObjeto0.addParagraph("Id").setFont(fontTableHeader);

											Cell cellObjeto1 = tableObjeto.getCellByPosition(1, 0);
											cellObjeto1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellObjeto1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellObjeto1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellObjeto1.addParagraph("Descrição").setFont(fontTableHeader);
										}

										// Gera os dados para a tabela da evidencia
										int linha = tableObjeto.getRowCount();
										Cell cellObjeto0 = tableObjeto.getCellByPosition(0, linha);
										cellObjeto0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellObjeto0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellObjeto0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellObjeto0.addParagraph(objeto.getEvidencia().getId().toString()).setFont(fontTableDetail);

										Cell cellObjeto1 = tableObjeto.getCellByPosition(1, linha);
										cellObjeto1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellObjeto1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellObjeto1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellObjeto1.addParagraph(objeto.getDescricao()).setFont(fontTableDetail);
									}
									break;
								default:
									break;
								}
							} else {
								if (solicitacaoProcedimentoPericial.getSetor().getId() != SetorEnum.NUBAF.getId()) {
									switch (periciaEvidencia.getEvidencia().getTipoEvidencia()) {

									case MATERIAL_BALISTICO:
										if (secaoVestigios != null) {
											if (!paragrafoBalisticaGerado) {
												Paragraph pb = secaoVestigios.addParagraph("\nMaterial Balístico\n");
												pb.setFont(fontParagrafoBold);
												pb.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												paragrafoBalisticaGerado = true;
											}

											EvidenciaArmamento balistica = evidenciaArmamentoDao.buscarUltimoArmamentoPorEvidencia(periciaEvidencia.getEvidencia());
											tableBalistica = secaoVestigios.getTableByName("tabelaVestigiosBalistica");
											if (tableBalistica == null) {
												// Montando cabeçalho da tabela de armamento no documento
												if (solicitacaoProcedimentoPericial.getSetor().getId() == SetorEnum.NUPEX.getId() && laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor()
														.getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_VIDA_HOMICIDIO.getId()) {
													tableBalistica = secaoVestigios.addTable(1, 7);

												} else {
													tableBalistica = secaoVestigios.addTable(1, 6);
												}

												tableBalistica.setTableName("tabelaVestigiosBalistica");

												Cell cell0 = tableBalistica.getCellByPosition(0, 0);
												cell0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cell0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cell0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cell0.addParagraph("Id").setFont(fontTableHeader);

												if (solicitacaoProcedimentoPericial.getSetor().getId() == SetorEnum.NUPEX.getId() && laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor()
														.getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_VIDA_HOMICIDIO.getId()) {
													Cell cell6 = tableBalistica.getCellByPosition(1, 0);
													cell6.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cell6.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cell6.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cell6.addParagraph("Quantidade").setFont(fontTableHeader);
												}

												Cell cell1 = tableBalistica.getCellByPosition(2, 0);
												cell1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cell1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cell1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cell1.addParagraph("Tipo").setFont(fontTableHeader);

												Cell cell2 = tableBalistica.getCellByPosition(3, 0);
												cell2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cell2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cell2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cell2.addParagraph("Nº Série").setFont(fontTableHeader);

												Cell cell3 = tableBalistica.getCellByPosition(4, 0);
												cell3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cell3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cell3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cell3.addParagraph("Marca").setFont(fontTableHeader);

												Cell cell4 = tableBalistica.getCellByPosition(5, 0);
												cell4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cell4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cell4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cell4.addParagraph("Modelo").setFont(fontTableHeader);

												Cell cell5 = tableBalistica.getCellByPosition(6, 0);
												cell5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cell5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cell5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cell5.addParagraph("Calibre").setFont(fontTableHeader);
											}

											// Alimentando tabela com os campos das evidÃªncias
											int linha = tableBalistica.getRowCount();
											Cell cell0 = tableBalistica.getCellByPosition(0, linha);
											cell0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cell0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cell0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cell0.addParagraph(balistica.getEvidencia().getId().toString()).setFont(fontTableDetail);

											if (solicitacaoProcedimentoPericial.getSetor().getId() == SetorEnum.NUPEX.getId() && laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame()
													.getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_VIDA_HOMICIDIO.getId()) {
												Cell cell6 = tableBalistica.getCellByPosition(1, linha);
												cell6.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cell6.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cell6.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												if (balistica.getTipoArma().getId().equals(TipoArmaEnum.ARMA.getId())) {
													cell6.addParagraph("1").setFont(fontTableDetail);
												} else {
													cell6.addParagraph(balistica.getQuantidade() != null ? balistica.getQuantidade().toString() : "").setFont(fontTableDetail);
												}
											}

											Cell cell1 = tableBalistica.getCellByPosition(2, linha);
											cell1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cell1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cell1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cell1.addParagraph(balistica.getEvidenciaArmamentoTipo().getDescricao()).setFont(fontTableDetail);

											Cell cell2 = tableBalistica.getCellByPosition(3, linha);
											cell2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cell2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cell2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cell2.addParagraph(balistica.getNumeroSerie()).setFont(fontTableDetail);

											Cell cell3 = tableBalistica.getCellByPosition(4, linha);
											cell3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cell3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cell3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cell3.addParagraph(balistica.getEvidenciaArmamentoMarca() != null ? balistica.getEvidenciaArmamentoMarca().getDescricao() : "").setFont(fontTableDetail);

											Cell cell4 = tableBalistica.getCellByPosition(5, linha);
											cell4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cell4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cell4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cell4.addParagraph(balistica.getEvidenciaArmamentoModelo() != null ? balistica.getEvidenciaArmamentoModelo().getDescricao() : "").setFont(fontTableDetail);

											Cell cell5 = tableBalistica.getCellByPosition(6, linha);
											cell5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cell5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cell5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cell5.addParagraph(balistica.getEvidenciaArmamentoCalibre() != null ? balistica.getEvidenciaArmamentoCalibre().getDescricao() : "")
													.setFont(fontTableDetail);
										}
										break;
									case DISPOSITIVO_TECNOLOGICO:
										if (secaoVestigios != null) {
											if (!paragrafoDispositivoGerado) {
												Paragraph pb = secaoVestigios.addParagraph("\nDispositivos Tecnológicos\n");
												pb.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												pb.setFont(fontParagrafoBold);
												paragrafoDispositivoGerado = true;
											}

											EvidenciaDispositivoTecnologico dispositivo = evidenciaDispositivoTecnologicoDao.buscarUltimoDispositivoPorEvidencia(periciaEvidencia.getEvidencia());
											tableDispositivo = secaoVestigios.getTableByName("tabelaVestigiosDispositivo");
											if (tableDispositivo == null) {

												// Cria o cabeÃ§alho da tabela.
												tableDispositivo = secaoVestigios.addTable(1, 7);
												tableDispositivo.setTableName("tabelaVestigiosDispositivo");

												Cell cellDispositivo0 = tableDispositivo.getCellByPosition(0, 0);
												cellDispositivo0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellDispositivo0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellDispositivo0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellDispositivo0.addParagraph("Id").setFont(fontTableHeader);

												Cell cellDispositivo1 = tableDispositivo.getCellByPosition(1, 0);
												cellDispositivo1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellDispositivo1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellDispositivo1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellDispositivo1.addParagraph("Tipo").setFont(fontTableHeader);

												Cell cellDispositivo2 = tableDispositivo.getCellByPosition(2, 0);
												cellDispositivo2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellDispositivo2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellDispositivo2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellDispositivo2.addParagraph("Fabricante").setFont(fontTableHeader);

												Cell cellDispositivo3 = tableDispositivo.getCellByPosition(3, 0);
												cellDispositivo3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellDispositivo3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellDispositivo3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellDispositivo3.addParagraph("Modelo").setFont(fontTableHeader);

												Cell cellDispositivo4 = tableDispositivo.getCellByPosition(4, 0);
												cellDispositivo4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellDispositivo4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellDispositivo4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellDispositivo4.addParagraph("Nº Série").setFont(fontTableHeader);

												Cell cellDispositivo5 = tableDispositivo.getCellByPosition(5, 0);
												cellDispositivo5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellDispositivo5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellDispositivo5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellDispositivo5.addParagraph("Capacidade").setFont(fontTableHeader);

												Cell cellDispositivo6 = tableDispositivo.getCellByPosition(6, 0);
												cellDispositivo6.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellDispositivo6.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellDispositivo6.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellDispositivo6.addParagraph("IMEIs").setFont(fontTableHeader);
											}

											// Cria os dados da tabela para a evidÃªncia.
											int linha = tableDispositivo.getRowCount();
											Cell cellDispositivo0 = tableDispositivo.getCellByPosition(0, linha);
											cellDispositivo0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDispositivo0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDispositivo0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDispositivo0.addParagraph(dispositivo.getEvidencia().getId().toString()).setFont(fontTableDetail);

											Cell cellDispositivo1 = tableDispositivo.getCellByPosition(1, linha);
											cellDispositivo1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDispositivo1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDispositivo1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDispositivo1.addParagraph(dispositivo.getEvidenciaDispositivoTecnologicoTipo().getDescricao()).setFont(fontTableDetail);

											Cell cellDispositivo2 = tableDispositivo.getCellByPosition(2, linha);
											cellDispositivo2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDispositivo2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDispositivo2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDispositivo2.addParagraph(
													dispositivo.getEvidenciaDispositivoTecnologicoFabricante() != null ? dispositivo.getEvidenciaDispositivoTecnologicoFabricante().getDescricao() : "")
													.setFont(fontTableDetail);

											Cell cellDispositivo3 = tableDispositivo.getCellByPosition(3, linha);
											cellDispositivo3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDispositivo3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDispositivo3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDispositivo3
													.addParagraph(
															dispositivo.getEvidenciaDispositivoTecnologicoModelo() != null ? dispositivo.getEvidenciaDispositivoTecnologicoModelo().getDescricao() : "")
													.setFont(fontTableDetail);

											Cell cellDispositivo4 = tableDispositivo.getCellByPosition(4, linha);
											cellDispositivo4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDispositivo4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDispositivo4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDispositivo4.addParagraph(dispositivo.getNumeroSerie()).setFont(fontTableDetail);

											Cell cellDispositivo5 = tableDispositivo.getCellByPosition(5, linha);
											cellDispositivo5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDispositivo5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDispositivo5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDispositivo5.addParagraph((dispositivo.getCapacidade() != null ? dispositivo.getCapacidade().toString() : "") + " "
													+ (dispositivo.getCapacidadeUnidade() != null ? dispositivo.getCapacidadeUnidade().getSigla() : "")).setFont(fontTableDetail);

											Cell cellDispositivo6 = tableDispositivo.getCellByPosition(6, linha);
											cellDispositivo6.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDispositivo6.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDispositivo6.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											List<EvidenciaDispositivoTecnologicoImei> imeis = evidenciaDispositivoTecnologicoImeiDao.buscarPorDispositivo(dispositivo);

											for (EvidenciaDispositivoTecnologicoImei imei : imeis) {
												cellDispositivo6.addParagraph(imei.getNumero()).setFont(fontTableDetail);
											}
										}
										break;
									case DOCUMENTO:
										if (secaoVestigios != null) {
											if (!paragrafoDocumentoGerado) {
												Paragraph pb = secaoVestigios.addParagraph("\nDocumentos\n");
												pb.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												pb.setFont(fontParagrafoBold);
												paragrafoDocumentoGerado = true;
											}

											EvidenciaDocumento documento = evidenciaDocumentoDao.buscarUltimoDocumentoPorEvidencia(periciaEvidencia.getEvidencia());
											tableDocumento = secaoVestigios.getTableByName("tabelaVestigiosDocumento");
											if (tableDocumento == null) {

												// Cria o cabeÃ§alho para a tabela de documentos.
												tableDocumento = secaoVestigios.addTable(1, 5);
												tableDocumento.setTableName("tabelaVestigiosDocumento");

												Cell cellDocumento0 = tableDocumento.getCellByPosition(0, 0);
												cellDocumento0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellDocumento0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellDocumento0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellDocumento0.addParagraph("Id").setFont(fontTableHeader);

												Cell cellDocumento1 = tableDocumento.getCellByPosition(1, 0);
												cellDocumento1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellDocumento1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellDocumento1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellDocumento1.addParagraph("Tipo").setFont(fontTableHeader);

												Cell cellDocumento2 = tableDocumento.getCellByPosition(2, 0);
												cellDocumento2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellDocumento2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellDocumento2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellDocumento2.addParagraph("Páginas").setFont(fontTableHeader);

												Cell cellDocumento3 = tableDocumento.getCellByPosition(3, 0);
												cellDocumento3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellDocumento3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellDocumento3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellDocumento3.addParagraph("Folhas").setFont(fontTableHeader);

												Cell cellDocumento4 = tableDocumento.getCellByPosition(4, 0);
												cellDocumento4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellDocumento4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellDocumento4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellDocumento4.addParagraph("Graf. Original").setFont(fontTableHeader);
											}

											// Gera os dados da tabela de documentos para a evidÃªncia
											int linha = tableDocumento.getRowCount();
											Cell cellDocumento0 = tableDocumento.getCellByPosition(0, linha);
											cellDocumento0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDocumento0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDocumento0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDocumento0.addParagraph(documento.getEvidencia().getId().toString()).setFont(fontTableDetail);

											Cell cellDocumento1 = tableDocumento.getCellByPosition(1, linha);
											cellDocumento1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDocumento1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDocumento1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDocumento1.addParagraph(documento.getEvidenciaDocumentoTipo().getDescricao()).setFont(fontTableDetail);

											Cell cellDocumento2 = tableDocumento.getCellByPosition(2, linha);
											cellDocumento2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDocumento2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDocumento2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDocumento2.addParagraph(documento.getPaginas() != null ? documento.getPaginas().toString() : "").setFont(fontTableDetail);

											Cell cellDocumento3 = tableDocumento.getCellByPosition(3, linha);
											cellDocumento3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDocumento3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDocumento3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDocumento3.addParagraph(documento.getFolhas() != null ? documento.getFolhas().toString() : "").setFont(fontTableDetail);

											Cell cellDocumento4 = tableDocumento.getCellByPosition(4, linha);
											cellDocumento4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellDocumento4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellDocumento4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellDocumento4.addParagraph(documento.getPaginasGrafismoOriginalPunho() != null ? documento.getPaginasGrafismoOriginalPunho().toString() : "")
													.setFont(fontTableDetail);
										}
										break;
									case MATERIAL_QUIMICO_BIOLOGICO:
										if (secaoVestigios != null) {
											if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() != TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_VIDA_HOMICIDIO
													.getId()) {
												if (!paragrafoMaterialGerado) {
													Paragraph pb = secaoVestigios.addParagraph("\nMateriais e Substâncias\n");
													pb.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													pb.setFont(fontParagrafoBold);
													paragrafoMaterialGerado = true;
												}

												EvidenciaMaterial material = evidenciaMaterialDao.buscarUltimoMaterialPorEvidencia(periciaEvidencia.getEvidencia());
												tableMaterial = secaoVestigios.getTableByName("tabelaVestigiosMaterial");
												if (tableMaterial == null) {

													// Cria cabeÃ§alho para a tabela de materiais
													tableMaterial = secaoVestigios.addTable(1, 6);
													tableMaterial.setTableName("tabelaVestigiosMaterial");

													Cell cellMaterial0 = tableMaterial.getCellByPosition(0, 0);
													cellMaterial0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellMaterial0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellMaterial0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellMaterial0.addParagraph("Id").setFont(fontTableHeader);

													Cell cellMaterial1 = tableMaterial.getCellByPosition(1, 0);
													cellMaterial1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellMaterial1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellMaterial1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellMaterial1.addParagraph("Tipo").setFont(fontTableHeader);

													Cell cellMaterial2 = tableMaterial.getCellByPosition(2, 0);
													cellMaterial2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellMaterial2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellMaterial2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellMaterial2.addParagraph("Apresentação").setFont(fontTableHeader);

													Cell cellMaterial3 = tableMaterial.getCellByPosition(3, 0);
													cellMaterial3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellMaterial3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellMaterial3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellMaterial3.addParagraph("Cor").setFont(fontTableHeader);

													Cell cellMaterial4 = tableMaterial.getCellByPosition(4, 0);
													cellMaterial4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellMaterial4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellMaterial4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellMaterial4.addParagraph("Qtd. Apresentada").setFont(fontTableHeader);

													Cell cellMaterial5 = tableMaterial.getCellByPosition(5, 0);
													cellMaterial5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
													cellMaterial5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
													cellMaterial5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
													cellMaterial5.addParagraph("Qtd. Utilizada").setFont(fontTableHeader);
												}

												// Gera os dados para a tabela de materias da evidÃªncia.
												int linha = tableMaterial.getRowCount();
												Cell cellMaterial0 = tableMaterial.getCellByPosition(0, linha);
												cellMaterial0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellMaterial0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellMaterial0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellMaterial0.addParagraph(material.getEvidencia().getId().toString()).setFont(fontTableDetail);

												Cell cellMaterial1 = tableMaterial.getCellByPosition(1, linha);
												cellMaterial1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellMaterial1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellMaterial1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellMaterial1.addParagraph(material.getEvidenciaMaterialTipo().getDescricao()).setFont(fontTableDetail);

												Cell cellMaterial2 = tableMaterial.getCellByPosition(2, linha);
												cellMaterial2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellMaterial2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellMaterial2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellMaterial2.addParagraph(material.getEvidenciaMaterialApresentacao() != null ? material.getEvidenciaMaterialApresentacao().getDescricao() : "")
														.setFont(fontTableDetail);

												Cell cellMaterial3 = tableMaterial.getCellByPosition(3, linha);
												cellMaterial3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellMaterial3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellMaterial3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellMaterial3.addParagraph(material.getCor() != null ? material.getCor().getDescricao() : "").setFont(fontTableDetail);

												Cell cellMaterial4 = tableMaterial.getCellByPosition(4, linha);
												cellMaterial4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellMaterial4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellMaterial4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellMaterial4.addParagraph((material.getQuantidade() != null ? material.getQuantidade().toString() : "")
														+ (material.getUnidadeMedida() != null ? material.getUnidadeMedida().getSigla() : "")).setFont(fontTableDetail);

												Cell cellMaterial5 = tableMaterial.getCellByPosition(5, linha);
												cellMaterial5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellMaterial5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellMaterial5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellMaterial5.addParagraph((material.getQuantidadeUtilizada() != null ? material.getQuantidadeUtilizada().toString() : "")
														+ (material.getUnidadeMedidaUtilizada() != null ? material.getUnidadeMedidaUtilizada().getSigla() : "")).setFont(fontTableDetail);
											}
										}
										break;
									case VEICULO:
										if (secaoVestigios != null) {
											if (!paragrafoVeiculoGerado) {
												Paragraph pb = secaoVestigios.addParagraph("\nVeículos\n");
												pb.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												pb.setFont(fontParagrafoBold);
												paragrafoVeiculoGerado = true;
											}

											EvidenciaVeiculo veiculo = evidenciaVeiculoDao.buscarUltimoVeiculoPorEvidencia(periciaEvidencia.getEvidencia());
											tableVeiculo = secaoVestigios.getTableByName("tabelaVestigiosVeiculo");
											if (tableVeiculo == null) {

												// Cria cabeÃ§alho para a tabela de veÃ­culos
												tableVeiculo = secaoVestigios.addTable(1, 10);
												tableVeiculo.setTableName("tabelaVestigiosVeiculo");

												Cell cellVeiculo0 = tableVeiculo.getCellByPosition(0, 0);
												cellVeiculo0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellVeiculo0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellVeiculo0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellVeiculo0.addParagraph("Id").setFont(fontTableHeader);

												Cell cellVeiculo1 = tableVeiculo.getCellByPosition(1, 0);
												cellVeiculo1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellVeiculo1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellVeiculo1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellVeiculo1.addParagraph("Tipo").setFont(fontTableHeader);

												Cell cellVeiculo2 = tableVeiculo.getCellByPosition(2, 0);
												cellVeiculo2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellVeiculo2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellVeiculo2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellVeiculo2.addParagraph("Marca").setFont(fontTableHeader);

												Cell cellVeiculo3 = tableVeiculo.getCellByPosition(3, 0);
												cellVeiculo3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellVeiculo3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellVeiculo3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellVeiculo3.addParagraph("Modelo").setFont(fontTableHeader);

												Cell cellVeiculo4 = tableVeiculo.getCellByPosition(4, 0);
												cellVeiculo4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellVeiculo4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellVeiculo4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellVeiculo4.addParagraph("Categoria").setFont(fontTableHeader);

												Cell cellVeiculo5 = tableVeiculo.getCellByPosition(5, 0);
												cellVeiculo5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellVeiculo5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellVeiculo5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellVeiculo5.addParagraph("Cor").setFont(fontTableHeader);

												Cell cellVeiculo6 = tableVeiculo.getCellByPosition(6, 0);
												cellVeiculo6.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellVeiculo6.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellVeiculo6.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellVeiculo6.addParagraph("Chassi").setFont(fontTableHeader);

												Cell cellVeiculo7 = tableVeiculo.getCellByPosition(7, 0);
												cellVeiculo7.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellVeiculo7.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellVeiculo7.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellVeiculo7.addParagraph("Placas").setFont(fontTableHeader);

												Cell cellVeiculo8 = tableVeiculo.getCellByPosition(8, 0);
												cellVeiculo8.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellVeiculo8.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellVeiculo8.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellVeiculo8.addParagraph("Placas / Adulterada").setFont(fontTableHeader);

												Cell cellVeiculo9 = tableVeiculo.getCellByPosition(9, 0);
												cellVeiculo9.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellVeiculo9.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellVeiculo9.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellVeiculo9.addParagraph("MOTOR").setFont(fontTableHeader);
											}

											// Gera os dados para a tabela da evidencia
											int linha = tableVeiculo.getRowCount();
											Cell cellVeiculo0 = tableVeiculo.getCellByPosition(0, linha);
											cellVeiculo0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo0.addParagraph(veiculo.getEvidencia().getId().toString()).setFont(fontTableDetail);

											Cell cellVeiculo1 = tableVeiculo.getCellByPosition(1, linha);
											cellVeiculo1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo1.addParagraph(veiculo.getEvidenciaVeiculoTipo().getDescricao()).setFont(fontTableDetail);

											Cell cellVeiculo2 = tableVeiculo.getCellByPosition(2, linha);
											cellVeiculo2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo2.addParagraph(veiculo.getEvidenciaVeiculoMarca() != null ? veiculo.getEvidenciaVeiculoMarca().getDescricao() : "").setFont(fontTableDetail);

											Cell cellVeiculo3 = tableVeiculo.getCellByPosition(3, linha);
											cellVeiculo3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo3.addParagraph(veiculo.getEvidenciaVeiculoModelo() != null ? veiculo.getEvidenciaVeiculoModelo().getDescricao() : "").setFont(fontTableDetail);

											Cell cellVeiculo4 = tableVeiculo.getCellByPosition(4, linha);
											cellVeiculo4.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo4.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo4.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo4.addParagraph("----").setFont(fontTableDetail);

											Cell cellVeiculo5 = tableVeiculo.getCellByPosition(5, linha);
											cellVeiculo5.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo5.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo5.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo5.addParagraph(veiculo.getCor() != null ? veiculo.getCor().getDescricao() : "").setFont(fontTableDetail);

											Cell cellVeiculo6 = tableVeiculo.getCellByPosition(6, linha);
											cellVeiculo6.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo6.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo6.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo6.addParagraph(veiculo.getChassi()).setFont(fontTableDetail);

											Cell cellVeiculo7 = tableVeiculo.getCellByPosition(7, linha);
											cellVeiculo7.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo7.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo7.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo7.addParagraph(veiculo.getPlacaOriginal().toUpperCase()).setFont(fontTableDetail);

											Cell cellVeiculo8 = tableVeiculo.getCellByPosition(8, linha);
											cellVeiculo8.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo8.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo8.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo8.addParagraph(veiculo.getPlaca().toUpperCase()).setFont(fontTableDetail);

											Cell cellVeiculo9 = tableVeiculo.getCellByPosition(9, linha);
											cellVeiculo9.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellVeiculo9.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellVeiculo9.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellVeiculo9.addParagraph(veiculo.getNumeroMotor()).setFont(fontTableDetail);
										}
										break;
									case OBJETO:
										if (secaoVestigios != null) {
											if (!paragrafoObjetoGerado) {
												Paragraph pb = secaoVestigios.addParagraph("\nObjetos\n");
												pb.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												pb.setFont(fontParagrafoBold);
												paragrafoObjetoGerado = true;
											}

											EvidenciaObjeto objeto = evidenciaObjetoDao.buscarUltimoObjetoPorEvidencia(periciaEvidencia.getEvidencia());
											tableObjeto = secaoVestigios.getTableByName("tabelaVestigiosObjeto");
											if (tableObjeto == null) {

												// Cria cabeÃ§alho para a tabela de veÃ­culos
												tableObjeto = secaoVestigios.addTable(1, 2);
												tableObjeto.setTableName("tabelaVestigiosObjeto");

												Cell cellObjeto0 = tableObjeto.getCellByPosition(0, 0);
												cellObjeto0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellObjeto0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellObjeto0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellObjeto0.addParagraph("Id").setFont(fontTableHeader);

												Cell cellObjeto1 = tableObjeto.getCellByPosition(1, 0);
												cellObjeto1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
												cellObjeto1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
												cellObjeto1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
												cellObjeto1.addParagraph("Descrição").setFont(fontTableHeader);
											}

											// Gera os dados para a tabela da evidencia
											int linha = tableObjeto.getRowCount();
											Cell cellObjeto0 = tableObjeto.getCellByPosition(0, linha);
											cellObjeto0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellObjeto0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellObjeto0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellObjeto0.addParagraph(objeto.getEvidencia().getId().toString()).setFont(fontTableDetail);

											Cell cellObjeto1 = tableObjeto.getCellByPosition(1, linha);
											cellObjeto1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellObjeto1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellObjeto1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellObjeto1.addParagraph(objeto.getDescricao()).setFont(fontTableDetail);
										}
										break;
									default:
										break;
									}
								}
							}
						}
					}
				}
			}

			// Resumo Material Balistico
			Section secaoResumoMaterialBalistico = laudoDocumento.getSectionByName("secaoResumoMaterialBalistico");

			if (secaoResumoMaterialBalistico != null) {
				limparSecao(secaoResumoMaterialBalistico);
				if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_VIDA_HOMICIDIO.getId()) {
					Paragraph p = secaoResumoMaterialBalistico.addParagraph("\nVESTÍGIOS\n");
					p.setFont(fontParagrafoBold);
				}

				int cartucho = 0;
				int estojo = 0;
				int projetil = 0;
				for (PericiaEvidencia evidencia : pericia.getListPericiaEvidencia()) {
					if (evidencia.isUtilizaNoLaudo()) {
						if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_VIDA_HOMICIDIO.getId()
								&& evidencia.getEvidencia().getTipoEvidencia().getId().equals(TipoEvidenciaEnum.MATERIAL_BALISTICO.getId())) {
							switch (evidencia.getEvidencia().getTipoEvidencia()) {
							case MATERIAL_BALISTICO:
								switch (evidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoArma()) {

								case ESTOJO:
									estojo = estojo + evidencia.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade();

									break;
								case CARTUCHO:
									cartucho = cartucho + evidencia.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade();

									break;
								case PROJETIL:
									projetil = projetil + evidencia.getEvidencia().getUltimaEvidenciaArmamento().getQuantidade();

									break;
								default:
									break;
								}
							default:
								break;
							}
						}
					}
				}

				Paragraph pResumoInicial;
				String sResumoInicial = "";

				sResumoInicial = sResumoInicial + ("Quando do levantamento técnico-pericial, após minuciosa busca no local mediato e imediato, o perito constatou os seguintes "
						+ "elementos materiais de interesse criminalístico:");

				pResumoInicial = secaoResumoMaterialBalistico.addParagraph(sResumoInicial);

				pResumoInicial.setFont(fontParagrafoPNormal);
				pResumoInicial.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

				if (estojo > 0) {
					Paragraph pResumoEstojo;
					String sResumoEstojo = "";

					sResumoEstojo = sResumoEstojo + ("» Total de " + estojo + " estojos de munição de arma de fogo, conforme descriminados na tabela acima, "
							+ "todos percutidos e deflagrados, encontrados no local mediato e em torno do corpo da vítima;");

					pResumoEstojo = secaoResumoMaterialBalistico.addParagraph(sResumoEstojo);

					pResumoEstojo.setFont(fontParagrafoPNormal);
					pResumoEstojo.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
				}

				if (cartucho > 0) {
					Paragraph pResumoCartucho;
					String sResumoCartucho = "";

					sResumoCartucho = sResumoCartucho + ("» Total de " + cartucho + " cartuchos de munição de arma de fogo, encontrados no local mediato e em torno do corpo da vítima;");

					pResumoCartucho = secaoResumoMaterialBalistico.addParagraph(sResumoCartucho);

					pResumoCartucho.setFont(fontParagrafoPNormal);
					pResumoCartucho.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
				}

				if (projetil > 0) {
					Paragraph pResumoProjetil;
					String sResumoProjetil = "";

					sResumoProjetil = sResumoProjetil + ("» Total de " + projetil
							+ " projetis de arma de fogo apresentando estriamentos decorrentes de percurso em cano de alma raiada e deformações acidentais provocadas por impacto, "
							+ "posterior ao seu disparo, em obstáculo resistente ao choque, encontrados no local mediato e em torno do corpo da vítima;");

					pResumoProjetil = secaoResumoMaterialBalistico.addParagraph(sResumoProjetil);

					pResumoProjetil.setFont(fontParagrafoPNormal);
					pResumoProjetil.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
				}

				Paragraph pResumoFinalVestigios;
				String sResumoFinalVestigios = "";

				sResumoFinalVestigios = sResumoFinalVestigios
						+ ("» Perfurações por PAF e manchas de sangue por impregnação na camisa da vítima, cujas localizações correspondem às das lesões sob a vestimenta;\n"
								+ "» Manchas de sangue por empoçamento sob e em torno do corpo da vítima, indicando que naquele local ela tombou e veio a óbito;\n");

				pResumoFinalVestigios = secaoResumoMaterialBalistico.addParagraph(sResumoFinalVestigios);

				pResumoFinalVestigios.setFont(fontParagrafoPNormal);
				pResumoFinalVestigios.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
			}

			// Seção discussão tipo arma
			Section secaoDiscussaoTipoArma = laudoDocumento.getSectionByName("secaoDiscussaoTipoArma");
			if (secaoDiscussaoTipoArma != null) {
				limparSecao(secaoDiscussaoTipoArma);

				if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_VIDA_HOMICIDIO.getId()) {
					Paragraph p = secaoDiscussaoTipoArma.addParagraph("TIPO DE ARMA\n");
					p.setFont(fontParagrafoPBold);

					Paragraph pDiscussaoTipoArma;
					String sDiscussaoTipoArma = "";

					sDiscussaoTipoArma = sDiscussaoTipoArma
							+ ("Os elementos de munição de arma de fogo encontrados no local permitem afirmar que foi(ram) utilizada(s) por parte do(s) agressor(es).\n");

					pDiscussaoTipoArma = secaoDiscussaoTipoArma.addParagraph(sDiscussaoTipoArma);

					pDiscussaoTipoArma.setFont(fontParagrafoPNormal);
					pDiscussaoTipoArma.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

					for (PericiaEvidencia periciaEvidencia : pericia.getListPericiaEvidencia()) {
						if (periciaEvidencia.isUtilizaNoLaudo()) {
							switch (periciaEvidencia.getEvidencia().getTipoEvidencia()) {
							case MATERIAL_BALISTICO:
								switch (periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getTipoArma()) {
								case ARMA:

									boolean paragrafoDiscussaoTipoArma = false;

									Table tableEvidenciaTipoArma;

									tableEvidenciaTipoArma = laudoDocumento.getTableByName("tableEvidenciaTipoArma");

									if (!paragrafoDiscussaoTipoArma) {
										paragrafoDiscussaoTipoArma = true;
									}

									if (secaoDiscussaoTipoArma != null) {
										tableEvidenciaTipoArma = secaoDiscussaoTipoArma.getTableByName("tableEvidenciaTipoArma");

										if (tableEvidenciaTipoArma == null) {
											// Montando cabeçalho da tabela de TIPO ARMA SEÇÃO DISCUSSÃO
											tableEvidenciaTipoArma = secaoDiscussaoTipoArma.addTable(1, 4);
											tableEvidenciaTipoArma.setTableName("tableEvidenciaTipoArma");

											// Montando colunas da tabela de TIPO ARMA SEÇÃO DISCUSSÃO
											Cell cellEvidenciaTipoArma0 = tableEvidenciaTipoArma.getCellByPosition(0, 0);
											cellEvidenciaTipoArma0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellEvidenciaTipoArma0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellEvidenciaTipoArma0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellEvidenciaTipoArma0.addParagraph("ID").setFont(fontTableHeader);

											Cell cellEvidenciaTipoArma1 = tableEvidenciaTipoArma.getCellByPosition(1, 0);
											cellEvidenciaTipoArma1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellEvidenciaTipoArma1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellEvidenciaTipoArma1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellEvidenciaTipoArma1.addParagraph("TIPO ARMA").setFont(fontTableHeader);

											Cell cellEvidenciaTipoArma2 = tableEvidenciaTipoArma.getCellByPosition(2, 0);
											cellEvidenciaTipoArma2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellEvidenciaTipoArma2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellEvidenciaTipoArma2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellEvidenciaTipoArma2.addParagraph("CALIBRE").setFont(fontTableHeader);

											Cell cellEvidenciaTipoArma3 = tableEvidenciaTipoArma.getCellByPosition(3, 0);
											cellEvidenciaTipoArma3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellEvidenciaTipoArma3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellEvidenciaTipoArma3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellEvidenciaTipoArma3.addParagraph("NÚMERO DE SÉRIE").setFont(fontTableHeader);
										}

										if (tableEvidenciaTipoArma != null) {

											int linhaTabela = tableEvidenciaTipoArma.getRowCount();

											// ID
											Cell cellEvidenciaTipoArma0 = tableEvidenciaTipoArma.getCellByPosition(0, linhaTabela);
											cellEvidenciaTipoArma0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellEvidenciaTipoArma0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellEvidenciaTipoArma0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellEvidenciaTipoArma0.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getId() != null
													? periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getId().toString()
													: " ").setFont(fontTableDetail);

											// TIPO ARMA
											Cell cellEvidenciaTipoArma1 = tableEvidenciaTipoArma.getCellByPosition(1, linhaTabela);
											cellEvidenciaTipoArma1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellEvidenciaTipoArma1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellEvidenciaTipoArma1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellEvidenciaTipoArma1.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoTipo() != null
													? periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoTipo().getDescricao()
													: " ").setFont(fontTableDetail);

											// CALIBRE
											Cell cellEvidenciaTipoArma2 = tableEvidenciaTipoArma.getCellByPosition(2, linhaTabela);
											cellEvidenciaTipoArma2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellEvidenciaTipoArma2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellEvidenciaTipoArma2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellEvidenciaTipoArma2.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre() != null
													? periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getEvidenciaArmamentoCalibre().getDescricao()
													: " ").setFont(fontTableDetail);

											// NUMERO DE SÉRIE
											Cell cellEvidenciaTipoArma3 = tableEvidenciaTipoArma.getCellByPosition(3, linhaTabela);
											cellEvidenciaTipoArma3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellEvidenciaTipoArma3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellEvidenciaTipoArma3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellEvidenciaTipoArma3.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getNumeroSerie() != null
													? periciaEvidencia.getEvidencia().getUltimaEvidenciaArmamento().getNumeroSerie()
													: " ").setFont(fontTableDetail);
										}
									}
								default:
									break;
								}
							default:
								break;
							}
						}
					}
				}
			}

			// Seção Material coletado para exames laboratoriais
			Section secaoMaterialColetadoExameLaboratorial = laudoDocumento.getSectionByName("secaoMaterialColetadoExameLaboratorial");

			if (secaoMaterialColetadoExameLaboratorial != null) {
				limparSecao(secaoMaterialColetadoExameLaboratorial);

				if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_VIDA_HOMICIDIO.getId()) {
					Paragraph p = secaoMaterialColetadoExameLaboratorial.addParagraph("MATERIAL COLETADO PARA EXAMES LABORATORIAIS\n");
					p.setFont(fontParagrafoPBold);

					Paragraph pMaterialLaboratorio0;
					String sMaterialLaboratorio0 = "";

					sMaterialLaboratorio0 = sMaterialLaboratorio0 + ("Por solicitação da autoridade policial presente no momento da realização da perícia, foi coletado o seguinte material:\n");

					pMaterialLaboratorio0 = secaoMaterialColetadoExameLaboratorial.addParagraph(sMaterialLaboratorio0);

					pMaterialLaboratorio0.setFont(fontParagrafoPNormal);
					pMaterialLaboratorio0.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);

					for (PericiaEvidencia periciaEvidencia : pericia.getListPericiaEvidencia()) {
						if (periciaEvidencia.isUtilizaNoLaudo()) {
							switch (periciaEvidencia.getEvidencia().getTipoEvidencia()) {
							case MATERIAL_QUIMICO_BIOLOGICO:

								boolean paragrafoEvidenciasMaterialQuimicoBiologico = false;

								Table tableEvidenciaMaterialBiologico;

								tableEvidenciaMaterialBiologico = laudoDocumento.getTableByName("tableEvidenciaMaterialBiologico");

								if (!paragrafoEvidenciasMaterialQuimicoBiologico) {
									paragrafoEvidenciasMaterialQuimicoBiologico = true;
								}

								if (secaoMaterialColetadoExameLaboratorial != null) {
									tableEvidenciaMaterialBiologico = secaoMaterialColetadoExameLaboratorial.getTableByName("tableEvidenciaMaterialBiologico");

									if (tableEvidenciaMaterialBiologico == null) {
										// Montando cabeçalho da tabela de MATERIAL QUIMICO
										tableEvidenciaMaterialBiologico = secaoMaterialColetadoExameLaboratorial.addTable(1, 4);
										tableEvidenciaMaterialBiologico.setTableName("tableEvidenciaMaterialBiologico");

										// Montando colunas da tabela de MATERIAL QUIMICO
										Cell cellEvidenciaMaterialBiologico0 = tableEvidenciaMaterialBiologico.getCellByPosition(0, 0);
										cellEvidenciaMaterialBiologico0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellEvidenciaMaterialBiologico0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellEvidenciaMaterialBiologico0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellEvidenciaMaterialBiologico0.addParagraph("EVIDÊNCIA").setFont(fontTableHeader);

										Cell cellEvidenciaMaterialBiologico1 = tableEvidenciaMaterialBiologico.getCellByPosition(1, 0);
										cellEvidenciaMaterialBiologico1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellEvidenciaMaterialBiologico1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellEvidenciaMaterialBiologico1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellEvidenciaMaterialBiologico1.addParagraph("Nº LACRE").setFont(fontTableHeader);

										Cell cellEvidenciaMaterialBiologico2 = tableEvidenciaMaterialBiologico.getCellByPosition(2, 0);
										cellEvidenciaMaterialBiologico2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellEvidenciaMaterialBiologico2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellEvidenciaMaterialBiologico2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellEvidenciaMaterialBiologico2.addParagraph("INVÓLUCRO DE").setFont(fontTableHeader);

										Cell cellEvidenciaMaterialBiologico3 = tableEvidenciaMaterialBiologico.getCellByPosition(3, 0);
										cellEvidenciaMaterialBiologico3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellEvidenciaMaterialBiologico3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellEvidenciaMaterialBiologico3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellEvidenciaMaterialBiologico3.addParagraph("FECHADO POR").setFont(fontTableHeader);
									}

									if (tableEvidenciaMaterialBiologico != null) {

										int linhaTabela = tableEvidenciaMaterialBiologico.getRowCount();

										// EVIDENCIA
										Cell cellEvidenciaMaterialBiologico0 = tableEvidenciaMaterialBiologico.getCellByPosition(0, linhaTabela);
										cellEvidenciaMaterialBiologico0.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellEvidenciaMaterialBiologico0.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellEvidenciaMaterialBiologico0.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellEvidenciaMaterialBiologico0.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaMaterialTipo() != null
												? periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaMaterialTipo().getDescricao()
												: " ").setFont(fontTableDetail);

										// NUMERO DO LACRE
										Cell cellEvidenciaMaterialBiologico1 = tableEvidenciaMaterialBiologico.getCellByPosition(1, linhaTabela);
										cellEvidenciaMaterialBiologico1.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellEvidenciaMaterialBiologico1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellEvidenciaMaterialBiologico1.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellEvidenciaMaterialBiologico1
												.addParagraph(periciaEvidencia.getEvidencia().getUltimoLacre() != null ? periciaEvidencia.getEvidencia().getUltimoLacre().getNumero() : "Sem lacre")
												.setFont(fontTableDetail);

										// INVÓLUCRO DE
										Cell cellEvidenciaMaterialBiologico2 = tableEvidenciaMaterialBiologico.getCellByPosition(2, linhaTabela);
										cellEvidenciaMaterialBiologico2.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
										cellEvidenciaMaterialBiologico2.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
										cellEvidenciaMaterialBiologico2.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
										cellEvidenciaMaterialBiologico2.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaMaterialApresentacao() != null
												? periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaMaterialApresentacao().getDescricao().toLowerCase()
												: " ").setFont(fontTableDetail);

										// FECHADO POR
										if (!periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaFechadoPor().equals(TipoFechamentoEnum.OUTRO)) {
											Cell cellEvidenciaMaterialBiologico3 = tableEvidenciaMaterialBiologico.getCellByPosition(3, linhaTabela);
											cellEvidenciaMaterialBiologico3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellEvidenciaMaterialBiologico3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellEvidenciaMaterialBiologico3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellEvidenciaMaterialBiologico3.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaFechadoPor() != null
													? periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getEvidenciaFechadoPor().getDescricao().toLowerCase()
													: " ").setFont(fontTableDetail);
										} else {
											Cell cellEvidenciaMaterialBiologico3 = tableEvidenciaMaterialBiologico.getCellByPosition(3, linhaTabela);
											cellEvidenciaMaterialBiologico3.setVerticalAlignment(VerticalAlignmentType.MIDDLE);
											cellEvidenciaMaterialBiologico3.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
											cellEvidenciaMaterialBiologico3.setBorders(CellBordersType.ALL_FOUR, new Border(Color.BLACK, 0.5, StyleTypeDefinitions.SupportedLinearMeasure.PT));
											cellEvidenciaMaterialBiologico3.addParagraph(periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getFechadoPorOutroTipo().toLowerCase() != null
													? periciaEvidencia.getEvidencia().getUltimaEvidenciaMaterial().getFechadoPorOutroTipo().toLowerCase()
													: " ").setFont(fontTableDetail);
										}
									}
								}
							default:
								break;
							}
						}
					}

					Paragraph pMaterialLaboratorio1;
					String sMaterialLaboratorio1 = "";

					sMaterialLaboratorio1 = sMaterialLaboratorio1
							+ ("\nO(s) material(is) foi(ram) devidamente acondicionado(s), lacrado(s) e encaminhado(s) para a Coordenadoria de Análises Laboratoriais Forenses. "
									+ "Os resultados de tais exames poderão ser enviados à autoridade competente mediante solicitação, via ofício da referida delegacia.");

					pMaterialLaboratorio1 = secaoMaterialColetadoExameLaboratorial.addParagraph(sMaterialLaboratorio1);

					pMaterialLaboratorio1.setFont(fontParagrafoPNormal);
					pMaterialLaboratorio1.setHorizontalAlignment(HorizontalAlignmentType.JUSTIFY);
				}
			}

			// Seção Exames complementares
			Section secaoExamesComplementares = laudoDocumento.getSectionByName("secaoExamesComplementares");

			if (secaoExamesComplementares != null) {
				limparSecao(secaoExamesComplementares);

				List<ExameSolicitacao> listaExameSolicitacao = new ArrayList<ExameSolicitacao>();

				listaExameSolicitacao = exameSolicitacaoDao.buscarExamesSuplementaresPorSolicitacaoPrincipal(solicitacaoProcedimentoPericial);

				if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_VIDA_HOMICIDIO.getId()) {
					if (listaExameSolicitacao != null) {
						Paragraph p = secaoExamesComplementares.addParagraph("EXAMES COMPLEMENTARES\n");
						p.setFont(fontParagrafoPBold);

						Paragraph pExamesComplementares;
						String sExamesComplementares = "";

						sExamesComplementares = sExamesComplementares + ("Foi(ram) solicitado(s) exame(s) complementar(es) através da(s): ");

						for (ExameSolicitacao exameComplementar : listaExameSolicitacao) {

							sExamesComplementares = sExamesComplementares + ("\n» Solicitação nº" + exameComplementar.getSolicitacaoProcedimentoPericial().getId() + ", ");

							sExamesComplementares = sExamesComplementares + ("o exame de " + exameComplementar.getTipoExameSetor().getTipoExame().getDescricao());

							sExamesComplementares = sExamesComplementares + (" para o " + exameComplementar.getTipoExameSetor().getSetor().getDescricaoCompleta() + "; ");

						}

						pExamesComplementares = secaoExamesComplementares.addParagraph(sExamesComplementares);

						pExamesComplementares.setFont(fontParagrafoPNormal);
						pExamesComplementares.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
					}
				}

			}

			// Verifica se o Documento possui seção de anexos e limpa o documento
			Section secaoAnexos = laudoDocumento.getSectionByName("secaoAnexos");
			// Fotos Evidencia Laudo
			long quantidadeEvidenciaFoto = periciaEvidenciaDao.buscarQuantidadeFotoEvidenciaAnexada(pericia);
			String diretorioTemp = FacesUtils.getServletContext().getRealPath("/temp");
			int idFigura = 1;

			if (!laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.ALCOOLEMIA.getId())
					&& !laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_ETANOL_EM_AMOSTRA_BIOLOGICA.getId())
					&& !laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_DROGAS_EM_SANGUE.getId())
					&& !laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_DROGAS_EM_URINA.getId())) {
				if (laudo.getListaLaudoFoto() != null && !laudo.getListaLaudoFoto().isEmpty()
						|| (quantidadeEvidenciaFoto > 0 && pericia.getListPericiaEvidencia() != null && !pericia.getListPericiaEvidencia().isEmpty())) {
					if (secaoAnexos != null)
						limparSecao(secaoAnexos);
					else {
						laudoDocumento.addPageBreak();
						secaoAnexos = laudoDocumento.appendSection("secaoAnexos");
					}
				}
			}

			// Gerar anexo do laudo
			if (secaoAnexos != null && laudo.getListaLaudoFoto() != null && !laudo.getListaLaudoFoto().isEmpty()) {

				Table tableTemp = secaoAnexos.getTableByName("tabelaLaudoAnexo");
				if (tableTemp != null)
					tableTemp.remove();

				Table tabelaEvidenciaFotoTemp = secaoAnexos.getTableByName("tabelaEvidenciaFoto");
				if (tabelaEvidenciaFotoTemp != null)
					tabelaEvidenciaFotoTemp.remove();

				if (tableTemp != null) {
					int r = tableTemp.getRowCount();
					int c = tableTemp.getColumnCount();

					for (int i = 0; i < r; i++) {
						for (int j = 0; j < c; j++) {
							Cell cell = tableTemp.getCellByPosition(j, i);
							if (cell.getImage() != null)
								cell.getImage().remove();

							Paragraph p = null;
							do {
								if (p != null)
									cell.removeParagraph(p);
								p = cell.getParagraphByIndex(0, false);
							} while (p != null);
						}
					}
//					tableTemp.remove();
				}
				if (tabelaEvidenciaFotoTemp != null) {
					int r = tabelaEvidenciaFotoTemp.getRowCount();
					int c = tabelaEvidenciaFotoTemp.getColumnCount();

					for (int i = 0; i < r; i++) {
						for (int j = 0; j < c; j++) {
							Cell cell = tabelaEvidenciaFotoTemp.getCellByPosition(j, i);
							if (cell.getImage() != null)
								cell.getImage().remove();
							Paragraph p = null;
							do {
								if (p != null)
									cell.removeParagraph(p);
								p = cell.getParagraphByIndex(0, false);
							} while (p != null);
						}
					}
//					tabelaEvidenciaFotoTemp.remove();
				}

				if (secaoAnexos != null) {
					Paragraph p = secaoAnexos.addParagraph("FOTOS ANEXADAS AO LAUDO\n");
					p.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
					p.setFont(new Font("Helvetica", FontStyle.BOLD, 11));
					
						int linhas = (int) Math.round(((double) laudo.getListaLaudoFoto().size() / 2));
						int colunas = 2;
						
						LaudoFoto lf = laudo.getListaLaudoFoto().stream().filter(
								foto -> !foto.isTamanhoExpandido()
								).findAny().orElse(null);
						
						if(lf != null) {
								
							Table tableFotoLaudo = secaoAnexos.addTable(linhas, colunas);
							tableFotoLaudo.setTableName("tabelaLaudoAnexo");
						
							int linha = 0;
							int coluna = 0;
							
							for (LaudoFoto laudoFoto : laudo.getListaLaudoFoto()) {
								if (laudoFoto.isUtilizarNoLaudo() && laudoFoto.getUsuario().getId().equals(laudo.getUsuario().getId()) && !laudoFoto.isTamanhoExpandido()) {
									
									InputStream imageInput = new ByteArrayInputStream(laudoFoto.getFotoAnexo());
									File out = File.createTempFile("laudoFoto" + laudoFoto.getId(), ".jpg", new File(diretorioTemp));
									BufferedImage img;
									img = ImageIO.read(imageInput);
									ImageIO.write(img, "jpg", out);
		
									Cell cell = tableFotoLaudo.getCellByPosition(coluna++, linha);
		
									if (coluna == 2) {
										coluna = 0;
										linha++;
									}
		
									cell.getTableRow().setHeight(50, true);
									cell.setVerticalAlignment(VerticalAlignmentType.TOP);
									cell.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
									Border border = Border.NONE;
									cell.setBorders(CellBordersType.NONE, border);
									border.setDistance(10.5);
									// Paragraph pImage = cell.addParagraph("");
									cell.addParagraph("");
									// Image imagemLaudoFoto = cell.setImage(new
									// URI(FacesUtils.getCompleteContextPath() + "temp/" + out.getName()));
									Image imagemLaudoFoto = cell.setImage((new File(diretorioTemp + "/" + out.getName())).toURI());
		
									FrameStyleHandler handler = imagemLaudoFoto.getStyleHandler();
									handler.setAchorType(AnchorType.AS_CHARACTER);
		
									FrameRectangle rect = imagemLaudoFoto.getRectangle();
									if (rect.getWidth() > 8.5 || rect.getHeight() > 8.5) {
										double proporcao;
										if (rect.getWidth() >= rect.getHeight()) {
											proporcao = rect.getWidth() / 8.5;
										} else {
											proporcao = rect.getHeight() / 8.5;
										}
										rect.setWidth((rect.getWidth() / proporcao));
										rect.setHeight((rect.getHeight() / proporcao));
									}
		
									imagemLaudoFoto.setRectangle(rect);
		
									cell.addParagraph("Fotografia " + StrUtil.lpad(Integer.toString(idFigura++), 2, '0') + " - " + laudoFoto.getDescricao());
									out.delete();
								}
							}
							
							if (coluna == 1) {
								Cell cell = tableFotoLaudo.getCellByPosition(coluna, linha);
								cell.setBorders(CellBordersType.NONE, Border.NONE);
							}
						}
						
						Integer contTable = 1;
						for (LaudoFoto laudoFotoG : laudo.getListaLaudoFoto()) {
							if (laudoFotoG.isUtilizarNoLaudo() && laudoFotoG.getUsuario().getId().equals(laudo.getUsuario().getId()) && laudoFotoG.isTamanhoExpandido()) {
									
									Table tableFotoGrande = secaoAnexos.addTable(1, 1);
									tableFotoGrande.setTableName("tabelaLaudoAnexoGrande" + contTable.toString());
									
									InputStream imageInput = new ByteArrayInputStream(laudoFotoG.getFotoAnexo());
									File out = File.createTempFile("laudoFoto" + laudoFotoG.getId(), ".jpg", new File(diretorioTemp));
									BufferedImage img;
									img = ImageIO.read(imageInput);
									ImageIO.write(img, "jpg", out);
		
									Cell cell = tableFotoGrande.getCellByPosition(0, 0);
									
									//cell.getTableRow().setHeight(170, true);
									cell.setVerticalAlignment(VerticalAlignmentType.TOP);
									cell.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
									Border border = Border.NONE;
									cell.setBorders(CellBordersType.NONE, border);
									//border.setDistance(20);
									cell.addParagraph("");
									Image imagemLaudoFoto = cell.setImage((new File(diretorioTemp + "/" + out.getName())).toURI());
		
									FrameStyleHandler handler = imagemLaudoFoto.getStyleHandler();
									handler.setAchorType(AnchorType.TO_PARAGRAPH);
									int tamanho = 17;
									FrameRectangle rect = imagemLaudoFoto.getRectangle();
									if (rect.getWidth() > tamanho || rect.getHeight() > tamanho) {
										double proporcao;
										if (rect.getWidth() >= rect.getHeight()) {
											proporcao = rect.getWidth() / tamanho;
										} else {
											proporcao = rect.getHeight() / tamanho;
										}
										rect.setWidth((rect.getWidth() / proporcao));
										rect.setHeight((rect.getHeight() / proporcao));
									}
		
									imagemLaudoFoto.setRectangle(rect);
		
									cell.addParagraph("Fotografia " + StrUtil.lpad(Integer.toString(idFigura++), 2, '0') + " - " + laudoFotoG.getDescricao());
									out.delete();
									contTable++;
							}
						}

				}
			}

			if (secaoAnexos != null && quantidadeEvidenciaFoto > 0 && pericia.getListPericiaEvidencia() != null && !pericia.getListPericiaEvidencia().isEmpty()) {
				Paragraph p = secaoAnexos.addParagraph("FOTOS ANEXADAS ÀS EVIDÊNCIAS\n");
				p.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
				p.setFont(new Font("Helvetica", FontStyle.BOLD, 11));

				List<PericiaEvidencia> listaPericiaEvidenciaUtilizadaLaudo = periciaEvidenciaDao.buscarPericiaEvidenciaUtilizadaLaudo(pericia);
				int linhas = (int) Math.round(((double) quantidadeEvidenciaFoto / 2));
				int colunas = 2;

				Table tabelaEvidenciaFotoTemp = secaoAnexos.getTableByName("tabelaEvidenciaFoto");
				if (tabelaEvidenciaFotoTemp != null)
					tabelaEvidenciaFotoTemp.remove();

				Table tabelaEvidenciaFoto = secaoAnexos.addTable(linhas, colunas);
				tabelaEvidenciaFoto.setTableName("tabelaEvidenciaFoto");

				int linha = 0;
				int coluna = 0;
				boolean foiAnexadaFotoAEvidencia = false;
				// int idFigura = 1;

				for (PericiaEvidencia periciaEvidencia : listaPericiaEvidenciaUtilizadaLaudo) {
					if (periciaEvidencia.isUtilizaNoLaudo() && periciaEvidencia.getUsuario().getId().equals(laudo.getUsuario().getId())) {
						List<EvidenciaFoto> listaEvidenciaFoto = evidenciaFotoDao.buscarFotoPorEvidencia(periciaEvidencia.getEvidencia());

						for (EvidenciaFoto evidenciaFoto : listaEvidenciaFoto) {
							if (evidenciaFoto.isUtilizarNoLaudo() && evidenciaFoto.getUsuario().getId().equals(getUsuarioAutenticado().getId())) {
								foiAnexadaFotoAEvidencia = true;
								InputStream imageInput = new ByteArrayInputStream(evidenciaFoto.getFotoAnexo());
								File out = File.createTempFile("evidenciaFoto" + evidenciaFoto.getId(), ".jpg", new File(diretorioTemp));
								BufferedImage img;
								img = ImageIO.read(imageInput);
								ImageIO.write(img, "jpg", out);

								Cell cell = tabelaEvidenciaFoto.getCellByPosition(coluna++, linha);

								if (coluna == 2) {
									coluna = 0;
									linha++;
								}

								cell.getTableRow().setHeight(50, true);
								cell.setVerticalAlignment(VerticalAlignmentType.TOP);
								cell.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
								Border border = Border.NONE;
								cell.setBorders(CellBordersType.NONE, border);
								border.setDistance(10.5);
								// Image imagemEvidenciaFoto = cell.setImage(new
								// URI(FacesUtils.getCompleteContextPath() + "temp/" + out.getName()));
								Image imagemEvidenciaFoto = cell.setImage((new File(diretorioTemp + "/" + out.getName())).toURI());

								FrameStyleHandler handler = imagemEvidenciaFoto.getStyleHandler();
								handler.setAchorType(AnchorType.AS_CHARACTER);

								FrameRectangle rectFoto = imagemEvidenciaFoto.getRectangle();

								if (rectFoto.getWidth() > 8.5 || rectFoto.getHeight() > 8.5) {
									double proporcao;
									if (rectFoto.getWidth() >= rectFoto.getHeight()) {
										proporcao = rectFoto.getWidth() / 8.5;
									} else {
										proporcao = rectFoto.getHeight() / 8.5;
									}
									rectFoto.setWidth((rectFoto.getWidth() / proporcao));
									rectFoto.setHeight((rectFoto.getHeight() / proporcao));
								}
								imagemEvidenciaFoto.setRectangle(rectFoto);

								cell.addParagraph("Fotografia " + StrUtil.lpad(Integer.toString(idFigura++), 2, '0') + " - Evidência " + evidenciaFoto.getEvidencia().getId() + " - "
										+ evidenciaFoto.getDescricao());
								out.delete();
							}
						}
					}
				}

				// REMOVE A TABELA DE FOTOS DAS EVIDENCIAS CASO NAO TENHAM SIDO CADASTRADAS NENHUMA PARA O USUARIO
				if (!foiAnexadaFotoAEvidencia) {
					tabelaEvidenciaFoto.remove();
				}

				if (solicitacaoProcedimentoPericial.getUltimoExameSolicitacao().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.ALCOOLEMIA.getId())
						|| solicitacaoProcedimentoPericial.getUltimoExameSolicitacao().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_ETANOL_EM_AMOSTRA_BIOLOGICA.getId())
						|| solicitacaoProcedimentoPericial.getUltimoExameSolicitacao().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_DROGAS_EM_SANGUE.getId())
						|| solicitacaoProcedimentoPericial.getUltimoExameSolicitacao().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.PESQUISA_DE_DROGAS_EM_URINA.getId())) {
					secaoAnexos.remove();
				}

				if (coluna == 1) {
					Cell cell = tabelaEvidenciaFoto.getCellByPosition(coluna, linha);
					cell.setBorders(CellBordersType.NONE, Border.NONE);
				}

				if (solicitacaoProcedimentoPericial.getUltimoExameSolicitacao().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.LESAO_CORPORAL_FLAGRANTE.getId())
						|| solicitacaoProcedimentoPericial.getUltimoExameSolicitacao().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.LESAO_CORPORAL_FLAGRANTE_NUAC.getId())) {
					Paragraph paragraphCNJ = secaoAnexos.addParagraph(
							"\nRegistro fotográfico do periciando em atendimento à determinação do Tribunal de Justiça do Estado do Ceará ( Ofício nº 113/2020 - GMF/CE), Recomendação 62/2020 e Pedido de Providências nº 0003065-32.2020.2.00.0000 do Conselho Nacional de Justiça (CNJ).\n");
					paragraphCNJ.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
					paragraphCNJ.setFont(new Font("Helvetica", FontStyle.BOLD, 11));
				}
			}

			Section secaoDeclaracao = laudoDocumento.getSectionByName("secaoDeclaracao");
			List<DeclaracaoConsentimento> ld = declaracaoConsentimentoDao.buscarPorSolicitacao(solicitacaoProcedimentoPericial);
			if (secaoDeclaracao != null && !ld.isEmpty() && verificarDeclaracaoAnexada() == true) {
				limparSecao(secaoDeclaracao);
				
				Paragraph p = secaoDeclaracao.addParagraph("DECLARAÇÃO DE CONSENTIMENTO");
				p.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
				p.setFont(new Font("Helvetica", FontStyle.BOLD, 11));
				
				int linhas = ld.size();
				int colunas = 1;

				Table tableFotoDeclaracao = secaoDeclaracao.addTable(linhas, colunas);
				tableFotoDeclaracao.setTableName("tabelaDeclaracao");

				int linha = 0;

				for (DeclaracaoConsentimento dc : ld) {
					if (dc.getDeclaracaoAssinada() != null) {

						InputStream imageInput = new ByteArrayInputStream(dc.getDeclaracaoAssinada().getArquivo());
						File out = File.createTempFile("declaracaoFoto" + dc.getId(), ".jpg", new File(diretorioTemp));
						BufferedImage img;
						img = ImageIO.read(imageInput);
						ImageIO.write(img, "jpg", out);

						Cell cell = tableFotoDeclaracao.getCellByPosition(0, linha);

						linha++;

						cell.getTableRow().setHeight(50, true);
						cell.setVerticalAlignment(VerticalAlignmentType.TOP);
						cell.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
						Border border = Border.NONE;
						cell.setBorders(CellBordersType.NONE, border);
						border.setDistance(10.5);
						cell.addParagraph("");
						Image imagemLaudoFoto = cell.setImage((new File(diretorioTemp + "/" + out.getName())).toURI());

						FrameStyleHandler handler = imagemLaudoFoto.getStyleHandler();
						handler.setAchorType(AnchorType.AS_CHARACTER);

						FrameRectangle rect = imagemLaudoFoto.getRectangle();
						if (rect.getWidth() > 15 || rect.getHeight() > 17) {
							double proporcao;
							if (rect.getWidth() >= rect.getHeight()) {
								proporcao = rect.getWidth() / 15;
							} else {
								proporcao = rect.getHeight() / 17;
							}
							rect.setWidth((rect.getWidth() / proporcao));
							rect.setHeight((rect.getHeight() / proporcao));
						}

						imagemLaudoFoto.setRectangle(rect);

						//cell.addParagraph("Declaração " + Integer.toString(linha));
						out.delete();
					}
				}
			}
//			} else {
//				laudoDocumento.addPageBreak();
//				secaoDeclaracao = laudoDocumento.appendSection("secaoDeclaracao");
//			}
			
			
			OutputStream arquivoLaudo = new ByteArrayOutputStream();
			laudoDocumento.save(arquivoLaudo);
			laudoDocumento.close();
			return ((ByteArrayOutputStream) arquivoLaudo).toByteArray();

//		} catch (LaudoSemMetodologiaException e) {
//			FacesUtils.addErrorMessage("Nenhuma metodologia foi adicionada ao laudo!");
//			e.printStackTrace();
//		} catch (LaudoSemConclusaoException e) {
//			FacesUtils.addErrorMessage("Nenhuma conclusão foi adicionada ao laudo!");
//			e.printStackTrace();
//		} catch (LaudoSemQuesitosException e) {
//			FacesUtils.addErrorMessage("Nenhum quesito foi adicionado ao laudo!");
//			e.printStackTrace();
		} catch (

		Exception e) {
			FacesUtils.addErrorMessage(StrUtil.MSG_ERRO_EXCEPTION + " " + e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	private void validarLaudo()
			throws DocumentoInvalidoException, LaudoSemMetodologiaException, LaudoSemConclusaoException, LaudoSemQuesitosException, LaudoNaoAnexadoException, LaudoSemDatasException, Exception {

		if (laudo.getLaudoModeloTipoExameSetor().getLaudoModelo().isPossuiMetodologia() && (laudo.getListaLaudoMetodologia() == null || laudo.getListaLaudoMetodologia().isEmpty()))
			throw new LaudoSemMetodologiaException();

		if (laudo.getLaudoModeloTipoExameSetor().getLaudoModelo().isPossuiConclusao() && (laudo.getListaLaudoConclusao() == null || laudo.getListaLaudoConclusao().isEmpty()))
			throw new LaudoSemConclusaoException();

		if (laudo.getLaudoModeloTipoExameSetor().getLaudoModelo().isPossuiQuesito() && (laudo.getListaLaudoQuesito() == null || laudo.getListaLaudoQuesito().isEmpty()))
			throw new LaudoSemQuesitosException();

		if (laudo.getLaudoModeloTipoExameSetor().getLaudoModelo().isPossuiNotas() && (laudo.getListaLaudoNotas() == null || laudo.getListaLaudoNotas().isEmpty()))
			throw new LaudoSemNotasException();

		if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId() == TipoExameEnum.PERICIA_EM_LOCAL_DE_CRIME_CONTRA_VIDA_HOMICIDIO.getId()) {
			ProcedimentoSolicitacaoOcorrencia ocorrencia = procedimentoSolicitacaoOcorrenciaDao.buscarUltimaOcorrenciaPorSolicitacao(solicitacaoProcedimentoPericial);
			if (ocorrencia.getDataHoraOcorrencia() == null || ocorrencia.getHoraAtendimento() == null || ocorrencia.getHoraChamada() == null) {
				throw new LaudoSemDatasException();
			}
		}

		if (isUpdating()) {
			validarDocumento();
		}
	}

	public void alterarPericiaEvidencia(PericiaEvidencia periciaEvidencia) {
		try {
			periciaEvidenciaDao.update(periciaEvidencia);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected String buildJNLP() {
		String serverName = FacesUtils.getServerName();
		int serverPort = FacesUtils.getServerPort();
		String sessionId = FacesUtils.getSessionId();
		String tokenPass = tokenDao.getPass();
		StringBuffer jnlp = new StringBuffer();
		jnlp.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

		if (serverPort == 8083)
			jnlp.append("<jnlp spec=\"1.4+\" codebase=\"http://").append(serverName).append(":").append(serverPort).append("\">\n");
		else if (serverPort == 80)
			jnlp.append("<jnlp spec=\"1.4+\" codebase=\"http://").append(serverName).append(":").append(serverPort).append("\">\n");
		else
			jnlp.append("<jnlp spec=\"1.4+\" codebase=\"http://").append(serverName).append(":").append(serverPort).append("\">\n");
//		jnlp.append("<jnlp spec=\"1.4+\" codebase=\"http://").append(serverName).append(":").append(serverPort).append("/galileu\">\n");

		jnlp.append("<information>\n").append("<title>PEFOCE - DigitalSignature</title>\n");
		jnlp.append("<vendor>Pericia Forense do Estado Ceara</vendor>\n");
		jnlp.append("<description>Aplicacao para assinatura de documentos</description>\n");
		jnlp.append("<description kind=\"PefoceSignPDF\"></description>\n");
		jnlp.append("</information>\n");

		jnlp.append("<security>\n");
		jnlp.append("<all-permissions/>\n");
		jnlp.append("</security>\n");

		jnlp.append("<resources>\n");
		jnlp.append("<j2se version=\"1.7+\"/>\n");
		// TESTE COM JAR AUTO ASSINADO
		jnlp.append("<jar href=\"galileu_assinador.jar\" main=\"true\" />\n");
		// COMENTADO SOMENTE PARA TESTE
		// JAR ASSINADO COM CODESIGNER
		// jnlp.append("<jar href=\"galileuPdfSigner.jar\" main=\"true\" />\n");
		jnlp.append("</resources>\n");

		jnlp.append("<application-desc main-class=\"SignerPdf\">\n");

		jnlp.append("<argument>").append(getUsuarioAutenticado().getId().toString()).append("</argument>\n");
		jnlp.append("<argument>").append(getUsuarioAutenticado().getCpf()).append("</argument>\n");
		jnlp.append("<argument>").append(ultimoLaudoAnexo.getId()).append("</argument>\n");
		jnlp.append("<argument>").append(sessionId).append("</argument>\n");
		jnlp.append("<argument>").append("true").append("</argument>\n");
		jnlp.append("<argument>").append(serverName).append("</argument>\n");
		jnlp.append("<argument>").append(serverPort).append("</argument>\n");
		jnlp.append("<argument>").append(tokenPass).append("</argument>\n");
		jnlp.append("</application-desc>\n</jnlp>\n");
		return (jnlp.toString());
	}

	/**
	 * Cria um arquivo JNLP para assinatura do perito adjunto.
	 * 
	 * @return
	 */
	protected String buildJNLPAdjunto() {
		String serverName = FacesUtils.getServerName();
		int serverPort = FacesUtils.getServerPort();
		String sessionId = FacesUtils.getSessionId();
		String tokenPass = tokenDao.getPass();

		StringBuffer jnlp = new StringBuffer();
		jnlp.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

		if (serverPort == 8083)
			jnlp.append("<jnlp spec=\"1.4+\" codebase=\"http://").append(serverName).append(":").append(serverPort).append("\">\n");
		else if (serverPort == 80)
			jnlp.append("<jnlp spec=\"1.4+\" codebase=\"http://").append(serverName).append(":").append(serverPort).append("\">\n");
		else
			jnlp.append("<jnlp spec=\"1.4+\" codebase=\"http://").append(serverName).append(":").append(serverPort).append("/galileu\">\n");

		jnlp.append("<information>\n").append("<title>PEFOCE - DigitalSignature</title>\n");
//		jnlp.append("<vendor>PERICIA FORENSE DO ESTADO CEARA</vendor>\n");
		jnlp.append("<description>Aplicacao para assinatura de documentos</description>\n");
		jnlp.append("<description kind=\"PefoceSignPDF\"></description>\n");
		jnlp.append("</information>\n");

		jnlp.append("<security>\n");
		jnlp.append("<all-permissions/>\n");
		jnlp.append("</security>\n");

		jnlp.append("<resources>\n");
		jnlp.append("<j2se version=\"1.7+\"/>\n");

		// TESTE COM JAR AUTO ASSINADO
		jnlp.append("<jar href=\"galileuAssinadorAdjunto.jar\" main=\"true\" />\n");
		jnlp.append("</resources>\n");

		jnlp.append("<application-desc main-class=\"SignerPdfAdjunto\">\n");

		jnlp.append("<argument>").append(getUsuarioAutenticado().getId().toString()).append("</argument>\n");
		jnlp.append("<argument>").append(getUsuarioAutenticado().getCpf()).append("</argument>\n");
		jnlp.append("<argument>").append(ultimoLaudoAnexo.getId()).append("</argument>\n");
		jnlp.append("<argument>").append(sessionId).append("</argument>\n");
		jnlp.append("<argument>").append("true").append("</argument>\n");
		jnlp.append("<argument>").append(serverName).append("</argument>\n");
		jnlp.append("<argument>").append(serverPort).append("</argument>\n");
		jnlp.append("<argument>").append(peritoRevisorSelecionado.getId()).append("</argument>\n");
		jnlp.append("<argument>").append(tokenPass).append("</argument>\n");
		jnlp.append("</application-desc>\n</jnlp>\n");
		return (jnlp.toString());
	}

	/**
	 * Cria um arquivo JNLP para assinatura do perito adjunto.
	 * 
	 * @return
	 */
	protected String buildJNLPPeritoRevisor() {
		String serverName = FacesUtils.getServerName();
		int serverPort = FacesUtils.getServerPort();
		String sessionId = FacesUtils.getSessionId();
		String tokenPass = tokenDao.getPass();

		StringBuffer jnlp = new StringBuffer();
		jnlp.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

		if (serverPort == 8083)
			jnlp.append("<jnlp spec=\"1.4+\" codebase=\"http://").append(serverName).append(":").append(serverPort).append("\">\n");
		else if (serverPort == 80)
			jnlp.append("<jnlp spec=\"1.4+\" codebase=\"http://").append(serverName).append(":").append(serverPort).append("\">\n");
		else
			jnlp.append("<jnlp spec=\"1.4+\" codebase=\"http://").append(serverName).append(":").append(serverPort).append("/galileu\">\n");

		jnlp.append("<information>\n").append("<title>PEFOCE - DigitalSignature</title>\n");
//		jnlp.append("<vendor>PERICIA FORENSE DO ESTADO CEARA</vendor>\n");
		jnlp.append("<description>Aplicacao para assinatura de documentos</description>\n");
		jnlp.append("<description kind=\"PefoceSignPDF\"></description>\n");
		jnlp.append("</information>\n");

		jnlp.append("<security>\n");
		jnlp.append("<all-permissions/>\n");
		jnlp.append("</security>\n");

		jnlp.append("<resources>\n");
		jnlp.append("<j2se version=\"1.7+\"/>\n");

		// TESTE COM JAR AUTO ASSINADO
		jnlp.append("<jar href=\"galileuAssinadorAdjunto.jar\" main=\"true\" />\n");
		jnlp.append("</resources>\n");

		jnlp.append("<application-desc main-class=\"SignerPdfRevisor\">\n");

		jnlp.append("<argument>").append(getUsuarioAutenticado().getId().toString()).append("</argument>\n");
		jnlp.append("<argument>").append(getUsuarioAutenticado().getCpf()).append("</argument>\n");
		jnlp.append("<argument>").append(ultimoLaudoAnexo.getId()).append("</argument>\n");
		jnlp.append("<argument>").append(sessionId).append("</argument>\n");
		jnlp.append("<argument>").append("true").append("</argument>\n");
		jnlp.append("<argument>").append(serverName).append("</argument>\n");
		jnlp.append("<argument>").append(serverPort).append("</argument>\n");
		jnlp.append("<argument>").append(ultimoLaudoAnexo.getPeritoRevisor().getId()).append("</argument>\n");
		jnlp.append("<argument>").append(tokenPass).append("</argument>\n");
		jnlp.append("</application-desc>\n</jnlp>\n");
		return (jnlp.toString());
	}

	/**
	 * Realiza a assinatura dos laudos periciais.
	 * 
	 * @param laudo
	 * @return
	 */
	public DefaultStreamedContent assinar(Laudo laudo) {

		try {
			this.laudo = laudo;
			this.laudo.setListaLaudoFoto(null);
			ultimoLaudoAnexo = laudoAnexoDao.buscarUltimoLaudoAnexoPorLaudo(laudo);
			InputStream input = null;

			if (ultimoLaudoAnexo != null && ultimoLaudoAnexo.getArquivoAnexado() != null && !ultimoLaudoAnexo.getTipoDocumentoAnexo().equals(TipoDocumentoAnexoEnum.LAUDO_ASSINADO)) {
				if (ultimoLaudoAnexo.getTipoDocumentoAnexo().equals(TipoDocumentoAnexoEnum.LAUDO_ODT)) {
					// METODO ANTIGO INICIO
					// InputStream in = new
					// ByteArrayInputStream(ultimoLaudoAnexo.getArquivoAnexado());
					// OdfDocument document = OdfDocument.loadDocument(in);
					// OutputStream out = new ByteArrayOutputStream();
					//
					// PdfOptions options = PdfOptions.getDefault();
					// PdfConverter.getInstance().convert(document, out, options);
					// METODO ANTIGO FIM

					InputStream in = new ByteArrayInputStream(ultimoLaudoAnexo.getArquivoAnexado());
					OutputStream out = new ByteArrayOutputStream();
					OpenOfficeConnection connection = null;

					try {

						// connect to an OpenOffice.org instance running on port 8100
						connection = new SocketOpenOfficeConnection(8100);
						if (connection.isConnected())
							connection.disconnect();

						connection.connect();

						// convert
						DocumentConverter converter = new OpenOfficeDocumentConverter(connection);

						DocumentFormat odtFormat = new DefaultDocumentFormatRegistry().getFormatByFileExtension("odt");
						DocumentFormat pdfFormat = new DefaultDocumentFormatRegistry().getFormatByFileExtension("pdf");
						converter.convert(in, odtFormat, out, pdfFormat);

						// close the connection
						connection.disconnect();

						LaudoAnexo laudoAnexo = new LaudoAnexo();
						laudoAnexo.setArquivoAnexado(((ByteArrayOutputStream) out).toByteArray());
						laudoAnexo.setLaudo(laudo);
						laudoAnexo.setUsuario(getUsuarioAutenticado());
						laudoAnexo.setTipoDocumentoAnexo(TipoDocumentoAnexoEnum.LAUDO_PDF);
						laudoAnexo.setNomeArquivo("arquivo.pdf.nao.assinado");

						if (getUsuarioAutenticado().isPeritoAdjunto()) {
							laudoDao.salvarPraPdf(ultimoLaudoAnexo, laudoAnexo, peritoRevisorSelecionado);
							ocultarDialogo("modalPeritoRevisorDlg");
						} else {
							laudoDao.salvarPraPdf(ultimoLaudoAnexo, laudoAnexo);
						}

						ultimoLaudoAnexo = laudoAnexo;
					} catch (ConnectException e) {
						FacesUtils.addErrorMessage("O servidor do LibreOffice não foi iniciado.");
						if (connection.isConnected())
							connection.disconnect();
						e.printStackTrace();
						throw new Exception();
					}
					if (connection != null && connection.isConnected())
						connection.disconnect();
				}

				if (getUsuarioAutenticado().isPeritoAdjunto()) {
					input = new ByteArrayInputStream(buildJNLPAdjunto().getBytes());
				} else {
					input = new ByteArrayInputStream(buildJNLP().getBytes());
				}

				return new DefaultStreamedContent(input, "application/x-java-jnlp-file", "signer.jnlp");
			}
		} catch (Exception e) {
			System.out.println("Erro ao tentar assinar laudo.");
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Escolhe o perito revisor e prepara o laudo para assinatura do Perito Adjunto
	 * 
	 * @param laudo
	 */
	public void prepararAssinarAdjunto(Laudo laudo) {
		peritoRevisorSelecionado = null;
		this.laudo = laudo;
		exibirDialogo("modalPeritoRevisorDlg");
	}

	public void cancelarAssinaturaAdjunto() {
		this.laudo = null;
		ocultarDialogo("modalPeritoRevisorDlg");
	}

	public DefaultStreamedContent assinarComoRevisor(Laudo laudo) {
		this.laudo = laudo;
		this.laudo.setListaLaudoFoto(null);
		ultimoLaudoAnexo = laudoAnexoDao.buscarUltimoLaudoAnexoPorLaudo(laudo);
		InputStream input = new ByteArrayInputStream(buildJNLPPeritoRevisor().getBytes());

		return new DefaultStreamedContent(input, "application/x-java-jnlp-file", "signer.jnlp");
	}

	/**
	 * Envia laudo para correção do perito adjunto.
	 * 
	 * @param solicitacaoProcedimentoPericial
	 */
	public void enviarPedidoCorrecaoLaudo(SolicitacaoProcedimentoPericial solicitacaoProcedimentoPericial, List<SolicitacaoProcedimentoPericial> listaSolicitacaoProcedimentoPericial) {
		this.solicitacaoProcedimentoPericial = solicitacaoProcedimentoPericial;
		this.listaSolicitacaoProcedimentoPericial = listaSolicitacaoProcedimentoPericial;
		Pericia pericia = periciaDao.buscaUltimaPericiaPorSolicitacaoResumida(this.solicitacaoProcedimentoPericial);
		this.laudo = laudoDao.buscarUltimoLaudoPorPericia(pericia);
		exibirDialogo("modalSolicitarCorrecaoLaudoDlg");
	}

	/**
	 * Confirmar pedido de correção do laudo.
	 */
	public void confirmarPedidoCorrecaoLaudo() {
		try {
			LaudoAnexo laudoPeritoAdjunto = laudoAnexoDao.buscarUltimoLaudoAnexoPorLaudo(this.laudo);
			LaudoAnexo ultimoLaudoNaoAssinado = laudoAnexoDao.buscarUltimolaudoAnexoODT(this.laudo);
			laudoDao.solicitarCorrecao(this.laudo, laudoPeritoAdjunto, ultimoLaudoNaoAssinado);
			if (this.solicitacaoProcedimentoPericial.getUltimaPericiaLaudo() != null)
				this.solicitacaoProcedimentoPericial.getUltimaPericiaLaudo().setLaudo(this.laudo);
			this.listaSolicitacaoProcedimentoPericial.remove(this.solicitacaoProcedimentoPericial);
			FacesUtils.addInfoMessage(StrUtil.MSG_SUCESSO_ENVIAR);
			ocultarDialogo("modalSolicitarCorrecaoLaudoDlg");
		} catch (Exception e) {
			FacesUtils.addErrorMessage(StrUtil.MSG_ERRO_INCLUIR);
			e.printStackTrace();
		}
	}

	public void cancelarPedidoCorrecaoLaudo() {
		this.laudo = null;
		ocultarDialogo("modalSolicitarCorrecaoLaudoDlg");
	}

	public void exibirOrientacaoCorrecao(Laudo laudo) {
		this.laudo = laudo;
		exibirDialogo("modalOrientacaoCorrecaoLaudoDlg");
	}

	public void fecharOrientacaoCorrecao() {
		this.laudo = null;
		ocultarDialogo("modalOrientacaoCorrecaoLaudoDlg");
	}

	public void assinarInformativo(Laudo laudo) {
		try {
			this.laudo = laudo;
			this.laudo.setListaLaudoFoto(null);
			ultimoLaudoAnexo = laudoAnexoDao.buscarUltimoLaudoAnexoPorLaudo(laudo);

			if (ultimoLaudoAnexo != null && ultimoLaudoAnexo.getArquivoAnexado() != null && !ultimoLaudoAnexo.getTipoDocumentoAnexo().equals(TipoDocumentoAnexoEnum.LAUDO_ASSINADO_ELETRONICAMENTE)) {
				if (ultimoLaudoAnexo.getTipoDocumentoAnexo().equals(TipoDocumentoAnexoEnum.LAUDO_ODT)) {

					String md5RegistroEletronico = DigestUtils.md5Hex(laudo.getNumero() + pericia.getUsuario().getRegistroFuncional().getMatricula() + pericia.getUsuario().getCpf());
					InputStream input = new ByteArrayInputStream(ultimoLaudoAnexo.getArquivoAnexado());

					// Carrega o modelo para inserção dos valores nos campos.
					TextDocument laudoDocumento;
					laudoDocumento = TextDocument.loadDocument(input);

					Font fontParagrafoPNormal = new Font("Helvetica", FontStyle.REGULAR, 9.5);
					Font fontParagrafoPBold = new Font("Helvetica", FontStyle.BOLD, 10.5);
					Font fontParagrafoPItalic = new Font("Helvetica", FontStyle.ITALIC, 10.5);

					Section secaoAssinatura = laudoDocumento.getSectionByName("secaoAssinatura");
					if (secaoAssinatura != null) {
						limparSecao(secaoAssinatura);
						Paragraph pNomeAssinante = secaoAssinatura.addParagraph(pericia.getUsuario().getRegistroFuncional().getTratamento().getAbreviacao() + " " + pericia.getUsuario().getNome());
						pNomeAssinante.setFont(fontParagrafoPBold);
						pNomeAssinante.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
						pericia.getUsuario().setUsuarioHistorico(usuarioDao.findLastUsuarioHistorico(pericia.getUsuario()));
						Paragraph pCargoAssinante = secaoAssinatura.addParagraph(pericia.getUsuario().getUsuarioHistorico().getCorporacaoCargo().getCargo().getDescricao() + " Matrícula: "
								+ pericia.getUsuario().getRegistroFuncional().getMatricula());
						pCargoAssinante.setFont(fontParagrafoPNormal);
						pCargoAssinante.setHorizontalAlignment(HorizontalAlignmentType.CENTER);

						if (laudo.getTipoLaudo().equals(TipoLaudoEnum.INFORMATIVO_TECNICO)) {
							// CRIA UM MD5 PARA ASSINATURA ELETRÔNICA PARA OS INFORMATIVOS TÉCNICOS.
							secaoAssinatura.addParagraph("");
							Paragraph pRegistroEletronico = secaoAssinatura.addParagraph("R.E.: " + md5RegistroEletronico);
							pRegistroEletronico.setFont(fontParagrafoPItalic);
							pRegistroEletronico.setHorizontalAlignment(HorizontalAlignmentType.CENTER);

							VariableField _campoCoordenadoria = laudoDocumento.getVariableFieldByName("campoRegistroEletronico");
							if (_campoCoordenadoria != null) {
								_campoCoordenadoria.updateField("R.E.:" + md5RegistroEletronico, null);
							}
						}
					}

					OutputStream arquivoLaudo = new ByteArrayOutputStream();
					laudoDocumento.save(arquivoLaudo);
					laudoDocumento.close();

					InputStream in = new ByteArrayInputStream(((ByteArrayOutputStream) arquivoLaudo).toByteArray());
					OutputStream out = new ByteArrayOutputStream();
					OpenOfficeConnection connection = null;

					connection = new SocketOpenOfficeConnection(8100);
					if (connection.isConnected())
						connection.disconnect();

					connection.connect();

					// convert
					DocumentConverter converter = new OpenOfficeDocumentConverter(connection);

					DocumentFormat odtFormat = new DefaultDocumentFormatRegistry().getFormatByFileExtension("odt");
					DocumentFormat pdfFormat = new DefaultDocumentFormatRegistry().getFormatByFileExtension("pdf");
					converter.convert(in, odtFormat, out, pdfFormat);

					// close the connection
					connection.disconnect();

					LaudoAnexo laudoAnexo = new LaudoAnexo();
					laudoAnexo.setArquivoAnexado(((ByteArrayOutputStream) out).toByteArray());
					laudoAnexo.setLaudo(laudo);
					laudoAnexo.setUsuario(getUsuarioAutenticado());
					laudoAnexo.setTipoDocumentoAnexo(TipoDocumentoAnexoEnum.LAUDO_ASSINADO_ELETRONICAMENTE);
					laudoAnexo.setNomeArquivo("arquivo.pdf.assinado.eletronicamente");
					laudoAnexo.setRegistroEletronico(md5RegistroEletronico);
					laudoAnexo.setDataProtocolo(new Date());

					laudoDao.salvarPraPdf(ultimoLaudoAnexo, laudoAnexo);
					ultimoLaudoAnexo = laudoAnexo;
					this.ultimoLaudoAnexo = laudoAnexo;

					this.pericia.setListPericiaLaudo(periciaDao.buscarPericiaLaudoPorPericia(this.pericia));
					for (PericiaLaudo pl : this.pericia.getListPericiaLaudo()) {
						pl.getLaudo().setUltimoLaudoAnexo(laudoAnexoDao.buscarUltimoLaudoAnexoPorLaudo(pl.getLaudo()));
						pl.getLaudo().setListaLaudoConclusao(null);
						pl.getLaudo().setListaLaudoNotas(null);
						pl.getLaudo().setListaLaudoMetodologia(null);
						pl.getLaudo().setListaLaudoQuesito(null);
						pl.getLaudo().setUltimoLaudoParecer(null);

					}
					FacesUtils.addInfoMessage("Informativo assinado com sucesso!");
				}
			}
		} catch (Exception e) {
			System.out.println("Erro ao tentar assinar laudo.");
			e.printStackTrace();
		}
	}

	public DefaultStreamedContent downloadInformativoAssinado() {
		try {
			InputStream downloadAquivoAssinado = new ByteArrayInputStream(ultimoLaudoAnexo.getArquivoAnexado());
			downloadPDF = new DefaultStreamedContent(downloadAquivoAssinado, StrUtil.CONTENT_TYPE_PDF_FILE,
					"Informativo Técnico - " + laudo.getNumero() + "-" + StrUtil.anoData(laudo.getDataInclusao()) + ".pdf");

			return downloadPDF;

		} catch (Exception e) {
			System.out.println("Erro ao tentar assinar laudo.");
			e.printStackTrace();
		}

		return null;
	}

//TODO Método para converter ODT para PDF que foi comentado devido a retirada de 4 pacotes	
//	public DefaultStreamedContent getVisualizarLaudoParaAssinatura() {
//
//		try {
//			if (laudo != null) {
//
//				ultimoLaudoAnexo = laudoAnexoDao.buscarUltimoLaudoAnexoPorLaudo(laudo);
//
//				if (ultimoLaudoAnexo != null && ultimoLaudoAnexo.getArquivoAnexado() != null) {
//					if (ultimoLaudoAnexo.getTipoDocumentoAnexo().equals(TipoDocumentoAnexoEnum.LAUDO_ODT)) {
//
//						InputStream in = new ByteArrayInputStream(ultimoLaudoAnexo.getArquivoAnexado());
//						OdfDocument document = OdfDocument.loadDocument(in);
//						OutputStream out = new ByteArrayOutputStream();
//
//						PdfOptions options = PdfOptions.getDefault();
//						// PdfOptions options = PdfOptions.create();
//						PdfConverter.getInstance().convert(document, out, options);
//
//						LaudoAnexo laudoAnexo = new LaudoAnexo();
//						laudoAnexo.setArquivoAnexado(((ByteArrayOutputStream) out).toByteArray());
//						laudoAnexo.setLaudo(laudo);
//						laudoAnexo.setUsuario(getUsuarioAutenticado());
//						laudoAnexo.setTipoDocumentoAnexo(TipoDocumentoAnexoEnum.LAUDO_PDF);
//						laudoAnexo.setNomeArquivo("arquivo.pdf.nao.assinado");
//
//						laudoDao.salvarPraPdf(ultimoLaudoAnexo, laudoAnexo);
//						ultimoLaudoAnexo = laudoAnexo;
//					}
//
//					InputStream input = new ByteArrayInputStream(ultimoLaudoAnexo.getArquivoAnexado());
//					downloadPDF = new DefaultStreamedContent(input, StrUtil.CONTENT_TYPE_PDF_FILE, "extensions-rocks.pdf");
//
//					return downloadPDF;
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return null;
//	}

	private String locale = "en";

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
//TODO Método para converter ODT para PDF que foi comentado devido a retirada de 4 pacotes
//	public void converterParaPdf(Laudo laudo) {
//		this.laudo = laudo;
//		getVisualizarLaudoParaAssinatura();
//		exibirDialogo("modalDocumentoAssinadoDlg");
//	}

	public void aguardando() {
		try {
			int tentantivas = 0;
			while (!ultimoLaudoAnexo.getTipoDocumentoAnexo().equals(TipoDocumentoAnexoEnum.LAUDO_ASSINADO) && tentantivas++ < 30) {
				ultimoLaudoAnexo = laudoAnexoDao.buscarUltimoLaudoAnexoPorLaudo(laudo);
				this.laudo.setUltimoLaudoAnexo(ultimoLaudoAnexo);
				TimeUnit.SECONDS.sleep(2);
				// Thread.sleep(5000);
			}

			if (((ultimoLaudoAnexo.getTipoDocumentoAnexo().equals(TipoDocumentoAnexoEnum.LAUDO_ASSINADO) && ultimoLaudoAnexo.getPeritoRevisor() == null)
					|| ultimoLaudoAnexo.getTipoDocumentoAnexo().equals(TipoDocumentoAnexoEnum.LAUDO_ASSINADO_COM_REVISOR)) && ultimoLaudoAnexo.getNumeroProtocolo() == null) {
				ConfiguracaoUsuario config = configuracaoUsuarioDao.buscarPorUsuario(getUsuarioAutenticado());
				if (config != null && config.isEnvioAutomaticoSIP()) {
					this.laudo.setUltimoLaudoAnexo(laudoDao.enviarLaudoAoSip(this.laudo.getUltimoLaudoAnexo()));
				}
				FacesUtils.addInfoMessage("Documento assinado com sucesso!");
			}

		} catch (DocumentoInvalidoException e) {
			FacesUtils.addErrorMessage("O documento não foi cadastrado no SIP!");
			e.printStackTrace();
		} catch (InterruptedException e) {
			FacesUtils.addErrorMessage(StrUtil.MSG_ERRO_EXCEPTION);
			e.printStackTrace();
		} catch (ProcedimentoNaoRecebidoNoSIPException e) {
			FacesUtils.addErrorMessage("Procedimento não recebido no SIP!");
			e.printStackTrace();
		} catch (SetorSemIDSIPException e) {
			FacesUtils.addErrorMessage("Delegacia sem a Identificação do SIP!");
			e.printStackTrace();
		} catch (SQLException e) {
			FacesUtils.addErrorMessage(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			FacesUtils.addErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Libera a janela depois que um Laudo é assinado.
	 */
	public void aguardandoAssinaturaRevisor() {
		try {
			int tentantivas = 0;
			while (!ultimoLaudoAnexo.getTipoDocumentoAnexo().equals(TipoDocumentoAnexoEnum.LAUDO_ASSINADO_COM_REVISOR) && tentantivas++ < 30) {
				ultimoLaudoAnexo = laudoAnexoDao.buscarUltimoLaudoAnexoPorLaudo(laudo);
				this.laudo.setUltimoLaudoAnexo(ultimoLaudoAnexo);
				TimeUnit.SECONDS.sleep(2);
				// Thread.sleep(5000);
			}

			if (ultimoLaudoAnexo.getTipoDocumentoAnexo().equals(TipoDocumentoAnexoEnum.LAUDO_ASSINADO_COM_REVISOR) && ultimoLaudoAnexo.getNumeroProtocolo() == null) {
				ConfiguracaoUsuario config = configuracaoUsuarioDao.buscarPorUsuario(getUsuarioAutenticado());
				if (config != null && config.isEnvioAutomaticoSIP()) {
					this.laudo.setUltimoLaudoAnexo(laudoDao.enviarLaudoAoSip(this.laudo.getUltimoLaudoAnexo()));
				}
				FacesUtils.addInfoMessage("Documento assinado com sucesso!");
			}

		} catch (DocumentoInvalidoException e) {
			FacesUtils.addErrorMessage("O documento não foi cadastrado no SIP");
			e.printStackTrace();
		} catch (InterruptedException e) {
			FacesUtils.addErrorMessage(StrUtil.MSG_ERRO_EXCEPTION);
			e.printStackTrace();
		} catch (ProcedimentoNaoRecebidoNoSIPException e) {
			FacesUtils.addErrorMessage("Procedimento não recebido no SIP!");
			e.printStackTrace();
		} catch (SetorSemIDSIPException e) {
			FacesUtils.addErrorMessage("Setor sem Identificação do SIP!");
			e.printStackTrace();
		} catch (SQLException e) {
			FacesUtils.addErrorMessage(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			FacesUtils.addErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}

	public void salvarLaudo() {
		try {
			validarLaudo();

			// SALVA O CODIGO PARA CONSULTA DE DPVAT
			if (laudo.getLaudoModeloTipoExameSetor().getTipoExameSetor().getTipoExame().getId().equals(TipoExameEnum.DPVAT.getId())) {
				String codigoValidacao = DigestUtils.md5Hex(laudo.getNumero().toString() + StrUtil.dataFormatada(laudo.getDataInclusao()));
				laudo.setCodigoValidacao(codigoValidacao.substring(0, 12));
			}

			laudo = laudoDao.salvarLaudo(periciaLaudo, tipoLaudo);
			// this.pericia.setListPericiaLaudo(periciaDao.buscarPericiaLaudoPorPericia(this.pericia));
			setBrowsing(true);
			laudo.setListaLaudoMetodologia(null);
			laudo.setListaLaudoConclusao(null);
			laudo.setListaLaudoNotas(null);
			laudo.setListaLaudoAnexo(null);
			laudo.setListaLaudoFoto(null);
			laudo.setListaLaudoParecer(null);
			laudo.setListaLaudoQuesito(null);
			laudo.setUltimoLaudoAnexo(laudoAnexoDao.buscarUltimoLaudoAnexoPorLaudo(laudo));
			this.periciaLaudo.setLaudo(laudo);

			if (this.pericia.getListPericiaLaudo() == null)
				this.pericia.setListPericiaLaudo(new ArrayList<PericiaLaudo>());

			if (!this.pericia.getListPericiaLaudo().contains(this.periciaLaudo))
				this.pericia.getListPericiaLaudo().add(periciaLaudo);

			FacesUtils.addInfoMessage(StrUtil.MSG_SUCESSO_ALTERAR);
		} catch (DocumentoInvalidoException e) {
			FacesUtils.addErrorMessage("O documento anexado não pertence ao laudo!");
			e.printStackTrace();
		} catch (LaudoNaoAnexadoException e) {
			FacesUtils.addErrorMessage("O documento não foi anexado ao laudo!");
			e.printStackTrace();
		} catch (LaudoSemMetodologiaException e) {
			FacesUtils.addErrorMessage("Não foram incluídas metodologias ao laudo!");
			e.printStackTrace();
		} catch (LaudoSemConclusaoException e) {
			FacesUtils.addErrorMessage("Não incluída conclusão laudo!");
			e.printStackTrace();
		} catch (LaudoSemQuesitosException e) {
			FacesUtils.addErrorMessage("Não foram incluídos quesitos ao laudo!");
			e.printStackTrace();
		} catch (Exception e) {
			FacesUtils.addErrorMessage(StrUtil.MSG_ERRO_EXCEPTION);
			e.printStackTrace();
		}

	}

	public void cancelar() {
		if (this.laudo != null)
			this.laudo.setListaLaudoFoto(null);
		ocultarDialogo("modalPericiaLaudoDlg");
	}

	/**
	 * Faz upload das imagens para o laudo
	 * 
	 * @param event
	 */
	public void adicionarLaudoFoto() {
		try {
			byte[] fotoRedimensionada = ImageUtil.redimensionarImagem(laudoFotoAnexada, 1024);
			LaudoFoto laudoFoto = new LaudoFoto();
			laudoFoto.setDataInclusao(new Date());
			laudoFoto.setUsuario(getUsuarioAutenticado());
			laudoFoto.setDescricao(descricaoLaudoFoto);
			laudoFoto.setLaudo(laudo);
			laudoFoto.setFotoAnexo(fotoRedimensionada);
			if (laudo.getId() != null) {
				laudoFoto = laudoFotoDao.save(laudoFoto);
			}
			if (laudo.getListaLaudoFoto() == null)
				laudo.setListaLaudoFoto(new ArrayList<LaudoFoto>());

			laudo.getListaLaudoFoto().add(laudoFoto);
			laudoFotoAnexada = null;
			descricaoLaudoFoto = null;
		} catch (Exception e) {
			FacesUtils.addErrorMessage(StrUtil.MSG_ERRO_EXCEPTION);
			e.printStackTrace();
		}
	}

	public void uploadLaudoFoto(FileUploadEvent event) {
		laudoFotoAnexada = event.getFile().getContents();
	}

	/**
	 * Atualiza o status da foto (utilizar ou não no laudo)
	 * 
	 * @param laudoFoto
	 */
	public void atualizarFoto(LaudoFoto laudoFoto) {
		try {
			if (laudoFoto.getId() != null)
				laudoFotoDao.update(laudoFoto);
		} catch (Exception e) {
			FacesUtils.addErrorMessage(StrUtil.MSG_ERRO_EXCEPTION);
			e.printStackTrace();
		}
	}

	public boolean isContemArquivoAnexado() {
		if (laudo.getListaLaudoAnexo() != null && laudo.getUltimoLaudoModeloAnexo().getModeloAnexo().length > 0)
			return true;
		else
			return false;
	}

	public void cancelarLaudo() {
		this.pericia.setListPericiaLaudo(periciaDao.buscarPericiaLaudoPorPericia(this.pericia));
		for (PericiaLaudo pl : this.pericia.getListPericiaLaudo()) {
			pl.getLaudo().setUltimoLaudoAnexo(laudoAnexoDao.buscarUltimoLaudoAnexoPorLaudo(pl.getLaudo()));
			pl.getLaudo().setListaLaudoConclusao(null);
			pl.getLaudo().setListaLaudoNotas(null);
			pl.getLaudo().setListaLaudoMetodologia(null);
			pl.getLaudo().setListaLaudoQuesito(null);
			pl.getLaudo().setUltimoLaudoParecer(null);

		}
		reiniciaContador();
		possuiQuesitoExtra = false;
		possuiEsquemaCorporal = false;
		desejaVerTutorialLaudoTorturaIstambul = false;
		setBrowsing(true);
	}

	private void limparSecao(Section secao) {
		if (secao != null) {
			Paragraph para = null;
			do {
				if (para != null)
					secao.removeParagraph(para);
				para = secao.getParagraphByIndex(0, false);
			} while (para != null);
		}
	}

	private void apagarSecao(Section secao) {
		if (secao != null) {
			Paragraph para = null;
			do {
				if (para != null)
					para = secao.getParagraphByIndex(0, false);
				secao.remove();
			} while (para != null);
		}
	}

	/**
	 * Envia o Laudo ao SIP
	 * 
	 * @param laudoAnexo
	 */
	public void enviarLaudo(Laudo laudo) {
		try {
			this.laudo = laudo;
			ultimoLaudoAnexo = laudoAnexoDao.buscarUltimoLaudoAnexoPorLaudo(laudo);

			if (ultimoLaudoAnexo.getNumeroProtocolo() == null || DireitosUtil.isPossuiDireito("Administrador")) {
				this.laudo.setUltimoLaudoAnexo(laudoDao.enviarLaudoAoSip(this.laudo.getUltimoLaudoAnexo()));
				FacesUtils.addInfoMessage("Laudo enviado ao SIP com sucesso!");
			} else
				FacesUtils.addInfoMessage("Laudo já enviado ao SIP com anterior!");

		} catch (DocumentoInvalidoException e) {
			FacesUtils.addWarnMessage("O documento (Guia/Ofício) não foi cadastrado no SIP");
			e.printStackTrace();
		} catch (ProcedimentoNaoRecebidoNoSIPException e) {
			e.printStackTrace();
			FacesUtils.addWarnMessage(StrUtil.MSG_WARN_SOLICITACAO_NAO_RECEBIDA_SIP);
		} catch (SetorSemIDSIPException e) {
			e.printStackTrace();
			FacesUtils.addWarnMessage(StrUtil.MSG_WARN_SETOR_NAO_CADASTRADO_NO_SIP);
		} catch (SQLRecoverableException e) {
			FacesUtils.addWarnMessage("Falha no envio! Tente novamente ou Contate a CTI.");
			e.printStackTrace();
		} catch (SQLException e) {
			FacesUtils.addErrorMessage("Ocorreu um erro de conexão com SIP! Contate a CTI.");
			e.printStackTrace();
		} catch (Exception e) {
			FacesUtils.addErrorMessage(StrUtil.MSG_ERRO_EXCEPTION);
			e.printStackTrace();
		}
	}
	
	
	/*
	 *Envia Laudo ao Dinamo
	 */
	
	
	
	public void enviarLaudoDinamo(JSONObject jsonObject, String token) throws IOException, JSONException {
				
				JSONObject laudoJson = jsonObject;  //new JSONObject();
				String URLParaCahamada = "http://localhost:8080/v1/guia/registrarlaudo";
				
			try {	
				if( token == null) {
					FacesUtils.addWarnMessage("Erro de comunicação com o Dinamo! Por favor contate a CTI.");				
					return;
				}
			
			
				if (laudoJson != null) {
					
					URL url = new URL(URLParaCahamada); 
					HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
					conexao.setRequestMethod("POST");
					conexao.setRequestProperty("Content-Type", "application/json");		
					conexao.setRequestProperty("x-access-token", token);
					conexao.setRequestProperty("Authorization", "Bearer " + token);
					
					// se a conexao for para saída
					conexao.setDoOutput(true); 
					
					//grava fluxo de saída
					OutputStream outputStream = conexao.getOutputStream();
					
					//grava os dados em bytes
					outputStream.write(laudoJson.toString().getBytes());
					
					//abre a conexao de acordo com a URL
					conexao.connect();
					
					// buscando resposta da conexao		
					
					int httpResult = conexao.getResponseCode();						
					
					if (httpResult == HttpURLConnection.HTTP_OK) {       
																				//stream é uma sequencia 
				        BufferedReader response = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
				        String line =  response.readLine();				        
				        while (line != null) {
				            System.out.println(line);
				            line = response.readLine();	
				        }
				     
				        response.close();
				    }else {
				    	throw new RuntimeException("HTTP error code : " + httpResult);
				    }
				  
				    outputStream.close();
				   
				    conexao.disconnect();
					
				} else {
					FacesUtils.addWarnMessage("Erro ao gerar dados do laudo. Por favor contate a CTI");
					return;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				FacesUtils.addWarnMessage("Erro de comunicação com o Dinamo! Por favor contate a CTI.");
				return;
				
			}	
			
	}
	

	/**
	 * Exclui uma foto da galeria.
	 */
	public void excluirFotoGaleria(LaudoFoto laudoFoto) {
		try {
			if (laudoFoto.getId() != null) {
				laudoFoto.setDataFinalizacao(new Date());
				laudoFotoDao.update(laudoFoto);
			}
			this.laudo.getListaLaudoFoto().remove(laudoFoto);
			FacesUtils.addInfoMessage(StrUtil.MSG_SUCESSO_EXCLUIR);
		} catch (Exception e) {
			FacesUtils.addErrorMessage(StrUtil.MSG_ERRO_EXCEPTION);
			e.printStackTrace();
		}
	}

	public void preencherQuesitoRespostaPadrao(LaudoQuesito laudoQuesito) {

		String respostaCustomizada = quesitoRespostaPadraoDao.buscarRespostaPorQuesitoEPadrao(laudoQuesito.getQuesito(), laudoQuesito.getQuesito().getRespostaPadrao());
		if (respostaCustomizada != null && !respostaCustomizada.isEmpty())
			laudoQuesito.setResposta(respostaCustomizada);
		else
			laudoQuesito.setResposta(laudoQuesito.getQuesito().getRespostaPadrao().getDescricao());
	}

	
	public boolean isPermiteEnvioSIP(PericiaLaudo periciaLaudo) {
		if (periciaLaudo.getLaudo().getUltimoLaudoAnexo() != null 
			&& !periciaLaudo.getPericia().getSolicitacaoProcedimentoPericial().getDocumentoProcedimentoPericial().getTipoDocumento().getDescricao().equals("Guia Dinamo")) {
			
			if ((periciaLaudo.getLaudo().getUltimoLaudoAnexo().getTipoDocumentoAnexo() == TipoDocumentoAnexoEnum.LAUDO_ASSINADO
					&& periciaLaudo.getLaudo().getUltimoLaudoAnexo().getNumeroProtocolo() == null && !getUsuarioAutenticado().isPeritoAdjunto()) ||

					(periciaLaudo.getLaudo().getUltimoLaudoAnexo().getTipoDocumentoAnexo() == TipoDocumentoAnexoEnum.LAUDO_ASSINADO
							&& periciaLaudo.getLaudo().getUltimoLaudoAnexo().getNumeroProtocolo() == null && periciaLaudo.getLaudo().getUltimoLaudoAnexo().getPeritoRevisor() == null
							&& getUsuarioAutenticado().isPeritoAdjunto())
					||

					(periciaLaudo.getLaudo().getUltimoLaudoAnexo().getTipoDocumentoAnexo() == TipoDocumentoAnexoEnum.LAUDO_ASSINADO_COM_REVISOR
							&& periciaLaudo.getLaudo().getUltimoLaudoAnexo().getNumeroProtocolo() == null && getUsuarioAutenticado().isPeritoAdjunto())
					||

					(periciaLaudo.getLaudo().getUltimoLaudoAnexo().getTipoDocumentoAnexo() == TipoDocumentoAnexoEnum.LAUDO_ASSINADO
							&& periciaLaudo.getLaudo().getUltimoLaudoAnexo().getNumeroProtocolo() != null && DireitosUtil.isPossuiDireito("Administrador"))) {
				return true;
			}
		}

		return false;
	}

	
	
	//METODO DE VERIFICACAO PARA ENVIAR AO DINAMO.
	public boolean isPermiteEnvioDinamo(PericiaLaudo periciaLaudo) {
		periciaLaudo.getLaudo().setUltimoLaudoAnexo(laudoAnexoDao.buscarUltimoLaudoAnexoPorLaudo(periciaLaudo.getLaudo()));		
		
		if (periciaLaudo.getPericia().getSolicitacaoProcedimentoPericial().getDocumentoProcedimentoPericial().getTipoDocumento().getDescricao().equals("Guia Dinamo")
				&& periciaLaudo.getLaudo().getUltimoLaudoAnexo() != null ) {
			
			if (	(periciaLaudo.getLaudo().getUltimoLaudoAnexo().getTipoDocumentoAnexo() == TipoDocumentoAnexoEnum.LAUDO_ASSINADO 
					&& periciaLaudo.getLaudo().getUltimoLaudoAnexo().getNumeroProtocolo() == null && !getUsuarioAutenticado().isPeritoAdjunto()) 			
					||					
					(periciaLaudo.getLaudo().getUltimoLaudoAnexo().getTipoDocumentoAnexo() == TipoDocumentoAnexoEnum.LAUDO_ASSINADO 
					&& periciaLaudo.getLaudo().getUltimoLaudoAnexo().getNumeroProtocolo() == null && periciaLaudo.getLaudo().getUltimoLaudoAnexo().getPeritoRevisor() == null 
					&& getUsuarioAutenticado().isPeritoAdjunto()) 
					||
					(periciaLaudo.getLaudo().getUltimoLaudoAnexo().getTipoDocumentoAnexo() == TipoDocumentoAnexoEnum.LAUDO_ASSINADO_COM_REVISOR 
					&& periciaLaudo.getLaudo().getUltimoLaudoAnexo().getNumeroProtocolo() == null && getUsuarioAutenticado().isPeritoAdjunto()) 
					||
					(periciaLaudo.getLaudo().getUltimoLaudoAnexo().getTipoDocumentoAnexo() == TipoDocumentoAnexoEnum.LAUDO_ASSINADO 
					&& periciaLaudo.getLaudo().getUltimoLaudoAnexo().getNumeroProtocolo() != null && DireitosUtil.isPossuiDireito("Administrador")) 
		           		
					) {
						return true;
			}
		}
		return false;
	}
	
	
	
	public Integer atualizaContador(LaudoQuesito lq) {

		if (lq.getQuesito().getBloco() != null) {

			if (blocoAnterior == null)
				blocoAnterior = new Bloco();

			if (!blocoAnterior.equals(lq.getQuesito().getBloco())) {
				blocoAnterior = lq.getQuesito().getBloco();
				contQuesitoBloco = 1;
				return contQuesitoBloco;
			} else {
				contQuesitoBloco++;
				return contQuesitoBloco;
			}

		}
		return contQuesito++;
	}
	
	public void reiniciaContador(){
		contQuesito = 1;
		contQuesitoBloco = 1;
		blocoAnterior = null;
	}
	
	public void ordenarListaLaudoQuesito(List<LaudoQuesito> listaLaudoQuesito) {
		listaLaudoQuesito.sort((lhs, rhs) -> {
			if(lhs.getQuesito().getBloco() != null && rhs.getQuesito().getBloco() != null) {
				if (lhs.getQuesito().getBloco().getId().equals(rhs.getQuesito().getBloco().getId()))
		            return Integer.compare(lhs.getQuesito().getOrdem(), rhs.getQuesito().getOrdem());
				else
					return lhs.getQuesito().getBloco().getId().compareTo(rhs.getQuesito().getBloco().getId());
			}else {
				return lhs.getQuesito().getOrdem().compareTo(rhs.getQuesito().getOrdem());
			}
	    });
	}
	
	public boolean verificarDeclaracaoAnexada() {
		boolean isDeclaracaoAnexada = false;
		List<DeclaracaoConsentimento> ld = declaracaoConsentimentoDao.buscarPorSolicitacao(solicitacaoProcedimentoPericial);
		if(!ld.isEmpty()) {
			for (DeclaracaoConsentimento dc : ld) {
				if(dc.getDeclaracaoAssinada() != null) {
					isDeclaracaoAnexada = true;
				}
			}
		}
		return isDeclaracaoAnexada;
	}
	
	// GETTERS & SETTERS
	public SolicitacaoProcedimentoPericial getSolicitacaoProcedimentoPericial() {
		return solicitacaoProcedimentoPericial;
	}

	public void setSolicitacaoProcedimentoPericial(SolicitacaoProcedimentoPericial solicitacaoProcedimentoPericial) {
		this.solicitacaoProcedimentoPericial = solicitacaoProcedimentoPericial;
	}

	public LaudoModeloDao getLaudoModeloDao() {
		return laudoModeloDao;
	}

	public void setLaudoModeloDao(LaudoModeloDao laudoModeloDao) {
		this.laudoModeloDao = laudoModeloDao;
	}

	public ExameSolicitacaoDao getExameSolicitacaoDao() {
		return exameSolicitacaoDao;
	}

	public void setExameSolicitacaoDao(ExameSolicitacaoDao exameSolicitacaoDao) {
		this.exameSolicitacaoDao = exameSolicitacaoDao;
	}

	public Laudo getLaudo() {
		return laudo;
	}

	public void setLaudo(Laudo laudo) {
		this.laudo = laudo;
	}

	public List<LaudoModeloTipoExameSetor> getListaLaudoModeloTipoExameSetor() {
		return listaLaudoModeloTipoExameSetor;
	}

	public void setListaLaudoModeloTipoExameSetor(List<LaudoModeloTipoExameSetor> listaLaudoModeloTipoExameSetor) {
		this.listaLaudoModeloTipoExameSetor = listaLaudoModeloTipoExameSetor;
	}

	public LaudoModeloTipoExameSetorDao getLaudoModeloTipoExameSetorDao() {
		return laudoModeloTipoExameSetorDao;
	}

	public void setLaudoModeloTipoExameSetorDao(LaudoModeloTipoExameSetorDao laudoModeloTipoExameSetorDao) {
		this.laudoModeloTipoExameSetorDao = laudoModeloTipoExameSetorDao;
	}

	public LaudoModeloConclusaoDao getLaudoModeloConclusaoDao() {
		return laudoModeloConclusaoDao;
	}

	public void setLaudoModeloConclusaoDao(LaudoModeloConclusaoDao laudoModeloConclusaoDao) {
		this.laudoModeloConclusaoDao = laudoModeloConclusaoDao;
	}

	public LaudoModeloMetodologiaDao getLaudoModeloMetodologiaDao() {
		return laudoModeloMetodologiaDao;
	}

	public void setLaudoModeloMetodologiaDao(LaudoModeloMetodologiaDao laudoModeloMetodologiaDao) {
		this.laudoModeloMetodologiaDao = laudoModeloMetodologiaDao;
	}

	public LaudoModeloQuesitoDao getLaudoModeloQuesitoDao() {
		return laudoModeloQuesitoDao;
	}

	public void setLaudoModeloQuesitoDao(LaudoModeloQuesitoDao laudoModeloQuesitoDao) {
		this.laudoModeloQuesitoDao = laudoModeloQuesitoDao;
	}

	public LaudoModeloMetodologia getLaudoModeloMetodologiaSelecionada() {
		return laudoModeloMetodologiaSelecionada;
	}

	public void setLaudoModeloMetodologiaSelecionada(LaudoModeloMetodologia laudoModeloMetodologiaSelecionada) {
		this.laudoModeloMetodologiaSelecionada = laudoModeloMetodologiaSelecionada;
	}

	public LaudoModeloConclusao getLaudoModeloConclusaoSelecionada() {
		return laudoModeloConclusaoSelecionada;
	}

	public void setLaudoModeloConclusaoSelecionada(LaudoModeloConclusao laudoModeloConclusaoSelecionada) {
		this.laudoModeloConclusaoSelecionada = laudoModeloConclusaoSelecionada;
	}

	public LaudoModeloQuesito getLaudoModeloQuesitoSelecionado() {
		return laudoModeloQuesitoSelecionado;
	}

	public void setLaudoModeloQuesitoSelecionado(LaudoModeloQuesito laudoModeloQuesitoSelecionado) {
		this.laudoModeloQuesitoSelecionado = laudoModeloQuesitoSelecionado;
	}

	public List<LaudoModeloQuesito> getListaLaudoModeloQuesito() {
		return listaLaudoModeloQuesito;
	}

	public void setListaLaudoModeloQuesito(List<LaudoModeloQuesito> listaLaudoModeloQuesito) {
		this.listaLaudoModeloQuesito = listaLaudoModeloQuesito;
	}

	public List<LaudoModeloConclusao> getListaLaudoModeloConclusao() {
		return listaLaudoModeloConclusao;
	}

	public void setListaLaudoModeloConclusao(List<LaudoModeloConclusao> listaLaudoModeloConclusao) {
		this.listaLaudoModeloConclusao = listaLaudoModeloConclusao;
	}

	public Pericia getPericia() {
		return pericia;
	}

	public void setPericia(Pericia pericia) {
		this.pericia = pericia;
	}

	public PericiaDao getPericiaDao() {
		return periciaDao;
	}

	public void setPericiaDao(PericiaDao periciaDao) {
		this.periciaDao = periciaDao;
	}

	public List<LaudoModeloMetodologia> getListaLaudoModeloMetodologia() {
		return listaLaudoModeloMetodologia;
	}

	public void setListaLaudoModeloMetodologia(List<LaudoModeloMetodologia> listaLaudoModeloMetodologia) {
		this.listaLaudoModeloMetodologia = listaLaudoModeloMetodologia;
	}

	public LaudoParecer getLaudoParecer() {
		return laudoParecer;
	}

	public void setLaudoParecer(LaudoParecer laudoParecer) {
		this.laudoParecer = laudoParecer;
	}

	public LaudoDao getLaudoDao() {
		return laudoDao;
	}

	public void setLaudoDao(LaudoDao laudoDao) {
		this.laudoDao = laudoDao;
	}

	public String getParecer() {
		return parecer;
	}

	public void setParecer(String parecer) {
		this.parecer = parecer;
	}

	public SolicitacaoEvidenciaDao getSolicitacaoEvidenciaDao() {
		return solicitacaoEvidenciaDao;
	}

	public void setSolicitacaoEvidenciaDao(SolicitacaoEvidenciaDao solicitacaoEvidenciaDao) {
		this.solicitacaoEvidenciaDao = solicitacaoEvidenciaDao;
	}

	public List<SolicitacaoEvidencia> getListaSolicitacaoEvidenciaSelecionada() {
		return listaSolicitacaoEvidenciaSelecionada;
	}

	public void setListaSolicitacaoEvidenciaSelecionada(List<SolicitacaoEvidencia> listaSolicitacaoEvidenciaSelecionada) {
		this.listaSolicitacaoEvidenciaSelecionada = listaSolicitacaoEvidenciaSelecionada;
	}

	public String getDescricaoLaudoFoto() {
		return descricaoLaudoFoto;
	}

	public void setDescricaoLaudoFoto(String descricaoLaudoFoto) {
		this.descricaoLaudoFoto = descricaoLaudoFoto;
	}

	public byte[] getLaudoFotoAnexada() {
		return laudoFotoAnexada;
	}

	public void setLaudoFotoAnexada(byte[] laudoFotoAnexada) {
		this.laudoFotoAnexada = laudoFotoAnexada;
	}

	public PericiaLaudo getPericiaLaudo() {
		return periciaLaudo;
	}

	public void setPericiaLaudo(PericiaLaudo periciaLaudo) {
		this.periciaLaudo = periciaLaudo;
	}

	public LaudoModeloAnexoDao getLaudoModeloAnexoDao() {
		return laudoModeloAnexoDao;
	}

	public void setLaudoModeloAnexoDao(LaudoModeloAnexoDao laudoModeloAnexoDao) {
		this.laudoModeloAnexoDao = laudoModeloAnexoDao;
	}

	public LaudoMetodologiaDao getLaudoMetodologiaDao() {
		return laudoMetodologiaDao;
	}

	public void setLaudoMetodologiaDao(LaudoMetodologiaDao laudoMetodologiaDao) {
		this.laudoMetodologiaDao = laudoMetodologiaDao;
	}

	public LaudoQuesitoDao getLaudoQuesitoDao() {
		return laudoQuesitoDao;
	}

	public void setLaudoQuesitoDao(LaudoQuesitoDao laudoQuesitoDao) {
		this.laudoQuesitoDao = laudoQuesitoDao;
	}

	public LaudoConclusaoDao getLaudoConclusaoDao() {
		return laudoConclusaoDao;
	}

	public void setLaudoConclusaoDao(LaudoConclusaoDao laudoConclusaoDao) {
		this.laudoConclusaoDao = laudoConclusaoDao;
	}

	public LaudoParecerDao getLaudoParecerDao() {
		return laudoParecerDao;
	}

	public void setLaudoParecerDao(LaudoParecerDao laudoParecerDao) {
		this.laudoParecerDao = laudoParecerDao;
	}

	public LaudoFotoDao getLaudoFotoDao() {
		return laudoFotoDao;
	}

	public void setLaudoFotoDao(LaudoFotoDao laudoFotoDao) {
		this.laudoFotoDao = laudoFotoDao;
	}

	public EvidenciaEnvolvidoPessoaDao getEvidenciaEnvolvidoPessoaDao() {
		return evidenciaEnvolvidoPessoaDao;
	}

	public void setEvidenciaEnvolvidoPessoaDao(EvidenciaEnvolvidoPessoaDao evidenciaEnvolvidoPessoaDao) {
		this.evidenciaEnvolvidoPessoaDao = evidenciaEnvolvidoPessoaDao;
	}

	public HistoricoProcedimentoSIPDao getHistoricoProcedimentoSIPDao() {
		return historicoProcedimentoSIPDao;
	}

	public void setHistoricoProcedimentoSIPDao(HistoricoProcedimentoSIPDao historicoProcedimentoSIPDao) {
		this.historicoProcedimentoSIPDao = historicoProcedimentoSIPDao;
	}

	public PericiaEvidenciaDao getPericiaEvidenciaDao() {
		return periciaEvidenciaDao;
	}

	public void setPericiaEvidenciaDao(PericiaEvidenciaDao periciaEvidenciaDao) {
		this.periciaEvidenciaDao = periciaEvidenciaDao;
	}

	public UsuarioDao getUsuarioDao() {
		return usuarioDao;
	}

	public void setUsuarioDao(UsuarioDao usuarioDao) {
		this.usuarioDao = usuarioDao;
	}

	public UploadedFile getLaudoUpload() {
		return laudoUpload;
	}

	public void setLaudoUpload(UploadedFile laudoUpload) {
		this.laudoUpload = laudoUpload;
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

	public void setEvidenciaDispositivoTecnologicoDao(EvidenciaDispositivoTecnologicoDao evidenciaDispositivoTecnologicoDao) {
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

	public EvidenciaDispositivoTecnologicoImeiDao getEvidenciaDispositivoTecnologicoImeiDao() {
		return evidenciaDispositivoTecnologicoImeiDao;
	}

	public void setEvidenciaDispositivoTecnologicoImeiDao(EvidenciaDispositivoTecnologicoImeiDao evidenciaDispositivoTecnologicoImeiDao) {
		this.evidenciaDispositivoTecnologicoImeiDao = evidenciaDispositivoTecnologicoImeiDao;
	}

	public EvidenciaFotoDao getEvidenciaFotoDao() {
		return evidenciaFotoDao;
	}

	public void setEvidenciaFotoDao(EvidenciaFotoDao evidenciaFotoDao) {
		this.evidenciaFotoDao = evidenciaFotoDao;
	}

	public UsuarioLacreCustodiaDao getUsuarioLacreCustodiaDao() {
		return usuarioLacreCustodiaDao;
	}

	public void setUsuarioLacreCustodiaDao(UsuarioLacreCustodiaDao usuarioLacreCustodiaDao) {
		this.usuarioLacreCustodiaDao = usuarioLacreCustodiaDao;
	}

	public LaudoAnexo getUltimoLaudoAnexo() {
		return ultimoLaudoAnexo;
	}

	public void setUltimoLaudoAnexo(LaudoAnexo ultimoLaudoAnexo) {
		this.ultimoLaudoAnexo = ultimoLaudoAnexo;
	}

	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}

	public DefaultStreamedContent getArquivoJNLP() {
		return downloadPDF;
	}

	public void setArquivoJNLP(DefaultStreamedContent arquivoJNLP) {
		this.downloadPDF = arquivoJNLP;
	}

	public PericiaLaudoDao getPericiaLaudoDao() {
		return periciaLaudoDao;
	}

	public void setPericiaLaudoDao(PericiaLaudoDao periciaLaudoDao) {
		this.periciaLaudoDao = periciaLaudoDao;
	}

	public DefaultStreamedContent getDownloadLaudoModelo() {
		return downloadLaudoModelo;
	}

	public void setDownloadLaudoModelo(DefaultStreamedContent downloadLaudoModelo) {
		this.downloadLaudoModelo = downloadLaudoModelo;
	}

	public SolicitacaoTramitacaoDao getSolicitacaoTramitacaoDao() {
		return solicitacaoTramitacaoDao;
	}

	public void setSolicitacaoTramitacaoDao(SolicitacaoTramitacaoDao solicitacaoTramitacaoDao) {
		this.solicitacaoTramitacaoDao = solicitacaoTramitacaoDao;
	}

	public DocumentoProcedimentoPericialDao getDocumentoProcedimentoPericialDao() {
		return documentoProcedimentoPericialDao;
	}

	public void setDocumentoProcedimentoPericialDao(DocumentoProcedimentoPericialDao documentoProcedimentoPericialDao) {
		this.documentoProcedimentoPericialDao = documentoProcedimentoPericialDao;
	}

	public TipoLaudoEnum getTipoLaudo() {
		return tipoLaudo;
	}

	public void setTipoLaudo(TipoLaudoEnum tipoLaudo) {
		this.tipoLaudo = tipoLaudo;
	}

	public LaudoFoto getLaudoFoto() {
		return laudoFoto;
	}

	public void setLaudoFoto(LaudoFoto laudoFoto) {
		this.laudoFoto = laudoFoto;
	}

	public AuditoriaLogDao getAuditoriaLogDao() {
		return auditoriaLogDao;
	}

	public void setAuditoriaLogDao(AuditoriaLogDao auditoriaLogDao) {
		this.auditoriaLogDao = auditoriaLogDao;
	}

	public ConfiguracaoUsuarioDao getConfiguracaoUsuarioDao() {
		return configuracaoUsuarioDao;
	}

	public void setConfiguracaoUsuarioDao(ConfiguracaoUsuarioDao configuracaoUsuarioDao) {
		this.configuracaoUsuarioDao = configuracaoUsuarioDao;
	}

	public QuesitoRespostaPadrao getQuesitoRespostaPadraoSelecionada() {
		return quesitoRespostaPadraoSelecionada;
	}

	public void setQuesitoRespostaPadraoSelecionada(QuesitoRespostaPadrao quesitoRespostaPadraoSelecionada) {
		this.quesitoRespostaPadraoSelecionada = quesitoRespostaPadraoSelecionada;
	}

	public RespostaPadrao getRespostaPadraoSelecionada() {
		return respostaPadraoSelecionada;
	}

	public void setRespostaPadraoSelecionada(RespostaPadrao respostaPadraoSelecionada) {
		this.respostaPadraoSelecionada = respostaPadraoSelecionada;
	}

	public Usuario getPeritoRevisorSelecionado() {
		return peritoRevisorSelecionado;
	}

	public void setPeritoRevisorSelecionado(Usuario peritoRevisorSelecionado) {
		this.peritoRevisorSelecionado = peritoRevisorSelecionado;
	}

	public List<SolicitacaoProcedimentoPericial> getListaSolicitacaoProcedimentoPericial() {
		return listaSolicitacaoProcedimentoPericial;
	}

	public void setListaSolicitacaoProcedimentoPericial(List<SolicitacaoProcedimentoPericial> listaSolicitacaoProcedimentoPericial) {
		this.listaSolicitacaoProcedimentoPericial = listaSolicitacaoProcedimentoPericial;
	}

	public DefaultStreamedContent getDownloadPDF() {
		return downloadPDF;
	}

	public void setDownloadPDF(DefaultStreamedContent downloadPDF) {
		this.downloadPDF = downloadPDF;
	}

	public EvidenciaObjetoDao getEvidenciaObjetoDao() {
		return evidenciaObjetoDao;
	}

	public void setEvidenciaObjetoDao(EvidenciaObjetoDao evidenciaObjetoDao) {
		this.evidenciaObjetoDao = evidenciaObjetoDao;
	}

	public TokenDao getTokenDao() {
		return tokenDao;
	}

	public void setTokenDao(TokenDao tokenDao) {
		this.tokenDao = tokenDao;
	}

	public MetodologiaDao getMetodologiaDao() {
		return metodologiaDao;
	}

	public void setMetodologiaDao(MetodologiaDao metodologiaDao) {
		this.metodologiaDao = metodologiaDao;
	}

	public ConclusaoDao getConclusaoDao() {
		return conclusaoDao;
	}

	public void setConclusaoDao(ConclusaoDao conclusaoDao) {
		this.conclusaoDao = conclusaoDao;
	}

	public RespostaPadraoDao getRespostaPadraoDao() {
		return respostaPadraoDao;
	}

	public void setRespostaPadraoDao(RespostaPadraoDao respostaPadraoDao) {
		this.respostaPadraoDao = respostaPadraoDao;
	}

	public QuesitoDao getQuesitoDao() {
		return quesitoDao;
	}

	public void setQuesitoDao(QuesitoDao quesitoDao) {
		this.quesitoDao = quesitoDao;
	}

	public QuesitoRespostaPadraoDao getQuesitoRespostaPadraoDao() {
		return quesitoRespostaPadraoDao;
	}

	public void setQuesitoRespostaPadraoDao(QuesitoRespostaPadraoDao quesitoRespostaPadraoDao) {
		this.quesitoRespostaPadraoDao = quesitoRespostaPadraoDao;
	}

	public QuesitoRespostaPadrao getRespostaQuesito() {
		return respostaQuesito;
	}

	public void setRespostaQuesito(QuesitoRespostaPadrao respostaQuesito) {
		this.respostaQuesito = respostaQuesito;
	}

	public SolicitacaoProcedimentoPericial getSolicitacao() {
		return solicitacao;
	}

	public void setSolicitacao(SolicitacaoProcedimentoPericial solicitacao) {
		this.solicitacao = solicitacao;
	}

	public SolicitacaoProcedimentoPericialFormularioDao getSolicitacaoProcedimentoFormularioDao() {
		return solicitacaoProcedimentoFormularioDao;
	}

	public void setSolicitacaoProcedimentoFormularioDao(SolicitacaoProcedimentoPericialFormularioDao solicitacaoProcedimentoFormularioDao) {
		this.solicitacaoProcedimentoFormularioDao = solicitacaoProcedimentoFormularioDao;
	}

	public LaudoModeloNotas getLaudoModeloNotasSelecionadas() {
		return laudoModeloNotasSelecionadas;
	}

	public void setLaudoModeloNotasSelecionadas(LaudoModeloNotas laudoModeloNotasSelecionadas) {
		this.laudoModeloNotasSelecionadas = laudoModeloNotasSelecionadas;
	}

	public List<LaudoModeloNotas> getListaLaudoModeloNotas() {
		return listaLaudoModeloNotas;
	}

	public void setListaLaudoModeloNotas(List<LaudoModeloNotas> listaLaudoModeloNotas) {
		this.listaLaudoModeloNotas = listaLaudoModeloNotas;
	}

	public LaudoModeloNotasDao getLaudoModeloNotasDao() {
		return laudoModeloNotasDao;
	}

	public void setLaudoModeloNotasDao(LaudoModeloNotasDao laudoModeloNotasDao) {
		this.laudoModeloNotasDao = laudoModeloNotasDao;
	}

	public LaudoNotasDao getLaudoNotasDao() {
		return laudoNotasDao;
	}

	public void setLaudoNotasDao(LaudoNotasDao laudoNotasDao) {
		this.laudoNotasDao = laudoNotasDao;
	}

//	public SolicitacaoResultado getResultadosAlcoolemia() {
//		return resultadosAlcoolemia;
//	}
//
//	public void setResultadosAlcoolemia(SolicitacaoResultado resultadosAlcoolemia) {
//		this.resultadosAlcoolemia = resultadosAlcoolemia;
//	}

	public SolicitacaoResultadoDao getSolicitacaoResultadoDao() {
		return solicitacaoResultadoDao;
	}

	public void setSolicitacaoResultadoDao(SolicitacaoResultadoDao solicitacaoResultadoDao) {
		this.solicitacaoResultadoDao = solicitacaoResultadoDao;
	}

	public ProcedimentoSolicitacaoOcorrenciaDao getProcedimentoSolicitacaoOcorrenciaDao() {
		return procedimentoSolicitacaoOcorrenciaDao;
	}

	public void setProcedimentoSolicitacaoOcorrenciaDao(ProcedimentoSolicitacaoOcorrenciaDao procedimentoSolicitacaoOcorrenciaDao) {
		this.procedimentoSolicitacaoOcorrenciaDao = procedimentoSolicitacaoOcorrenciaDao;
	}

	public PessoaDocumentoDao getPessoaDocumentoDao() {
		return pessoaDocumentoDao;
	}

	public void setPessoaDocumentoDao(PessoaDocumentoDao pessoaDocumentoDao) {
		this.pessoaDocumentoDao = pessoaDocumentoDao;
	}

	public PessoaDocumento getPessoaDocumento() {
		return pessoaDocumento;
	}

	public void setPessoaDocumento(PessoaDocumento pessoaDocumento) {
		this.pessoaDocumento = pessoaDocumento;
	}

	public EvidenciaEnvolvidoPessoa getEvidenciaEnvolvidoPessoa() {
		return evidenciaEnvolvidoPessoa;
	}

	public void setEvidenciaEnvolvidoPessoa(EvidenciaEnvolvidoPessoa evidenciaEnvolvidoPessoa) {
		this.evidenciaEnvolvidoPessoa = evidenciaEnvolvidoPessoa;
	}

	public PessoaLesao getPessoaLesao() {
		return pessoaLesao;
	}

	public void setPessoaLesao(PessoaLesao pessoaLesao) {
		this.pessoaLesao = pessoaLesao;
	}

	public PessoaLesaoDao getPessoaLesaoDao() {
		return pessoaLesaoDao;
	}

	public void setPessoaLesaoDao(PessoaLesaoDao pessoaLesaoDao) {
		this.pessoaLesaoDao = pessoaLesaoDao;
	}

	public List<PessoaLesao> getListaPessoaLesao() {
		return listaPessoaLesao;
	}

	public void setListaPessoaLesao(List<PessoaLesao> listaPessoaLesao) {
		this.listaPessoaLesao = listaPessoaLesao;
	}

	public SetorDao getSetorDao() {
		return setorDao;
	}

	public void setSetorDao(SetorDao setorDao) {
		this.setorDao = setorDao;
	}

	public SolicitacaoProcedimentoPericialDao getSolicitacaoProcedimentoPericialDao() {
		return solicitacaoProcedimentoPericialDao;
	}

	public void setSolicitacaoProcedimentoPericialDao(SolicitacaoProcedimentoPericialDao solicitacaoProcedimentoPericialDao) {
		this.solicitacaoProcedimentoPericialDao = solicitacaoProcedimentoPericialDao;
	}

	public Integer getContQuesito() {
		return contQuesito;
	}

	public void setContQuesito(Integer contQuesito) {
		this.contQuesito = contQuesito;
	}

	public Integer getContBloco() {
		return contQuesitoBloco;
	}

	public void setContBloco(Integer contBloco) {
		this.contQuesitoBloco = contBloco;
	}

	public Integer getContQuesitoBloco() {
		return contQuesitoBloco;
	}

	public void setContQuesitoBloco(Integer contQuesitoBloco) {
		this.contQuesitoBloco = contQuesitoBloco;
	}
	
	public Bloco getBlocoAnterior() {
		return blocoAnterior;
	}

	public void setBlocoAnterior(Bloco blocoAnterior) {
		this.blocoAnterior = blocoAnterior;
	}
	
	public boolean isPossuiQuesitoExtra() {
		return possuiQuesitoExtra;
	}

	public void setPossuiQuesitoExtra(boolean possuiQuesitoExtra) {
		this.possuiQuesitoExtra = possuiQuesitoExtra;
	}

	public String getDescricaoQuesitoExtra() {
		return descricaoQuesitoExtra;
	}

	public void setDescricaoQuesitoExtra(String descricaoQuesitoExtra) {
		this.descricaoQuesitoExtra = descricaoQuesitoExtra;
	}

	public BlocoDao getBlocoDao() {
		return blocoDao;
	}

	public void setBlocoDao(BlocoDao blocoDao) {
		this.blocoDao = blocoDao;
	}

	public Quesito getQuesitoExtra() {
		return quesitoExtra;
	}

	public void setQuesitoExtra(Quesito quesitoExtra) {
		this.quesitoExtra = quesitoExtra;
	}

	public boolean isInserindoQuesitoExtra() {
		return inserindoQuesitoExtra;
	}

	public void setInserindoQuesitoExtra(boolean inserindoQuesitoExtra) {
		this.inserindoQuesitoExtra = inserindoQuesitoExtra;
	}

	public String getOrdemQuesitoExtra() {
		return ordemQuesitoExtra;
	}

	public void setOrdemQuesitoExtra(String ordemQuesitoExtra) {
		this.ordemQuesitoExtra = ordemQuesitoExtra;
	}

	public boolean isPossuiEsquemaCorporal() {
		return possuiEsquemaCorporal;
	}

	public void setPossuiEsquemaCorporal(boolean possuiEsquemaCorporal) {
		this.possuiEsquemaCorporal = possuiEsquemaCorporal;
	}

	public DeclaracaoConsentimentoDao getDeclaracaoConsentimentoDao() {
		return declaracaoConsentimentoDao;
	}

	public void setDeclaracaoConsentimentoDao(DeclaracaoConsentimentoDao declaracaoConsentimentoDao) {
		this.declaracaoConsentimentoDao = declaracaoConsentimentoDao;
	}

	public DeclaracaoConsentimentoArquivoDao getDeclaracaoConsentimentoArquivoDao() {
		return declaracaoConsentimentoArquivoDao;
	}

	public void setDeclaracaoConsentimentoArquivoDao(DeclaracaoConsentimentoArquivoDao declaracaoConsentimentoArquivoDao) {
		this.declaracaoConsentimentoArquivoDao = declaracaoConsentimentoArquivoDao;
	}

	public boolean isDesejaVerTutorialLaudoTorturaIstambul() {
		return desejaVerTutorialLaudoTorturaIstambul;
	}

	public void setDesejaVerTutorialLaudoTorturaIstambul(boolean desejaVerTutorialLaudoTorturaIstambul) {
		this.desejaVerTutorialLaudoTorturaIstambul = desejaVerTutorialLaudoTorturaIstambul;
	}

	
}