package org.tondo.certimport;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author TondoDev
 *
 */
public class Certimport {
	
	public static void main(String[] args) {
		Certimport app = new Certimport();
		args = new String[] {"-url"};
		
		CommandLine parsedArgs = app.parseCmdArgs(args);
		// syntax error in command line args
		// do nothing - help printed in parsing error handler
		if (parsedArgs == null) {
			System.exit(1);
		} else if (parsedArgs.getOptions().length == 0 || parsedArgs.hasOption('h')) {
			// handling help
			app.printHelp(null);
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
	
	
	public CommandLine parseCmdArgs(String[] args) {
		CommandLineParser parser = new DefaultParser();
		try {
			// at first parse for help or other information options
			// it is two step parsing to avoid clash with required functional options
			CommandLine infoResult = parser.parse(infoOptions, args, true);
			if (infoResult.getOptions().length > 0) {
				return infoResult;
			}
			
			return parser.parse(functionalOptions, args);
		} catch (ParseException e) {
			printHelp("Options parsing error " + e.getMessage());
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
		this.functionalOptions.addOption(trust);

		// truststore password
		Option pwd = Option.builder("pw")
				.argName("password")
				.hasArg().desc("password used to load trustore")
			.build();
		this.functionalOptions.addOption(pwd);

		// server URL
		Option url = Option.builder("url")
				.argName("URL")
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
