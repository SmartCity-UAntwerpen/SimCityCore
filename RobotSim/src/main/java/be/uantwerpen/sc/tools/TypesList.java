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
 * Created by Thomas on 7/05/2017.
 * Service for supported bot types
 */

@Service
public class TypesList
{
    private static String TYPESLIST = "TypesList.xml";

    public List<String> getTypes() throws Exception
    {
        List<String> types = new ArrayList<String>();
        ClassLoader classLoader = getClass().getClassLoader();

        try
        {
            File typesFile = new File(classLoader.getResource(TYPESLIST).toURI());

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(typesFile);

            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName("simulated").item(0).getChildNodes();

            for(int i = 0; i < nodeList.getLength(); i++)
            {
                Node node = nodeList.item(i);

                if(node.getNodeType() == Node.ELEMENT_NODE)
                {
                    types.add(types.size(), ((Element)node).getElementsByTagName("name").item(0).getTextContent());
                }
            }
        }
        catch(IOException e)
        {
            throw new Exception("Could not load types list! " + e.getMessage());
        }

        return types;
    }
}

