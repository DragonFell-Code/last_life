package com.dragon.lastlife.tests.factory;

import com.quiptmc.core.QuiptIntegration;

import java.io.File;

public class FakeIntegration extends QuiptIntegration {

    File file = new File("run/plugins/lastlife");
    @Override
    public void enable() {
        if(!file.exists()) file.mkdir();
    }

    @Override
    public File dataFolder() {
        return file;
    }

    @Override
    public String name() {
        return "lastlife";
    }

    @Override
    public String version() {
        return "FAKE";
    }
}
