package com.dragon.lastlife;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.JarLibrary;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class Loader implements PluginLoader {

    Logger logger = LoggerFactory.getLogger("QuiptLoader");

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        File dependenciesFolder = new File("dependencies/quipt-paper");
        if (!dependenciesFolder.exists()) dependenciesFolder.mkdirs();
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/gradle.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (File file : Objects.requireNonNull(dependenciesFolder.listFiles())) {
            if (file.getName().endsWith(".jar")) {
                classpathBuilder.addLibrary(new JarLibrary(file.toPath()));
            }

        }
        System.out.println(properties);
        MavenLibraryResolver central = new MavenLibraryResolver();

        central.addRepository(new RemoteRepository.Builder("central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());
        central.addDependency(new Dependency(new DefaultArtifact("org.json:json:" + properties.getProperty("json_version")), null));
        central.addDependency(new Dependency(new DefaultArtifact("org.eclipse.jetty:jetty-server:" + properties.getProperty("jetty_server_version")), null));
        central.addDependency(new Dependency(new DefaultArtifact("org.eclipse.jetty:jetty-servlet:" + properties.getProperty("jetty_servlet_version")), null));

        MavenLibraryResolver quipt = new MavenLibraryResolver();
        Authentication auth = new AuthenticationBuilder()
                .addUsername("QuickScythe")
                .addPassword(System.getenv("PACKAGES_TOKEN"))
                .build();

        quipt.addRepository(new RemoteRepository.Builder("quipt", "default", "https://maven.pkg.github.com/Quipt-Minecraft/quipt").setAuthentication(auth).build());
        quipt.addDependency(new Dependency(new DefaultArtifact("com.quiptmc:core:" + properties.getProperty("quipt_version")), null));
        quipt.addDependency(new Dependency(new DefaultArtifact("com.quiptmc:common:" + properties.getProperty("quipt_version")), null));
        quipt.addDependency(new Dependency(new DefaultArtifact("com.quiptmc:paper:" + properties.getProperty("quipt_version")), null));
        classpathBuilder.addLibrary(quipt);

        classpathBuilder.addLibrary(central);
    }
}
