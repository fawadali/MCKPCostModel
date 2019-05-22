package cpUDFCostModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
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

class Parameters {

	int stageId = 0;
	String variantName = null;
	double exeTimeOfVariant = 0.0;
	double exeCostOfVariant = 0.0;
	double totalExeCostOfVariant = 0.0;
	double totalExeTime = 0.0;
	boolean isMin = false;

}

public class CostModelDriver {

	/* VM CONFIGURATIONS m1.small */
	public static final Double SPLITSIZE = 64.0;
	public static final double NUMBER_OF_MAPPERS = 2;
	public static final double VM_COST_PER_HOUR_FACTOR = 0.011;

	public static final Double TIME_INITIALIZATION_VM = 0.0;
	public static final Double TIME_UPLOAD_DATA = 0.0;
	public static final Double TIME_DOWNLOAD_DATA = 0.0;
	public static final Double TIME_TRANSFER_DATA = 0.0;

	public static final Double COST_INITIALIZATION_VM = 0.0;
	public static final Double COST_UPLOAD_DATA = 0.0;
	public static final Double COST_DOWNLOAD_DATA = 0.0;
	public static final Double COST_TRANSFER_DATA = 0.0;

	/**/
	public static final Double BUDGET = 4.0;
	public static final Double DATA_SIZE = 1024.0;

	public static int TOTAL_VARIANTS = 0;
	public static int TOTAL_STAGES = 0;

	public static void main(String[] args) {

		double allocatedMappers = DATA_SIZE / SPLITSIZE;
		double numberOfInstaces = Math.ceil(allocatedMappers
				/ NUMBER_OF_MAPPERS);

		List<Parameters> params = loadParameters();

		for (Parameters p : params) {
			// p.totalExeTime in seconds
			p.totalExeTime = p.exeTimeOfVariant + TIME_INITIALIZATION_VM
					+ TIME_UPLOAD_DATA + TIME_DOWNLOAD_DATA
					+ TIME_TRANSFER_DATA;

			// p.totalExeTime in seconds CONVERT to HRS
			p.totalExeCostOfVariant = p.exeCostOfVariant ;

		}

		// /printing
		DecimalFormat f = new DecimalFormat("##.00000");
		for (Parameters p : params) {
			System.out.println(p.stageId + ":" + p.variantName + " : "
					+ p.totalExeTime + " : "
					+ f.format(p.totalExeCostOfVariant));

		}

		System.out.println("TOTAL VARIANTS: " + TOTAL_VARIANTS);
		System.out.println("TOTAL STAGES: " + TOTAL_STAGES);

		MCKP.FormulateMILP(params, TOTAL_STAGES, TOTAL_VARIANTS, BUDGET);
		int ret = MCKP.solveMILP();
		if (ret == 0) {
			MCKP.printMILPResult();

		} else
			System.out.println("No Optimal Solution for the required budget");
		MCKP.dumpMILPMemory();

	}

	public static List<Parameters> loadParameters() {
		List<Parameters> params = new ArrayList<>();

		try {
			// Get the DOM Builder Factory
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();

			// Get the DOM Builder
			DocumentBuilder builder = factory.newDocumentBuilder();

			// Load and Parse the XML Document
			// document contains the complete XML as a Tree
			InputStream stream = new FileInputStream(
					"/home/sali/Desktop/ADBIS2019/cpUDFCostModel/Resources/parameters.xml");
			Document document = builder.parse(stream);
			stream.close();

			// iterating through the nodes and extracting the data

			NodeList nodelist = document.getDocumentElement().getChildNodes();
			int prevId = 0;

			for (int i = 0; i < nodelist.getLength(); i++) {
				Node node = nodelist.item(i);
				if (node instanceof Element) {
					Parameters pars = new Parameters();
					pars.stageId = Integer.parseInt(node.getAttributes()
							.getNamedItem("id").getNodeValue());

					if (prevId != pars.stageId) {
						prevId = pars.stageId;
						TOTAL_STAGES++;
					}

					NodeList childNodes = node.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						Node cNode = childNodes.item(j);

						// identifying the child tag

						if (cNode instanceof Element) {

							String content = cNode.getLastChild()
									.getTextContent().trim();

							switch (cNode.getNodeName()) {
							case "variant":
								pars.variantName = content;
								TOTAL_VARIANTS++;
								break;

							case "exetime":
								pars.exeTimeOfVariant = Double
										.parseDouble(content);
								break;
							
							case "cost":
								pars.exeCostOfVariant = Double
										.parseDouble(content);
								break;

							}
						}
					}

					params.add(pars);

				}

			}

		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return params;
	}

}
