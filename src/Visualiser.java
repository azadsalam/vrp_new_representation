import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.ViewportLayout;
import javax.swing.border.StrokeBorder;


public class Visualiser
{ 
	static ArrayList<Individual> individuals;
	static ArrayList<String> names;
	static public double scale;
    static ProblemInstance problemInstance;
    static double[] dep_x,dep_y,cus_x,cus_y;
    static double center_x,center_y;
    static int[] d_x,d_y,c_x,c_y;

    static public int window_width = 1200;
    static public int drawingBoard_width = 800;
    static public int optionPanel_width = 400;
    static public int height = 650;
    
	String inputFileName=null;
	public Visualiser(String string,ProblemInstance problemInstance) 
	{
		// TODO Auto-generated constructor stub
		inputFileName = string;
			
		individuals = new ArrayList<Individual>();
		names = new ArrayList<String>();
		
		Scanner scanner=null;
		try 
		{
			scanner = new Scanner(new File(inputFileName));
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        this.problemInstance=problemInstance;
        
        dep_x=new double[problemInstance.depotCount];
    	dep_y=new double[problemInstance.depotCount];
    	cus_x=new double[problemInstance.customerCount];
    	cus_y=new double[problemInstance.customerCount];
    	
    	d_x=new int[problemInstance.depotCount];
    	d_y=new int[problemInstance.depotCount];
    	c_x=new int[problemInstance.customerCount];
    	c_y=new int[problemInstance.customerCount];
    	
        for(int i=0;i<problemInstance.depotCount;i++)
        {
        	String line=scanner.nextLine();
        	StringTokenizer tok=new StringTokenizer(line,", \t\n");
        	tok.nextToken();
        	dep_x[i]=Double.parseDouble(tok.nextToken());
        	dep_y[i]=Double.parseDouble(tok.nextToken())*-1;
        }
        for(int i=0;i<problemInstance.customerCount;i++)
        {
        	String line=scanner.nextLine();
        	StringTokenizer tok=new StringTokenizer(line,", \t\n");
        	tok.nextToken();
        	cus_x[i]= Double.parseDouble(tok.nextToken());
        	cus_y[i]= Double.parseDouble(tok.nextToken())*-1;
        }
        double max_x,max_y,min_x,min_y;
        
        max_x=Maximum(dep_x,cus_x);
        min_x=Minimum(dep_x,cus_x);        
        max_y=Maximum(dep_y, cus_y);
        min_y=Minimum(dep_y, cus_y);
        
       // System.out.println("Max x, min x, maxy min y : "+max_x+" "+min_x+" "+max_y+" "+min_y);
             
        double x_factor=((double)drawingBoard_width*0.80/(max_x-min_x));
        double y_factor=((double)height*0.80/(max_y-min_y));
        
        for(int i=0;i<dep_x.length;i++)
        {
        	dep_x[i] += (Math.abs(min_x) + 5);
        	dep_y[i] += (Math.abs(min_y) + 5);	
        }
        
        for(int i=0;i<cus_x.length;i++)
        {
        	cus_x[i] += (Math.abs(min_x) + 5);
        	cus_y[i] += (Math.abs(min_y) + 5);	
        }
        
        max_x=Maximum(cus_x,dep_x);
        min_x=Minimum(cus_x,dep_x);
        max_y=Maximum(dep_y, cus_y);
        min_y=Minimum(cus_y, dep_y);
        
        double scale_x = Visualiser.drawingBoard_width / max_x *0.95 ;
        double scale_y = Visualiser.height / max_y *0.95;
        
        scale = Math.min(scale_x, scale_y);
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                Window sk = new Window();
                sk.setVisible(true);
            }
        });
	}
	
	
	public void initialise() 
	{
		
	}
	private double Minimum(double[] dep_x2, double[] cus_x2) {
		// TODO Auto-generated method stub
		double min=dep_x2[0];
		for(int i=0;i<dep_x2.length;i++){
			if(dep_x2[i]<min) min=dep_x2[i];
		}
		for(int i=0;i<cus_x2.length;i++){
			if(cus_x2[i]<min) min=cus_x2[i];
		}
		
		return min;
	}
	private double Maximum(double[] dep_x2, double[] cus_x2) 
	{
		// TODO Auto-generated method stub
		double min=dep_x2[0];
		for(int i=0;i<dep_x2.length;i++){
			if(dep_x2[i]>min) min=dep_x2[i];
		}
		for(int i=0;i<cus_x2.length;i++){
			if(cus_x2[i]>min) min=cus_x2[i];
		}
		
		return min;
	}
	


	public void drawIndividual(Individual individual,String name) 
	{
		individuals.add(individual);
		names.add(name);
/*
		try
		{
			Window.surface.repaint();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}*/
	}
}

class Window extends JFrame {

	static public Surface surface=null;  
    static public OptionsPanel optionsPanel; 
    static public int selectedIndividual = 0;
    
    public Window() {

        initUI();
    }

    private void initUI() {

        setTitle("VISUALISING MDPVRP");
        setSize(Visualiser.window_width+18, Visualiser.height+40);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //setLocationRelativeTo(this);
        
        Surface surface = new Surface();  
        OptionsPanel optionsPanel = new OptionsPanel(); 
        
        JPanel wholePanel = new JPanel();

        wholePanel.setSize(Visualiser.window_width,Visualiser.height);
        surface.setSize(Visualiser.window_width,Visualiser.height);
        wholePanel.setLayout(new BorderLayout());
        
        surface.setLayout(null);
//        surface.setLocation(new Point(0,0));
        
        //optionsPanel.setLayout(null);
  //      optionsPanel.setLocation(new Point(Visualiser.drawingBoard_width,0));
        
        JScrollPane scrollPane = new JScrollPane(surface);
        scrollPane.setPreferredSize(new Dimension(Visualiser.drawingBoard_width-10,Visualiser.height-20));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
       
        
        wholePanel.add(scrollPane,BorderLayout.WEST);
        
        wholePanel.add(optionsPanel,BorderLayout.EAST);
        
        getContentPane().add(wholePanel);

    }

}

class Surface extends JPanel {

	public Surface() {
		// TODO Auto-generated constructor stub
		//setSize(Visualiser.drawingBoard_width,Visualiser.height);
		//setBorder(BorderFactory.createLineBorder(Color.red));
        setPreferredSize(new Dimension(Visualiser.drawingBoard_width*2,Visualiser.height*2));

		setBackground(Color.WHITE);
	}
    private void doDrawing(Graphics g) 
    {

        if(Visualiser.individuals.size()==0)return;
    	Individual individual = Visualiser.individuals.get(Window.selectedIndividual);
    	int selectedPeriod = 0;
    	
    	ArrayList<Integer> selectedVehicles = new ArrayList<Integer>();
    	for(int i=0;i<individual.problemInstance.vehicleCount;i++)
    		selectedVehicles.add(i);

    	//Scale
        for(int i=0;i<Visualiser.problemInstance.depotCount;i++)
        {
        	double x = Visualiser.dep_x[i]*Visualiser.scale;
        	double y = Visualiser.dep_y[i]*Visualiser.scale;
        	
        	Visualiser.d_x[i]=(int)x;
        	Visualiser.d_y[i]=(int)y;
        }
        for(int i=0;i<Visualiser.problemInstance.customerCount;i++)
        {
        	double x = Visualiser.cus_x[i]*Visualiser.scale;
        	double y = Visualiser.cus_y[i]*Visualiser.scale;

        	Visualiser.c_x[i]=(int)(x);
        	Visualiser.c_y[i]=(int)(y);
        }

        Graphics2D g2d = (Graphics2D) g;
        
        //draw depots
        
        int depotRadius = 8;
        g2d.setColor(Color.GREEN);
        for(int i=0;i<Visualiser.d_x.length;i++)
        {
            g2d.fillOval(Visualiser.d_x[i] - depotRadius, Visualiser.d_y[i] - depotRadius, depotRadius*2, depotRadius*2); 
            g2d.drawString("D"+i, Visualiser.d_x[i]+depotRadius, Visualiser.d_y[i]);
            //System.out.println("Drawing Depot on : "+Visualiser.d_x[i]+" "+Visualiser.d_y[i]);
        }
        
        int clientRadius = 4;
        for(int i=0;i<Visualiser.c_x.length;i++)
        {
        	if(individual.periodAssignment[selectedPeriod][i]==true) g2d.setColor(Color.CYAN);
        	else g2d.setColor(new Color(245,245,240));
            g2d.fillOval(Visualiser.c_x[i] - clientRadius , Visualiser.c_y[i] - clientRadius, clientRadius*2, clientRadius*2);
            

        	if(individual.periodAssignment[selectedPeriod][i]==true)
    		{
        		g2d.setColor(Color.black);
                g2d.drawString(""+i, Visualiser.c_x[i] + clientRadius ,Visualiser.c_y[i]- clientRadius );
    		}
        	else g2d.setColor(Color.lightGray);
        }
        
        for(int i=0;i<selectedVehicles.size();i++)
        {
        	int selectedVehicle = selectedVehicles.get(i);
        	ArrayList<Integer> route = individual.routes.get(selectedPeriod).get(selectedVehicle);
        	
        	g2d.setColor(new Color(15,25,25));
 
        	if(route.size()==0)continue;
        	
        	int depot  = individual.problemInstance.depotAllocation[selectedVehicle];
        	
        	g2d.setStroke(new BasicStroke(3));
        	g2d.drawLine(Visualiser.d_x[depot], Visualiser.d_y[depot], Visualiser.c_x[route.get(0)], Visualiser.c_y[route.get(0)]);
        	for(int clientIndex=1;clientIndex<route.size();clientIndex++)
        	{
        		int prev = route.get(clientIndex-1);
        		int cur = route.get(clientIndex);
        		g2d.drawLine(Visualiser.c_x[prev], Visualiser.c_y[prev], Visualiser.c_x[cur], Visualiser.c_y[cur]);
        	}
        	int last= route.get(route.size()-1);
        	g2d.drawLine(Visualiser.d_x[depot], Visualiser.d_y[depot], Visualiser.c_x[last], Visualiser.c_y[last]);

        }
    
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        doDrawing(g);
    }
}

class OptionsPanel extends JPanel
{

	public OptionsPanel() {
		// TODO Auto-generated constructor stub
		setSize(Visualiser.optionPanel_width,Visualiser.height);
		setPreferredSize(new Dimension( Visualiser.optionPanel_width,Visualiser.height));
		setBackground(Color.DARK_GRAY);

	}
	private static final long serialVersionUID = 5188642461382060738L;
	
}

