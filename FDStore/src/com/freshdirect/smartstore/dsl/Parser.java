package com.freshdirect.smartstore.dsl;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

public class Parser {

    Context defaultContext = new Context();

    public Context getContext() {
        return defaultContext;
    }

    public void setContext(Context defaultContext) {
        this.defaultContext = defaultContext;
    }

    public BlockExpression parse(final Context context, Reader reader) throws CompileException {
        StreamTokenizer tokenizer = new StreamTokenizer(reader);
        tokenizer.parseNumbers();

        tokenizer.whitespaceChars(' ', ' ');
        tokenizer.whitespaceChars('\t', '\t');

        tokenizer.ordinaryChar('$');
        tokenizer.ordinaryChar('(');
        tokenizer.ordinaryChar(')');
        tokenizer.ordinaryChar('+');
        tokenizer.ordinaryChar('-');
        tokenizer.ordinaryChar('*');
        tokenizer.ordinaryChar('"');
        tokenizer.ordinaryChar('/');

        tokenizer.wordChars('a', 'z');
        tokenizer.wordChars('A', 'Z');
        tokenizer.wordChars('_', '_');

        tokenizer.eolIsSignificant(false);

        BlockExpression current = new BlockExpression();
        try {
            parseExpression(context, tokenizer, current);
            current.visit(null, new ExpressionVisitor() {
                public void visit(Expression parent, Expression expression) {
                    expression.context = context;
                }
            });
            return current;
        } catch (IOException e) {
            throw new CompileException("Error during parsing the source:" + e.getMessage(), e);
        } catch (VisitException e) {
            throw new CompileException("Error during parsing the source:" + e.getMessage(), e);
        }
    }

    public BlockExpression parse(Context context, String expr) throws CompileException {
        return parse(context, new StringReader(expr));
    }

    public BlockExpression parse(String expr) throws CompileException {
        return parse(defaultContext, expr);
    }

    public BlockExpression parse(Reader rd) throws CompileException {
        return parse(defaultContext, rd);
    }

    private void parseExpression(Context context, StreamTokenizer tokenizer, Expression current) throws IOException, CompileException {
        int tt = 0;
        do {
            tt = tokenizer.nextToken();
            switch (tt) {
                case StreamTokenizer.TT_EOF:
                    if (!(current instanceof BlockExpression) && !(current instanceof Operation)) {
                        throw new CompileException(CompileException.SYNTAX_ERROR, "Unexpected end of expression: " + current.toCode() + " (" + current + ")");
                    }
                    if (current instanceof Operation) {
                        if (((Operation) current).includeParent) {
                            throw new CompileException(CompileException.SYNTAX_ERROR, "Unexpected end of expression: "+current.toCode() +", ')' expected !");
                        }
                    }
                    break;
                case ')':
                    // handle the case of 'round(5+3)*3', where the ')' mark not
                    // just the end of the operation, but also the function.
                    if (current instanceof Operation) {
                        if (!((Operation) current).includeParent) {
                            tokenizer.pushBack();
                        }
                    }
                    return;
                case ';':
                case ',':
                    if (current instanceof Operation) {
                        // it's a comma, the operation ends here
                        return;
                    }
                    break;

                case StreamTokenizer.TT_NUMBER:
                    current.add(new NumberExp(tokenizer.nval));
                    break;
                case StreamTokenizer.TT_WORD: {
                    if (context.isFunction(tokenizer.sval)) {
                        FunctionCall fc = new FunctionCall(tokenizer.sval);
                        int tc = tokenizer.nextToken();
                        if (tc != '(') {
                            throw new CompileException(CompileException.SYNTAX_ERROR, "Syntax error : '(' expected: ");
                        }
                        parseExpression(context, tokenizer, fc);
                        current.add(fc);
                    } else {
                        if (context.isVariable(tokenizer.sval)) {
                            current.add(new VariableExpression(tokenizer.sval));
                        } else {
                            String name = tokenizer.sval;
                            int tc = tokenizer.nextToken();
                            if (tc != '(') {
                                throw new CompileException(CompileException.UNKNOWN_VARIABLE, "Unknown variable : "+name);
                            } else {
                                throw new CompileException(CompileException.UNKNOWN_FUNCTION, "Unknown function : "+name);
                            }
                        }
                    }
                    break;
                }
                case '+':
                case '*':
                case '/':
                case ':':
                case '-':
                case '%': {
                    if (current instanceof Operation) {
                        // we are already in a operation.
                        Operation o = (Operation) current;
                        if (tt == '-' && !o.expectOperator) {
                            // handle the 5 * - 3 case
                            int t2 = tokenizer.nextToken();
                            if (t2 == StreamTokenizer.TT_NUMBER) {
                                current.add(new NumberExp(-tokenizer.nval));
                            } else {
                                throw new CompileException(CompileException.SYNTAX_ERROR,
                                        "Syntax error,the following sequence detected : 'operator' - 'notnumber' !");
                            }
                        } else {
                            o.addOperand((char) tt);
                        }
                    } else {
                        Expression lastExpression = current.lastExpression();
                        if (lastExpression == null) {
                            throw new CompileException(CompileException.SYNTAX_ERROR, "No previous expression in the current context:" + current.toCode()
                                    + ", operation:" + (char) tt);
                        }
                        current.removeLastExpression();
                        Operation oper = new Operation(lastExpression);
                        oper.addOperand((char) tt);
                        parseExpression(context, tokenizer, oper);
                        current.add(oper);
                    }
                    break;
                }
                case '(': {
                    Operation oper = new Operation();
                    oper.setIncludeParent(true);
                    parseExpression(context, tokenizer, oper);
                    current.add(oper);
                    break;
                }
                case '"': {
                    current.add(new StringExp(parseString(context, tokenizer)));
                    break;
                }
                default: {
                    throw new CompileException(CompileException.SYNTAX_ERROR, "unknown tt:" + tt + "='" + (char) tt + "', svalue : " + tokenizer.sval);
                }
            }
        } while (tt != StreamTokenizer.TT_EOF);
    }

    private String parseString(Context context, StreamTokenizer tokenizer) throws IOException, CompileException {
        StringBuffer current = new StringBuffer();
        int tt;
        do {
            tt = tokenizer.nextToken();
            switch (tt) {
                case StreamTokenizer.TT_EOF:
                    throw new CompileException(CompileException.SYNTAX_ERROR, "Unexpected end of string: " + current );
                case '"' : {
                    return current.toString();
                }
                default : { 
                    if (current.length()>0) {
                        current.append(' ');
                    }
                    current.append(tokenizer.sval);
                }
            }
        } while (tt != StreamTokenizer.TT_EOF);
        return current.toString();
    }

    public static void main(String[] args) throws CompileException {
        Parser p = new Parser();
        p.parse(new Context(), "a + b;a*c;min(ablak, 14)");
    }

}
