package com.godaddy.sonar.ruby.metricfu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.sonar.api.BatchExtension;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.yaml.snakeyaml.Yaml;

import com.godaddy.sonar.ruby.metricfu.FlayReason.Match;

public class MetricfuYamlParser implements BatchExtension {
    private final ModuleFileSystem moduleFileSystem;
    private Logger logger = Logger.getLogger(MetricfuYamlParser.class);

	private static final String REPORT_FILE = "tmp/metric_fu/report.yml";
	private static Pattern escapePattern = Pattern.compile("\\e\\[\\d+m", Pattern.CASE_INSENSITIVE);

	protected Map<String, Object> metricfuResult = null;

	ArrayList<Map<String, Object>> saikuroFiles = null;
	Map<String, Object> caneViolations = null;
	ArrayList<Map<String, Object>> roodiProblems = null;
	ArrayList<Map<String, Object>> reekFiles = null;
	ArrayList<Map<String, Object>> flayReasons = null;

	public MetricfuYamlParser(ModuleFileSystem moduleFileSystem) {
		this(moduleFileSystem, REPORT_FILE);
    }

	@SuppressWarnings("unchecked")
	public MetricfuYamlParser(ModuleFileSystem moduleFileSystem, String filename) {
        this.moduleFileSystem = moduleFileSystem;

		try {
            FileInputStream input = new FileInputStream(new File(moduleFileSystem.baseDir(), filename));
			Yaml yaml = new Yaml();

			this.metricfuResult = (Map<String, Object>)yaml.loadAs(input, Map.class);
		} catch (FileNotFoundException e) {
			logger.error(e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<SaikuroComplexity> parseSaikuro(String fileNameFromModule) {
		if (saikuroFiles == null) {
			Map<String, Object> saikuro = (Map<String, Object>) metricfuResult.get(":saikuro");
			saikuroFiles = (ArrayList<Map<String, Object>>) saikuro.get(":files");			
		}

		List<SaikuroComplexity> complexities = new ArrayList<SaikuroComplexity>();
		if (saikuroFiles != null) {

			for (Map<String, Object> fileInfo : saikuroFiles) {
				String fileNameFromResults = (String) fileInfo.get(":filename");

				if (fileNameFromResults.contains(fileNameFromModule)) {
					ArrayList<Map<String, Object>> classesInfo = (ArrayList<Map<String, Object>>) fileInfo.get(":classes");

					for (Map<String, Object> classInfo : classesInfo) {
						ArrayList<Map<String, Object>> methods = (ArrayList<Map<String, Object>>) classInfo.get(":methods");

						for (Map<String, Object> method : methods) {
							SaikuroComplexity complexity = new SaikuroComplexity();
							complexity.setFile(fileNameFromResults);
							complexity.setName((String) method.get(":name"));
							complexity.setComplexity((Integer) method.get(":complexity"));
							complexity.setLine((Integer) method.get(":lines"));
							complexities.add(complexity);
						}
					}
				}
			}
		}
		return complexities;
	}

	@SuppressWarnings("unchecked")
	public List<CaneViolation> parseCane(String filename) {
		if (caneViolations == null) {
			Map<String, Object> caneResult = (Map<String, Object>) metricfuResult.get(":cane");
			caneViolations = (Map<String, Object>) caneResult.get(":violations");
		}

		List<CaneViolation> violations = new ArrayList<CaneViolation>();
		if (caneViolations != null) {
			ArrayList<Map<String, Object>> caneViolationsComplexityResult = (ArrayList<Map<String, Object>>) caneViolations.get(":abc_complexity");
			for (Map<String, Object> caneViolationsLineResultRow : caneViolationsComplexityResult) {
				String file = (String)caneViolationsLineResultRow.get(":file");
				if (file.length() > 0 && file.contains(filename)) {
					CaneComplexityViolation violation = new CaneComplexityViolation();
					violation.setFile(file);
					violation.setMethod((String)caneViolationsLineResultRow.get(":method"));
					violation.setComplexity(Integer.parseInt((String)caneViolationsLineResultRow.get(":complexity")));
					violations.add(violation);
				}
			}

			ArrayList<Map<String, Object>> caneViolationsLineResult = (ArrayList<Map<String, Object>>) caneViolations.get(":line_style");
			for (Map<String, Object> caneViolationsLineResultRow : caneViolationsLineResult) { 	
				String parts[] = ((String)caneViolationsLineResultRow.get(":line")).split(":");
				if (parts[0].length() > 0 && parts[0].contains(filename)) {
					CaneLineStyleViolation violation = new CaneLineStyleViolation();
					violation.setFile(parts[0]);
					violation.setLine(Integer.parseInt(parts[1]));
					violation.setDescription((String)caneViolationsLineResultRow.get(":description")); 
					violations.add(violation);
				}
			}

			ArrayList<Map<String, Object>> caneViolationsCommentResult = (ArrayList<Map<String, Object>>) caneViolations.get(":comment");
			for (Map<String, Object> caneViolationsLineResultRow : caneViolationsCommentResult) { 	
				String parts[] = ((String)caneViolationsLineResultRow.get(":line")).split(":");
				if (parts[0].length() > 0 && parts[0].contains(filename)) {
					CaneCommentViolation violation = new CaneCommentViolation();
					violation.setFile(parts[0]);
					violation.setLine(Integer.parseInt(parts[1]));
					violation.setClassName((String)caneViolationsLineResultRow.get(":class_name")); 
					violations.add(violation);
				}
			}
		}
		return violations;
	}

	@SuppressWarnings("unchecked")
	public List<RoodiProblem> parseRoodi(String filename) {
		if (roodiProblems == null) {
			Map<String, Object> roodi = (Map<String, Object>) metricfuResult.get(":roodi");
			roodiProblems = (ArrayList<Map<String, Object>>) roodi.get(":problems");			
		}

		List<RoodiProblem> problems = new ArrayList<RoodiProblem>();
		if (roodiProblems != null) {

			for (Map<String, Object> prob : roodiProblems) {
				String file = escapePattern.matcher(safeString((String) prob.get(":file"))).replaceAll("");

				if (file.contains(filename)) {
					RoodiProblem problem = new RoodiProblem();
					problem.setFile(file);
					problem.setLine(safeInteger((String)prob.get(":line")));
					problem.setProblem(escapePattern.matcher(safeString((String) prob.get(":problem"))).replaceAll(""));

					if (problem.getFile().length() > 0 && problem.getLine() > 0) {
						problems.add(problem);
					}
				}
			}
		}
		return problems;
	}

	@SuppressWarnings("unchecked")
	public List<ReekSmell> parseReek(String filename) {
		if (reekFiles == null) {
			Map<String, Object> reek = (Map<String, Object>) metricfuResult.get(":reek");
			reekFiles = (ArrayList<Map<String, Object>>) reek.get(":matches");			
		}

		List<ReekSmell> smells = new ArrayList<ReekSmell>();
		if (reekFiles != null) {

			for (Map<String, Object> resultFile : reekFiles) {
				String file = safeString((String) resultFile.get(":file_path"));

				if (file.length() > 0 && file.contains(filename)) {
					ArrayList<Map<String, Object>> resultSmells = (ArrayList<Map<String, Object>>) resultFile.get(":code_smells");

					for (Map<String, Object> resultSmell : resultSmells) { 	
						ReekSmell smell = new ReekSmell();
						smell.setFile(file);
						smell.setMethod(safeString((String)resultSmell.get(":method")));
						smell.setMessage(safeString((String)resultSmell.get(":message")));
						smell.setType(safeString((String)resultSmell.get(":type")));
						smells.add(smell);
					}
				}
			}
		}
		return smells;
	}

	@SuppressWarnings("unchecked")
	public List<FlayReason> parseFlay() {
		if (flayReasons == null) {
			Map<String, Object> flay = (Map<String, Object>) metricfuResult.get(":flay");
			flayReasons = (ArrayList<Map<String, Object>>) flay.get(":matches");			
		}

		List<FlayReason> reasons = new ArrayList<FlayReason>();
		if (flayReasons != null) {

			for (Map<String, Object> resultReason : flayReasons) {
				FlayReason reason = new FlayReason();
				reason.setReason(safeString((String) resultReason.get(":reason")));

				ArrayList<Map<String, Object>> resultMatches = (ArrayList<Map<String, Object>>) resultReason.get(":matches");
				for (Map<String, Object> resultDuplication : resultMatches) { 	
					Match match = reason.new Match((String)resultDuplication.get(":name"));
					
					// If flay was run with --diff, we should have the number of lines in the duplication. If not, make it 1.
					Integer line = safeInteger((String)resultDuplication.get(":line"));
					if (line > 0) {
						match.setStartLine(line);
						match.setLines(1);
					} else {
						Integer start = safeInteger((String)resultDuplication.get(":start"));
						if (start > 0) {
							match.setStartLine(start);
						}
						Integer lines = safeInteger((String)resultDuplication.get(":lines"));
						if (lines > 0) {
							match.setLines(lines);
						}
					}
					reason.getMatches().add(match);
				}
				reasons.add(reason);
			}
		}
		return reasons;
	}

	private String safeString (String s) {
		if (s == null) {
			return "";
		}
		return s;
	}

	private Integer safeInteger (String s) {
		try {			
			return Integer.parseInt(s);
		} catch (Exception e) {
			return 0;
		}
	}
}
