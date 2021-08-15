package org.secomm.pitwitter.loaders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.secomm.pitwitter.database.CategoriesDatabaseHandler;
import org.secomm.pitwitter.model.Category;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CategoriesDatabaseLoader {

    private Category category;

    private Gson gson;

    public void loadDatabase(CategoriesDatabaseHandler categoriesDatabaseHandler) {

        gson = new GsonBuilder().create();
        if (setCategory()) {
            categoriesDatabaseHandler.addCategory(category);
        }
    }

    private boolean setCategory() {

        try {
            if (Files.exists(Paths.get("category.json"))) {
                FileReader reader = new FileReader("category.json");
                category = gson.fromJson(reader, Category.class);
                reader.close();
                Files.delete(Paths.get("category.json"));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
