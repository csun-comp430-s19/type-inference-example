package simplescala_experimentation.typechecker;

import simplescala_experimentation.util.ImmutableList;

public class ZipfileReadEntry {
    public final String filename;
    public final ImmutableList<String> contents;

    public ZipfileReadEntry(final String filename,
                            final ImmutableList<String> contents) {
        this.filename = filename;
        this.contents = contents;
    }

    public boolean equals(final Object other) {
        if (other instanceof ZipfileReadEntry) {
            final ZipfileReadEntry z = (ZipfileReadEntry)other;
            return (z.filename.equals(filename) &&
                    z.contents.equals(contents));
        } else {
            return false;
        }
    }

    public String toString() {
	return filename;
    }
}
