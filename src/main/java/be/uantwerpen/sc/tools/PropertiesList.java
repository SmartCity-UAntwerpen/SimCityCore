package be.uantwerpen.sc.tools;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for supported bot properties
 */
@Service
public class PropertiesList
{
    private static String PROPERTIESLIST = "PropertiesList.xml";

    public List<String> getProperties() throws Exception {
        List<String> properties = new ArrayList<String>();
        ClassLoader classLoader = getClass().getClassLoader();

        try
        {
            File propertiesFile = new File(classLoader.getResource(PROPERTIESLIST).toURI());

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(propertiesFile);

            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName("simulated").item(0).getChildNodes();

            for(int i = 0; i < nodeList.getLength(); i++)
            {
                Node node = nodeList.item(i);

                if(node.getNodeType() == Node.ELEMENT_NODE)
                {
                    properties.add(properties.size(), ((Element)node).getElementsByTagName("name").item(0).getTextContent());
                }
            }
        }
        catch(IOException e)
        {
            throw new Exception("Could not load properties list! " + e.getMessage());
        }

        return properties;
    }
}
