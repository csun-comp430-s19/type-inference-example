package simplescala_experimentation.typechecker;

import simplescala_experimentation.util.ImmutableList;
import simplescala_experimentation.util.Cons;
import simplescala_experimentation.util.Nil;
import simplescala_experimentation.parser.SExpParser;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;


public class ZipfileReader implements Iterable<ZipfileReadEntry> {
    private ZipInputStream input;
    private boolean iteratorMade;
    
    public ZipfileReader(final File zipfile) throws IOException {
        input = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipfile)));
        iteratorMade = false;
    }

    private ZipEntry getNextEntry() throws IOException {
        final ZipEntry retval = input.getNextEntry();
        if (retval == null) {
            input.close();
        }
        return retval;
    }

    private ImmutableList<String> readLines() throws IOException {
        return SExpParser.readLinesFromReaderNoClose(new BufferedReader(new InputStreamReader(input)));
    }
    
    private class ZipIterator implements Iterator<ZipfileReadEntry> {
        private ZipEntry nextEntry;
        public ZipIterator() throws IOException {
            nextEntry = getNextEntry();
        }

        public boolean hasNext() {
            return nextEntry != null;
        }

        public ZipfileReadEntry next() {
            try {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final ZipfileReadEntry retval =
                    new ZipfileReadEntry(nextEntry.getName(),
                                         readLines());
                nextEntry = getNextEntry();
                return retval;
            } catch (final IOException e) {
                throw new NoSuchElementException(e.getMessage());
            }
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public Iterator<ZipfileReadEntry> iterator() {
        try {
            if (!iteratorMade) {
                iteratorMade = true;
                return new ZipIterator();
            } else {
                throw new RuntimeException("Iterator already made");
            }
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
