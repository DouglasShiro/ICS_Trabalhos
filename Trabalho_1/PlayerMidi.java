//package player;


/*
*				Universidade de Brasilia
*
*	Trabalho_1 Introdução à Computação Sônica 2/2015
*	--------------------------------------------------
*
*	Caio Freitas Moura			13/0007021
*	Douglas Shiro Yokoyama		13/0024902
*
*	--------------------------------------------------
*	Player Midi - Nome_Player
*
*	1. Implementcao de um tocador de arquivos MIDI:
*
*   	Conteúdo mínimo de controles (na interface gráfica):
*    	- seletor de arquivos (.mid ou .midi);
*		- tocar, pausar, parar;
*		- posicionamento do instante inicial;
*    	- volume;
*		- visualização do conteúdo midi.
*
*
*	2. Desdobramento da interface gráfica que opera o tocador:
*
*    	- Implementacao da exibição dos parâmetros da (partitura da) 
*		  música que está sendo (ou está para ser) tocada:
*
*    	- Parâmetros de partitura:
*   		-> clave, fórmula de compasso, metro, andamento;
*    		-> armadura de tonalidade;
*    		-> indicação de tempo real em [hh mm ss].
*/

import java.text.DecimalFormat;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JSlider;
import javax.swing.JProgressBar;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.UIManager;
import javax.swing.Box;
import javax.swing.JTextArea;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Font;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.midi.*;

/**/
public class PlayerMidi extends JFrame implements Runnable
{
	//private static final long serialVersionUID = -1452732911835371330L;

	/*** Bloco de declaracao de Variaveis para o programa ***/
	
	//Tamanho da aplicacao e posicao onde ira aparecer na tela 
	private int largura = 590;
	private int altura 	= 480;

	private int posx	= 400;
	private int posy	= 100;

    //private ImageIcon logo;

    // Nome diretorio para a aplicacao
	private String diretorio	= System.getProperty("user.dir");

	//



	// Botoes e legendas dos botoes para abrir aquivo, tocar midi, pausar e parar execucao
	final JButton botaoABRIR   	= criaBotao("Abrir", 12);
	final JButton botaoTOCAR  	= criaBotao("\u25b6", 12);
	final JButton botaoPAUSAR 	= criaBotao("\u25ae\u25ae", 12);
	final JButton botaoPARAR  	= criaBotao("\u25fc", 12);

	// Botoes de informacao
 	final JButton botaoCAMINHO  = criaBotao(" DIR: "+ diretorio, 12); 
  	final JButton botaoARQUIVO  = criaBotao(" Arquivo: ", 12); 
  	final JButton botaoDURACAO  = criaBotao(" Dura\u00e7\u00e3o: ", 12); 
  	final JButton botaoINSTANTE = criaBotao("             ", 12);  
  	final JButton botaoVOLUME 	= criaBotao("", 12); 

  	final JButton botaoARMADURA  = criaBotao("Ton: ", 12);
 	final JButton botaoCOMPASSO  = criaBotao("Compasso: ", 12);
 	final JButton botaoANDAMENTO  = criaBotao("Andamento: ", 12);
  	

  	// Configuracoes player Midi
  	private Sequencer	sequenciador	= null;
  	private Sequence	sequencia;
  	private Receiver	receptor		= null;
  	private long		inicio 			= 0;

  	// Controle Volume, valor atual, barra de volume 
  	private int 	volumeATUAL			= 75;
    private int     andamentoATUAL      = 50;
  	private JSlider	barraVOLUME			= new JSlider(JSlider.HORIZONTAL, 0, 127, volumeATUAL);
  	private JSlider	barraANDAMENTO		= new JSlider(JSlider.HORIZONTAL, 0, 250, andamentoATUAL);

  	// Barra de Progresso da sequencia Midi
  	private JProgressBar barraPROGRESSO = new JProgressBar();

  	//
  	private Container painel = getContentPane();

  	// execucao sequencia Midi
  	private boolean   executando	= false;
  	static private int alterado		= -1;

  	// 
    static final int MENSAGEM_ANDAMENTO = 0x51;  
    static final int MENSAGEM_TEXTO = 0x01;  
 	static final int FORMULA_DE_COMPASSO = 0x58;	
 	static final int MENSAGEM_TOM = 0X59;
 	static final int formula_de_compasso = 0x58;

    //Variaveis para a formula do compasso
    private int denominadorCOMPASSO;
    private int numeradorCOMPASSO;

    //
   

    //
    private Track trilha;

 	/*** Fim do bloco de Variaveis ***/

    // Retorno para o metodo do getCompasso 
   	static private class ParCompasso
	{
		int x, y;

		ParCompasso(int x_, int y_)
		{ 
			this.x = x_;
        	this.y = y_;          
      	}

	    int getX()
	    { 
	    	return x;
	    }
	     
	    int getY()
	   	{ 
	   		return y;
     	}
	}

  	/*Metodo main
	*
  	*/
	public static void main(String[] args)
	{
		PlayerMidi tocador = new PlayerMidi();
		Thread		thread  = new Thread(tocador);
		thread.start();
	}

	/*
	*
	*/
	public PlayerMidi()
	{
		super("PlayerMidi");
		gerenciadorInterface();      

        //logo = new ImageIcon(getClass().getResource("icon.png"));

        Color corBotao	= new Color(102, 255, 102);
        Color corInfo	= new Color(153, 204, 255);

        botaoABRIR.setBackground(corBotao);
        botaoTOCAR.setBackground(corBotao);
        botaoPAUSAR.setBackground(corBotao);
        botaoPARAR.setBackground(corBotao);

    	botaoCAMINHO.setBackground(corInfo);
		botaoARQUIVO.setBackground(corInfo);
		botaoDURACAO.setBackground(corInfo);
		botaoINSTANTE.setBackground(corInfo);
		botaoVOLUME.setBackground(corInfo);
		botaoARMADURA.setBackground(corInfo);
		botaoCOMPASSO.setBackground(corInfo);
		botaoANDAMENTO.setBackground(corInfo);

        //Apenas Botao para selecao de arquivo inicia ativo
        botaoABRIR.setEnabled(true);
        botaoTOCAR.setEnabled(false);
        botaoPAUSAR.setEnabled(false);
        botaoPARAR.setEnabled(false);

        try
        {
            JPanel p1 = new JPanel();
            JPanel p2 = new JPanel();
            JPanel p3 = new JPanel();
            JPanel p4 = new JPanel();
            JPanel p5 = new JPanel();
            JPanel p6 = new JPanel();
            JOptionPane.showMessageDialog(frame,
                        "Eggs are not supposed to be green.");
            painel.setLayout(new GridLayout(6,0));
        	
            /*** Acoes dos botoes ***/
        	botaoABRIR.addActionListener(new ActionListener()
        		{
        			public void actionPerformed(ActionEvent e)
        			{
        				abrir();
        			}	
        		}
        	);

        	botaoTOCAR.addActionListener(new ActionListener()
        		{
        			public void actionPerformed(ActionEvent e)
        			{
        				tocar(botaoCAMINHO.getText(),inicio);
        			}	
        		}
        	);

        	botaoPAUSAR.addActionListener(new ActionListener()
        		{
        			public void actionPerformed(ActionEvent e)
        			{
        				inicio = sequenciador.getMicrosecondPosition();
                        pausar();
        			}	
        		}
        	);

        	botaoPARAR.addActionListener(new ActionListener()
        		{
        			public void actionPerformed(ActionEvent e)
        			{
        				parar();
        			}	
        		}
        	);

        	botaoVOLUME.setText(""+(volumeATUAL*100)/127);

            barraVOLUME.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        JSlider source = (JSlider)e.getSource();
                        if(!source.getValueIsAdjusting())
                        {
                            int valor = (int)source.getValue();

                            ShortMessage mensagemVOLUME = new ShortMessage();
                            for(int i=0; i<16; i++)
                            {
                                try 
                                { 
                                    mensagemVOLUME.setMessage(ShortMessage.CONTROL_CHANGE, i, 7, valor);
                                    receptor.send(mensagemVOLUME, -1);
                                }
                                catch (InvalidMidiDataException e1) {}
                            }
                            volumeATUAL = valor;
                            botaoVOLUME.setText("" + (volumeATUAL*100)/127);
                        }
                    }
                }
            );
            
            barraANDAMENTO.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent e)
                {
                    JSlider source = (JSlider)e.getSource();
                    if(!source.getValueIsAdjusting())
                    {
                        int valor = (int)source.getValue();
                        sequenciador.stop();
                        for(int i=0; i<16; i++)
                        {
                        	botaoANDAMENTO.setText("Andamento: "+(valor*250)/127+"; ");
                            alterado = valor;
                        }
                        inicio = sequenciador.getMicrosecondPosition();
                        tocar(botaoCAMINHO.getText(),inicio);
                    }
                }
            }
        );
			
        	// Setar cores

            barraANDAMENTO.setPreferredSize(new Dimension(150,20));
            barraANDAMENTO.setFocusable(false);

            barraPROGRESSO.setPreferredSize(new Dimension(200,20));
            barraPROGRESSO.setFocusable(false);

            JLabel vol = new JLabel("Volume:");
            barraVOLUME.setPreferredSize(new Dimension(150,20));
            barraVOLUME.setFocusable(false);
          
          	//JLabel and = new JLabel("Andamento:");
          	//barraANDAMENTO.setPreferredSize(new Dimension(50,20));
          	//barraANDAMENTO.setFocusable(false);

            p1.add(botaoCAMINHO);
            
            p2.add(botaoTOCAR);
            p2.add(botaoPAUSAR);
            p2.add(botaoPARAR);                            
            p2.add(botaoARQUIVO);  
            p2.add(botaoABRIR);

            p3.add(botaoDURACAO);                                         
            p3.add(barraPROGRESSO);
            p3.add(botaoINSTANTE); 

            p4.add(botaoANDAMENTO);
            //p4.add(areaINFORMACAO);

			p5.add(botaoARMADURA);
			p5.add(botaoCOMPASSO);
			p5.add(botaoANDAMENTO);
			p5.add(barraANDAMENTO);

            p6.add(vol); 
            p6.add(barraVOLUME);
            p6.add(botaoVOLUME); 

            //p6.add(and);
            //p6.add(barraANDAMENTO);
            //p6.add(botaoANDAMENTO);

            painel.add(p1);                                        
            painel.add(p2);                     
            painel.add(p3);                     
            painel.add(p4);                     
            painel.add(p5);                     
            painel.add(p6); 
    	
    		setSize(largura, altura);
    		setLocation(posx, posy);
    		setDefaultCloseOperation(EXIT_ON_CLOSE);
    		setVisible(true);
   		
        }
    	catch(Exception e)
    	{
    		System.out.println(e.getMessage());
    	}	
    }
	
	/*Metodo: JButton criaBotao(String legenda, float tamanhoFonte)
	* 
	*/
	public JButton criaBotao(String legenda, float tamanhoFonte)
	{
		JButton botao = new JButton(legenda);
		botao.setMargin(new Insets(2,2,2,2));
		botao.setFocusable(false);
		botao.setFont(botao.getFont().deriveFont(Font.PLAIN));
		botao.setFont(botao.getFont().deriveFont(tamanhoFonte));
		return botao;
	}

	/*Metodo: retardo
	* Gera uma pausa em milisegundos
	*/
	void retardo(int miliseg)
	{
		try
		{
			Thread.sleep(miliseg);
		}
		catch(InterruptedException e)
		{	}	

	}

	/*Metodo: void tocar(String caminho, long inicio
	* função de ativacao do botao tocar
	*/
	public void tocar(String caminho, long inicio)
	{
		try
		{
			File arquivoMidi	= new File(caminho);
			sequencia		    = MidiSystem.getSequence(arquivoMidi);
			sequenciador		= MidiSystem.getSequencer();
			
			Track trilha[] = sequencia.getTracks();
            ParCompasso p = null;
            p = getFormulaCompasso(trilha[0]);
            botaoCOMPASSO.setText("Compasso: "+p.getX()+"/"+p.getY());
            
            String a = armadura(trilha[0]);
            botaoARMADURA.setText("Ton: "+a);

	        int bpm = -1;
	        String an = null;
	        if(alterado==-1){
	            bpm = (int)getAndamento(trilha[0]);
            }else{
            	bpm = alterado;
            	setAndamento(sequencia, bpm);
            }
            if(bpm<0)
			{
				an = "0";
			}
            else if(bpm>0 && bpm<=19)
            {
            	an = "Larghissimo";
            }
            else if(bpm>19 && bpm<=40)
            {
            	an = "Grave";
            }
            else if(bpm>40 && bpm<=45)
            {
            	an = "Lento";
            }
            else if(bpm>45 && bpm<=50)
            {
            	an = "Largo";
            }
            else if(bpm>50 && bpm<=55)
            {
            	an = "Larghetto";
            }
            else if(bpm>55 && bpm<=65)
            {
            	an = "Adagio";
            }
            else if(bpm>65 && bpm<=69)
            {
            	an = "Adagietto";
            }
            else if(bpm>75 && bpm<=90)
            {
            	an = "Andante";
            }
            else if(bpm>90 && bpm<=100)
            {
            	an = "Andante Moderato";
            }
            else if(bpm>100 && bpm<=112)
            {
            	an = "Moderato";
            }
            else if(bpm>112 && bpm<=116)
            {
            	an = "Allegro Moderatto";
            }
            else if(bpm>116 && bpm<=120)
            {
            	an = "Allegretto";
            }
            else if(bpm>120 && bpm<=160)
            {
            	an = "Allegro";
            }
            else if(bpm>160 && bpm<=168)
            {
            	an = "Vivace";
            }
            else if(bpm>168 && bpm<=180)
            {
            	an = "Vivacissimo";
            }
            else if(bpm>180 && bpm<=200)
            {
            	an = "Presto";
            }
            else if(bpm>200)
            {
            	an = "Prestissimo";
            }
            botaoANDAMENTO.setText("Andamento: \n"+bpm+"bpm"+"; "+an);
			

			sequenciador.setSequence(sequencia);
			sequenciador.open();

			retardo(500);
			sequenciador.start();

			receptor = sequenciador.getTransmitters().iterator().next().getReceiver();
            sequenciador.getTransmitter().setReceiver(receptor);

            botaoARQUIVO.setText("Arquivo: \""+ arquivoMidi.getName() + "\"");

            long duracao = sequencia.getMicrosecondLength()/1000000;

            botaoDURACAO.setText("\nDura\u00e7\u00e3o:"+ formataInstante(duracao));
            botaoINSTANTE.setText(formataInstante(0));

            sequenciador.setMicrosecondPosition(inicio);

            if (sequenciador.isRunning())
            {
            	duracao = sequenciador.getMicrosecondLength();
            	executando = true;
            }
            else
            {
            	executando = false;
            	sequenciador.stop();
            	sequenciador.close();
            	inicio = 0L;
            	duracao = 0;
            }

            botaoABRIR.setEnabled(false);
            botaoTOCAR.setEnabled(false);
            botaoPAUSAR.setEnabled(true);
            botaoPARAR.setEnabled(true);

		}
		catch(MidiUnavailableException e1) { System.out.println(e1+" : Dispositivo Midi nao disponivel.");}
        catch(InvalidMidiDataException e2) { System.out.println(e2+" : Erro nos dados midi."); }
        catch(IOException              e3) { System.out.println(e3+" : O arquivo Midi nao foi encontrado.");   }
        catch(Exception 				e) { System.out.println(e.toString());  } 

	}	

	/*metodo: void pausar()
	* operacao feita com o acionamento do botao pause
	* pausa a sequencia que está sendo executada
	*/
	public void pausar()
	{
		executando = false;
		sequenciador.stop();

		botaoABRIR.setEnabled(false);            
        botaoTOCAR.setEnabled(true);
        botaoPAUSAR.setEnabled(false);
        botaoPARAR.setEnabled(false);  
	}

	/*Metodo: void parar()
	* operacao feita com o acionamento do botao parar
	* para a sequencia que está sendo executada
	*/
	public void parar()
	{
		executando = false;
		sequenciador.stop();
		sequenciador.close();
		sequenciador = null;
		inicio = 0L;

		botaoABRIR.setEnabled(true);
		botaoTOCAR.setEnabled(true);
		botaoPAUSAR.setEnabled(false);
		botaoPARAR.setEnabled(false);

		barraPROGRESSO.setValue(0);
		botaoINSTANTE.setText(formataInstante(0));
	}

	/*Metodo void abrir()
	* Operacao feita com o acionamento do botao abrir
	* abre janela para selecao de arquivo .midi/.mid
	*/
	public void abrir()
	{
		JFileChooser selecionaArquivo = new JFileChooser(".");
		selecionaArquivo.setFileSelectionMode(JFileChooser.FILES_ONLY);              
        selecionaArquivo.setFileFilter(new FileFilter()
        {
            public boolean accept(File f)
            {
                if (!f.isFile()) return true;
                String name = f.getName().toLowerCase();
                if (name.endsWith(".mid"))  return true;
                if (name.endsWith(".midi")) return true;
                return false;
            }

            public String getDescription()
            { 
            	return "Arquivo Midi (*.mid,*.midi)";
            }
        });

        selecionaArquivo.showOpenDialog(this);  

        
        botaoCAMINHO.setText(selecionaArquivo.getSelectedFile().toString());  
        File arqseqnovo = selecionaArquivo.getSelectedFile();
        
        try 
        { 
            if(sequenciador!=null && sequenciador.isRunning()) 
         	{ 
          		sequenciador.stop();
                sequenciador.close();
                sequenciador = null;
             }

            Sequence sequencianova = MidiSystem.getSequence(arqseqnovo);           
            double duracao = sequencianova.getMicrosecondLength()/1000000.0d;
         
            botaoARQUIVO.setText("Arquivo: \"" + arqseqnovo.getName() + "\"");                
            botaoDURACAO.setText("\nDura\u00e7\u00e3o:"+ formataInstante(duracao));                   
          
            botaoTOCAR.setEnabled(true);
            botaoPAUSAR.setEnabled(false);
            botaoPARAR.setEnabled(false);
            botaoARMADURA.setEnabled(true);
            botaoCOMPASSO.setEnabled(true);
            botaoANDAMENTO.setEnabled(true);
        }
        catch (Throwable e1) 
        {
         	System.out.println("Erro ao carregar Arquivo Midi: "+ e1.toString());
        }        

	}


	
    /*Função: getAndamento(Track trilha);
	* Retorna o valor do andamento em bpm
    */
	float getAndamento(Track trilha) throws InvalidMidiDataException
	{
		try{
			for(int i =0; i<trilha.size(); i++)
			{
				MidiMessage m = trilha.get(i).getMessage();
				if(((MetaMessage) m).getType() == MENSAGEM_ANDAMENTO)
				{
					MetaMessage m1 = (MetaMessage)m;
					byte[] data = m1.getData();
	        
					byte primeiro = data[0];
					byte segundo  = data[1];
					byte terceiro = data[2];

					long microseg = (long)(primeiro*Math.pow(2, 16) + segundo *Math.pow(2,  8) + terceiro);
					return (float)(60000000.0/microseg);                                  
				}
			}
		}catch(Throwable e){}  
		return 0;
	}

	 static Sequence setAndamento(Sequence sequencia, int bpm) throws InvalidMidiDataException
	 {
		Track[] trilhas = sequencia.getTracks();
	
		MetaMessage mensagemDeAndamento = new MetaMessage();
		int microssegundos = (int)(60000000.0 / bpm);
		byte dados[] = new byte[3];
		dados[0] = (byte)(microssegundos >>> 16);
		dados[1] = (byte)(microssegundos >>> 8);
		dados[2] = (byte)(microssegundos);
		mensagemDeAndamento.setMessage(0x51, dados, 3);
		for(int i=0; i<trilhas.length; i++)
		{
			trilhas[i].add(new MidiEvent(mensagemDeAndamento, 0));
		}
		return sequencia;
	 }
	
	static ParCompasso getFormulaCompasso (Track trilha)
	{
		int p=1, q=1;
		for(int i =0; i<trilha.size(); i++)
		{
			MidiMessage m = trilha.get(i).getMessage();
			if(m instanceof MetaMessage)
			{
				if (((MetaMessage)m).getType() == formula_de_compasso)
				{
					MetaMessage m1 = (MetaMessage)m;
					byte[] data = m1.getData();
					p = data [0];
					q = data [1];
					return new ParCompasso(p,q);
				}
			}
		}
		return new ParCompasso(p,q);
	}

	static String armadura(Track trilha) throws InvalidMidiDataException
	{
		String s = "       ";
		
			int i = 0;
			for(; i< trilha.size(); i++)
			{
				MidiMessage m = trilha.get(i).getMessage();
				System.out.println(m);
				if(((MetaMessage)m).getType() == MENSAGEM_TOM)
				{
					MetaMessage m1 = (MetaMessage)m;
					byte[] data = m1.getData();
					byte ton = data[0];
					byte maior = data[1];
					
					String modo = "Maior";
					if(maior == 1){
						modo = "Menor";
					}
					
					// Definindo armadura
					if(modo.equals("Maior"))
					{
						switch (ton)
						{
							case -7: s = "Dób Maior"; break;
							case -6: s = "Solb Maior"; break;
							case -5: s = "Réb Maior"; break;
							case -4: s = "Láb Maior"; break;
							case -2: s = "Sib Maior"; break;
							case -1: s = "Fá Maior"; break;
							case  0: s = "Dó Maior"; break;
							case  1: s = "Sol Maior"; break;
							case  2: s = "Ré Maior"; break;
							case  3: s = "Lá Maior"; break;
							case  4: s = "Mi Maior"; break;
							case  5: s = "Si Maior"; break;
							case  6: s = "Fá# Maior"; break;
							case  7: s = "Dó# Maior"; break;
							default: s = "NULL";
						}	
					}
					if(modo.equals("Menor"))
					{
						switch (ton)
						{
							case -7: s = "Láb Menor"; break;
							case -6: s = "Mib Menor"; break;
							case -5: s = "Sib Menor"; break;
							case -4: s = "Fá Menor"; break;
							case -2: s = "Dó Menor"; break;
							case -1: s = "Sol Menor"; break;
							case  0: s = "Ré Menor"; break;
							case  1: s = "Lá Menor"; break;
							case  2: s = "Mi Menor"; break;
							case  3: s = "Si Menor"; break;
							case  4: s = "Fá# Menor"; break;
							case  5: s = "Dó# Menor"; break;
							case  6: s = "Ré# Menor"; break;
							case  7: s = "Lá# Menor"; break;
							default: s = "NULL";
						}	
					}
				}
			}
		return s; 
	}


		static String getTexto(Track trilha) throws InvalidMidiDataException
	{
		String texto = "";
		for(int i =0; i<trilha.size(); i++)
		{
			MidiMessage m = trilha.get(i).getMessage();
			if (((MetaMessage)m).getType() == MENSAGEM_TEXTO)
			{
				MetaMessage m1 = (MetaMessage)m;
				byte[] data = m1.getData();
				
				for(int j=0; j<data.length; j++)
				{
					texto = texto+(char)data[j];
				}
			}
		}
		return texto;
	}

   
	/*Metodo void run()
	*
	*/
	public void run()
	{ 
        double duracao;
        double tempo;
        int    posicao =0;
        
        while(true) 
        {                      
            if (executando)
            { 
            	duracao   = sequenciador.getMicrosecondLength()/1000000;
              	tempo     = sequenciador.getMicrosecondPosition()/1000000;
              	posicao   = (int) ((tempo*100)/duracao);
              	
              	try 
              	{  		
                    barraPROGRESSO.setValue(posicao);								
                    botaoINSTANTE.setText(formataInstante(tempo));     
                    retardo(1000);
                    if(tempo >= duracao) 
                    {  
                		barraPROGRESSO.setValue(0);								
                        botaoINSTANTE.setText(formataInstante(0));   
                              
                        botaoABRIR.setEnabled(true);
                        botaoTOCAR.setEnabled(true);
                        botaoPAUSAR.setEnabled(false);
                        botaoPARAR.setEnabled(false);                        
                   }
                }
                catch(Exception e) 
                { 
                	System.out.println(e.getMessage());  
                }  
            }  
            
            else
            { 
            	try{ retardo(1000);                                          
            }
                catch(Exception e) 
                { 
                	System.out.println(e.getMessage());  
                }
            }                                       
        }
    }

	/*Metodo: String formataInstante(double t1);
	*
	*/
	public String formataInstante(double t1)
	{
        String inicio    = "";

        //--------início
        double h1  = (int)(t1/3600.0);
        double m1  = (int)((t1 - 3600*h1)/60);
        double s1  = (t1 - (3600*h1 +60*m1));


        double h1r  = t1/3600.0;
        double m1r  = (t1 - 3600*h1)/60.0f;
        double s1r  = (t1 - (3600*h1 +60*m1));

        String sh1="";
        String sm1="";
        String ss1="";

        if     (h1 ==0) sh1 = "00";
        else if(h1 <10) sh1 = "0"+reformata(h1, 0);
        else if(h1<100) sh1 = "" +reformata(h1, 0);
        else            sh1 = "" +reformata(h1, 0);

        if     (m1 ==0) sm1 = "00";
        else if(m1 <10) sm1= "0"+reformata(m1, 0);
        else if(m1 <60) sm1 = ""+reformata(m1, 0);

        if     (s1 ==0) ss1 = "00";
        else if(s1 <10) ss1 = "0"+reformata(s1r, 2);
        else if(s1 <60) ss1 = reformata(s1r, 2);

        return inicio = "\n" + "   "+sh1+"h "+       sm1+"m "+    ss1+"s";
	}

	/*Metodo: String reformata(double x, int casas)
	*
	*/
	public String reformata(double x, int casas)
	{ 
		DecimalFormat df = new DecimalFormat() ;
        df.setGroupingUsed(false);
        df.setMaximumFractionDigits(casas);
        return df.format(x);
	}


	/*Metodo: void gerenciadorInterface()
	*
	*/
	private void gerenciadorInterface()
	{
            UIManager.put("FileChooser.openDialogTitleText", "Abrir arquivo midi");
            UIManager.put("FileChooser.lookInLabelText", "Buscar em");
            UIManager.put("FileChooser.openButtonText", "Abrir");
            UIManager.put("FileChooser.cancelButtonText", "Cancelar");
            UIManager.put("FileChooser.fileNameLabelText", "Nome do arquivo");
            UIManager.put("FileChooser.filesOfTypeLabelText", "Tipo");
            UIManager.put("FileChooser.openButtonToolTipText", "Abrir o arquivo selecionado");
            UIManager.put("FileChooser.cancelButtonToolTipText","Cancelar");
            UIManager.put("FileChooser.fileNameHeaderText","Nome");
            UIManager.put("FileChooser.upFolderToolTipText", "Subir um nível");
            UIManager.put("FileChooser.homeFolderToolTipText","Nível home");
            UIManager.put("FileChooser.newFolderToolTipText","Criar pasta");
            UIManager.put("FileChooser.listViewButtonToolTipText","Em lista");
            UIManager.put("FileChooser.newFolderButtonText","Criar pasta");
            UIManager.put("FileChooser.renameFileButtonText", "Mudar o nome");
            UIManager.put("FileChooser.deleteFileButtonText", "Deletar");
            UIManager.put("FileChooser.filterLabelText", "Extensão de arquivo");
            UIManager.put("FileChooser.detailsViewButtonToolTipText", "Com detalhes");
            UIManager.put("FileChooser.fileSizeHeaderText","Tamanho");
            UIManager.put("FileChooser.fileDateHeaderText", "Data de modificação");
            UIManager.put("FileChooser.acceptAllFileFilterText", "Binário");

            UIManager.put("FileChooser.saveButtonText", "Salvar");
            UIManager.put("FileChooser.saveDialogTitleText", "Salvar em");
            UIManager.put("FileChooser.saveInLabelText", "Salvar em");
            UIManager.put("FileChooser.saveButtonToolTipText", "Salvar arquivo selecionado");

            UIManager.put("OptionPane.yesButtonText",    "Sim");
            UIManager.put("OptionPane.noButtonText",     "Não");
            UIManager.put("OptionPane.cancelButtonText", "Cancelar");

            UIManager.put("FileChooser.listFont", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("FileChooser.fileNameLabelText", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("FileChooser.filesOfTypeLabelText", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));

            UIManager.put("JSlider.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("JSlider.listFont", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("JSlider", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("JFileChooser.listFont", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("JFileChooser.fileNameLabelText", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("JFileChooser.filesOfTypeLabelText", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("JButton", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));

            UIManager.put("OptionPane.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("Button.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("RadioButton.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 10)));

            UIManager.put("Label.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("JLabel.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("JLabel", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            
            
            UIManager.put("ComboBox.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("ToolTip.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 10)));
            UIManager.put("EditorPane.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("List.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("Panel.listFont", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("Panel.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("Table.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 10)));
            UIManager.put("TextArea.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 10)));
            UIManager.put("TextField.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 10)));
            UIManager.put("TextPane.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 10)));
            UIManager.put("JTextArea.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 10)));
            UIManager.put("JTextField.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 10)));
            UIManager.put("JTextPane.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 10)));
            UIManager.put("InternalFrame.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("InternalFrame.titleFont",new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("Frame.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("Frame.titleFont",new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));
            UIManager.put("ScrollPane.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 11)));

            UIManager.put("ProgressBar.font", new javax.swing.plaf.FontUIResource(new Font("Arial", java.awt.Font.PLAIN, 10)));
	}

}

