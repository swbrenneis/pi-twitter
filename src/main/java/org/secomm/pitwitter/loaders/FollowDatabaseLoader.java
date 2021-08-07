package org.secomm.pitwitter.loaders;

import io.jsondb.JsonDBTemplate;
import org.secomm.pitwitter.config.FollowContext;
import org.secomm.pitwitter.config.Module;
import org.secomm.pitwitter.config.Modules;

import java.util.ArrayList;
import java.util.List;

public class FollowDatabaseLoader {

    public void loadDatabase(JsonDBTemplate jsonDBTemplate) {

        Modules modules = new Modules();
        modules.setId("00003");
        Module module = new Module();
        module.setName("botncop");
        List<FollowContext> following = new ArrayList<>();
        FollowContext followContext = new FollowContext("@OfficialBotNCop", 0,
                "https://discord.com/api/webhooks/865325874077499433/J-2fsnn1gZCkYoebA7uq12ZFqvWixwIgfnKv2-y0y0MYHI0CWAFxOKcN9cCFUPF9gnh1");
        following.add(followContext);
        module.setFollowing(following);
        List<Module> moduleList = new ArrayList<>();
        moduleList.add(module);
        modules.setModules(moduleList);
        jsonDBTemplate.upsert(modules);
    }
}