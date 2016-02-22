package org.tondo.certimport;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * @author TondoDev
 *
 */
public class Certimport 
{
    public static void main( String[] args )
    {
       Options cmdOptions = new Options();
       cmdOptions.addOption("h", "print this help");
       
       // trusstore path - one argument
       Option trust =  Option.builder("t")
    		   	.argName("path")
    		   	.hasArg()
    		   	.desc("path to trustore used for authenticate server")
    		   	.build();
       cmdOptions.addOption(trust);
       
       // truststore password
       Option pwd = Option.builder("p")
    		   .argName("password")
    		   .hasArg()
    		   .desc("password used to load trustore")
    		   .build();
       cmdOptions.addOption(pwd);
       
       // server URL
       Option url = Option.builder("url")
    		   .argName("URL")
    		   .desc("https URL of remote server")
    		   .hasArg()
    		   .build();
       cmdOptions.addOption(url);
       
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
       cmdOptions.addOptionGroup(addCertGroup);
       
       
       // forced add
       Option forced = Option.builder("f")
    		   .desc("Force add of certificate even if server is already trusted")
    		   .build();
       cmdOptions.addOption(forced);
       
       
       HelpFormatter formatter = new HelpFormatter();
       formatter.printHelp("Certimport", cmdOptions);
    }
}
