/*
 * To change this license header, choose License Headers in Project RedmineConnectionProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;
import data.Project;

import java.util.ArrayList;
/**
 *
 * @author user
 */
public class ProjectOwner {
    private String name;
    private ArrayList<Project> hisProjects;
    private String apiKey;

    public ProjectOwner(String name) {
        this.name = name;
        hisProjects = new ArrayList<Project>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Project> getHisProjects() {
        return hisProjects;
    }

    public void setHisProjects(ArrayList<Project> hisProjects) {
        this.hisProjects = hisProjects;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Name: "); sb.append(this.name);
        sb.append("\n");
        sb.append("Apikey: "); sb.append(this.apiKey);
        sb.append("\n");
        this.hisProjects.forEach((Project p) -> {
            sb.append("\t").append(p.toString()).append("\n");
        });
        return sb.toString();
    }
}
