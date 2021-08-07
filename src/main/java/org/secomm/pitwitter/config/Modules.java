package org.secomm.pitwitter.config;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

import java.util.List;

@Document(collection = "modules", schemaVersion = "1.0")
public class Modules {

    @Id
    private String Id;

    private List<Module> modules;

    public Modules() {
    }

    public Modules(String id, List<Module> modules) {
        Id = id;
        this.modules = modules;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    @Override
    public String toString() {
        return "Modules{" +
                "Id='" + Id + '\'' +
                ", modules=" + modules +
                '}';
    }
}
