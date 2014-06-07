package monitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TeePrintStream extends PrintStream {
  protected OutputStream parent;

  protected String fileName;


  public TeePrintStream(PrintStream orig, OutputStream os, boolean flush)
      throws IOException {
    super(orig, true);
    fileName = "(opened Stream)";
    parent = os;
  }

  public TeePrintStream(PrintStream orig, OutputStream os) throws IOException {
    this(orig, os, true);
  }

  public TeePrintStream(PrintStream os, String fn) throws IOException {
    this(os, fn, true);
  }

  public TeePrintStream(PrintStream orig, String fn, boolean flush)
      throws IOException {
    this(orig, new FileOutputStream(fn), flush);
  }

  @Override
  public boolean checkError() {
    return super.checkError();
  }

  @Override
  public void write(int x) {
      try {
          parent.write(x);
          super.write(x);
      } catch (IOException ex) {
          Logger.getLogger(TeePrintStream.class.getName()).log(Level.SEVERE, null, ex);
      }
  }

  @Override
  public void write(byte[] x, int o, int l) {
      try {
          parent.write(x, o, l); // "write once;
      } catch (IOException ex) {
          Logger.getLogger(TeePrintStream.class.getName()).log(Level.SEVERE, null, ex);
      }
    super.write(x, o, l); // write somewhere else."
  }

  @Override
  public void close() {
      super.close();
  }

  @Override
  public void flush() {
      try {
          parent.flush();
          super.flush();
      } catch (IOException ex) {
          Logger.getLogger(TeePrintStream.class.getName()).log(Level.SEVERE, null, ex);
      }
  }
}