package com.godaddy.sonar.ruby.metricfu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlayMatch
{

	public final String mass;
	public final List<FlayDuplication> matches;
	public final boolean isIdentical;

    // TODO factor mass mutliplier in to mass -> length calculation
	private static final Pattern pattern = Pattern.compile(".*(IDENTICAL|Similar) .* \\(mass(\\*\\d+)? = (\\d+)\\)");

	public FlayMatch(String mass, List<FlayDuplication> matches, boolean isIdentical) {
		this.mass = mass;
		this.matches = matches;
		this.isIdentical = isIdentical;
	}

	@SuppressWarnings("unchecked")
	public static FlayMatch fromMap(Map<String, Object> match)
	{
		String reason = (String) match.get(":reason");
		Matcher matcher = pattern.matcher(reason);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Could not parse flay reason: " + reason);
		}

		boolean isIdentical = "IDENTICAL".equals(matcher.group(1));
		String mass = matcher.group(3);

		List<FlayDuplication> matches = new ArrayList<FlayDuplication>();
		for (Map<String, Object> dupe : (List<Map<String, Object>>) match.get(":matches"))
		{
			matches.add(FlayDuplication.fromObjects(dupe.get(":name"), dupe.get(":line"), mass));
		}

		return new FlayMatch(mass, matches, isIdentical);
	}
}
