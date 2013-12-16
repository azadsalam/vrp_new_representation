import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

class Surface extends JPanel {

	public Surface() {
		// TODO Auto-generated constructor stub
		setBackground(Color.GRAY);
	}
    private void doDrawing(Graphics g) {

    	
        Graphics2D g2d = (Graphics2D) g;
        
        //draw circle at center
        g2d.setColor(Color.BLUE);
        g2d.fillOval(Visualiser.width/2, Visualiser.height/2, 25, 25);   
        
        //draw depots
        g2d.setColor(new Color(231, 21, 1));
        for(int i=0;i<Visualiser.d_x.length;i++)
        {
            g2d.fillOval(Visualiser.d_x[i], Visualiser.d_y[i], 15, 15);   
        }
        
        
        g2d.setColor(new Color(131, 21, 131));
        for(int i=0;i<Visualiser.c_x.length;i++)
        {
            g2d.fillOval(Visualiser.c_x[i], Visualiser.c_y[i], 5, 5);   
        }
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        doDrawing(g);
    }
}

class Window extends JFrame {

    public Window() {

        initUI();
    }

    private void initUI() {

        setTitle("VISUALISING MDPVRP");
        setSize(Visualiser.width, Visualiser.height);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setLocationRelativeTo(this);
        add(new Surface());

    }


}
public class Visualiser
{
	
    ProblemInstance problemInstance;
    double[] dep_x,dep_y,cus_x,cus_y;
    static double center_x,center_y;
    static int[] d_x,d_y,c_x,c_y;

    static public int width = 600;
    static public int height = 600;
	String inputFileName=null;
	public Visualiser(String string,ProblemInstance problemInstance) {
		// TODO Auto-generated constructor stub
		inputFileName = string;
		
		
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
        	dep_y[i]=Double.parseDouble(tok.nextToken());
        }
        for(int i=0;i<problemInstance.customerCount;i++)
        {
        	String line=scanner.nextLine();
        	StringTokenizer tok=new StringTokenizer(line,", \t\n");
        	tok.nextToken();
        	cus_x[i]= Double.parseDouble(tok.nextToken());
        	cus_y[i]= Double.parseDouble(tok.nextToken());
        }
        double max_x,max_y,min_x,min_y;

        max_x=Maximum(dep_x,cus_x);
        min_x=Minimum(dep_x,cus_x);
        max_y=Maximum(dep_y, cus_y);
        min_y=Minimum(dep_y, cus_y);
        
     
        center_x = (min_x+max_x)/2;
        center_y = (min_y+max_y)/2;
        
        double x_factor=((double)width*0.90/(max_x-min_x));
        double y_factor=((double)height*0.90/(max_y-min_y));
        
        if(x_factor<y_factor) y_factor = x_factor;
        else x_factor = y_factor;
        System.out.println(x_factor+" "+y_factor);
        

     //   x_factor=10;
       // y_factor = 10;
      

        for(int i=0;i<problemInstance.depotCount;i++)
        {
        	double x = dep_x[i]*x_factor;
        	double y = dep_y[i]*y_factor;

        	x += width/2;
        	y += height/2;
        	
        	x += center_x;
        	y += center_y;
        	
        	x *= 0.8;
        	y *= 0.8;
        	
        	d_x[i]=(int)x;
        	d_y[i]=(int)y;
        }
        for(int i=0;i<problemInstance.customerCount;i++)
        {
        	double x = cus_x[i]*x_factor;
        	double y = cus_y[i]*y_factor;

        	x += width/2;
        	y += height/2;
        	
        	x += center_x;
        	y += center_y;
        	
        	x*= 0.8;
        	y *= 0.8;
        	
        	c_x[i]=(int)(x);
        	c_y[i]=(int)(y);
        }
        
        
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
	private double Maximum(double[] dep_x2, double[] cus_x2) {
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
	
}
