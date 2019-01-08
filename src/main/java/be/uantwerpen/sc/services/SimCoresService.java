package be.uantwerpen.sc.services;

import be.uantwerpen.sc.models.sim.SimBot;
import be.uantwerpen.sc.models.sim.SimCore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * Created by Thomas on 5/05/2017.
 * SimCoreService responsible for loading JAR file
 */
@Service
public class SimCoresService
{
    private static String configFileLocation;
    private static String jarFileLocation;

    @Value("${configFile}")
    private void setConfigFileLocation(String configFile) {
        configFileLocation = configFile;
    }

    @Value("${jarFile}")
    private void setJarFileLocation(String jarFile) {
        jarFileLocation = jarFile;
    }

    private final static String coreConfigFile = "BotCoreConfig.xml";

    public static SimCore getSimulationCore(SimBot bot)
    {
        return getSimulationCore(bot.getType());
    }

    public static SimCore getSimulationCore(String type)
    {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        Document document;
        SimCore simCore = null;

        // The location is based on the development mode and the supplied location in the corresponding properties file
        File configFile = new File(configFileLocation + coreConfigFile);

        if(!configFile.exists() || configFile.isDirectory())
        {

            //Configuration file is not available
            System.err.println("Configuration file: '" + configFile.getAbsolutePath() + "' not found!");
            return null;
        }

        try
        {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(configFile);
            document.getDocumentElement().normalize();

            NodeList coreList = document.getElementsByTagName("core");

            int i = 0;
            boolean found = false;

            while(i < coreList.getLength() && !found)
            {
                Node coreNode = coreList.item(i);
                try {
                    if(coreNode.getNodeType() == Node.ELEMENT_NODE)
                    {
                        Element element = (Element) coreNode;
                        String coreLocation;
                        String coreVersion;

                        if(element.getAttribute("type").equals(type))
                        {
                            try {
                                coreLocation = element.getElementsByTagName("jar").item(0).getTextContent();

                                //Check if core file exists
                                if(!new File(coreLocation).exists())
                                {
                                    File locationTest = new File(jarFileLocation + coreLocation);

                                    if(!locationTest.exists()) {
                                        //Core file can not be found in filesystem!
                                        System.err.println("JAR file: '" + locationTest.getAbsolutePath() + "' can not be found!");

                                        i++;
                                        continue;
                                    }
                                }
                            }
                            catch(Exception e) {
                                throw new Exception("JAR attribute unknown!");
                            }

                            try {
                                coreVersion = element.getElementsByTagName("version").item(0).getTextContent();
                            }
                            catch(Exception e) {
                                throw new Exception("VERSION attribute unknown!");
                            }

                            simCore = new SimCore(jarFileLocation + coreLocation, coreVersion);
                            found = true;
                        }
                    }

                    i++;
                }
                catch(Exception e) {
                    System.err.println("Failed to parse Core configuration. Configuration file contains errors! Exception: " + e.getMessage());
                }
            }

            if(!found)
                System.err.println("Could not find core of type: " + type + " in the configuration file!");
        }
        catch(Exception e) {
            System.err.println("Could not parse configuration file: " + coreConfigFile + "!");
            e.printStackTrace();
        }

        return simCore;
    }
}