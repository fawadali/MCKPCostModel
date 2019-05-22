package cpUDFCostModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class VirtualMachine {

	String id = null;
	String vmtype = null;
	double cost = -1.0;
	int mappers = 0;
	double totalCost = 0.0;
	double requiredInstances = 0;

}

public class PropertiesParser {

	public static List<VirtualMachine> loadVMConfigurations() {
		List<VirtualMachine> vms = new ArrayList<>();
		try {
			// Get the DOM Builder Factory
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();

			// Get the DOM Builder
			DocumentBuilder builder = factory.newDocumentBuilder();

			// Load and Parse the XML Document
			// document contains the complete XML as a Tree
			InputStream stream = new FileInputStream(
					"/home/sali/Desktop/ADBIS2019/cpUDFCostModel/Resources/config.xml");
			Document document = builder.parse(stream);
			stream.close();

			// iterating through the nodes and extracting the data

			NodeList nodelist = document.getDocumentElement().getChildNodes();

			for (int i = 0; i < nodelist.getLength(); i++) {
				Node node = nodelist.item(i);
				if (node instanceof Element) {
					VirtualMachine vm = new VirtualMachine();
					vm.id = node.getAttributes().getNamedItem("id")
							.getNodeValue();

					NodeList childNodes = node.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						Node cNode = childNodes.item(j);

						// identifying the child tag

						if (cNode instanceof Element) {
							String content = cNode.getLastChild()
									.getTextContent().trim();

							switch (cNode.getNodeName()) {
							case "vmtype":
								vm.vmtype = content;
								break;

							case "cost":
								vm.cost = Double.parseDouble(content);
								break;

							case "mappers":
								vm.mappers = Integer.parseInt(content);
								break;

							}
						}
					}
					vms.add(vm);

				}

			}
			// /printing
			/*
			 * for (VirtualMachine v : vms) { System.out.println(v.vmtype + ":"
			 * + v.cost + ":" + v.mappers);
			 * 
			 * }
			 */

		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return vms;
	}

}