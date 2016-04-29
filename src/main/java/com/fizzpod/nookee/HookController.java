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
@RequestMapping("/hooks")
public class HookController {

	private Logger LOGGER = LoggerFactory.getLogger(HookController.class);

	@Value("${hooks.root}")
	private String hookRoot = "./hooks";

	private ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

	public HookController() {
		List<ScriptEngineFactory> factories = scriptEngineManager.getEngineFactories();

		for (ScriptEngineFactory factory : factories) {
			LOGGER.info("ScriptEngineFactory Info");

			String engName = factory.getEngineName();
			String engVersion = factory.getEngineVersion();
			String langName = factory.getLanguageName();
			String langVersion = factory.getLanguageVersion();

			LOGGER.info("Script Engine: {} ({})", engName, engVersion);

			List<String> engNames = factory.getNames();
			for (String name : engNames) {
				LOGGER.info("Engine Alias: {}", name);
			}

			LOGGER.info("Language: {} {}", langName, langVersion);

		}
	}

	@RequestMapping(path = "/**", method = RequestMethod.POST)
	public ResponseEntity<?> doHook(@RequestBody String message, HttpServletRequest request) {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		LOGGER.debug("Received request at {} with message {}", path, message);
		Object response = invokeHook(path, message);
		ResponseEntity<?> entity = new ResponseEntity<>(HttpStatus.OK);
		if(response != null) {
			entity = new ResponseEntity<>(response, HttpStatus.OK);
		}
		return entity;
	}

	private Object invokeHook(String path, String message) {
		Object result = null;
		File hookFolder = resolveHookFolder(path);
		if (hookFolder != null && hookFolder.exists() && hookFolder.isDirectory()) {
			for (File hook : hookFolder.listFiles()) {
				if (hook.isFile()) {
					ScriptEngine engine = this.findEngine(hook);
					if(engine != null) {
						String script;
						try {
							script = FileUtils.readFileToString(hook, "UTF-8");
							engine.put("request", message);
							LOGGER.debug("Invoking script {}", script);
							result = engine.eval(script);
						} catch (IOException e) {
							LOGGER.warn("Could not read script file {}", hook.getName());
						} catch (ScriptException e) {
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

	private File resolveHookFolder(String path) {
		File root = new File(hookRoot);
		File hookFolder = new File(root, path);
		LOGGER.info("Hook folder resolved to: {}", hookFolder);
		return hookFolder;
	}

	private ScriptEngine findEngine(File hook) {
		ScriptEngine engine = null;
		String extension = FilenameUtils.getExtension(hook.getName());
		LOGGER.info("Looking for script engine for extension {}", extension);
		try {
			engine = scriptEngineManager.getEngineByExtension(extension);
		} catch (NullPointerException e) {
			LOGGER.error("Could not get a script engine for extension {}", extension);
		}
		return engine;
	}
	
}
