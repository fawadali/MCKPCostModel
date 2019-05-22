package cpUDFCostModel;

import java.util.List;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

/*MCKP as MILP*/
public class MCKP {

	private static boolean must_be_bin = true;
	private static LpSolve lp;
	private static int[] colno;
	private static double[] row;
	private static double[] objFunc;
	private static int variants;

	public static int solveMILP() {
		int ret = 0;
		try {
			ret = lp.solve();
		} catch (LpSolveException e) {
			ret = 5;
			e.printStackTrace();
		}
		return ret;
	}

	public static void printMILPResult() {

		try {
			// print solution
			System.out.println("Value of objective function: "
					+ lp.getObjective());
			lp.getVariables(row);
			for (int a = 0; a < variants; a++) {
				System.out.println(lp.getColName(a + 1) + ": " + row[a]);
			}

		} catch (LpSolveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void dumpMILPMemory() {
		lp.deleteLp();
	}

	public static void FormulateMILP(List<Parameters> params, int stages,
			int vars, double budget) {
		try {
			variants = vars;
			lp = LpSolve.makeLp(0, variants); // creating model with 0 rows and
												// variant number of variables

			// MCKP Choose ONE variant Constraints
			for (int currStage = 1; currStage <= stages; currStage++) {
				int l = 0;
				colno = new int[variants + 1];
				row = new double[variants + 1];
				for (Parameters p : params) {
					lp.setColName(l + 1, p.variantName);
					colno[l] = l + 1;
					lp.setBinary(l + 1, must_be_bin);
					if (currStage == p.stageId) {

						row[l] = 1;
					} else {
						row[l] = 0;
					}
					l++;
				}
				lp.addConstraintex(l, row, colno, LpSolve.EQ, 1);

			}
			// MCKP COST Constraints
			int j = 0;
			colno = new int[variants + 1];
			row = new double[variants + 1];
			objFunc = new double[variants + 1];

			for (Parameters p : params) {
				colno[j] = j + 1;
				row[j] = p.totalExeCostOfVariant;
				objFunc[j] = p.exeTimeOfVariant;
				j++;

			}
			lp.addConstraintex(j, row, colno, LpSolve.LE, budget + 0.1);
			// [ 5.5 <= s <= 6.5]
			lp.addConstraintex(j, row, colno, LpSolve.GE, 0);
			lp.setObjFnex(j, objFunc, colno);

			// writing MILP model to file - DELETE THIS STATEMENT LATER
			lp.writeLp("model.lp");

		} catch (LpSolveException e) {
			e.printStackTrace();
		}
	}
}
