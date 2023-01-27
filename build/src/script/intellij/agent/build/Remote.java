package intellij.agent.build;

import java.nio.file.Path;
import java.util.List;

import amadeus.maho.lang.AccessLevel;
import amadeus.maho.lang.FieldDefaults;
import amadeus.maho.lang.RequiredArgsConstructor;
import amadeus.maho.util.build.Distributive;
import amadeus.maho.util.build.Github;

import static intellij.agent.build.Build.*;

public interface Remote {
    
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC)
    class GithubConfig {
        
        String token = "<missing>";
        
    }
    
    GithubConfig githubConfig = workspace.config().load(new GithubConfig());
    
    Github github = Github.make(Github.authorization(githubConfig.token));
    
    Github.Repo repo = github["NekoCaffeine"]["amadeus.maho.intellij"];
    
    static void release(final Path distributive = build()) = Distributive.release(workspace, module, repo, List.of(distributive));
    
}
