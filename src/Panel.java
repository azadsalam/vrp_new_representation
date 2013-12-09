import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JPanel;

/**
 *
 * @author define
 */
@SuppressWarnings("serial")
public class Panel extends JPanel{
    Individual instance;;
    String fileName=null;
    ProblemInstance problem;
    Random r=new Random();
    double[] dep_x,dep_y,cus_x,cus_y;
    int[] d_x,d_y,c_x,c_y;
    ArrayList<Integer>activePeriod;
    ArrayList<Integer>vehicleList;
    public Panel(Individual f,String fileNname){
    	try {
    		activePeriod=new ArrayList<>();
    		vehicleList=new ArrayList<>();
	    	this.setPreferredSize(new Dimension(800,700));
	        this.instance=f;
	        fileName="original/"+fileNname;        
	        
			Scanner scan=new Scanner(new File(fileName));
		
	        problem=instance.problemInstance;
	        
	        dep_x=new double[problem.depotCount];
	    	dep_y=new double[problem.depotCount];
	    	cus_x=new double[problem.customerCount];
	    	cus_y=new double[problem.customerCount];
	    	
	    	d_x=new int[problem.depotCount];
	    	d_y=new int[problem.depotCount];
	    	c_x=new int[problem.customerCount];
	    	c_y=new int[problem.customerCount];
	        for(int i=0;i<problem.depotCount;i++){
	        	String line=scan.nextLine();
	        	StringTokenizer tok=new StringTokenizer(line,", \t\n");
	        	tok.nextToken();
	        	dep_x[i]=Double.parseDouble(tok.nextToken());
	        	dep_y[i]=Double.parseDouble(tok.nextToken());
	        }
	        for(int i=0;i<problem.customerCount;i++){
	        	String line=scan.nextLine();
	        	StringTokenizer tok=new StringTokenizer(line,", \t\n");
	        	tok.nextToken();
	        	cus_x[i]=Double.parseDouble(tok.nextToken());
	        	cus_y[i]=Double.parseDouble(tok.nextToken());
	        }
	        double max_x,max_y,min_x,min_y;
	        max_x=Maximum(dep_x,cus_x);
	        min_x=Minimum(dep_x,cus_x);
	        max_y=Maximum(dep_y, cus_y);
	        min_y=Minimum(dep_y, cus_y);
	        
	        double x_factor=(max_x-min_x)/650;
	        double y_factor=(max_y-min_y)/600;
	        System.out.println(x_factor+" "+y_factor);
	        for(int i=0;i<problem.depotCount;i++){
	        	d_x[i]=(int)((dep_x[i]-min_x)/x_factor)+15;
	        	d_y[i]=(int)((dep_y[i]-min_y)/y_factor)+15;
	        }
	        for(int i=0;i<problem.customerCount;i++){
	        	c_x[i]=(int)((cus_x[i]-min_x)/x_factor)+15;
	        	c_y[i]=(int)((cus_y[i]-min_y)/y_factor)+15;
	        }
	        System.out.println(c_x.length+" "+d_x.length);
	        for(int i=0;i<problem.customerCount;i++){
	        	System.out.println(c_x[i]+" "+c_y[i]);
	        }
	        //repaint();
    	} catch (Exception e) {
			// TODO Auto-generated catch block
    		System.out.println("hello!!!");
			e.printStackTrace();
		}
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
	public void setVecleList(ArrayList<Integer>vList){
		this.vehicleList=vList;
	}
	public void setInstance(Individual in){
		this.instance=in;
	}
	public void setPeriodList(ArrayList<Integer>list){
		this.activePeriod=list;
	}
	@Override
	
    public void paint(Graphics g){
		super.paint(g);
        //g.drawString("hello", 100, 100);
        g.setColor(Color.red);
        
        for(int i=0;i<problem.depotCount;i++){
            int x=d_x[i];
            int y=d_y[i];
            g.fillRect(x, y, 5, 5);
        }
        
        for(int i=0;i<problem.customerCount;i++){
            int x=c_x[i];
            int y=c_y[i];
            g.setColor(Color.BLACK);
            //g.drawString(String.valueOf(i), x, y);
            g.setColor(Color.BLACK);
            g.fillRect(x, y, 3, 3);
        }
        
        Iterator<Integer>periodIt=activePeriod.iterator();
        while(periodIt.hasNext()){
        	int period=periodIt.next();
        	//instance;
        	boolean[]periodAssignment=instance.periodAssignment[period];
        	/*for(int a=0;a<periodAssignment.length;a++){
        		System.out.print(periodAssignment[a]+"\t");
        	}
        	System.out.println();*/
        	int []permutation=instance.permutation[period];
        	/*for(int a=0;a<permutation.length;a++){
        		System.out.print(permutation[a]+"\t");
        	}
        	System.out.println("\nCut");*/
        	int []cut=instance.routePartition[period];
        	/*for(int a=0;a<cut.length;a++){
        		System.out.print(cut[a]+"\t");
        	}
        	System.out.println();*/
        	int previous_max=-1;
        	Vector<Integer>sequence=new Vector<Integer>();
        	for(int i=0;i<cut.length;i++){
        		if(!vehicleList.contains(i))continue;
        		sequence.clear();
        		int current_max=cut[i];
        		sequence.add(problem.depotAllocation[i]);
        		
        		for(int j=previous_max+1;j<=current_max;j++){
        			if(periodAssignment[permutation[j]]==true){
        				sequence.add(permutation[j]);
        			}
        		}
        		previous_max=current_max;
        		for(int a=0;a<sequence.size();a++){
        			
                		System.out.print(sequence.get(a)+"\t");
                	
                	
        		}
        		System.out.println();
        		g.setColor(new Color(r.nextInt(256),r.nextInt(256),r.nextInt(256)));
        		if(sequence.size()>1){
        			int sz=sequence.size();
        			g.drawLine(d_x[sequence.get(0)], d_y[sequence.get(0)], c_x[sequence.get(1)],c_y[sequence.get(1)]);
        			for(int k=1;k<sz-1;k++){
            			g.drawLine(c_x[sequence.get(k)], c_y[sequence.get(k)], c_x[sequence.get(k+1)],c_y[sequence.get(k+1)]);       				
        			}
        			g.drawLine(c_x[sequence.get(sz-1)], c_y[sequence.get(sz-1)], d_x[sequence.get(0)],d_y[sequence.get(0)]);
        			
        		}
        	}
        }
    }

}
