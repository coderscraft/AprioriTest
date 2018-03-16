package com.ravi.home;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Rule {

	List<Condition> conditions = new ArrayList<Condition>();
	String outcome;

	public String verify(Map<String, String> model) {
		boolean conditionMatch = true;
		boolean outcomeMatch = true;
		for (int i = 0; i < conditions.size(); i++) {
			Condition cond = conditions.get(i);
			if(!model.get(cond.key).equals(cond.value)) {
				conditionMatch = false;
				break;
			}
		}
		if(!model.get("y").equals(outcome)) {
				outcomeMatch = false;
		}
		if(conditionMatch == true && outcomeMatch == true) {
			if(outcome.equals("yes")) {
				return "TP";
			} else if(outcome.equals("no")) {
				return "TN";
			}
		} else if(conditionMatch == true && outcomeMatch == false) {
			if(outcome.equals("yes")) {
				return "FP";
			} else if(outcome.equals("no")) {
				return "FN";
			}
		}
		return null;
	}

}
