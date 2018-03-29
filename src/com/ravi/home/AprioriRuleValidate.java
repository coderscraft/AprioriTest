package com.ravi.home;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class AprioriRuleValidate {
	
	static String[] headers = {"job","month","duration","pdays","previous","poutcome","empVarRate","consConfIdx","euribor3m","nrEmployed","y"};
        static String defaultGuess = null;

	public static void main(String[] args) throws FileNotFoundException {
		//List<Map<String,String>> models = loadValidationData("/Users/ravirane/Desktop/GMU/Final Data/csv/final_300.csv");
		List<Map<String,String>> models = loadValidationData("./data.csv");
		//List<Rule> rules = loadRules("/Users/ravirane/Desktop/GMU/Final Data/rule/rule.txt");
		List<Rule> rules = loadRules("./rule.txt");
                // ADDITION
                pickDefaultGuess(rules);
                // --------
		if(models.size() > 0) {
			Map<String, Integer> outcome = validate(models, rules);
                        // ADDITION
			double TP = outcome.get("TP") >= 0 ? outcome.get("TP")+1 : 0;
			double TN = outcome.get("TN") >= 0 ? outcome.get("TN")+1 : 0;
			double FP = outcome.get("FP") >= 0 ? outcome.get("FP")+1 : 0;
			double FN = outcome.get("FN") >= 0 ? outcome.get("FN")+1 : 0;
                        // --------
			
			double accuracy = (TP+TN)/(TP+FP+FN+TN);
			double precision = TP/(TP+FP);
			double recall = TP/(TP+FN);
			double f1 = 2*(recall * precision) / (recall + precision);
			try {
	            //File statText = new File("/Users/ravirane/Desktop/GMU/Final Data/csv//result.txt");
				File statText = new File("./result.txt");
	            FileOutputStream is = new FileOutputStream(statText);
	            OutputStreamWriter osw = new OutputStreamWriter(is);    
	            Writer w = new BufferedWriter(osw);
	            w.write("-------------------" + "\n");
	            w.write("TP - " + TP + "\n");
	            w.write("TN - " + TN + "\n");
	            w.write("FP - " + FP + "\n");
	            w.write("FN - " + FN + "\n");
	            w.write("-------------------" + "\n");
	            w.write("Accuracy - " + accuracy + "\n");
	            w.write("Precision - " + precision + "\n");
	            w.write("Recall - " + recall + "\n");
	            w.write("F1 - " + f1 + "\n");
	            w.write("-------------------" + "\n");
	            w.close();
	        } catch (IOException e) {
	            System.err.println("Problem writing to the file statsTest.txt");
	        }
		}
	}

	private static Map<String, Integer> validate(List<Map<String,String>> models, List<Rule> rules) {
		Map<String, Integer> outcome = new HashMap<String, Integer>();
                boolean foundRule = false;
		outcome.put("TP", -1);
		outcome.put("TN", -1);
		outcome.put("FP", -1);
		outcome.put("FN", -1);
		for(int i = 0; i < models.size(); i++) {
                        foundRule = false;
			Map<String,String> model = models.get(i);
			for(int j = 0; j < rules.size(); j++) {
				Rule r = rules.get(j);
				String test = r.verify(model);
				if(test != null) {
					outcome.put(test,outcome.get(test) + 1);
                                        foundRule = true;
					break;
				}
			}
                        // ADDITION
                        // Tried all rules, non applied
                        if (!foundRule) {
                            String answer = model.get("y");
                            if(answer.equals("yes")){
                                // Correct: Yes | Guess: Yes
                                if(defaultGuess.equals("yes"))
                                    outcome.put("TP",outcome.get("TP") + 1);
                                // Correct: Yes | Guess: No
                                else
                                    outcome.put("FN",outcome.get("FN") + 1);
                            }
                            else {
                                // Correct: No | Guess: No
                                if(defaultGuess.equals("no"))
                                    outcome.put("TN",outcome.get("TN") + 1);
                                // Correct: No | Guess: Yes
                                else
                                    outcome.put("FP",outcome.get("FP") + 1);
                            }
                        }
                        // --------
		}
		return outcome;
	}

	private static List<Rule> loadRules(String string) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(string));
		List<Rule> rules = new ArrayList<>();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String parts[] = line.split("==>");
			if(parts.length == 2) {
				Rule r = new Rule();
				List<Condition> conditions = new ArrayList<Condition>();
				String condns[] = parts[0].trim().split(" ");
				for(int i = 0; i<condns.length; i++) {
					String keyVal[] = condns[i].split("=");
					if(keyVal.length == 2) {
						Condition con = new Condition();
						con.key = keyVal[0].trim();
						con.value = keyVal[1].trim();
						conditions.add(con);
					}
				}
				r.conditions = conditions;
				String outcome[] = parts[1].trim().split("=");
				r.outcome = outcome[1];
				rules.add(r);
			}
		}
		scanner.close();
		return rules;
	}
        
        // ADDITION
        /*
            Function used to pick a default guess when no rules apply
        */
        private static void pickDefaultGuess(List<Rule> rules){
            int countYes = 0, countNo = 0;
            
            // Go through each rule and count yes and no conclusions
            for(Rule r : rules){
                if(r.outcome.equals("yes")){
                    countYes++;
                }
                else {
                    countNo++;
                }
            }
            
            // If more yes, default guess is no
            if(countYes > countNo){
                defaultGuess = "no";
            }
            // If more no, default gess is yes
            else if (countNo > countYes){
                defaultGuess = "yes";
            }
            // If equal, pick a random option
            else {
                String[] options = {"yes", "no"};
                Random r = new Random();
                defaultGuess = options[r.nextInt(options.length)];
            }
        }
        // --------

	private static List<Map<String,String>> loadValidationData(String string)
			throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(string));
		List<Map<String,String>> models = new ArrayList<>();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String parts[] = line.split(",");
			Map<String, String> m = getModel(parts);
			if (m != null) {
				models.add(m);
			}
		}
		scanner.close();
		return models;
	}

	private static Map<String, String> getModel(String[] parts) {
		if (parts.length == 11) {
			Map<String, String> dataMap = new HashMap<String, String>();
			for(int i = 0; i < parts.length; i++) {
				dataMap.put(headers[i], parts[i].trim());
			}
			return dataMap;
		} else {
			System.out.println("Error in data : " + parts);
			return null;
		}
	}

}
