package es.amplia.oda.hardware.atmanager.grammar;

import es.amplia.oda.hardware.atmanager.api.ATEvent;
import es.amplia.oda.hardware.atmanager.grammar.SyntacticParser.Tokens;

import java.util.ArrayList;
import java.util.List;

public class ResponseGrammar {
    /*
     * <response> ::= EXTENDED_NAME COLON <param> (COMMA <param>)*
     * <param> ::= NUMBER
     *          | STRING
     *          | HEX_DATA
     *          | IPv4
     */
    private SyntacticParser syntacticParser;

    public ATEvent parse(String lineWithPlus) {
        this.syntacticParser = new SyntacticParser(lineWithPlus);
        ATEvent r = response();
        assertTokenIs(Tokens.EOF);
        return r;
    }

    private void assertTokenIs(Tokens token) {
        if (syntacticParser.current() != token)
            throw new GrammarException("Unexpected token '" + syntacticParser.current() + "'");
    }

    private void advanceAndAssertTokenIs(Tokens token) {
        syntacticParser.advance();
        assertTokenIs(token);
    }

    private void advanceSkippingSpace() {
        syntacticParser.advance();
        while (syntacticParser.current() == Tokens.SPACE)
            syntacticParser.advance();
    }

    private ATEvent response() {
        while (syntacticParser.current() == Tokens.SPACE)
            syntacticParser.advance();
        assertTokenIs(Tokens.EXTENDED_NAME);
        String cmd = syntacticParser.getLastCommand();
        advanceAndAssertTokenIs(Tokens.COLON);
        advanceSkippingSpace();
        List<String> parameters = new ArrayList<>();
        if (syntacticParser.current() == Tokens.EOF) {
            throw new GrammarException("Unexpected end of line");
        }
        param(parameters);
        while (syntacticParser.current() == Tokens.COMMA) {
            advanceSkippingSpace();
            param(parameters);
        }
        return ATEvent.event(cmd, parameters);
    }

    private void param(List<String> parameters) {
        Tokens t = syntacticParser.current();
        if (t == Tokens.NUMBER) {
            parameters.add(syntacticParser.getLastNumber() + "");
            advanceSkippingSpace();
        } else if (t == Tokens.STRING) {
            parameters.add(syntacticParser.getLastString());
            advanceSkippingSpace();
        } else if (t == Tokens.CONSTANT) {
            parameters.add(syntacticParser.getLastConstant());
            advanceSkippingSpace();
        } else if (t == Tokens.IPV4) {
            parameters.add(syntacticParser.getLastIPAddress());
            advanceSkippingSpace();
        } else {
            parameters.add("");
        }
    }
}
