package com.godaddy.sonar.ruby.metricfu;

public class FlayDuplication
{
    /**
     * The name of the file in which duplication was found, relative to the project root.
     */
	public final String name;

    /**
     * The first duplicated line.
     */
	public final String line;

    /**
     * The "mass" of duplication, roughly the number of sexprs duplicated.
     *
     * See https://github.com/seattlerb/sexp_processor/blob/master/lib/sexp.rb#L201.
     */
	public final String mass;

	public static FlayDuplication fromObjects(Object name, Object line, Object mass) {
		return new FlayDuplication(String.valueOf(name), String.valueOf(line), String.valueOf(mass));
	}

	public FlayDuplication(String name, String line, String mass) {
		this.name = name;
		this.line = line;
		this.mass = mass;
	}
}