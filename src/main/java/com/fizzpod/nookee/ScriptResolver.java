package com.fizzpod.nookee;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

public final class ScriptResolver {

    private final Logger LOGGER = LoggerFactory.getLogger(ScriptResolver.class);

    private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    private final File scriptRoot;

    public ScriptResolver(File scriptRoot) {
        this.scriptRoot = scriptRoot;
        final List<ScriptEngineFactory> factories = scriptEngineManager.getEngineFactories();

        for (final ScriptEngineFactory factory : factories) {
            LOGGER.info("ScriptEngineFactory Info");

            final String engName = factory.getEngineName();
            final String engVersion = factory.getEngineVersion();
            final String langName = factory.getLanguageName();
            final String langVersion = factory.getLanguageVersion();

            LOGGER.info("Script Engine: {} ({})", engName, engVersion);
            LOGGER.info("     Language: {} ({})", langName, langVersion);
            
            final List<String> engNames = factory.getNames();
            for (final String name : engNames) {
                LOGGER.info("        Alias: {}", name);
            }

        }
    }

    public Script getScript(final String path, final RequestMethod verb) {
        File scriptFile = findScript(path, verb);
        Script script = loadScript(scriptFile);
        return script;
    }

    private Script loadScript(File scriptFile) {
        Script script = null;
        if (scriptFile != null) {
            try {
                final String scriptContent = FileUtils.readFileToString(scriptFile, "UTF-8");
                final ScriptEngine engine = this.resolveEngine(scriptFile);
                if(StringUtils.hasText(scriptContent) && engine != null) {
                    script = new Script(scriptContent, engine);
                }
            } catch (IOException e) {
                LOGGER.error("Could not read script file {}", scriptFile, e);
            }
        }
        return script;
    }

    private ScriptEngine resolveEngine(File scriptFile) {
        ScriptEngine engine = null;
        final String extension = FilenameUtils.getExtension(scriptFile.getName());
        LOGGER.info("Looking for script engine for extension {}", extension);
        try {
            engine = scriptEngineManager.getEngineByExtension(extension);
        } catch (final NullPointerException e) {
			LOGGER.error("Could not get a script engine for extension {}", extension);
		}
		return engine;
    }

    private File findScript(final String path, final RequestMethod verb) {
        File scriptPath = new File(scriptRoot, path);
        File scriptFile = scanForScript(scriptPath, verb);
        return scriptFile;
    }

    /**
     * Work the way up the tree to thescript root.
     * Look for the a file starting with a matching verb.
     */
    private File scanForScript(final File scriptPath, final RequestMethod verb) {
        File folder = scriptPath;
        File scriptFile = null;
        while(!folder.equals(scriptRoot)) {
            if(folder.exists() && folder.isDirectory()) {
                LOGGER.info("Scanning {} for script with verb {}", scriptPath, verb);
                IOFileFilter fileFileFilter = FileFilterUtils.fileFileFilter();
                IOFileFilter prefixFileFilter = FileFilterUtils.prefixFileFilter(verb.name());
                File[] matchingFiles = folder.listFiles((FilenameFilter) FileFilterUtils.and(fileFileFilter, prefixFileFilter));
                if(matchingFiles != null && matchingFiles.length >= 1) {
                    scriptFile = matchingFiles[0];
                    break;
                }
            }
            folder = folder.getParentFile();
        }
        return scriptFile;
    }
}