



import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/*****************************************
 * First take an object of <b>plotter</b> class<br/>
 * Parameter of constructor <br/> <h2>1->individual whom to visualize</h2> <br/> <h2>2->name of the problem instance;</h2><br/><br/><br/>
 * 
 * After that call the <b>plot()</b> function to visualize;<br/>
 * After any mutation call the method <b>replot(Individual)</b><br/>
 * <h1>Happy Visualization :D</h1>
 */
public class Plotter {
	Individual insTance;
	final Panel p;
	String name="pr10";
	public Plotter(Individual i,String name){
		this.insTance=i;
		this.name=name;
		p=new Panel(insTance,this.name);
	}
	public void plot(){
		try {	
		       
		       JFrame fr=new JFrame();
		       fr.setSize(1000, 700);
		       fr.setLayout(new FlowLayout());
		       
		       p.setPeriodList(new ArrayList<Integer>());
		       fr.add(p);
		       JPanel pane=new JPanel();
		       pane.setPreferredSize(new Dimension(150,700));
		       pane.setBackground(Color.LIGHT_GRAY);
		       pane.setLayout(new FlowLayout(FlowLayout.CENTER));
		       final Vector<JCheckBox>checkBoxList=new Vector<JCheckBox>(); 
		       for(int a=0;a<insTance.problemInstance.periodCount;a++){
		    	   JCheckBox box1=new JCheckBox();
		    	   checkBoxList.add(a,box1);
		    	   JLabel l=new JLabel("Period number : "+a);
		    	   pane.add(box1);
		    	   pane.add(l);
		       }
		       JButton btn=new JButton("Refresh");
		       btn.setPreferredSize(new Dimension(130,20));
		       btn.addActionListener(new ActionListener() {
				

				public void actionPerformed(ActionEvent arg0) {
					ArrayList<Integer>list=new ArrayList<Integer>();
					for(int a=0;a<checkBoxList.size();a++){
						JCheckBox ch=checkBoxList.get(a);
						if(ch.isSelected()){
							list.add(a);
						}
					}
					p.setPeriodList(list);
					p.repaint();
					
				}
			});
		       pane.add(btn);
		       
		       
		       final Vector<JCheckBox>VehicleBoxList=new Vector<JCheckBox>(); 
		       for(int a=0;a<insTance.problemInstance.vehicleCount;a++){
		    	   JCheckBox box1=new JCheckBox();
		    	   VehicleBoxList.add(a,box1);
		    	   JLabel l=new JLabel("Vehicle Number : "+a);
		    	   pane.add(box1);
		    	   pane.add(l);
		       }
		       JButton btn2=new JButton("Refresh");
		       btn2.setPreferredSize(new Dimension(130,20));
		       btn2.addActionListener(new ActionListener() {
				
				
				public void actionPerformed(ActionEvent arg0) {
					ArrayList<Integer>list=new ArrayList<Integer>();
					for(int a=0;a<VehicleBoxList.size();a++){
						JCheckBox ch=VehicleBoxList.get(a);
						if(ch.isSelected()){
							list.add(a);
						}
					}
					p.setVecleList(list);
					p.repaint();
					
				}
			});
		       pane.add(btn2);
		       JScrollPane scroll=new JScrollPane(pane);
		       
		       scroll.setPreferredSize(new Dimension(160,650));
		       
		       fr.add(scroll);
		       fr.setVisible(true);
		       fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		       
		       
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	public static void main(String[] args) {
       Individual instance;
		try {
			String n="pr10";
			Scanner scan=new Scanner(new File("MDPVRP/"+n+".txt"));
			scan.nextLine();
			instance = new Individual(new ProblemInstance(scan, null));
			instance.initialise();
			Plotter ploter=new Plotter(instance,n);
			ploter.plot();
			for(int i=0;i<10;i++){
			Thread.sleep(10000);
			ploter.applyMutation(instance);
			ploter.rePlot(instance);
			}
		}catch(Exception e){
			
		}
        
    }
	
	public void rePlot(Individual in){
		p.setInstance(in);
		p.repaint();
	}
	
/*	public static void Plot(Individual instance){
		try {	
	       
	       JFrame fr=new JFrame();
	       fr.setSize(1000, 700);
	       fr.setLayout(new FlowLayout());
	       final Panel p=new Panel(instance);
	       p.setPeriodList(new ArrayList<Integer>());
	       fr.add(p);
	       JPanel pane=new JPanel();
	       pane.setPreferredSize(new Dimension(150,700));
	       pane.setBackground(Color.LIGHT_GRAY);
	       pane.setLayout(new FlowLayout(FlowLayout.CENTER));
	       final Vector<JCheckBox>checkBoxList=new Vector<JCheckBox>(); 
	       for(int a=0;a<instance.problemInstance.periodCount;a++){
	    	   JCheckBox box1=new JCheckBox();
	    	   checkBoxList.add(a,box1);
	    	   JLabel l=new JLabel("Period number : "+a);
	    	   pane.add(box1);
	    	   pane.add(l);
	       }
	       JButton btn=new JButton("Refresh");
	       btn.setPreferredSize(new Dimension(130,50));
	       btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ArrayList<Integer>list=new ArrayList<Integer>();
				for(int a=0;a<checkBoxList.size();a++){
					JCheckBox ch=checkBoxList.get(a);
					if(ch.isSelected()){
						list.add(a);
					}
				}
				p.setPeriodList(list);
				p.repaint();
				
			}
		});
	       pane.add(btn);
	       
	       
	       final Vector<JCheckBox>VehicleBoxList=new Vector<JCheckBox>(); 
	       for(int a=0;a<instance.problemInstance.vehicleCount;a++){
	    	   JCheckBox box1=new JCheckBox();
	    	   VehicleBoxList.add(a,box1);
	    	   JLabel l=new JLabel("Vehicle Number : "+a);
	    	   pane.add(box1);
	    	   pane.add(l);
	       }
	       JButton btn2=new JButton("Refresh");
	       btn2.setPreferredSize(new Dimension(130,50));
	       btn2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ArrayList<Integer>list=new ArrayList<Integer>();
				for(int a=0;a<VehicleBoxList.size();a++){
					JCheckBox ch=VehicleBoxList.get(a);
					if(ch.isSelected()){
						list.add(a);
					}
				}
				p.setVecleList(list);
				p.repaint();
				
			}
		});
	       pane.add(btn2);
	       
	       fr.add(pane);
	       fr.setVisible(true);
	       fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	       
	       
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/
	private  void applyMutation(Individual instance) {
		// TODO Auto-generated method stub
		double r=Math.random();
		if(r<0.25){
			instance.mutatePeriodAssignment();
		}
		else  if(r>=0.25 && r<0.50){
			instance.mutatePermutationBySwappingAnyTwo();
		}
		else if(r<0.75){
			instance.mutatePermutationWithAdjacentSwap();
		}
		else{
			instance.mutateRoutePartition();
		}
	}
}
