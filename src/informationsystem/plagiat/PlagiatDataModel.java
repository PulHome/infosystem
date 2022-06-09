package informationsystem.plagiat;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class PlagiatDataModel {

    private SimpleStringProperty taskName;
    private SimpleListProperty<Double> listOfResults;

    PlagiatDataModel(String name, List<Double> aListOfResults){
        this.taskName = new SimpleStringProperty(name);
        this.listOfResults = new SimpleListProperty<>(FXCollections.observableArrayList(aListOfResults));
    }

    public String getTaskName(){ return taskName.get();}
    public void setTaskName(String value){ taskName.set(value);}

    public ObservableList<Double> getListOfResults(){ return listOfResults.get();}
    public void setListOfResults(ObservableList<Double> value){ listOfResults.set(value);}
}