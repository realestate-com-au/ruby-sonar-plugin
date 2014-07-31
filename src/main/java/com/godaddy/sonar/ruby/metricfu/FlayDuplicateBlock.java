package com.godaddy.sonar.ruby.metricfu;

public class FlayDuplicateBlock {

    public final int startLine;
    public final int length;

    public FlayDuplicateBlock(int startLine, int length) {
        this.startLine = startLine;
        this.length = length;
    }

    public static FlayDuplicateBlock fromDuplication(FlayDuplication duplication) {
        return new FlayDuplicateBlock(Integer.valueOf(duplication.line), lengthFromMass(Integer.valueOf(duplication.mass)));
    }

    public String asXML(String resourceKey) {
        return String.format("<b r=\"%s\" s=\"%s\" l=\"%s\" />", resourceKey, startLine, length);
    }

    private static int lengthFromMass(int mass) {
    /*
     * Just a SWAG to derive a duplication "length" from flay's "mass", which is more or less the number of sexprs:
     *
     *  def mass
     *    @mass ||= self.structure.flatten.size
     *  end
     *
     *  https://github.com/seattlerb/sexp_processor/blob/master/lib/sexp.rb#L201
     */
        return mass / 8;
    }
}
