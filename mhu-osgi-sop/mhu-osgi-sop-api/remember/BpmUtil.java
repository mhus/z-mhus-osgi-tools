package de.mhus.osgi.sop.api.action;

import de.mhus.lib.adb.DbTransaction;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.strategy.TaskContext;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.Sop;
import de.mhus.osgi.sop.api.action.BpmCase.STATUS;

public class BpmUtil {

	private static Log log = Log.getLog(BpmUtil.class);
	
	/**
	 * set new status and save and append journal entry only if the status is different to the current status.
	 * 
	 * @param caze
	 * @param newStatus
	 * @return true if really saved
	 * @throws MException 
	 */
	public static boolean setCaseStatus(BpmCase caze, STATUS newStatus) throws MException {
		
		if (caze == null) return false;
		if (caze.getStatus().equals(newStatus)) return false;
		
		caze.setStatus(newStatus);
		caze.appendComment(BpmUtil.class,"1","Status: " + newStatus );
		caze.save();

		return true;
	}

	public static void appendComment(TaskContext context, Object source, Object ... msg) {
		try {
			
			BpmCase caze = BpmUtil.getCase(context);
			if (caze == null) return;
			
			DbTransaction.lock(caze);
			caze.reload();
			
			caze.appendComment(source,msg);
			caze.save();
			
		} catch (Throwable t) {
			log.d(t);
		} finally {
			DbTransaction.release();
		}
	}

	public static void appendComment(TaskContext context, OperationResult ret) {
		

		try {
			BpmCase caze = BpmUtil.getCase(context);
			if (caze == null) return;
			
			DbTransaction.lock(caze);
			caze.reload();
			
			
			if (ret == null) {
				caze.appendComment(BpmUtil.class,"Empty Result");
				caze.setStatusCode(OperationResult.INTERNAL_ERROR);
			} else {
				caze.appendComment(ret.getOperationPath(),
						ret.getReturnCode(), 
						ret.getMsg(), 
						context.getErrorMessage(), 
						context.getParameters(),
						ret.getResult()
						);
				caze.setStatusCode(ret.getReturnCode());
				caze.setMsg(ret.getMsg());
			}
			caze.save();
			
		} catch (Throwable t) {
			log.d(t);
		} finally {
			DbTransaction.release();
		}
	}

	public static BpmCase getCase(TaskContext context) throws MException {
		
		BpmCase caze = null;

		{
			String caseId = context.getParameters().getString("_mfw_caseId",null);
			caze = Sop.getApi(ActionApi.class).getCase(caseId);
		}
		
		if (caze == null) {
			String caseId = context.getParameters().getString("_bpm_caseId",null);
			caze = Sop.getApi(ActionApi.class).getCase(caseId);
		}
		
		if (caze == null)
			context.addErrorMessage("case not found");
		
		return caze;
	}
	
	
}
