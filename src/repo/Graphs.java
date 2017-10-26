// This file was generated on Wed Oct 25, 2017 12:58 (UTC-04) by REx v5.45 which is Copyright (c) 1979-2017 by Gunther Rademacher <grd@gmx.net>
// REx command line: Graphs.ebnf -java -tree -main -basex -faster -name org.basex.modules.rdf.Graphs

package org.basex.modules.rdf;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.Arrays;

import org.basex.build.MemBuilder;
import org.basex.build.SingleParser;
import org.basex.core.MainOptions;
import org.basex.io.IOContent;
import org.basex.query.value.item.Str;
import org.basex.query.value.node.ANode;
import org.basex.query.value.node.DBNode;
import org.basex.util.Atts;
import org.basex.util.Token;

public class Graphs
{
  public static void main(String args[]) throws Exception
  {
    if (args.length == 0)
    {
      System.out.println("Usage: java Graphs INPUT...");
      System.out.println();
      System.out.println("  parse INPUT, which is either a filename or literal text enclosed in curly braces\n");
    }
    else
    {
      for (String arg : args)
      {
        Writer w = new OutputStreamWriter(System.out, "UTF-8");
        XmlSerializer s = new XmlSerializer(w);
        String input = read(arg);
        Graphs parser = new Graphs(input, s);
        try
        {
          parser.parse_trigDoc();
        }
        catch (ParseException pe)
        {
          throw new RuntimeException("ParseException while processing " + arg + ":\n" + parser.getErrorMessage(pe));
        }
        finally
        {
          w.close();
        }
      }
    }
  }

  public static class ParseException extends RuntimeException
  {
    private static final long serialVersionUID = 1L;
    private int begin, end, offending, expected, state;

    public ParseException(int b, int e, int s, int o, int x)
    {
      begin = b;
      end = e;
      state = s;
      offending = o;
      expected = x;
    }

    @Override
    public String getMessage()
    {
      return offending < 0 ? "lexical analysis failed" : "syntax error";
    }

    public int getBegin() {return begin;}
    public int getEnd() {return end;}
    public int getState() {return state;}
    public int getOffending() {return offending;}
    public int getExpected() {return expected;}
  }

  public interface EventHandler
  {
    public void reset(CharSequence string);
    public void startNonterminal(String name, int begin);
    public void endNonterminal(String name, int end);
    public void terminal(String name, int begin, int end);
    public void whitespace(int begin, int end);
  }

  public static class TopDownTreeBuilder implements EventHandler
  {
    private CharSequence input = null;
    private Nonterminal[] stack = new Nonterminal[64];
    private int top = -1;

    @Override
    public void reset(CharSequence input)
    {
      this.input = input;
      top = -1;
    }

    @Override
    public void startNonterminal(String name, int begin)
    {
      Nonterminal nonterminal = new Nonterminal(name, begin, begin, new Symbol[0]);
      if (top >= 0) addChild(nonterminal);
      if (++top >= stack.length) stack = Arrays.copyOf(stack, stack.length << 1);
      stack[top] = nonterminal;
    }

    @Override
    public void endNonterminal(String name, int end)
    {
      stack[top].end = end;
      if (top > 0) --top;
    }

    @Override
    public void terminal(String name, int begin, int end)
    {
      addChild(new Terminal(name, begin, end));
    }

    @Override
    public void whitespace(int begin, int end)
    {
    }

    private void addChild(Symbol s)
    {
      Nonterminal current = stack[top];
      current.children = Arrays.copyOf(current.children, current.children.length + 1);
      current.children[current.children.length - 1] = s;
    }

    public void serialize(EventHandler e)
    {
      e.reset(input);
      stack[0].send(e);
    }
  }

  public static abstract class Symbol
  {
    public String name;
    public int begin;
    public int end;

    protected Symbol(String name, int begin, int end)
    {
      this.name = name;
      this.begin = begin;
      this.end = end;
    }

    public abstract void send(EventHandler e);
  }

  public static class Terminal extends Symbol
  {
    public Terminal(String name, int begin, int end)
    {
      super(name, begin, end);
    }

    @Override
    public void send(EventHandler e)
    {
      e.terminal(name, begin, end);
    }
  }

  public static class Nonterminal extends Symbol
  {
    public Symbol[] children;

    public Nonterminal(String name, int begin, int end, Symbol[] children)
    {
      super(name, begin, end);
      this.children = children;
    }

    @Override
    public void send(EventHandler e)
    {
      e.startNonterminal(name, begin);
      int pos = begin;
      for (Symbol c : children)
      {
        if (pos < c.begin) e.whitespace(pos, c.begin);
        c.send(e);
        pos = c.end;
      }
      if (pos < end) e.whitespace(pos, end);
      e.endNonterminal(name, end);
    }
  }

  public static class XmlSerializer implements EventHandler
  {
    private CharSequence input;
    private String delayedTag;
    private Writer out;

    public XmlSerializer(Writer w)
    {
      input = null;
      delayedTag = null;
      out = w;
    }

    @Override
    public void reset(CharSequence string)
    {
      writeOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?" + ">");
      input = string;
    }

    @Override
    public void startNonterminal(String name, int begin)
    {
      if (delayedTag != null)
      {
        writeOutput("<");
        writeOutput(delayedTag);
        writeOutput(">");
      }
      delayedTag = name;
    }

    @Override
    public void endNonterminal(String name, int end)
    {
      if (delayedTag != null)
      {
        delayedTag = null;
        writeOutput("<");
        writeOutput(name);
        writeOutput("/>");
      }
      else
      {
        writeOutput("</");
        writeOutput(name);
        writeOutput(">");
      }
    }

    @Override
    public void terminal(String name, int begin, int end)
    {
      if (name.charAt(0) == '\'')
      {
        name = "TOKEN";
      }
      startNonterminal(name, begin);
      characters(begin, end);
      endNonterminal(name, end);
    }

    @Override
    public void whitespace(int begin, int end)
    {
      characters(begin, end);
    }

    private void characters(int begin, int end)
    {
      if (begin < end)
      {
        if (delayedTag != null)
        {
          writeOutput("<");
          writeOutput(delayedTag);
          writeOutput(">");
          delayedTag = null;
        }
        writeOutput(input.subSequence(begin, end)
                         .toString()
                         .replace("&", "&amp;")
                         .replace("<", "&lt;")
                         .replace(">", "&gt;"));
      }
    }

    public void writeOutput(String content)
    {
      try
      {
        out.write(content);
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
    }
  }

  public static ANode parseTrigDoc(Str str) throws IOException
  {
    BaseXFunction baseXFunction = new BaseXFunction()
    {
      @Override
      public void execute(Graphs p) {p.parse_trigDoc();}
    };
    return baseXFunction.call(str);
  }

  public static abstract class BaseXFunction
  {
    protected abstract void execute(Graphs p);

    public ANode call(Str str) throws IOException
    {
      String input = str.toJava();
      SingleParser singleParser = new SingleParser(new IOContent(""), MainOptions.get())
      {
        @Override
        protected void parse() throws IOException {}
      };
      MemBuilder memBuilder = new MemBuilder(input, singleParser);
      memBuilder.init();
      BaseXTreeBuilder treeBuilder = new BaseXTreeBuilder(memBuilder);
      Graphs parser = new Graphs();
      parser.initialize(input, treeBuilder);
      try
      {
        execute(parser);
      }
      catch (ParseException pe)
      {
        memBuilder = new MemBuilder(input, singleParser);
        memBuilder.init();
        Atts atts = new Atts();
        atts.add(Token.token("b"), Token.token(pe.getBegin() + 1));
        atts.add(Token.token("e"), Token.token(pe.getEnd() + 1));
        if (pe.getOffending() < 0)
        {
          atts.add(Token.token("s"), Token.token(pe.getState()));
        }
        else
        {
          atts.add(Token.token("o"), Token.token(pe.getOffending()));
          atts.add(Token.token("x"), Token.token(pe.getExpected()));
        }
        memBuilder.openElem(Token.token("ERROR"), atts, new Atts());
        memBuilder.text(Token.token(parser.getErrorMessage(pe)));
        memBuilder.closeElem();
      }
      return new DBNode(memBuilder.data());
    }
  }

  public static class BaseXTreeBuilder implements EventHandler
  {
    private CharSequence input;
    private MemBuilder builder;
    private Atts nsp = new Atts();
    private Atts atts = new Atts();

    public BaseXTreeBuilder(MemBuilder b)
    {
      input = null;
      builder = b;
    }

    @Override
    public void reset(CharSequence string)
    {
      input = string;
    }

    @Override
    public void startNonterminal(String name, int begin)
    {
      try
      {
        builder.openElem(Token.token(name), atts, nsp);
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void endNonterminal(String name, int end)
    {
      try
      {
        builder.closeElem();
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void terminal(String name, int begin, int end)
    {
      if (name.charAt(0) == '\'')
      {
        name = "TOKEN";
      }
      startNonterminal(name, begin);
      characters(begin, end);
      endNonterminal(name, end);
    }

    @Override
    public void whitespace(int begin, int end)
    {
      characters(begin, end);
    }

    private void characters(int begin, int end)
    {
      if (begin < end)
      {
        try
        {
          builder.text(Token.token(input.subSequence(begin, end).toString()));
        }
        catch (IOException e)
        {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private static String read(String input) throws Exception
  {
    if (input.startsWith("{") && input.endsWith("}"))
    {
      return input.substring(1, input.length() - 1);
    }
    else
    {
      byte buffer[] = new byte[(int) new java.io.File(input).length()];
      java.io.FileInputStream stream = new java.io.FileInputStream(input);
      stream.read(buffer);
      stream.close();
      String content = new String(buffer, System.getProperty("file.encoding"));
      return content.length() > 0 && content.charAt(0) == '\uFEFF'
           ? content.substring(1)
           : content;
    }
  }

  public Graphs()
  {
  }

  public Graphs(CharSequence string, EventHandler t)
  {
    initialize(string, t);
  }

  public void initialize(CharSequence string, EventHandler eh)
  {
    eventHandler = eh;
    input = string;
    size = input.length();
    reset(0, 0, 0);
  }

  public CharSequence getInput()
  {
    return input;
  }

  public int getTokenOffset()
  {
    return b0;
  }

  public int getTokenEnd()
  {
    return e0;
  }

  public final void reset(int l, int b, int e)
  {
            b0 = b; e0 = b;
    l1 = l; b1 = b; e1 = e;
    end = e;
    eventHandler.reset(input);
  }

  public void reset()
  {
    reset(0, 0, 0);
  }

  public static String getOffendingToken(ParseException e)
  {
    return e.getOffending() < 0 ? null : TOKEN[e.getOffending()];
  }

  public static String[] getExpectedTokenSet(ParseException e)
  {
    String[] expected;
    if (e.getExpected() < 0)
    {
      expected = getTokenSet(- e.getState());
    }
    else
    {
      expected = new String[]{TOKEN[e.getExpected()]};
    }
    return expected;
  }

  public String getErrorMessage(ParseException e)
  {
    String[] tokenSet = getExpectedTokenSet(e);
    String found = getOffendingToken(e);
    String prefix = input.subSequence(0, e.getBegin()).toString();
    int line = prefix.replaceAll("[^\n]", "").length() + 1;
    int column = prefix.length() - prefix.lastIndexOf('\n');
    int size = e.getEnd() - e.getBegin();
    return e.getMessage()
         + (found == null ? "" : ", found " + found)
         + "\nwhile expecting "
         + (tokenSet.length == 1 ? tokenSet[0] : java.util.Arrays.toString(tokenSet))
         + "\n"
         + (size == 0 || found != null ? "" : "after successfully scanning " + size + " characters beginning ")
         + "at line " + line + ", column " + column + ":\n..."
         + input.subSequence(e.getBegin(), Math.min(input.length(), e.getBegin() + 64))
         + "...";
  }

  public void parse_trigDoc()
  {
    eventHandler.startNonterminal("trigDoc", e0);
    for (;;)
    {
      lookahead1W(13);              // END | WhiteSpace | IRIREF | PNAME_NS | PNAME_LN | BLANK_NODE_LABEL | ANON | '(' |
                                    // '@base' | '@prefix' | 'BASE' | 'GRAPH' | 'PREFIX' | '[' | '{'
      if (l1 == 1)                  // END
      {
        break;
      }
      switch (l1)
      {
      case 21:                      // '@base'
      case 22:                      // '@prefix'
      case 23:                      // 'BASE'
      case 25:                      // 'PREFIX'
        whitespace();
        parse_directive();
        break;
      default:
        whitespace();
        parse_block();
      }
    }
    eventHandler.endNonterminal("trigDoc", e0);
  }

  private void parse_block()
  {
    eventHandler.startNonterminal("block", e0);
    switch (l1)
    {
    case 32:                        // '{'
      parse_wrappedGraph();
      break;
    case 16:                        // '('
    case 26:                        // '['
      parse_triples2();
      break;
    case 24:                        // 'GRAPH'
      consume(24);                  // 'GRAPH'
      lookahead1W(6);               // WhiteSpace | IRIREF | PNAME_NS | PNAME_LN | BLANK_NODE_LABEL | ANON
      whitespace();
      parse_labelOrSubject();
      lookahead1W(3);               // WhiteSpace | '{'
      whitespace();
      parse_wrappedGraph();
      break;
    default:
      parse_triplesOrGraph();
    }
    eventHandler.endNonterminal("block", e0);
  }

  private void parse_triplesOrGraph()
  {
    eventHandler.startNonterminal("triplesOrGraph", e0);
    parse_labelOrSubject();
    lookahead1W(8);                 // WhiteSpace | IRIREF | PNAME_NS | PNAME_LN | 'a' | '{'
    switch (l1)
    {
    case 32:                        // '{'
      whitespace();
      parse_wrappedGraph();
      break;
    default:
      whitespace();
      parse_predicateObjectList();
      consume(19);                  // '.'
    }
    eventHandler.endNonterminal("triplesOrGraph", e0);
  }

  private void parse_triples2()
  {
    eventHandler.startNonterminal("triples2", e0);
    switch (l1)
    {
    case 26:                        // '['
      parse_blankNodePropertyList();
      lookahead1W(7);               // WhiteSpace | IRIREF | PNAME_NS | PNAME_LN | '.' | 'a'
      if (l1 != 19)                 // '.'
      {
        whitespace();
        parse_predicateObjectList();
      }
      consume(19);                  // '.'
      break;
    default:
      parse_collection();
      lookahead1W(5);               // WhiteSpace | IRIREF | PNAME_NS | PNAME_LN | 'a'
      whitespace();
      parse_predicateObjectList();
      consume(19);                  // '.'
    }
    eventHandler.endNonterminal("triples2", e0);
  }

  private void parse_wrappedGraph()
  {
    eventHandler.startNonterminal("wrappedGraph", e0);
    consume(32);                    // '{'
    lookahead1W(11);                // WhiteSpace | IRIREF | PNAME_NS | PNAME_LN | BLANK_NODE_LABEL | ANON | '(' | '[' |
                                    // '}'
    if (l1 != 33)                   // '}'
    {
      whitespace();
      parse_triplesBlock();
    }
    consume(33);                    // '}'
    eventHandler.endNonterminal("wrappedGraph", e0);
  }

  private void parse_triplesBlock()
  {
    eventHandler.startNonterminal("triplesBlock", e0);
    parse_triples();
    if (l1 == 19)                   // '.'
    {
      consume(19);                  // '.'
      lookahead1W(11);              // WhiteSpace | IRIREF | PNAME_NS | PNAME_LN | BLANK_NODE_LABEL | ANON | '(' | '[' |
                                    // '}'
      if (l1 != 33)                 // '}'
      {
        whitespace();
        parse_triplesBlock();
      }
    }
    eventHandler.endNonterminal("triplesBlock", e0);
  }

  private void parse_labelOrSubject()
  {
    eventHandler.startNonterminal("labelOrSubject", e0);
    switch (l1)
    {
    case 6:                         // BLANK_NODE_LABEL
    case 15:                        // ANON
      parse_BlankNode();
      break;
    default:
      parse_iri();
    }
    eventHandler.endNonterminal("labelOrSubject", e0);
  }

  private void parse_directive()
  {
    eventHandler.startNonterminal("directive", e0);
    switch (l1)
    {
    case 22:                        // '@prefix'
      parse_prefixID();
      break;
    case 21:                        // '@base'
      parse_base();
      break;
    case 25:                        // 'PREFIX'
      parse_sparqlPrefix();
      break;
    default:
      parse_sparqlBase();
    }
    eventHandler.endNonterminal("directive", e0);
  }

  private void parse_prefixID()
  {
    eventHandler.startNonterminal("prefixID", e0);
    consume(22);                    // '@prefix'
    lookahead1W(1);                 // WhiteSpace | PNAME_NS
    consume(4);                     // PNAME_NS
    lookahead1W(0);                 // WhiteSpace | IRIREF
    consume(3);                     // IRIREF
    lookahead1W(2);                 // WhiteSpace | '.'
    consume(19);                    // '.'
    eventHandler.endNonterminal("prefixID", e0);
  }

  private void parse_base()
  {
    eventHandler.startNonterminal("base", e0);
    consume(21);                    // '@base'
    lookahead1W(0);                 // WhiteSpace | IRIREF
    consume(3);                     // IRIREF
    lookahead1W(2);                 // WhiteSpace | '.'
    consume(19);                    // '.'
    eventHandler.endNonterminal("base", e0);
  }

  private void parse_sparqlPrefix()
  {
    eventHandler.startNonterminal("sparqlPrefix", e0);
    consume(25);                    // 'PREFIX'
    lookahead1W(1);                 // WhiteSpace | PNAME_NS
    consume(4);                     // PNAME_NS
    lookahead1W(0);                 // WhiteSpace | IRIREF
    consume(3);                     // IRIREF
    eventHandler.endNonterminal("sparqlPrefix", e0);
  }

  private void parse_sparqlBase()
  {
    eventHandler.startNonterminal("sparqlBase", e0);
    consume(23);                    // 'BASE'
    lookahead1W(0);                 // WhiteSpace | IRIREF
    consume(3);                     // IRIREF
    eventHandler.endNonterminal("sparqlBase", e0);
  }

  private void parse_triples()
  {
    eventHandler.startNonterminal("triples", e0);
    switch (l1)
    {
    case 26:                        // '['
      parse_blankNodePropertyList();
      lookahead1W(10);              // WhiteSpace | IRIREF | PNAME_NS | PNAME_LN | '.' | 'a' | '}'
      if (l1 != 19                  // '.'
       && l1 != 33)                 // '}'
      {
        whitespace();
        parse_predicateObjectList();
      }
      break;
    default:
      parse_subject();
      lookahead1W(5);               // WhiteSpace | IRIREF | PNAME_NS | PNAME_LN | 'a'
      whitespace();
      parse_predicateObjectList();
    }
    eventHandler.endNonterminal("triples", e0);
  }

  private void parse_predicateObjectList()
  {
    eventHandler.startNonterminal("predicateObjectList", e0);
    parse_verb();
    lookahead1W(14);                // WhiteSpace | IRIREF | PNAME_NS | PNAME_LN | BLANK_NODE_LABEL | INTEGER |
                                    // DECIMAL | DOUBLE | STRING_LITERAL_QUOTE | STRING_LITERAL_SINGLE_QUOTE |
                                    // STRING_LITERAL_LONG_SINGLE_QUOTE | STRING_LITERAL_LONG_QUOTE | ANON | '(' | '[' |
                                    // 'false' | 'true'
    whitespace();
    parse_objectList();
    for (;;)
    {
      if (l1 != 20)                 // ';'
      {
        break;
      }
      consume(20);                  // ';'
      lookahead1W(12);              // WhiteSpace | IRIREF | PNAME_NS | PNAME_LN | '.' | ';' | ']' | 'a' | '}'
      if (l1 == 3                   // IRIREF
       || l1 == 4                   // PNAME_NS
       || l1 == 5                   // PNAME_LN
       || l1 == 29)                 // 'a'
      {
        whitespace();
        parse_verb();
        lookahead1W(14);            // WhiteSpace | IRIREF | PNAME_NS | PNAME_LN | BLANK_NODE_LABEL | INTEGER |
                                    // DECIMAL | DOUBLE | STRING_LITERAL_QUOTE | STRING_LITERAL_SINGLE_QUOTE |
                                    // STRING_LITERAL_LONG_SINGLE_QUOTE | STRING_LITERAL_LONG_QUOTE | ANON | '(' | '[' |
                                    // 'false' | 'true'
        whitespace();
        parse_objectList();
      }
    }
    eventHandler.endNonterminal("predicateObjectList", e0);
  }

  private void parse_objectList()
  {
    eventHandler.startNonterminal("objectList", e0);
    parse_object();
    for (;;)
    {
      lookahead1W(9);               // WhiteSpace | ',' | '.' | ';' | ']' | '}'
      if (l1 != 18)                 // ','
      {
        break;
      }
      consume(18);                  // ','
      lookahead1W(14);              // WhiteSpace | IRIREF | PNAME_NS | PNAME_LN | BLANK_NODE_LABEL | INTEGER |
                                    // DECIMAL | DOUBLE | STRING_LITERAL_QUOTE | STRING_LITERAL_SINGLE_QUOTE |
                                    // STRING_LITERAL_LONG_SINGLE_QUOTE | STRING_LITERAL_LONG_QUOTE | ANON | '(' | '[' |
                                    // 'false' | 'true'
      whitespace();
      parse_object();
    }
    eventHandler.endNonterminal("objectList", e0);
  }

  private void parse_verb()
  {
    eventHandler.startNonterminal("verb", e0);
    switch (l1)
    {
    case 29:                        // 'a'
      consume(29);                  // 'a'
      break;
    default:
      parse_predicate();
    }
    eventHandler.endNonterminal("verb", e0);
  }

  private void parse_subject()
  {
    eventHandler.startNonterminal("subject", e0);
    switch (l1)
    {
    case 3:                         // IRIREF
    case 4:                         // PNAME_NS
    case 5:                         // PNAME_LN
      parse_iri();
      break;
    default:
      parse_blank();
    }
    eventHandler.endNonterminal("subject", e0);
  }

  private void parse_predicate()
  {
    eventHandler.startNonterminal("predicate", e0);
    parse_iri();
    eventHandler.endNonterminal("predicate", e0);
  }

  private void parse_object()
  {
    eventHandler.startNonterminal("object", e0);
    switch (l1)
    {
    case 3:                         // IRIREF
    case 4:                         // PNAME_NS
    case 5:                         // PNAME_LN
      parse_iri();
      break;
    case 6:                         // BLANK_NODE_LABEL
    case 15:                        // ANON
    case 16:                        // '('
      parse_blank();
      break;
    case 26:                        // '['
      parse_blankNodePropertyList();
      break;
    default:
      parse_literal();
    }
    eventHandler.endNonterminal("object", e0);
  }

  private void parse_literal()
  {
    eventHandler.startNonterminal("literal", e0);
    switch (l1)
    {
    case 8:                         // INTEGER
    case 9:                         // DECIMAL
    case 10:                        // DOUBLE
      parse_NumericLiteral();
      break;
    case 30:                        // 'false'
    case 31:                        // 'true'
      parse_BooleanLiteral();
      break;
    default:
      parse_RDFLiteral();
    }
    eventHandler.endNonterminal("literal", e0);
  }

  private void parse_blank()
  {
    eventHandler.startNonterminal("blank", e0);
    switch (l1)
    {
    case 16:                        // '('
      parse_collection();
      break;
    default:
      parse_BlankNode();
    }
    eventHandler.endNonterminal("blank", e0);
  }

  private void parse_blankNodePropertyList()
  {
    eventHandler.startNonterminal("blankNodePropertyList", e0);
    consume(26);                    // '['
    lookahead1W(5);                 // WhiteSpace | IRIREF | PNAME_NS | PNAME_LN | 'a'
    whitespace();
    parse_predicateObjectList();
    consume(27);                    // ']'
    eventHandler.endNonterminal("blankNodePropertyList", e0);
  }

  private void parse_collection()
  {
    eventHandler.startNonterminal("collection", e0);
    consume(16);                    // '('
    for (;;)
    {
      lookahead1W(15);              // WhiteSpace | IRIREF | PNAME_NS | PNAME_LN | BLANK_NODE_LABEL | INTEGER |
                                    // DECIMAL | DOUBLE | STRING_LITERAL_QUOTE | STRING_LITERAL_SINGLE_QUOTE |
                                    // STRING_LITERAL_LONG_SINGLE_QUOTE | STRING_LITERAL_LONG_QUOTE | ANON | '(' | ')' |
                                    // '[' | 'false' | 'true'
      if (l1 == 17)                 // ')'
      {
        break;
      }
      whitespace();
      parse_object();
    }
    consume(17);                    // ')'
    eventHandler.endNonterminal("collection", e0);
  }

  private void parse_NumericLiteral()
  {
    eventHandler.startNonterminal("NumericLiteral", e0);
    switch (l1)
    {
    case 8:                         // INTEGER
      consume(8);                   // INTEGER
      break;
    case 9:                         // DECIMAL
      consume(9);                   // DECIMAL
      break;
    default:
      consume(10);                  // DOUBLE
    }
    eventHandler.endNonterminal("NumericLiteral", e0);
  }

  private void parse_RDFLiteral()
  {
    eventHandler.startNonterminal("RDFLiteral", e0);
    parse_String();
    lookahead1W(16);                // WhiteSpace | IRIREF | PNAME_NS | PNAME_LN | BLANK_NODE_LABEL | LANGTAG |
                                    // INTEGER | DECIMAL | DOUBLE | STRING_LITERAL_QUOTE | STRING_LITERAL_SINGLE_QUOTE |
                                    // STRING_LITERAL_LONG_SINGLE_QUOTE | STRING_LITERAL_LONG_QUOTE | ANON | '(' | ')' |
                                    // ',' | '.' | ';' | '[' | ']' | '^^' | 'false' | 'true' | '}'
    if (l1 == 7                     // LANGTAG
     || l1 == 28)                   // '^^'
    {
      switch (l1)
      {
      case 7:                       // LANGTAG
        consume(7);                 // LANGTAG
        break;
      default:
        consume(28);                // '^^'
        lookahead1W(4);             // WhiteSpace | IRIREF | PNAME_NS | PNAME_LN
        whitespace();
        parse_iri();
      }
    }
    eventHandler.endNonterminal("RDFLiteral", e0);
  }

  private void parse_BooleanLiteral()
  {
    eventHandler.startNonterminal("BooleanLiteral", e0);
    switch (l1)
    {
    case 31:                        // 'true'
      consume(31);                  // 'true'
      break;
    default:
      consume(30);                  // 'false'
    }
    eventHandler.endNonterminal("BooleanLiteral", e0);
  }

  private void parse_String()
  {
    eventHandler.startNonterminal("String", e0);
    switch (l1)
    {
    case 11:                        // STRING_LITERAL_QUOTE
      consume(11);                  // STRING_LITERAL_QUOTE
      break;
    case 12:                        // STRING_LITERAL_SINGLE_QUOTE
      consume(12);                  // STRING_LITERAL_SINGLE_QUOTE
      break;
    case 13:                        // STRING_LITERAL_LONG_SINGLE_QUOTE
      consume(13);                  // STRING_LITERAL_LONG_SINGLE_QUOTE
      break;
    default:
      consume(14);                  // STRING_LITERAL_LONG_QUOTE
    }
    eventHandler.endNonterminal("String", e0);
  }

  private void parse_iri()
  {
    eventHandler.startNonterminal("iri", e0);
    switch (l1)
    {
    case 3:                         // IRIREF
      consume(3);                   // IRIREF
      break;
    default:
      parse_PrefixedName();
    }
    eventHandler.endNonterminal("iri", e0);
  }

  private void parse_PrefixedName()
  {
    eventHandler.startNonterminal("PrefixedName", e0);
    switch (l1)
    {
    case 5:                         // PNAME_LN
      consume(5);                   // PNAME_LN
      break;
    default:
      consume(4);                   // PNAME_NS
    }
    eventHandler.endNonterminal("PrefixedName", e0);
  }

  private void parse_BlankNode()
  {
    eventHandler.startNonterminal("BlankNode", e0);
    switch (l1)
    {
    case 6:                         // BLANK_NODE_LABEL
      consume(6);                   // BLANK_NODE_LABEL
      break;
    default:
      consume(15);                  // ANON
    }
    eventHandler.endNonterminal("BlankNode", e0);
  }

  private void consume(int t)
  {
    if (l1 == t)
    {
      whitespace();
      eventHandler.terminal(TOKEN[l1], b1, e1);
      b0 = b1; e0 = e1; l1 = 0;
    }
    else
    {
      error(b1, e1, 0, l1, t);
    }
  }

  private void whitespace()
  {
    if (e0 != b1)
    {
      eventHandler.whitespace(e0, b1);
      e0 = b1;
    }
  }

  private int matchW(int set)
  {
    int code;
    for (;;)
    {
      code = match(set);
      if (code != 2)                // WhiteSpace
      {
        break;
      }
    }
    return code;
  }

  private void lookahead1W(int set)
  {
    if (l1 == 0)
    {
      l1 = matchW(set);
      b1 = begin;
      e1 = end;
    }
  }

  private int error(int b, int e, int s, int l, int t)
  {
    throw new ParseException(b, e, s, l, t);
  }

  private int     b0, e0;
  private int l1, b1, e1;
  private EventHandler eventHandler = null;
  private CharSequence input = null;
  private int size = 0;
  private int begin = 0;
  private int end = 0;

  private int match(int tokenSetId)
  {
    begin = end;
    int current = end;
    int result = INITIAL[tokenSetId];
    int state = 0;

    for (int code = result & 255; code != 0; )
    {
      int charclass;
      int c0 = current < size ? input.charAt(current) : 0;
      ++current;
      if (c0 < 0x80)
      {
        charclass = MAP0[c0];
      }
      else if (c0 < 0xd800)
      {
        int c1 = c0 >> 4;
        charclass = MAP1[(c0 & 15) + MAP1[(c1 & 31) + MAP1[c1 >> 5]]];
      }
      else
      {
        if (c0 < 0xdc00)
        {
          int c1 = current < size ? input.charAt(current) : 0;
          if (c1 >= 0xdc00 && c1 < 0xe000)
          {
            ++current;
            c0 = ((c0 & 0x3ff) << 10) + (c1 & 0x3ff) + 0x10000;
          }
        }

        int lo = 0, hi = 5;
        for (int m = 3; ; m = (hi + lo) >> 1)
        {
          if (MAP2[m] > c0) {hi = m - 1;}
          else if (MAP2[6 + m] < c0) {lo = m + 1;}
          else {charclass = MAP2[12 + m]; break;}
          if (lo > hi) {charclass = 0; break;}
        }
      }

      state = code;
      int i0 = (charclass << 8) + code - 1;
      code = TRANSITION[(i0 & 15) + TRANSITION[i0 >> 4]];

      if (code > 255)
      {
        result = code;
        code &= 255;
        end = current;
      }
    }

    result >>= 8;
    if (result == 0)
    {
      end = current - 1;
      int c1 = end < size ? input.charAt(end) : 0;
      if (c1 >= 0xdc00 && c1 < 0xe000)
      {
        --end;
      }
      return error(begin, end, state, -1, -1);
    }

    if (end > size) end = size;
    return (result & 63) - 1;
  }

  private static String[] getTokenSet(int tokenSetId)
  {
    java.util.ArrayList<String> expected = new java.util.ArrayList<>();
    int s = tokenSetId < 0 ? - tokenSetId : INITIAL[tokenSetId] & 255;
    for (int i = 0; i < 34; i += 32)
    {
      int j = i;
      int i0 = (i >> 5) * 129 + s - 1;
      int i1 = i0 >> 3;
      int f = EXPECTED[(i0 & 7) + EXPECTED[(i1 & 7) + EXPECTED[i1 >> 3]]];
      for ( ; f != 0; f >>>= 1, ++j)
      {
        if ((f & 1) != 0)
        {
          expected.add(TOKEN[j]);
        }
      }
    }
    return expected.toArray(new String[]{});
  }

  private static final int[] MAP0 =
  {
    /*   0 */ 0, 0, 0, 0, 0, 0, 0, 0, 0, 57, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 57, 2, 3,
    /*  35 */ 4, 2, 5, 2, 6, 7, 8, 2, 9, 10, 11, 12, 2, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 14, 15, 16, 2, 17, 2,
    /*  64 */ 18, 19, 20, 21, 21, 22, 23, 24, 25, 26, 27, 27, 27, 27, 27, 27, 28, 27, 29, 30, 27, 31, 27, 27, 32, 27,
    /*  90 */ 27, 33, 34, 35, 36, 37, 38, 39, 40, 21, 21, 41, 42, 27, 27, 43, 27, 27, 44, 27, 45, 27, 46, 27, 47, 48,
    /* 116 */ 49, 50, 27, 27, 51, 27, 27, 52, 38, 53, 2, 54
  };

  private static final int[] MAP1 =
  {
    /*   0 */ 108, 124, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 156, 181, 181, 181, 181,
    /*  21 */ 181, 214, 215, 213, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214,
    /*  42 */ 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214,
    /*  63 */ 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214,
    /*  84 */ 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214,
    /* 105 */ 214, 214, 214, 254, 247, 270, 286, 302, 318, 334, 350, 387, 387, 387, 379, 435, 427, 435, 427, 435, 435,
    /* 126 */ 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 404, 404, 404, 404, 404, 404, 404,
    /* 147 */ 420, 435, 435, 435, 435, 435, 435, 435, 435, 365, 387, 387, 388, 386, 387, 387, 435, 435, 435, 435, 435,
    /* 168 */ 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 387, 387, 387, 387, 387, 387, 387, 387,
    /* 189 */ 387, 387, 387, 387, 387, 387, 387, 387, 387, 387, 387, 387, 387, 387, 387, 387, 387, 387, 387, 387, 387,
    /* 210 */ 387, 387, 387, 434, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435,
    /* 231 */ 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 435, 387, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    /* 256 */ 0, 0, 0, 0, 0, 0, 0, 57, 1, 0, 0, 1, 0, 0, 57, 2, 3, 4, 2, 5, 2, 6, 7, 8, 2, 9, 10, 11, 12, 2, 13, 13, 13,
    /* 289 */ 13, 13, 13, 13, 13, 13, 13, 14, 15, 16, 2, 17, 2, 18, 19, 20, 21, 21, 22, 23, 24, 25, 26, 27, 27, 27, 27,
    /* 316 */ 27, 27, 28, 27, 29, 30, 27, 31, 27, 27, 32, 27, 27, 33, 34, 35, 36, 37, 38, 39, 40, 21, 21, 41, 42, 27,
    /* 342 */ 27, 43, 27, 27, 44, 27, 45, 27, 46, 27, 47, 48, 49, 50, 27, 27, 51, 27, 27, 52, 38, 53, 2, 54, 54, 54, 54,
    /* 369 */ 54, 54, 54, 54, 54, 54, 54, 54, 56, 56, 54, 54, 54, 54, 54, 54, 54, 55, 54, 54, 54, 54, 54, 54, 54, 54,
    /* 395 */ 54, 54, 54, 54, 54, 54, 54, 54, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 55, 56,
    /* 421 */ 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 54, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56, 56,
    /* 447 */ 56, 56, 56, 56
  };

  private static final int[] MAP2 =
  {
    /*  0 */ 57344, 63744, 64976, 65008, 65536, 983040, 63743, 64975, 65007, 65533, 983039, 1114111, 54, 56, 54, 56, 56,
    /* 17 */ 54
  };

  private static final int[] INITIAL =
  {
    /*  0 */ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 526, 15, 16, 17
  };

  private static final int[] TRANSITION =
  {
    /*    0 */ 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 928, 943,
    /*   18 */ 1453, 1453, 1443, 2221, 1092, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 967, 1350, 982,
    /*   36 */ 1443, 2221, 1092, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2726, 1006, 1453, 2344, 1194,
    /*   53 */ 1495, 1270, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1047, 1061, 1350, 982, 1443, 2221,
    /*   70 */ 1092, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1077, 1102, 982, 1443, 2221, 1092,
    /*   87 */ 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1513, 1118, 1350, 1381, 1655, 1789, 1133, 1453,
    /*  104 */ 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1389, 1159, 1350, 982, 1443, 2221, 1092, 1453, 1453,
    /*  121 */ 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1454, 1240, 1350, 982, 1443, 2221, 1092, 1453, 1453, 1453,
    /*  138 */ 1453, 1453, 1453, 1453, 1453, 1453, 951, 1296, 1350, 1021, 1443, 2221, 1092, 1453, 1453, 1453, 1453,
    /*  155 */ 1453, 1453, 1453, 1453, 1453, 1214, 1327, 1350, 982, 1443, 2221, 1092, 1453, 1453, 1453, 1453, 1453,
    /*  172 */ 1453, 1453, 1453, 1453, 951, 1366, 2199, 2423, 1557, 2436, 2211, 1453, 1453, 1453, 1453, 1453, 1453,
    /*  189 */ 1453, 1453, 1453, 2467, 1405, 1421, 1483, 1470, 1834, 1433, 1453, 1453, 1453, 1453, 1453, 1453, 1453,
    /*  206 */ 1453, 1453, 990, 1529, 1545, 2313, 1920, 1584, 1600, 1681, 1452, 1453, 1453, 1453, 1453, 1453, 1453,
    /*  223 */ 1453, 1697, 1712, 1748, 1740, 1727, 1255, 1645, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453,
    /*  240 */ 1224, 1764, 1350, 982, 1443, 2221, 1092, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1615,
    /*  257 */ 1630, 1453, 982, 1443, 2221, 1092, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1805,
    /*  274 */ 1453, 982, 1443, 2221, 1092, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2352, 1850, 1350, 982,
    /*  292 */ 1443, 2221, 1092, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2637, 1893, 2058, 1936, 1995,
    /*  309 */ 1584, 1600, 1681, 1452, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2787, 1952, 2058, 2859, 1995, 1584,
    /*  326 */ 1600, 1681, 1452, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2637, 1952, 2058, 2859, 1995, 1584, 1600,
    /*  343 */ 1681, 1452, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2637, 1952, 1983, 2011, 2027, 1584, 1600, 1681,
    /*  360 */ 1452, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2637, 1952, 2058, 2859, 2142, 1584, 1600, 1681, 1452,
    /*  377 */ 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2845, 1952, 1908, 2801, 2070, 2814, 2211, 1453, 1453, 1453,
    /*  394 */ 1453, 1453, 1453, 1453, 1453, 1453, 2637, 1952, 1908, 2801, 2070, 1311, 2211, 1453, 1453, 1453, 1453,
    /*  411 */ 1453, 1453, 1453, 1453, 1453, 2637, 1952, 1908, 2801, 2070, 2708, 2211, 1453, 1453, 1453, 1453, 1453,
    /*  428 */ 1453, 1453, 1453, 1453, 2637, 1952, 1908, 2801, 2070, 2814, 2211, 1453, 1453, 1453, 1453, 1453, 1453,
    /*  445 */ 1453, 1453, 1453, 2890, 1952, 1908, 2801, 1877, 2814, 2211, 1453, 1453, 1453, 1453, 1453, 1453, 1453,
    /*  462 */ 1453, 1453, 2637, 2043, 1908, 2801, 2070, 2814, 2211, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453,
    /*  479 */ 1453, 2637, 1952, 1967, 2801, 2070, 2814, 2211, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453,
    /*  496 */ 2637, 1952, 2130, 2086, 2070, 2917, 2211, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2637,
    /*  513 */ 1952, 1908, 2801, 2070, 2814, 1779, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1143, 2115,
    /*  530 */ 1453, 982, 1443, 2221, 1092, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2158, 1204,
    /*  547 */ 1342, 1184, 2393, 1174, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1280, 2237, 1453, 982,
    /*  564 */ 1443, 2221, 1092, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2283, 1507, 982, 1443,
    /*  581 */ 2221, 1092, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1031, 2329, 1865, 2651, 1557, 2663,
    /*  598 */ 2211, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2368, 1453, 982, 1443, 2221, 1092,
    /*  615 */ 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2409, 1952, 2298, 2859, 1995, 1584, 1600, 1681,
    /*  632 */ 1452, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2637, 2452, 2058, 2483, 1995, 2499, 1600, 1681, 1452,
    /*  649 */ 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2637, 1952, 1983, 2267, 2515, 2531, 1600, 1681, 1452, 1453,
    /*  666 */ 1453, 1453, 1453, 1453, 1453, 1453, 2607, 2622, 2058, 2483, 1995, 2547, 1600, 1681, 1452, 1453, 1453,
    /*  683 */ 1453, 1453, 1453, 1453, 1453, 2637, 1952, 1908, 2801, 2070, 2814, 2383, 1453, 1453, 1453, 1453, 1453,
    /*  700 */ 1453, 1453, 1453, 1453, 2637, 1952, 1908, 2904, 2070, 2814, 2211, 1453, 1453, 1453, 1453, 1453, 1453,
    /*  717 */ 1453, 1453, 1453, 2637, 1952, 1908, 2563, 2070, 2099, 2211, 1453, 1453, 1453, 1453, 1453, 1453, 1453,
    /*  734 */ 1453, 1453, 2637, 2592, 1908, 2801, 2070, 2814, 2211, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453,
    /*  751 */ 1453, 2637, 1952, 2252, 2563, 2070, 2099, 2211, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453,
    /*  768 */ 2637, 1952, 1908, 2801, 2679, 2814, 2211, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2757,
    /*  785 */ 2772, 1908, 2563, 2070, 2099, 2211, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2637, 1952,
    /*  802 */ 2173, 2695, 2070, 2576, 2211, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2637, 1952, 1908,
    /*  819 */ 2801, 2070, 2814, 2211, 2724, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1820, 2368, 1453, 982,
    /*  836 */ 1443, 2221, 1092, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1665, 2742, 1453, 982, 1443,
    /*  853 */ 2221, 1092, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 967, 1453, 982, 1443, 2221, 1092,
    /*  871 */ 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2830, 1568, 2651, 1557, 2663, 2211, 1453,
    /*  888 */ 1453, 1453, 1453, 1453, 1453, 1453, 1453, 1453, 2637, 1952, 2186, 2651, 1557, 2663, 2211, 1453, 1453,
    /*  905 */ 1453, 1453, 1453, 1453, 1453, 1453, 1453, 928, 2875, 1453, 982, 1443, 2221, 1092, 1453, 1453, 1453, 1453,
    /*  923 */ 1453, 1453, 1453, 1453, 1453, 788, 788, 788, 788, 788, 788, 788, 788, 788, 788, 788, 788, 788, 788, 788,
    /*  943 */ 788, 0, 0, 788, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 32, 0, 786, 19, 0, 0, 0, 0, 0,
    /*  975 */ 0, 0, 0, 0, 0, 53, 56, 0, 0, 0, 0, 53, 0, 0, 56, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2338, 2338,
    /* 1006 */ 30, 786, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3123, 56, 0, 0, 0, 0, 53, 0, 0, 56, 0, 77, 0, 0, 0, 0, 0, 0,
    /* 1037 */ 25, 0, 0, 0, 0, 25, 0, 25, 25, 25, 786, 786, 786, 786, 786, 786, 786, 786, 786, 786, 786, 786, 786, 786,
    /* 1061 */ 786, 786, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 56, 0, 0, 786, 19, 0, 0, 41, 0, 0, 0, 0, 0, 0, 0, 53, 56,
    /* 1092 */ 0, 0, 0, 0, 71, 0, 0, 0, 0, 74, 0, 0, 0, 0, 0, 0, 0, 0, 0, 41, 1578, 0, 0, 0, 0, 0, 31, 786, 19, 0, 0, 0,
    /* 1124 */ 0, 0, 0, 0, 0, 0, 0, 53, 3382, 0, 0, 0, 0, 71, 0, 0, 0, 0, 3584, 0, 0, 0, 0, 0, 0, 24, 0, 0, 0, 0, 6936,
    /* 1155 */ 0, 6936, 6936, 6936, 4352, 786, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 56, 0, 0, 0, 0, 88, 0, 0, 0, 0, 92,
    /* 1184 */ 0, 0, 0, 0, 0, 0, 88, 0, 0, 92, 0, 0, 0, 0, 0, 0, 87, 0, 0, 74, 0, 0, 0, 0, 0, 0, 0, 0, 0, 43, 0, 0, 0,
    /* 1217 */ 0, 0, 0, 0, 0, 0, 4864, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5376, 0, 0, 5376, 0, 0, 0, 4608, 786, 19, 0, 0, 0, 0,
    /* 1247 */ 0, 0, 0, 0, 0, 0, 53, 56, 0, 0, 0, 0, 1302, 1302, 71, 0, 0, 0, 74, 0, 0, 0, 1302, 0, 0, 0, 0, 3840, 0, 0,
    /* 1277 */ 0, 0, 74, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7168, 0, 0, 7168, 0, 0, 0, 32, 786, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    /* 1308 */ 0, 53, 56, 0, 0, 0, 0, 6423, 23, 71, 0, 0, 0, 74, 0, 0, 0, 23, 2144, 4864, 786, 19, 0, 0, 0, 0, 0, 0, 0,
    /* 1337 */ 0, 0, 0, 53, 56, 0, 0, 0, 53, 52, 0, 56, 55, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1578, 0, 0, 0, 0, 0, 32, 786,
    /* 1368 */ 19, 0, 21, 0, 23, 0, 0, 0, 23, 23, 23, 53, 56, 0, 0, 0, 53, 53, 74, 56, 3328, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    /* 1398 */ 0, 0, 4352, 0, 4352, 4352, 4352, 5153, 786, 19, 0, 40, 0, 44, 0, 0, 0, 44, 44, 44, 53, 56, 33, 0, 57, 44,
    /* 1424 */ 44, 0, 0, 0, 40, 0, 42, 1578, 44, 0, 0, 0, 44, 71, 0, 0, 0, 0, 74, 0, 0, 0, 0, 0, 0, 71, 0, 0, 74, 0, 0,
    /* 1455 */ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4608, 65, 0, 0, 44, 44, 44, 71, 0, 0, 74, 0, 0, 0, 44, 44, 0,
    /* 1486 */ 0, 53, 0, 0, 56, 0, 0, 44, 44, 0, 0, 0, 0, 0, 0, 101, 71, 0, 0, 74, 74, 0, 0, 0, 0, 0, 7424, 0, 0, 0, 0,
    /* 1517 */ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31, 31, 2338, 786, 19, 0, 21, 1578, 23, 0, 0, 0, 23, 23, 23, 53, 56, 2338,
    /* 1545 */ 2617, 2338, 23, 23, 0, 0, 0, 21, 64, 1578, 0, 23, 1857, 0, 0, 23, 23, 23, 71, 0, 0, 74, 0, 0, 0, 23, 23,
    /* 1572 */ 0, 0, 0, 21, 0, 1578, 0, 23, 0, 0, 0, 23, 97, 98, 0, 0, 23, 23, 71, 0, 104, 105, 74, 0, 109, 110, 23,
    /* 1599 */ 2144, 111, 112, 0, 23, 71, 114, 115, 116, 117, 74, 118, 119, 120, 121, 63, 19, 0, 0, 0, 19, 19, 19, 19,
    /* 1623 */ 19, 0, 19, 19, 19, 19, 19, 19, 786, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 56, 0, 0, 0, 1302, 71, 0, 0, 0,
    /* 1653 */ 0, 74, 0, 0, 0, 0, 0, 0, 71, 0, 0, 91, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8704, 8704, 8704, 8704, 0, 0, 0, 0,
    /* 1682 */ 122, 123, 73, 53, 124, 125, 76, 56, 126, 127, 128, 129, 103, 71, 108, 0, 1280, 0, 0, 1302, 1302, 1302,
    /* 1704 */ 1302, 1302, 0, 1302, 1302, 1302, 1302, 1302, 1302, 786, 19, 0, 1280, 1578, 1302, 0, 45, 0, 1302, 1302,
    /* 1724 */ 1302, 53, 56, 0, 0, 0, 1302, 1302, 1302, 71, 0, 0, 74, 0, 0, 0, 1302, 1302, 0, 0, 53, 0, 0, 56, 0, 0,
    /* 1750 */ 1302, 1302, 0, 0, 0, 0, 0, 1578, 0, 0, 0, 0, 0, 1302, 5376, 786, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53,
    /* 1778 */ 56, 0, 0, 0, 6679, 71, 0, 0, 0, 0, 74, 0, 0, 0, 0, 0, 0, 71, 71, 0, 0, 106, 74, 0, 0, 0, 0, 0, 786, 1024,
    /* 1808 */ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 56, 0, 0, 0, 8448, 0, 0, 0, 0, 8448, 0, 0, 0, 0, 8448, 0, 0, 0, 0, 44,
    /* 1839 */ 44, 71, 0, 0, 0, 74, 0, 0, 0, 44, 0, 37, 786, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 56, 0, 0, 23, 23, 0,
    /* 1870 */ 0, 0, 21, 0, 1578, 1578, 23, 1857, 0, 0, 23, 85, 23, 71, 0, 0, 74, 0, 0, 0, 23, 23, 2144, 23, 786, 19, 0,
    /* 1897 */ 21, 1578, 23, 0, 0, 0, 48, 23, 23, 53, 56, 0, 0, 23, 23, 2109, 0, 0, 21, 0, 1578, 0, 23, 1857, 0, 0, 23,
    /* 1924 */ 23, 23, 71, 89, 90, 74, 93, 94, 2893, 23, 23, 2144, 69, 23, 0, 0, 53, 0, 0, 56, 0, 0, 23, 23, 2109, 81,
    /* 1950 */ 82, 1578, 23, 786, 19, 0, 21, 1578, 23, 0, 0, 0, 23, 23, 23, 53, 56, 0, 0, 23, 23, 2109, 0, 0, 21, 0,
    /* 1976 */ 1578, 0, 23, 1857, 0, 0, 68, 0, 58, 23, 23, 2109, 0, 0, 21, 64, 1578, 0, 23, 1857, 0, 0, 23, 23, 23, 71,
    /* 2002 */ 89, 90, 74, 93, 94, 0, 23, 23, 2144, 23, 70, 0, 0, 53, 0, 0, 56, 58, 0, 23, 23, 2109, 81, 82, 1578, 1857,
    /* 2028 */ 0, 0, 6167, 23, 23, 71, 89, 90, 74, 93, 94, 0, 23, 23, 2144, 23, 786, 19, 0, 21, 1578, 23, 0, 0, 0, 23,
    /* 2054 */ 49, 50, 53, 56, 0, 0, 23, 23, 2109, 0, 0, 21, 64, 1578, 0, 23, 1857, 0, 0, 23, 23, 23, 71, 0, 0, 74, 0,
    /* 2081 */ 0, 0, 23, 23, 2144, 23, 23, 0, 72, 53, 0, 75, 56, 0, 0, 23, 23, 2109, 0, 0, 0, 0, 23, 23, 71, 71, 0, 0,
    /* 2109 */ 74, 74, 0, 0, 23, 2144, 6936, 786, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 56, 0, 0, 23, 23, 2109, 0, 62,
    /* 2137 */ 21, 0, 1578, 0, 23, 1857, 0, 0, 23, 23, 86, 71, 89, 90, 74, 93, 94, 0, 23, 23, 2144, 0, 786, 39, 0, 0,
    /* 2163 */ 43, 0, 0, 0, 0, 0, 0, 0, 52, 55, 0, 0, 23, 23, 2109, 0, 63, 21, 0, 1578, 0, 23, 1857, 0, 0, 23, 23, 0, 0,
    /* 2192 */ 0, 21, 0, 1578, 0, 23, 1857, 0, 0, 23, 23, 0, 0, 0, 21, 0, 1578, 1578, 23, 0, 0, 0, 23, 71, 0, 0, 0, 0,
    /* 2220 */ 74, 0, 0, 0, 0, 0, 0, 71, 0, 0, 0, 74, 0, 0, 0, 0, 0, 7168, 786, 19, 0, 0, 0, 0, 4096, 0, 0, 0, 0, 0, 53,
    /* 2251 */ 56, 0, 0, 23, 60, 2109, 0, 0, 21, 0, 1578, 0, 23, 1857, 0, 67, 23, 23, 0, 0, 53, 0, 0, 56, 58, 0, 23, 23,
    /* 2279 */ 2109, 81, 82, 1578, 38, 786, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 56, 0, 0, 59, 23, 2109, 0, 0, 21, 64,
    /* 2307 */ 1578, 0, 23, 1857, 66, 0, 23, 23, 0, 0, 53, 0, 0, 56, 2617, 2893, 23, 23, 0, 81, 82, 1578, 25, 786, 19,
    /* 2332 */ 0, 21, 1578, 23, 0, 0, 0, 23, 23, 23, 53, 56, 0, 0, 71, 53, 3072, 0, 56, 56, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    /* 2361 */ 0, 0, 0, 0, 26, 0, 0, 0, 786, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 56, 0, 0, 113, 23, 71, 0, 0, 0, 0, 74,
    /* 2393 */ 0, 0, 0, 0, 0, 0, 88, 71, 0, 0, 92, 74, 0, 0, 0, 0, 0, 21, 0, 0, 23, 7703, 23, 7703, 7703, 0, 7703, 23,
    /* 2421 */ 7703, 23, 23, 23, 0, 0, 53, 0, 0, 56, 0, 77, 23, 23, 80, 0, 0, 0, 0, 23, 23, 71, 0, 0, 0, 74, 0, 0, 0,
    /* 2450 */ 23, 80, 23, 786, 19, 0, 21, 1578, 23, 0, 0, 46, 23, 23, 23, 53, 56, 0, 0, 5120, 0, 0, 0, 0, 5120, 0,
    /* 2476 */ 5120, 5120, 0, 5120, 0, 33, 33, 23, 23, 0, 53, 53, 0, 56, 56, 0, 0, 23, 23, 2109, 81, 82, 1578, 97, 98,
    /* 2501 */ 0, 0, 23, 23, 71, 71, 104, 105, 74, 74, 109, 110, 23, 2144, 1857, 0, 84, 23, 23, 23, 71, 89, 90, 74, 93,
    /* 2526 */ 94, 0, 23, 8215, 2144, 97, 98, 5632, 0, 23, 23, 71, 0, 104, 105, 74, 0, 109, 110, 7959, 2144, 97, 98, 0,
    /* 2550 */ 99, 23, 23, 71, 71, 104, 105, 74, 74, 109, 110, 23, 2144, 23, 23, 0, 53, 53, 0, 56, 56, 0, 0, 23, 23,
    /* 2575 */ 2109, 0, 0, 0, 0, 23, 23, 71, 103, 0, 0, 74, 108, 0, 0, 23, 2144, 23, 786, 19, 0, 21, 1578, 23, 0, 0, 47,
    /* 2602 */ 23, 23, 23, 53, 56, 0, 21, 0, 0, 23, 23, 23, 23, 23, 0, 23, 23, 23, 23, 35, 35, 786, 19, 0, 21, 1578, 23,
    /* 2629 */ 0, 0, 0, 23, 23, 23, 53, 56, 0, 21, 0, 0, 23, 23, 23, 23, 23, 0, 23, 23, 23, 23, 23, 23, 0, 0, 53, 0, 0,
    /* 2658 */ 56, 0, 0, 23, 23, 0, 0, 0, 0, 23, 23, 71, 0, 0, 0, 74, 0, 0, 0, 23, 0, 1857, 83, 0, 23, 23, 23, 71, 0, 0,
    /* 2688 */ 74, 0, 0, 0, 95, 23, 2144, 23, 23, 0, 73, 53, 0, 76, 56, 0, 0, 23, 79, 2109, 0, 0, 0, 0, 23, 100, 71, 0,
    /* 2716 */ 0, 0, 74, 0, 0, 0, 23, 2144, 5888, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 30, 8704, 786, 0, 0,
    /* 2746 */ 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 56, 0, 21, 0, 0, 23, 23, 23, 23, 23, 0, 23, 23, 23, 23, 36, 36, 786, 19,
    /* 2775 */ 0, 21, 1578, 23, 0, 0, 0, 23, 23, 23, 53, 56, 0, 21, 0, 0, 23, 23, 23, 23, 23, 0, 23, 23, 23, 27, 23, 23,
    /* 2803 */ 0, 0, 53, 0, 0, 56, 0, 0, 23, 23, 2109, 0, 0, 0, 0, 23, 23, 71, 0, 0, 0, 74, 0, 0, 0, 23, 2144, 0, 786,
    /* 2832 */ 19, 0, 21, 0, 23, 0, 0, 0, 23, 23, 23, 53, 56, 0, 21, 0, 0, 23, 23, 23, 23, 23, 0, 23, 23, 23, 28, 23,
    /* 2860 */ 23, 0, 0, 53, 0, 0, 56, 0, 0, 23, 23, 2109, 81, 82, 1578, 788, 786, 0, 788, 0, 0, 0, 24, 0, 0, 0, 0, 0,
    /* 2888 */ 53, 56, 0, 21, 0, 0, 23, 23, 23, 23, 23, 0, 23, 23, 23, 29, 23, 23, 0, 0, 53, 0, 0, 56, 0, 0, 78, 23,
    /* 2916 */ 2109, 0, 0, 0, 0, 23, 23, 71, 102, 0, 0, 74, 107, 0, 0, 23, 2144
  };

  private static final int[] EXPECTED =
  {
    /*   0 */ 5, 13, 21, 24, 24, 32, 40, 48, 56, 64, 79, 87, 72, 83, 91, 112, 123, 99, 106, 120, 131, 138, 145, 152,
    /*  24 */ 154, 154, 154, 154, 154, 154, 154, 154, 12, 20, 524292, 4, 60, 536870972, 32892, 537395260, 536870972,
    /*  41 */ 136052740, 537395260, 67207292, 672661564, 132219004, -1006502020, -1006370948, -601882628, 4, 8, 4, 16,
    /*  53 */ 32, 48, 32768, 64, 6291456, 8388656, 16777264, 33554480, 18432, 12288, 1792, 1536, 1792, 1073741872,
    /*  67 */ -2147483600, 128, 268435456, 8, 16, 1536, 1024, 1073741872, -2147483600, 128, 8, 8, 32, 32, 32, 48, 64,
    /*  84 */ 2097152, 4194304, 8388656, 16777264, 33554480, 16384, 2048, 2048, 8192, 4096, 4096, 1024, 1073741872,
    /*  97 */ -2147483600, 128, 8, 8, 4194304, 33554480, 16384, 16384, 16384, 2048, 8192, 8192, 8192, 4096, 4096, 8, 8,
    /* 114 */ 2097152, 4194304, 16777264, 33554480, 16384, 16384, 4194304, 16384, 16384, 2048, 2048, 8192, 8192, 4096,
    /* 128 */ 4096, 1073741872, 128, 4096, 16384, 16384, 8192, 8192, 16384, 16384, 8192, 0, 0, 0, 1, 0, 0, 0, 1, 2, 2,
    /* 149 */ 2, 2, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0
  };

  private static final String[] TOKEN =
  {
    "(0)",
    "END",
    "WhiteSpace",
    "IRIREF",
    "PNAME_NS",
    "PNAME_LN",
    "BLANK_NODE_LABEL",
    "LANGTAG",
    "INTEGER",
    "DECIMAL",
    "DOUBLE",
    "STRING_LITERAL_QUOTE",
    "STRING_LITERAL_SINGLE_QUOTE",
    "STRING_LITERAL_LONG_SINGLE_QUOTE",
    "STRING_LITERAL_LONG_QUOTE",
    "'[]'",
    "'('",
    "')'",
    "','",
    "'.'",
    "';'",
    "'@base'",
    "'@prefix'",
    "'BASE'",
    "'GRAPH'",
    "'PREFIX'",
    "'['",
    "']'",
    "'^^'",
    "'a'",
    "'false'",
    "'true'",
    "'{'",
    "'}'"
  };
}

// End
