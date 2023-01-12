package intellij.agent;

import java.util.concurrent.CompletableFuture;

import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginSet;
import com.intellij.openapi.extensions.PluginId;

import amadeus.maho.lang.SneakyThrows;

@SneakyThrows
public class MahoInit {
    
    public static void awaitPluginDescriptorLoaded(final CompletableFuture<PluginSet> future) = whenPluginSetLoaded(future.get());
    
    public static void whenPluginSetLoaded(final PluginSet set) {
        final IdeaPluginDescriptorImpl maho = set.findEnabledPlugin(PluginId.getId("amadeus.maho"));
        if (maho != null && maho.appContainerDescriptor.components != null)
            maho.appContainerDescriptor.components.stream().filter(config -> config.implementationClass != null).forEach(config -> Class.forName(config.implementationClass, true, maho.getClassLoader()));
    }

}
