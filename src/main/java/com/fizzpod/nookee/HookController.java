package com.fizzpod.nookee;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

@RestController
@RequestMapping("/apis")
public class HookController {

	private final Logger LOGGER = LoggerFactory.getLogger(HookController.class);

    @Value("${hooks.root}")
    private final String hookRoot = "./hooks";

    private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    public HookController() {
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

    @RequestMapping(path = "/**", method = RequestMethod.GET)
    public ResponseEntity<?> doGet(final HttpServletRequest request) {
        final String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        LOGGER.debug("GET {}", path);
        final Object response = invokeHook(path, null);
        ResponseEntity<?> entity = new ResponseEntity<>(HttpStatus.OK);
        if (response != null) {
            entity = new ResponseEntity<>(response, HttpStatus.OK);
        }
        return entity;
    }

    @RequestMapping(path = "/**", method = RequestMethod.POST)
    public ResponseEntity<?> doPost(@RequestBody final String message, final HttpServletRequest request) {
        final String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        LOGGER.info("POST {} : {}", path, message);
        final Object response = invokeHook(path, message);
        ResponseEntity<?> entity = new ResponseEntity<>(HttpStatus.OK);
        if (response != null) {
            entity = new ResponseEntity<>(response, HttpStatus.OK);
        }
        return entity;
    }

    @RequestMapping(path = "/**", method = RequestMethod.PUT)
    public ResponseEntity<?> doPut(@RequestBody final String message, final HttpServletRequest request) {
        final String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        LOGGER.info("POST {} : {}", path, message);
        final Object response = invokeHook(path, message);
        ResponseEntity<?> entity = new ResponseEntity<>(HttpStatus.OK);
        if (response != null) {
            entity = new ResponseEntity<>(response, HttpStatus.OK);
        }
        return entity;
    }

    @RequestMapping(path = "/**", method = RequestMethod.PATCH)
    public ResponseEntity<?> doPatch(@RequestBody final String message, final HttpServletRequest request) {
        final String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        LOGGER.info("POST {} : {}", path, message);
        final Object response = invokeHook(path, message);
        ResponseEntity<?> entity = new ResponseEntity<>(HttpStatus.OK);
        if (response != null) {
            entity = new ResponseEntity<>(response, HttpStatus.OK);
        }
        return entity;
    }

    @RequestMapping(path = "/**", method = RequestMethod.DELETE)
    public ResponseEntity<?> doDelete(@RequestBody final String message, final HttpServletRequest request) {
        final String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        LOGGER.info("POST {} : {}", path, message);
        final Object response = invokeHook(path, message);
        ResponseEntity<?> entity = new ResponseEntity<>(HttpStatus.OK);
        if (response != null) {
            entity = new ResponseEntity<>(response, HttpStatus.OK);
        }
        return entity;
    }

    private Object invokeHook(final String path, final String message) {
        Object result = null;
        final File hookFolder = resolveHookFolder(path);
        if (hookFolder != null && hookFolder.exists() && hookFolder.isDirectory()) {
            for (final File hook : hookFolder.listFiles()) {
                if (hook.isFile()) {
                    final ScriptEngine engine = this.findEngine(hook);
                    if (engine != null) {
                        String script;
                        try {
                            script = FileUtils.readFileToString(hook, "UTF-8");
                            engine.put("request", message);
                            LOGGER.debug("Invoking script {}", script);
                            result = engine.eval(script);
                        } catch (final IOException e) {
                            LOGGER.warn("Could not read script file {}", hook.getName());
                        } catch (final ScriptException e) {
                            LOGGER.error("Error executing script {}", hook.getName(), e);
                        }
                    } else {
                        LOGGER.warn("No script engine for hook {}", hook.getName());
                    }
                    if (result != null) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    private File resolveHookFolder(final String path) {
        final File root = new File(hookRoot);
        final File hookFolder = new File(root, path);
        LOGGER.info("Hook folder resolved to: {}", hookFolder);
        return hookFolder;
    }

    private ScriptEngine findEngine(final File hook) {
        ScriptEngine engine = null;
        final String extension = FilenameUtils.getExtension(hook.getName());
        LOGGER.info("Looking for script engine for extension {}", extension);
        try {
            engine = scriptEngineManager.getEngineByExtension(extension);
        } catch (final NullPointerException e) {
			LOGGER.error("Could not get a script engine for extension {}", extension);
		}
		return engine;
	}
	
}
