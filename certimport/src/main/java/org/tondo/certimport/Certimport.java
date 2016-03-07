package org.tondo.certimport;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.tondo.certimport.handlers.CertStoreResult;
import org.tondo.certimport.handlers.StoringConfiguration;
import org.tondo.certimport.handlers.StoringConfiguration.ConfBuilder;

/**
 * @author TondoDev
 *
 */
public class Certimport {
	
	public static void main(String[] args) {
		Certimport app = new Certimport();
		if (!app.run(args)) {
			System.exit(1);
		} else {
			System.exit(0);
		}
	}
	   
	private Options functionalOptions;
	private Options infoOptions;
	private HelpFormatter helpPrinter;
	
	public Certimport() {
		this.helpPrinter = new HelpFormatter();
		this.initCmdOptions();
	}
	
	
	public boolean run(String[] args) {
		CommandLine parsedArgs = this.parseCmdArgs(args);
		// syntax error in command line args
		// do nothing - help printed in parsing error handler
		if (parsedArgs == null) {
			return false;
		} else if (parsedArgs.getOptions().length == 0 || parsedArgs.hasOption('h')) {
			// handling help
			this.printHelp(null);
			return true;
		}
		
		return !this.execute(parsedArgs);
	}
	
	
	public CommandLine parseCmdArgs(String[] args) {
		CommandLineParser parser = new DefaultParser();
		try {
			// at first parse for help or other information options
			// it is two step parsing to avoid clash with required functional options
			CommandLine infoResult = parser.parse(infoOptions, args, true);
			if (args.length == 0 || infoResult.getOptions().length > 0) {
				return infoResult;
			}
			
			CommandLine functionalResult = parser.parse(functionalOptions, args);
			return validateArgs(functionalResult) ? functionalResult : null;
		} catch (ParseException e) {
			printHelp("Options parsing error: " + e.getMessage());
			return null;
		}
	}
	
	private boolean validateArgs(CommandLine args) {
		if (args.hasOption('c') && args.hasOption('f')) {
			System.err.println("Forced options is not aplicable to check operation!");
			return false;
		}
		return true;
	}
	
	private boolean execute(CommandLine args) {
		boolean createIfNotExists = false;
		String truststorePath = args.getOptionValue("t");
		if (truststorePath == null) {
			truststorePath = args.getOptionValue("tc");
			createIfNotExists = true;
		}
		String pwd = args.getOptionValue("pw");
		
		TrustedConnectionManager manager = createManager(truststorePath, pwd, createIfNotExists);
		if (manager == null) {
			return false;
		}
		
		ResultHandler resultHandler = null;
		ConfBuilder configBuilder = StoringConfiguration.builder();
		if(args.hasOption("aa")) {
			configBuilder.setOption(CertStoringOption.CHAIN);
		} else if (args.hasOption("ar")) {
			configBuilder.setOption(CertStoringOption.ROOT);
		} else if (args.hasOption("al")) {
			configBuilder.setOption(CertStoringOption.LEAF);
		} else if (args.hasOption("c")) {
			configBuilder.setOption(CertStoringOption.DONT_ADD);
			resultHandler = new CheckExistenceHandler();
		} else {
			// parsing cmd should avoid this branch
			System.out.println("Unexpected state: Unknown action!");
			return false;
		}
		
		configBuilder.setAddEvenIfTrusted(args.hasOption('f'));
		try {
			CertStoreResult result = manager.addCertificate(new URL(args.getOptionValue("url")), configBuilder.create());
			resultHandler.printResultInfo(result);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return false;
		}
		
		// for actions which caused modification of trustore save its new state
		if (!args.hasOption('c')) {
			try (FileOutputStream fos = new FileOutputStream(truststorePath)) {
				manager.save(fos, pwd.toCharArray());
			} catch (IOException e) {
				System.err.println(e.getMessage());
				return false;
			}
		}
		
		return true;
	}
	
	private TrustedConnectionManager createManager(String path, String pwd, boolean createIfNeeded) {
		if (Files.exists(Paths.get(path))) {
			try (InputStream truststore = new FileInputStream(path)) {
				return new TrustedConnectionManager(truststore, pwd.toCharArray());
			} catch (FileNotFoundException e) {
				System.err.println(e.getMessage());
				return null;
			} catch (IOException e) {
				System.err.println(e.getMessage());
				return null;
			} catch (Exception e) {
				System.err.println("Unexpected error occured! " + e.getMessage());
				return null;
			}
		} else if (createIfNeeded) {
			return new TrustedConnectionManager(null, pwd.toCharArray());
		} else {
			System.err.println("Truststore file not found. Run with -tc if you want create empty.");
			return null;
		}
	}
	
	private void initCmdOptions() {
		this.infoOptions = new Options()
				.addOption("h", "print this help");
		
		this.functionalOptions = new Options();
		// help is present in functional options only for presence
		// in list of all options in help
		this.functionalOptions.addOption("h", "print this help");

		// trusstore path - one argument
		Option trust = Option.builder("t")
				.argName("path")
				.hasArg()
				.desc("path to trustore used for authenticate server")
			.build();
		Option trustCreate = Option.builder("tc")
				.argName("path")
				.hasArg()
				.desc("path to trustore used for authenticate server, if not exist file is created")
			.build();
		OptionGroup trustGroup = new OptionGroup();
		trustGroup.setRequired(true);
		trustGroup.addOption(trust);
		trustGroup.addOption(trustCreate);
		this.functionalOptions.addOptionGroup(trustGroup);

		// truststore password
		Option pwd = Option.builder("pw")
				.argName("password")
				.hasArg().desc("password used to load trustore")
			.build();
		this.functionalOptions.addOption(pwd);

		// server URL
		Option url = Option.builder("url")
				.argName("URL")
				.required()
				.desc("https URL of remote server")
				.hasArg()
			.build();
		this.functionalOptions.addOption(url);

		Option addAll = Option.builder("aa")
				.desc("add all certificates send by server to trustore")
			.build();
		Option addRoot = Option.builder("ar")
				.desc("add root certificate to truststore")
			.build();

		Option addLeaf = Option.builder("al")
				.desc("add leaf certificate to truststore")
			.build();
		Option check = Option.builder("c")
				.desc("verify if provided server is trusted")
			.build();
		
		OptionGroup addCertGroup = new OptionGroup();
		addCertGroup.setRequired(false);
		addCertGroup.addOption(addRoot);
		addCertGroup.addOption(addLeaf);
		addCertGroup.addOption(check);
		addCertGroup.addOption(addAll);
		this.functionalOptions.addOptionGroup(addCertGroup);

		// forced add
		Option forced = Option.builder("f")
				.desc("Force add of certificate even if server is already trusted")
			.build();
		this.functionalOptions.addOption(forced);
	}
	
	public void printHelp(String leadingMsg) {
		if (leadingMsg != null) {
			System.out.println(leadingMsg);
		}
		
		this.helpPrinter.printHelp(
				"certimport -url <https_url> -(aa|al|ar|c[-f]) -t <trustore_path> -pw <trustore_pwd>", 
				// some text before arguemtn list
				"Certificate download utility", 
				this.functionalOptions, 
				// text after argument list
				"Created by TondoDev while learning Apache Commons CLI library");
	}
	
}
