import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class Main 
{
	
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException 
	{
	//	System.out.println("Saikat!");
		
		final Solver solver = new Solver();
		solver.initialise();
		Thread thread1=new Thread() {			
			@Override
			public void run() {
				solver.solve();	
				
			}
		};
		//Thread thread2=new visualize();
		
		thread1.start();
		//thread2.start();
		
		
	}

}
