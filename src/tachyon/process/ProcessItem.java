/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tachyon.process;

import compilerapi.JavaFileManager.ErrorReader;
import compilerapi.JavaFileManager.OutputReader;
import java.io.IOException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Aniket
 */
public class ProcessItem {

    private final BooleanProperty isCancelled;
    private final ObjectProperty<String> nameProperty;
    private final ObjectProperty<Process> processProperty;
    private int exitValue;
    private String errorMessage;
    private OutputReader reader;
    private ErrorReader error;

    public ProcessItem(String name, Process proc) {
        nameProperty = new SimpleObjectProperty<>(name);
        processProperty = new SimpleObjectProperty<>(proc);
        isCancelled = new SimpleBooleanProperty(false);
    }
    
    public BooleanProperty isCancelledProperty() {
        return isCancelled;
    }

    public ObjectProperty<Process> processProperty() {
        return processProperty;
    }

    public ObjectProperty<String> nameProperty() {
        return nameProperty;
    }

    public String getName() {
        return nameProperty.get();
    }

    public Process getProcess() {
        return processProperty.get();
    }

    public void setProcess(Process con) {
        processProperty.set(con);
    }

    public void setName(String str) {
        nameProperty.set(str);
    }

    private void endProcess(Process p) {
        try {
            p.getOutputStream().close();
        } catch (IOException ex) {
        }
        if (p.isAlive()) {
            p.destroyForcibly();
        }
    }

    public boolean isCancelled() {
        return isCancelled.get();
    }

    public void cancel() {
        isCancelled.set(true);
        endProcess(getProcess());
        processProperty().addListener((ob, older, newer) -> {
            if (newer != null) {
                endProcess(newer);
            }
        });
    }

    public int getExitValue() {
        return exitValue;
    }

    public void setExitValue(int exitValue) {
        this.exitValue = exitValue;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setReader(OutputReader reader) {
        this.reader = reader;
    }

    public void setError(ErrorReader error) {
        this.error = error;
    }
    
    public String getOutput() {
        return reader.get();
    }
    
    public String getErrorOutput() {
        return error.get();
    }

}
