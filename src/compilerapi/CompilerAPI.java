/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilerapi;

import java.io.File;
import tachyon.process.ProcessItem;

/**
 *
 * @author Aniket
 */
public class CompilerAPI {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //For concurrent operations, wrap the following code in a new thread
        ProcessItem ite;
        JavaFileManager.getIsolatedJavaFileManager().runIndividualFile(ite = new ProcessItem(null, null), new File("C:\\Users\\Aniket\\Documents\\TachyonProjects\\JavaProject1\\src\\com\\what\\HelloWorld.java"));
        System.out.println(ite.isCancelled());
        System.out.println(ite.getExitValue());
        System.out.println(ite.getErrorMessage());
        System.out.println(ite.getOutput());
        System.out.println(ite.getErrorOutput());
        
        //End of new Thread
    }

}
